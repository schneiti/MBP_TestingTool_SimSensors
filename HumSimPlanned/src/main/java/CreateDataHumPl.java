import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Simulator for a planned simulation of a humidity-sensor, simulation two events (humidity decrease/increase) combinable with anomalies.
 */
public class CreateDataHumPl {
    static float upperLimit = 90.000f;
    static float max_rateOfChange = 0.07f;
    static int frequency = 500;
    static float tempHumValue;
    static float startHum;
    static float upperLimNext;
    static float lowerLimNext;
    static float humValue;
    static float optimumRHUL;
    static float tempForward;
    static float valueNotRounded;
    static float optimumRHLL;
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
    public static ArrayList<Float> CreateData(int event, int anomaly, String room, float simTime, int amountEvents,
                                       int amountAnomalies) {

        if (event == 1 || event == 2) {
            if (anomaly == 3 || anomaly == 4 || anomaly == 5 || anomaly == 6) {
                if (room.equals("a") || room.equals("b") || room.equals("c") || room.equals("d") || room.equals("e")
                        || room.equals("f")) {
                    simList = plannedTestHumChange(room, event, anomaly, simTime, amountEvents, amountAnomalies);

                }
            }
        } else if (event == 3 || event == 4 || event == 5 || event == 6) {
            // calculate the list of temperatures with the selected outlier type
            simList = plannedTestWrongMeasure(event, amountAnomalies, simTime);

        }
        return simList;
    }

    /**
     * Calculates a periodical increasing and decreasing list of humidity change with planned anomalies.
     *
     * @param anomaly         kind of anomaly
     * @param amountAnomalies amount of anomalies to be simulated
     * @param time            simulation time
     * @return simulation List
     */
    public static ArrayList<Float> plannedTestWrongMeasure(int anomaly, int amountAnomalies, float time) {
        ArrayList<Float> humChangePlanned = new ArrayList<>();

        int counter = 1;

        // set the values of the optimal room temperature depending on the specifie room
        setOptimumHumidity("a");

        int simTime = (int) ((time * 3600) + 0.5);
        // calculate the start value of the simulation
        startHum = calcNextValue(optimumRHLL, optimumRHUL);
        humChangePlanned.add(startHum);

        // Calculate the list of increasing temperature
        while (humChangePlanned.size() < simTime) {
            while (nextValue <= optimumRHUL) {
                if (counter < simTime) {
                    lowerLimNext = (float) humChangePlanned.get(counter - 1);
                    upperLimNext = (float) humChangePlanned.get(counter - 1) + max_rateOfChange;
                    nextValue = calcNextValue(lowerLimNext, upperLimNext);
                    humChangePlanned.add(nextValue);
                    counter++;
                } else {
                    break;
                }

            }
            while (nextValue >= optimumRHLL) {

                if (counter < simTime) {
                    upperLimNext = (float) humChangePlanned.get(counter - 1);
                    lowerLimNext = (float) humChangePlanned.get(counter - 1) - max_rateOfChange;
                    nextValue = calcNextValue(lowerLimNext, upperLimNext);
                    humChangePlanned.add(nextValue);
                    counter++;
                } else {
                    break;
                }

            }
        }
        
        // Calculate a List of random Positions for the outliers
        Set randomPosList = calcRandomList(amountAnomalies, humChangePlanned);

        if (anomaly != 6) {
            humChangePlanned = manipulate(randomPosList, anomaly, humChangePlanned);

        }

        return humChangePlanned;

    }

