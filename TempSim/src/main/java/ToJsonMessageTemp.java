/**
 * Defines all parameters needed for the MQTT-Message in json format to send.
 */
public class ToJsonMessageTemp {
    private String component;
    private String id;
    private ValueJson value;

    /**
     * Returns the kind of component for the MQTT-Message
     *
     * @return kind of component
     */
    public String getComponent() {
        return component;
    }

    /**
     * Returns the sensor id to which the client have to send the MQTT-Message.
     *
     * @return sensor id of the MQTT-Message
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the simulated value to be send to the MQTT-Broker.
     *
     * @return value of the simulation to be send to the MQTT-Broker
     */
    public ValueJson getValue() {
        return this.value;
    }


    /**
     * Set the kind of component for the MQTT-Message out of the topic.
     *
     * @param topic kind of component
     */
    public void setComponent(String topic) {
        String[] component = topic.split("/");
        this.component = component[0];
    }

    /**
     * Sets the id to which the client have to send the MQTT-Message.
     *
     * @param topic of the MQTT-Message
     */
    public void setId(String topic) {
        String[] id = topic.split("sensor/");
        this.id = id[1];
    }

    /**
     * Sets the simulated value for the MQTT-Message.
     *
     * @param value of the simulation
     */
    public void setValue(float value) {
        this.value = new ValueJson(value);
    }
}
