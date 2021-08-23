import java.util.ArrayList;
import java.util.Random;

/**
 * Simulator for a simulation of a temperature-sensor, simulation two events (temperature decrease/increase) combinable with anomalies.
 */
public class CreateDataTemp {
    static float lowerLimit = -55.00f;
    static float upperLimit = 125.00f;
    static float max_rateOfChange = 0.025f;
    static final int frequency = 1000;
    static float tempTempValue;
    static float startTemp;
    static float upperLimNext;
    static float lowerLimNext;
    static float optimumRtUL;
    static float optimumRtLL;
    static float nextValue;
    static Random r = new Random();
    static ArrayList<Float> simList = null;


    /**
     * Handles the calls to the methods in different cases and returns the simulation list.
     *
     * @param event   humidity rise/decrease
     * @param anomaly kind of anomaly
     * @param room    room in which humidity should be measured
     * @return user defined simulation List
     */
    public static ArrayList CreateDataTemp(int event, int anomaly, String room) {
        if (event == 1 || event == 2) {
            if (anomaly == 3 || anomaly == 4 || anomaly == 5 || anomaly == 6) {
                if (room.equals("a") || room.equals("b") || room.equals("c") || room.equals("d") || room.equals("e")
                        || room.equals("f")) {
                    simList = temperatureChange(room, event, anomaly);
                }
            }
        } else if (event == 3 || event == 4 || event == 5 || event == 6) {
            // calculate the list of temperatures with the selected ano´maly type
            simList = simWrongMeasures(event);
        }
        return simList;
    }


    /**
     * Simulation of wrong measured values generates a list of sensor data of the
     * classes "outlier", "wrong value type", "missing value".
     *
     * @param anomaly Kind of anomaly
     * @return Array list with simulated sensor data with anomalies
     */
    public static ArrayList<Float> simWrongMeasures(int anomaly) {
        ArrayList<Float> wrongMeasuresList = new ArrayList<>();
        setOptimumTemp("a");

        // Calculate the random Position of the wrong measure
        int manipulatePos = r.nextInt(19 + 1);

        // Calculate a random start value within the upper and lower limits of the temperature sensor
        startTemp = calcNextValue(optimumRtLL, optimumRtUL);
        wrongMeasuresList.add(0, startTemp);

        // Generate the list of sensor data with the wrong value (depending on the testCase) at the calculated position.
        for (int i = 0; i <= 20; i++) {
            if (i == manipulatePos) {
                if(anomaly!=6){
                    wrongMeasuresList = manipulate(manipulatePos, anomaly, wrongMeasuresList);
                }

            } else if (i == manipulatePos + 1) {
                if (manipulatePos != 0) {
                    lowerLimNext = (float) wrongMeasuresList.get(i - 2);
                    upperLimNext = (float) wrongMeasuresList.get(i - 2) + (max_rateOfChange * 2);
                    nextValue = calcNextValue(lowerLimNext, upperLimNext);
                    wrongMeasuresList.add(i, nextValue);
                }

            } else {
                if (i != 0) {
                    lowerLimNext = (float) wrongMeasuresList.get(i - 1);
                    upperLimNext = (float) wrongMeasuresList.get(i - 1) + max_rateOfChange;
                    nextValue = calcNextValue(lowerLimNext, upperLimNext);
                    wrongMeasuresList.add(i, nextValue);
                }

            }
        }

        return wrongMeasuresList;
    }

