import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * MQTT-Client
 */
public class Main_hum {

    String hostname;
    int port;
    static String clientID = "";
    static boolean useNewData;
    static int event = 0;
    static int anomaly = 0;
    static String room = "";
    static String fileName;
    static ArrayList<Float> simList = null;

    MqttClient client;
    MqttConnectOptions connOpts = new MqttConnectOptions();

    /**
     * Configure the MQTT-Client
     *
     * @param hostname from connections.txt
     * @param port     from connections.txt
     * @param clientID from connections.txt
     */
    public Main_hum(String hostname, int port, String clientID) throws MqttException {
        this.hostname = hostname;
        this.port = port;
        Main_hum.clientID = clientID;
        client = new MqttClient("tcp://" + hostname, clientID);
        connOpts.setCleanSession(true);
    }

    /**
     * Publishing the message on a specific topic to the MQTT-Broker
     *
     * @param topic out of the connections.txt
     * @param msg   simulated value
     */
    public void sendData(String topic, MqttMessage msg) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(msg.getPayload());
        client.publish(topic, mqttMessage.getPayload(), 0, false);
    }

    /**
     * Connecting to the MQTT-Broker
     */
    public void start() throws MqttException {
        client.connect(connOpts);
    }

    /**
     * Disconnecting from the MQTT-Broker
     */
    public void stop() throws MqttException {
        client.disconnect();
    }

    public static void main(String[] args) throws InterruptedException, IOException, MqttException {
        String hostname;
        String topic_pub;
        List<String> topics = new ArrayList<>();
        List<String> brokerIps = new ArrayList<>();
        boolean configExsists = false;
        String topic;
        String ip;

        // default values
        useNewData = true;
        event = 2;
        anomaly = 3;
        room = "a";
        int freq = 500;

        ObjectMapper mapper = new ObjectMapper();
        String json = args[0];


        try {

            // 1. convert JSON array to Array objects
            JsonHandlerHum[] pp1 = mapper.readValue(json, JsonHandlerHum[].class);

            for (JsonHandlerHum parameter : pp1) {
                switch (parameter.getName()) {
                    case "event":
                        event = Integer.parseInt(parameter.getValue());
                        break;
                    case "anomaly":
                        anomaly = Integer.parseInt(parameter.getValue());
                        break;
                    case "room":
                        room = parameter.getValue();
                        break;
                    case "useNewData":
                        useNewData = Boolean.parseBoolean(parameter.getValue());
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String configFileName = "connections.txt";
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path configFile = currentPath.resolve(configFileName);
        File fileExist = new File(String.valueOf(configFile));

        // check connections.txt exists
        while (!configExsists) {
            configExsists = fileExist.exists();
            Thread.sleep(1);
        }


        // Parse the connections.txt
        BufferedReader br = new BufferedReader(new FileReader(fileExist));
        String line;

        while ((line = br.readLine()) != null) {
            String[] pars = line.split("=");
            topic = pars[0].replaceAll("\n", "").trim();
            ip = pars[1].replaceAll("\n", "").trim();
            topics.add(topic);
            brokerIps.add(ip);
        }

        hostname = brokerIps.get(0);
        topic_pub = topics.get(0);

        System.out.println("Connection to: " + hostname + " pub on topic " + topic_pub);

        // Starting the MQTT-Client
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        String id = "id_".concat(time.format(cal.getTime()));

        // start the publisher
        Main_hum publisher = new Main_hum(hostname, 1883, id);
        publisher.start();

        try {
            fileName = "hum".concat(Integer.toString(event)).concat(Integer.toString(anomaly).concat(room));

            if (useNewData) {
                // Generate the simulation list with the configurations of the user
                simList = CreateDataHum.CreateDataHum(event, anomaly, room);
                
                // save data to xml
                DataHandlerHum.saveToXML("hum", simList, fileName);

                for (Float value : simList) {
                    // convert the single values of the simulation list to json format
                    String message = DataHandlerHum.dataToJson(value, topic_pub);
                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());

                    // publish the data in the defined frequency
                    publisher.sendData(topic_pub, mqttMessage);
                    Thread.sleep(freq);
                }
            } else {
                try {
                    // find the xml-file with the specific filename
                    File file = new File(fileName + ".xml");
                    DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder();
                    Document doc = dBuilder.parse(file);

                    // get the simulation list out of the xml
                    if (doc.hasChildNodes()) {
                        simList = DataHandlerHum.getData(doc.getChildNodes());
                    }

                    for (Float value : simList) {
                        // convert the single values of the simulation list to json format
                        String message = DataHandlerHum.dataToJson(value, topic_pub);
                        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                        // publish the data in the defined frequency
                        publisher.sendData(topic_pub, mqttMessage);
                        Thread.sleep(freq);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("end due to: " + e.getMessage());
        }

        // stop the publisher
        publisher.stop();

    }
}
