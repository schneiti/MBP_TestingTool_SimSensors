import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Simulator for a planned simulation of a temperature-sensor, simulation two events (temperature decrease/increase) combinable with anomalies.
 */
public class CreateDataTempPl {
    static float upperLimit = 125.00f;
    static float max_rateOfChange = 0.025f;
    static float tempTempValue;
    static float startTemp;
    static float upperLimNext;
    static float lowerLimNext;
    static float tempValue;
    static float optimumRtUL;
    static float optimumRtLL;
    static float nextValue;
    static Random r = new Random();
    static ArrayList<Float> simList = null;


    /**
     * Handles the calls to the methods in different cases and returns the simulation list.
     *
     * @param event           humidity rise/decrease
     * @param anomaly         kind of anomaly
     * @param room            room in which humidity should be measured
     * @param simTime         simulation time
     * @param amountEvents    amount of events to be simulated
     * @param amountAnomalies amount of anomalies to be simulated
     * @return user defined simulation List
     */
    public static ArrayList<Float> CreateData(int event, int anomaly, String room, float simTime, int amountEvents, int amountAnomalies) {
        if (event == 1 || event == 2) {
            if (anomaly == 3 || anomaly == 4 || anomaly == 5 || anomaly == 6) {
                if (room.equals("a") || room.equals("b") || room.equals("c") || room.equals("d") || room.equals("e")
                        || room.equals("f")) {
                    simList = plannedTestTempChange(room, event, anomaly, simTime, amountEvents, amountAnomalies);
                }
            }
        } else if (event == 3 || event == 4 || event == 5 || event == 6) {
            simList = plannedTestWrongMeasure(event, amountAnomalies, simTime);
        }
        return simList;
    }

    /**
     * Planned Simulation of Outliers.
     *
     * @param anomaly         Kind of anomaly
     * @param amountAnomalies Amount of outliers
     * @param time            simulation time
     * @return simulation List
     */
    public static ArrayList<Float> plannedTestWrongMeasure(int anomaly, int amountAnomalies, float time) {
        ArrayList<Float> tempChangePlanned = new ArrayList<>();

        int counter = 1;

        // set the values of the optimal room temperature depending on the specifie room
        setOptimumTemp("a");

        int simTime = (int) ((time * 3600) + 0.5);

        // calculate the start temperature
        startTemp = calcMaxTempChange(optimumRtLL, optimumRtUL);
        tempChangePlanned.add(startTemp);


        // Calculate the list of increasing temperature
        while (tempChangePlanned.size() < simTime) {
            while (nextValue <= optimumRtUL) {
                if (counter < simTime) {
                    lowerLimNext = (float) tempChangePlanned.get(counter - 1);
                    upperLimNext = (float) tempChangePlanned.get(counter - 1) + max_rateOfChange;
                    nextValue = calcMaxTempChange(lowerLimNext, upperLimNext);
                    tempChangePlanned.add(nextValue);
                    counter++;
                } else {
                    break;
                }
            }
            while (nextValue >= optimumRtLL) {
                if (counter < simTime) {
                    upperLimNext = (float) tempChangePlanned.get(counter - 1);
                    lowerLimNext = (float) tempChangePlanned.get(counter - 1) - max_rateOfChange;
                    nextValue = calcMaxTempChange(lowerLimNext, upperLimNext);
                    tempChangePlanned.add(nextValue);
                    counter++;
                } else {
                    break;
                }

            }
        }


        if (anomaly != 6) {
            Set manipulatePosList = calcRandomList(amountAnomalies, tempChangePlanned);
            tempChangePlanned = manipulate(manipulatePosList, anomaly, tempChangePlanned);
        }

        return tempChangePlanned;

    }


