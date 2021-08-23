import java.util.ArrayList;
import java.util.Random;


/**
 * Simulator of a humidity-sensor, simulation two events (humidity decrease/increase) combinable with anomalies.
 */
public class CreateDataHum {
    static float lowerLimit = 20.000f;
    static float upperLimit = 90.000f;
    static float max_rateOfChange = 0.07f;
    static int frequency = 500;
    static float tempHumValue;
    static float startHum;
    static float upperLimNext;
    static float lowerLimNext;
    static float optimumRHUL;
    static float optimumRHLL;
    static float nextValue;
    static Random r = new Random();
    static ArrayList<Float> simList = null;

    /**
     * Handles the calls to the methods in different cases and returns the simulation list.
     *
     * @param event  humidity rise/decrease
     * @param anomaly kind of anomaly
     * @param room   room in which humidity should be measured
     * @return  user defined simulation List
     */
    public static ArrayList CreateDataHum(int event, int anomaly, String room) {

        if (event == 1 || event == 2) {
            if (anomaly == 3 || anomaly == 4 || anomaly == 5 || anomaly == 6) {
                if (room.equals("a") || room.equals("b") || room.equals("c") || room.equals("d") || room.equals("e")
                        || room.equals("f")) {
                    simList = humidityChange(room, event, anomaly);
                }
            }
        } else if (event == 3 || event == 4 || event == 5 || event == 6) {
            // calculate the list of temperatures with the selected outlier type
            simList = simWrongMeasures(event);
        }
        return simList;
    }

    /**
     * Simulation of wrong measured values Generates a list of sensor data of the
     * classes "outlier", "wrong value type", "missing value", "accuracy class".
     *
     * @param anomaly kind of anomaly
     * @return Array list with simulated sensor data
     */
    public static ArrayList<Float> simWrongMeasures(int anomaly) {
        ArrayList<Float> wrongMeasuresList = new ArrayList<>();

        // Calculate the random Position of the wrong Measure
        int manipulatePos = r.nextInt(19 + 1);
        setOptimumHumidity("a");

        
        // Calculate a random start value within the upper and lower limits of the humidity sensor
        startHum = calcHumChange(optimumRHLL, optimumRHUL);
        wrongMeasuresList.add(0, startHum);

        // Generate the list of sensor data with the wrong value (depending on the test case) at the calculated position.
        for (int i = 0; i <= 20; i++) {
            if (i == manipulatePos) {
                if(anomaly!= 6){
                    manipulate(manipulatePos, anomaly, wrongMeasuresList);
                }

            } else if (i == manipulatePos + 1) {
                if (manipulatePos != 0) {
                    lowerLimNext = (float) wrongMeasuresList.get(i - 2);
                    upperLimNext = (float) wrongMeasuresList.get(i - 2) + (max_rateOfChange * 2);
                    nextValue = calcHumChange(lowerLimNext, upperLimNext);
                    wrongMeasuresList.add(i, nextValue);
                }

            } else {
                if (i != 0) {
                    lowerLimNext = (float) wrongMeasuresList.get(i - 1);
                    upperLimNext = (float) wrongMeasuresList.get(i - 1) + max_rateOfChange;
                    nextValue = calcHumChange(lowerLimNext, upperLimNext);
                    wrongMeasuresList.add(i, nextValue);
                }

            }
        }

        return wrongMeasuresList;
    }

