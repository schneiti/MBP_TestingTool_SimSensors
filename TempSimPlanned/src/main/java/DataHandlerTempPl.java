import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Handles the formatting of the generated data for sending to the broker as well as storing and reading them to reuse.
 */
public class DataHandlerTempPl {
    static String jsonMessage;
    static ToJsonMessageTempPl json = new ToJsonMessageTempPl();
    static ObjectMapper mapper = new ObjectMapper();


    /**
     * Convert string of sensor data to array list to work with.
     *
     * @param savedData saved list of simulated senor values
     * @return array list with saved sensor datas
     */
    public static ArrayList toArrayList(String savedData) {

        String s = savedData.replaceAll("\\[|\\]", "");
        List<String> myList = new ArrayList<String>(Arrays.asList(s.split(", ")));

        List<Float> retList = new ArrayList<>();
        for (String l : myList) {
            retList.add(Float.parseFloat(l));
        }

        return (ArrayList) myList;
    }

    /**
     * Puts every entry of the simulation list into the right json format for the MQTT message
     *
     * @param simList list of simulated values
     * @return single sensor values in json format
     */
    public static String jsonOutput(ArrayList<Float> simList)
            throws IOException {
        for (Float value : simList) {
            json.setValue(value);
            // Java objects to JSON string - pretty-print
            jsonMessage = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        }
        return jsonMessage;
    }


    /**
     * Creates Json to send to the broker.
     *
     * @param value of the simulation list
     * @return Json Message
     */
    public static String dataToJson(Float value, String id) throws IOException {

        json.setValue(value);
        json.setId(id);
        json.setComponent(id);

        jsonMessage = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        return jsonMessage;
    }


    /**
     * Saves the simulated datas and informations of the user input in a xml-file.
     *
     * @param type     of sensor
     * @param simList  list of simulates sensor values
     * @param fileName file name of the xml-file
     */
    public static void saveToXML(String type, ArrayList<Float> simList, String fileName, float simTime, int amountEvents, int amountOutliers) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = docBuilder.newDocument();

        // build new document
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName + ".xml"));

        Element rootElement = doc.createElement(type);
        doc.appendChild(rootElement);

        // TestCase
        Element testCase2Save = doc.createElement(fileName);
        rootElement.appendChild(testCase2Save);

        Element simList2Save = doc.createElement("Sensor-Daten");
        Element metadata = doc.createElement("Metadaten");
        testCase2Save.appendChild(metadata);
        Element frequence = doc.createElement("Frequenz");
        frequence.appendChild(doc.createTextNode(String.valueOf(1000)));
        metadata.appendChild(frequence);
        Element maxRateOfChange = doc.createElement("Maxmimale-Aenderungsrate");
        maxRateOfChange.appendChild(doc.createTextNode(String.valueOf(CreateDataTempPl.max_rateOfChange)));
        metadata.appendChild(maxRateOfChange);
        Element simtime = doc.createElement("Simulations-Zeit");
        simtime.appendChild(doc.createTextNode(String.valueOf(simTime)));
        metadata.appendChild(simtime);

        Element amountevents = doc.createElement("Anzahl-Events");
        amountevents.appendChild(doc.createTextNode(String.valueOf(amountEvents)));
        metadata.appendChild(amountevents);

        Element amountoutliers = doc.createElement("Anzahl-Ausreisser");
        amountoutliers.appendChild(doc.createTextNode(String.valueOf(amountOutliers)));
        metadata.appendChild(amountoutliers);
        simList2Save.appendChild(doc.createTextNode(String.valueOf(simList)));
        testCase2Save.appendChild(simList2Save);
        transformer.transform(source, result);
    }

    /**
     * Extracts the simulated values of a one-dimensional sensor out of a xml file for a specific testcase.
     *
     * @param nodeList of the xml-file
     * @return list of simulated values of a one-dimensional sensor
     */
    public static ArrayList<Float> getData(NodeList nodeList) throws IOException {
        ArrayList<Float> simList = null;

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);

            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) tempNode;
                String existingData = e.getElementsByTagName("Sensor-Daten").item(0).getTextContent();

                simList = toArrayList(existingData);
                jsonOutput(simList);

            }

        }
        return simList;

    }

}