    /**
     * Simulation of the temperature rise or fall in a certain room, which violate
     * the limits of the optimal room temperature.
     *
     * @param room    in which the temp is measured
     * @param event   temperature rise/fall to be simulated
     * @param anomaly anomaly to be simulated
     * @return simulated List
     */
    public static ArrayList<Float> temperatureChange(String room, int event, int anomaly) {
        ArrayList<Float> tempChange = new ArrayList<>();
        int counter = 1;
        int simTime;
        float i;

        // set the values of the optimal room temperature depending on the specified room
        setOptimumTemp(room);

        // calculate the temperature rise (case 1) or the temperature drop (case 2)
        switch (event) {
            case 1:
                // Random start value between 5°C under the optimum lower limit and 1°C under the
                // optimum upper limit
                startTemp = calcNextValue((optimumRtLL - 5.00f), (optimumRtUL - 1.00f));
                tempChange.add(0, startTemp);

                simTime = calcSimTime(optimumRtLL, optimumRtUL, event);

                // Calculate the list of increasing temperature
                i = startTemp;
                while (i < simTime) {
                    lowerLimNext = tempChange.get(counter - 1);
                    upperLimNext = tempChange.get(counter - 1) + max_rateOfChange;
                    nextValue = calcNextValue(lowerLimNext, upperLimNext);
                    tempChange.add(nextValue);
                    ++counter;
                    i = nextValue;
                }

                break;
            case 2:
                // Random start value between 2°C over the optimum lower limit and 1°C over the  optimum upper limit
                startTemp = calcNextValue((optimumRtLL + 2.00f), (optimumRtUL + 1.00f));
                tempChange.add(0, startTemp);

                simTime = calcSimTime(optimumRtLL, optimumRtUL, event);

                // Calculate the list of decreasing temperature
                i = startTemp;
                while (i > simTime) {
                    lowerLimNext = (float) tempChange.get(counter - 1) - max_rateOfChange;
                    upperLimNext = (float) tempChange.get(counter - 1);
                    nextValue = calcNextValue(lowerLimNext, upperLimNext);
                    tempChange.add(nextValue);
                    ++counter;
                    i = nextValue;
                }
                break;
        }

        if (anomaly != 6) {
            // random position of the anomaly in the simulation list
            int manipulatePos = r.nextInt(tempChange.size() + 1);
            tempChange = manipulate(manipulatePos, anomaly, tempChange);
        }


        return tempChange;
    }


    /**
     * Returns the simulation list combined with the anomaly to be simulated.
     *
     * @param manipulatePos position of the anomaly in the simulation list
     * @param anomaly       anomaly to be simulated
     * @param simList       simulation list to be manipulated with an anomaly
     * @return manipulated simulation list with an anomaly
     */
    private static ArrayList<Float> manipulate(int manipulatePos, int anomaly, ArrayList<Float> simList) {

        switch (anomaly) {
            case 3:
                if (manipulatePos == 0) {
                    lowerLimNext = startTemp + max_rateOfChange;

                } else {
                    lowerLimNext = (float) simList.get(manipulatePos - 1) + max_rateOfChange;
                }
                upperLimNext = upperLimit;
                nextValue = calcNextValue(lowerLimNext, upperLimNext);
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
     * Calculates the random desired temperature value within the specified limits.
     *
     * @param lowerLimit lower limit for the random tempValue
     * @param upperLimit upper limit for the random tempValue
     * @return random temperature value within the limits
     */
    private static float calcNextValue(float lowerLimit, float upperLimit) {
        tempTempValue = lowerLimit + r.nextFloat() * (upperLimit - lowerLimit);

        return (float) (Math.round(tempTempValue * 100) / 100.0);
    }


    /**
     * Calculates the maximum simulation time for the test case of temperature
     * increase and decrease.
     *
     * @param event       temperature rise/decrease
     * @param optimumRtLL optimum room temperature - lower limit
     * @param optimumRtUL optimum room temperature - upper limit
     * @return simulation time
     */
    private static int calcSimTime(float optimumRtLL, float optimumRtUL, int event) {
        int simulationTime = 0;
        switch (event) {
            case 1:
                simulationTime = (int) (optimumRtUL + 2);
                break;
            case 2:
                simulationTime = (int) optimumRtLL - 2;
                break;
        }
        return simulationTime;

    }


    /**
     * Defines the optimum room temperature for the selected room.
     *
     * @param room where the temp should be measured
     */
    private static void setOptimumTemp(String room) {
        switch (room) {
            case "a": // living room
            case "b": // office
                optimumRtLL = 20.00f;
                optimumRtUL = 23.00f;
                break;
            case "c": // bedroom
                optimumRtLL = 16.00f;
                optimumRtUL = 18.00f;
                break;
            case "d": // bathroom
                optimumRtLL = 23.00f;
                optimumRtUL = 25.00f;
                break;
            case "e": // kitchen
                optimumRtLL = 18.00f;
                optimumRtUL = 20.00f;
                break;
            case "f": // basement
                optimumRtLL = 10.00f;
                optimumRtUL = 15.00f;
                break;
        }
    }

}