    /**
     * Simulates the planned temperature rise/decrease with the right amount of events and the combination of anomalies
     * (Periodical increase and decrease until simulation time is reached)
     *
     * @param room            in which the temp is measured
     * @param event           temp rise/decrease
     * @param anomaly         combination with anomalies
     * @param simTime         in hours
     * @param amountEvents    amount of temp riseses/decreases
     * @param amountAnomalies amount of outliers in the simulation
     * @return simulation list
     */
    public static ArrayList<Float> plannedTestTempChange(String room, int event, int anomaly, float simTime, int amountEvents, int amountAnomalies) {
        ArrayList<Float> tempChangePlanned = new ArrayList<>();
        float timeForEvent = timeForEvent(simTime, amountEvents);
        float tempChange = calcMaxTempChange(timeForEvent);
        int counter = 1;
        int simTimeOne = (int) (timeForEvent + 0.5);

        // set the values of the optimal room temperature depending on the specifie room
        setOptimumTemp(room);


        float valueNotRounded;
        switch (event) {
            case 1: // increase

                // define where the simulation should start: upper limit- (tempchange/2);
                startTemp = optimumRtUL - (tempChange / 2);
                tempChangePlanned.add(0, startTemp);

                // Calculate the list of increasing temperature
                float tempForward = startTemp;
                for (int j = 1; j < simTimeOne; j++) {
                    valueNotRounded = tempForward + max_rateOfChange;
                    tempForward = valueNotRounded;
                    tempChangePlanned.add((float) (Math.round(valueNotRounded * 100) / 100.0));
                }

                for (int i = 1; i < ((amountEvents * 2) - 1); i++) {
                    if (i % 2 == 0) {
                        for (int j = 0; j <= simTimeOne; j++) {
                            tempChangePlanned.add(tempChangePlanned.get(j));
                        }
                    } else {
                        for (int j = 1; j <= simTimeOne; j++) {
                            tempChangePlanned.add(tempChangePlanned.get(simTimeOne - j));
                        }
                    }

                }
                break;
            case 2:
                float tempStartTemp = optimumRtLL + (tempChange / 2);
                startTemp = (float) (Math.round((tempStartTemp * 100) / 100.0));

                tempChangePlanned.add(0, startTemp);
                // Calculate the list of increasing temperature
                tempForward = startTemp;
                for (int j = 0; j < simTimeOne; j++) {
                    valueNotRounded = tempForward - max_rateOfChange;
                    tempForward = valueNotRounded;
                    tempChangePlanned.add((float) (Math.round((valueNotRounded) * 100) / 100.0));
                }

                for (int i = 1; i < ((amountEvents * 2) - 1); i++) {
                    if (i % 2 == 0) {
                        for (int j = 0; j <= simTimeOne; j++) {
                            tempChangePlanned.add(tempChangePlanned.get(j));
                        }
                    } else {
                        for (int j = 0; j <= simTimeOne; j++) {

                            tempChangePlanned.add(tempChangePlanned.get(simTimeOne - j));
                        }
                    }

                }
                break;
        }

        if (anomaly != 6) {
            // Calculate a List of random Positions for the outliers
            Set manipulatePosList = calcRandomList(amountAnomalies, tempChangePlanned);
            tempChangePlanned = manipulate(manipulatePosList, anomaly, tempChangePlanned);


        }
        return tempChangePlanned;

    }

    /**
     * Returns the simulation list combined with the anomaly to be simulated.
     *
     * @param manipulatePosList position list of the anomaly in the simulation list
     * @param anomaly           anomaly to be simulated
     * @param simList           simulation list to be manipulated with an anomaly
     * @return manipulated simulation list with an anomaly
     */
    private static ArrayList<Float> manipulate(Set manipulatePosList, int anomaly, ArrayList<Float> simList) {
        for (Object elem : manipulatePosList) {
            switch (anomaly) {
                case 3:
                    if ((Integer) elem == 0) {
                        lowerLimNext = startTemp + max_rateOfChange;

                    } else {
                        lowerLimNext = (float) simList.get((Integer) elem - 1) + max_rateOfChange;
                    }
                    upperLimNext = upperLimit;
                    nextValue = calcMaxTempChange(lowerLimNext, upperLimNext);
                    simList.set((Integer) elem, nextValue);
                    break;
                case 4:
                    simList.set((Integer) elem, -1f);
                    break;
                case 5:
                    simList.set((Integer) elem, -1f);
                    break;
            }
        }

        return simList;

    }


    /**
     * Calculates the maximum temperature change that can occure in the time for one event.
     *
     * @param timeForEvent time needed for one temperature rise/decrease
     * @return maximal temp change for one event
     */
    private static float calcMaxTempChange(float timeForEvent) {
        float tempChange = timeForEvent * max_rateOfChange;
        if (tempChange > 15) {
            max_rateOfChange = 15 / timeForEvent;
            tempChange = 15;
        }
        return tempChange;
    }


    /**
     * Calculates the amount of smaples for one event in case of a planned simulation.
     *
     * @param simulationTime simulation time defined by the user
     * @param amountEvents   amount of temperature rises/decreases
     * @return time for one Event in seconds
     */
    private static float timeForEvent(float simulationTime, int amountEvents) {
        float tempAmount = (amountEvents * 2) - 1;
        float timeInMilli = simulationTime * 3600f;

        return timeInMilli / tempAmount;
    }


    /**
     * Calculates random numbers for the outliers in the simulation list.
     *
     * @param simList simulation list
     * @return random position for the outlier
     */
    private static int getRandomNumbers(ArrayList simList) {
        int randomNumb;
        randomNumb = r.nextInt(simList.size());
        return randomNumb;
    }

    /**
     * Generates a Set of different positions for the outliers.
     *
     * @param amountAnomalies amount of anomalies to simulate
     * @param simList         simulated List without anomalies
     * @return Set of different random positions
     */
    private static Set calcRandomList(int amountAnomalies, ArrayList simList) {
        int wrongMeasurePos;
        Set randomPosList = new HashSet<>();

        for (int i = 0; i < amountAnomalies; i++) {
            wrongMeasurePos = getRandomNumbers(simList);
            while (randomPosList.contains(wrongMeasurePos)) {
                wrongMeasurePos = getRandomNumbers(simList);
            }
            randomPosList.add(wrongMeasurePos);
        }
        return randomPosList;
    }


    /**
     * Calculates the random desired temperature value within the specified limits.
     *
     * @param lowerLimit lower limit of the sensor values
     * @param upperLimit upper limit of the sensor values
     * @return random temperature value within the limits
     */
    private static float calcMaxTempChange(float lowerLimit, float upperLimit) {
        tempTempValue = lowerLimit + r.nextFloat() * (upperLimit - lowerLimit);
        tempValue = (float) (Math.round(tempTempValue * 100) / 100.0);

        return tempValue;

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