    /**
     * Simulates the planned humidity rise/decrease with the right amount of Events and the combination of anomalies.
     * (Periodical increase and decrease until simulation time is reached)
     *
     * @param room            room in which humidity should be measured
     * @param event           humidity rise/decrease
     * @param anomaly         kind of anomaly
     * @param time            in hours
     * @param amountEvents    amount of humidity rise/decrease to simulate
     * @param amountAnomalies amount of anomalies to simulate
     * @return simulation List
     */
    public static ArrayList<Float> plannedTestHumChange(String room, int event, int anomaly, float time,
                                                             int amountEvents, int amountAnomalies) {
        ArrayList<Float> humChangePlanned = new ArrayList<>();
        float timeForEvent = timeForEvent(time, amountEvents);
        float tempChange = calcMaxHumChange(timeForEvent);
        int counter = 1;
        int simTimeOne = (int) (timeForEvent + 0.5);

        // set the values of the optimal room humidity depending on the specifie room
        setOptimumHumidity(room);

        // definiere, wo die Simulation beginnen soll --> Nach testfall entscheiden
        // Wenn HumAnstieg, dann Obergrenze - (humChange/2);
        // Wenn HumAbfall, dann Untergrenze + (HumChange/2);

        switch (event) {
            case 1:
                startHum = optimumRHUL - (tempChange / 2);

                humChangePlanned.add(0, startHum);
                // Calculate the list of increasing humidity
                tempForward = startHum;
                for (int j = 1; j < simTimeOne; j++) {
                    valueNotRounded = tempForward + max_rateOfChange;
                    tempForward = valueNotRounded;
                    humChangePlanned.add((float) (Math.round(valueNotRounded * 100) / 100.0));
                    counter++;
                }

                for (int i = 1; i < ((amountEvents * 2) - 1); i++) {
                    if (i % 2 == 0) {
                        for (int j = 0; j <= simTimeOne; j++) {
                            humChangePlanned.add(humChangePlanned.get(j));
                        }
                    } else if (i % 2 == 1) {
                        for (int j = 1; j < simTimeOne; j++) {
                            humChangePlanned.add(humChangePlanned.get(simTimeOne - j));
                        }
                    }

                }
                break;
            case 2:
                float tempStartTemp = optimumRHLL + (tempChange / 2);
                startHum = (float) (Math.round((tempStartTemp * 100) / 100.0));

                humChangePlanned.add(0, startHum);
                // Calculate the list of increasing humidity

                tempForward = startHum;
                for (int j = 0; j < simTimeOne; j++) {
                    valueNotRounded = tempForward - max_rateOfChange;
                    humChangePlanned.add((float) (Math.round((valueNotRounded) * 100) / 100.0));
                    counter++;
                }

                for (int i = 1; i < ((amountEvents * 2) - 1); i++) {
                    if (i % 2 == 0) {
                        for (int j = 0; j <= simTimeOne; j++) {
                            humChangePlanned.add(humChangePlanned.get(j));
                        }
                    } else if (i % 2 == 1) {
                        for (int j = 0; j <= simTimeOne; j++) {

                            humChangePlanned.add(humChangePlanned.get(simTimeOne - j));
                        }
                    }

                }
                break;

        }
        if (anomaly != 6) {
            // Calculate a List of random Positions for the outliers
            Set manipulatePosList = calcRandomList(amountAnomalies, humChangePlanned);
            humChangePlanned = manipulate(manipulatePosList, anomaly, humChangePlanned);

        }

        return humChangePlanned;

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
                        lowerLimNext = startHum + max_rateOfChange;
                    } else {
                        lowerLimNext = (float) simList.get((Integer) elem - 1) + max_rateOfChange;
                    }
                    upperLimNext = upperLimit;
                    nextValue = calcNextValue(lowerLimNext, upperLimNext);
                    simList.add((Integer) elem, nextValue);
                    break;
                case 4:
                    simList.add((Integer) elem, -1f);
                    break;
                case 5:
                    simList.add((Integer) elem, -1f);
                    break;
            }
        }
        return simList;
    }


    /**
     * Calculates random numbers for the outliers in the simulation list.
     *
     * @param simList simulation list
     * @return random position for the outlier
     */
    public static int getRandomNumbers(ArrayList simList) {
        return r.nextInt(simList.size());
    }

    /**
     * Generates a Set of different positions for the outliers.
     *
     * @param amountAnomalies amount of anomalies to simulate
     * @param simList         simulated List without anomalies
     * @return Set of different random positions
     */
    public static Set calcRandomList(int amountAnomalies, ArrayList simList) {
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
     * Calculates the amount of smaples for one event in case of a planned simulation.
     *
     * @param simulationTime simulation time defined by the user
     * @param amountEvents   amount of humidity rise/decrease
     * @return time for one Event in seconds
     */
    private static float timeForEvent(float simulationTime, int amountEvents) {
        float humAmount = (amountEvents * 2) - 1;
        float timeInMilli = simulationTime * 3600f;

        return timeInMilli / humAmount;
    }

    /**
     * Calculates the maximum humidity change that can occure in the time for one event.
     *
     * @param timeForEvent time needed for one event
     * @return maximal humidity change for the time of one event
     */
    private static float calcMaxHumChange(float timeForEvent) {
        float humChange = timeForEvent * max_rateOfChange;
        if (humChange > 15) {
            max_rateOfChange = 15 / timeForEvent;
            humChange = 15;
        }
        return humChange;
    }


    /**
     * Calculates the random desired humidity value within the specified limits.
     *
     * @param lowerLimit lower limit for the random humidity value
     * @param upperLimit upper limit for the random humidity value
     * @return random humidity value within the limits
     */
    private static float calcNextValue(float lowerLimit, float upperLimit) {
        tempHumValue = lowerLimit + r.nextFloat() * (upperLimit - lowerLimit);
        humValue = (float) (Math.round(tempHumValue * 100) / 100.0);

        return humValue;

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