    /**
     * Simulation of the humidity rise or fall in a certain room, which violate the
     * limits of the optimal room humidity.
     *
     * @param room    room in which humidity should be measured
     * @param event   humidity rise/decrease
     * @param anomaly kind of anomaly
     * @return list with simulated sensor data combined with anomalies
     */
    public static ArrayList<Float> humidityChange(String room, int event, int anomaly) {
        ArrayList<Float> humidityChange = new ArrayList<>();
        int counter = 1;
        int simTime;
        float i;

        // set the values of the optimal room humidity depending on the specific room
        setOptimumHumidity(room);

        // calculate the humidity rise (case 1) or the humidity decrease (case 2)
        switch (event) {
            case 1:
                // Random start value between 5%-2% under the optimum upper limit
                startHum = calcHumChange((optimumRHLL - 5.00f), (optimumRHUL - 2.00f));
                humidityChange.add(0, startHum);

                simTime = calcSimTime(optimumRHLL, optimumRHUL, event);

                // Calculate the list of increasing humidity
                i = startHum;
                while (i < simTime) {
                    lowerLimNext = (float) humidityChange.get(counter - 1);
                    upperLimNext = (float) humidityChange.get(counter - 1) + max_rateOfChange;
                    nextValue = calcHumChange(lowerLimNext, upperLimNext);
                    humidityChange.add(nextValue);
                    ++counter;
                    i = nextValue;
                }

                break;
            case 2:
                // Random start value between 5-2% over the optimum lower limit
                startHum = calcHumChange((optimumRHLL + 2.00f), (optimumRHLL + 5.00f));
                humidityChange.add(0, startHum);

                simTime = calcSimTime(optimumRHLL, optimumRHUL, event);

                // Calculate the list of decreasing temperature
                i = startHum;
                while (i > simTime) {
                    lowerLimNext = (float) humidityChange.get(counter - 1) - max_rateOfChange;
                    upperLimNext = (float) humidityChange.get(counter - 1);
                    nextValue = calcHumChange(lowerLimNext, upperLimNext);
                    humidityChange.add(nextValue);
                    ++counter;
                    i = nextValue;
                }
                break;
        }

        // combination of the simulation of the specific event with the user defined anomaly
        if (anomaly != 6) {
            // Calculate the random Position of the wrong Measure
            int manipulatePos = r.nextInt(humidityChange.size() + 1);
            humidityChange = manipulate(manipulatePos, anomaly, humidityChange);
        }

        return humidityChange;

    }

    private static ArrayList<Float> manipulate(int manipulatePos, int anomaly, ArrayList<Float> simList){
        switch (anomaly) {
            case 3:
                if (manipulatePos == 0) {
                    lowerLimNext = startHum + max_rateOfChange;
                } else {
                    lowerLimNext = (float) simList.get(manipulatePos - 1) + max_rateOfChange;
                }
                upperLimNext = upperLimit;
                nextValue = calcHumChange(lowerLimNext, upperLimNext);
                simList.add(manipulatePos, nextValue);

                break;
            case 4:
                simList.add(manipulatePos, -1f);
                break;
            case 5:
                simList.add(manipulatePos, -1f);
                break;
        }

        return simList;
    }


	/**
	 * Calculates the random desired humidity value within the specified limits.
	 *
	 * @param lowerLimit lower limit for the random humidity value
	 * @param upperLimit uppper limit for the random humidity value
	 * @return random humidity value within the limits
	 */
    private static float calcHumChange(float lowerLimit, float upperLimit) {
        tempHumValue = lowerLimit + r.nextFloat() * (upperLimit - lowerLimit);

        return (float) (Math.round(tempHumValue * 100) / 100.0);
    }

	/**
	 * Calculates the maximum simulation time for the test case of humidity increase
	 * and decrease.
	 *
	 * @param optimumRHLL optimum room humidity lower limit
	 * @param optimumRHUL optimum room humidity upper limit
	 * @param event       humidity rise/decrease
	 * @return simulation time
	 */
    private static int calcSimTime(float optimumRHLL, float optimumRHUL, int event) {
        int simulationTime = 0;
        switch (event) {
            case 1:
                simulationTime = (int) (optimumRHUL + 2);
                break;
            case 2:
                simulationTime = (int) optimumRHLL - 2;
                break;
        }
        return simulationTime;

    }

	/**
	 * Defines the optimum room humidity for the selected room.
	 *
	 * @param room where the humidity should be measured
	 */
	private static void setOptimumHumidity(String room) {
		switch (room) {
			case "a": // living room
			case "b": // office
			case "c": // bedroom
				optimumRHLL = 40.00f;
				optimumRHUL = 60.00f;
				break;
			case "d": // bathroom
				optimumRHLL = 50.00f;
				optimumRHUL = 70.00f;
				break;
			case "e": // kitchen
				optimumRHLL = 50.00f;
				optimumRHUL = 60.00f;
				break;
			case "f": // basement
				optimumRHLL = 50.00f;
				optimumRHUL = 65.00f;
				break;
		}
	}


}
