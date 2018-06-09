//package iwium.asia;

import robocode.*;

import java.io.*;
import java.util.*;

public class MyRobot extends AdvancedRobot {

    //learning params
    private final static double ALPHA = 0.01;
    private final static double GAMMA = 0.8;
    private static final double EXPLORE_RATE = 0.8;

    //robot state
    private double robot_x = 0;
    private double robot_y = 0;
    private double angleToEnemy = 0;
    private double distanceToEnemy = 0;
    private double enemyEnergy = 0;

    // q table params
    private static final int POSITION_BUCKETS = 8;
    private static final int ANGLE_BUCKETS = 4;
    private static final int DISTANCE_BUCKETS = 4;
    private static final int ACTIONS_SIZE = 4;

    private Map<String, Double> QTable = new HashMap<String, Double>();

    private double totalReward = 0.0;
    private double reward = 0;

    private static final String TAB = "    ";


    private CSVWriter csvWriter = new CSVWriter("data.csv");


    public void run() {

//        initQTable();
//        saveQTable();

        csvWriter.writeLineSeparatedWithDelimiter("header1", "header2");

        try {
            loadQTable();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

            int action = getAction();
            String stateAction = getCurrentState() + action;
            double oldValue = QTable.get(stateAction);

            //perform action
            performAction(action);

            //update the table
            int getMaxValueAction = getMaxAction(getCurrentState());
            double futureValue = QTable.get(getCurrentState() + getMaxValueAction);
            double updateValue = (1 - ALPHA) * oldValue + ALPHA * (reward + GAMMA * futureValue);
            QTable.put(stateAction, updateValue);
            writeStateActionToCSV(action);

            totalReward += reward;
            reward = 0;
        }
    }

    private void performAction(int action) {

        System.out.println("Performing action: " + action);

        switch (action) {
            case 1:
                //move left
                setTurnLeft(45);
                setAhead(150);
                break;
            case 2:
                //move right
                setTurnRight(45);
                setAhead(150);
                break;
            case 3:
                //turn gun left
                setTurnGunLeft(45);
                break;
            case 4:
                //turn gun right
                setTurnGunRight(45);
                break;
        }

        execute();
    }

    private String getCurrentState() {

        //quantizied
        int x = quantizePosition(getX()); //robot_x
        int y = quantizePosition(getY()); //robot_y
        int angle = quantizeAngle(angleToEnemy);
        int distanceToEnemy = quantizeDistance(this.distanceToEnemy);

        return x + "" + y + "" + angle + "" + distanceToEnemy;
    }

    private void writeStateActionToCSV(int action) {

        int x = quantizePosition(getX()); //robot_x
        int y = quantizePosition(getY()); //robot_y
        int angle = quantizeAngle(angleToEnemy);
        int distanceToEnemy = quantizeDistance(this.distanceToEnemy);

        csvWriter.writeLineSeparatedWithDelimiter("" + x, "" + y, "" + angle, "" + distanceToEnemy, "" + action);
    }

    private Random random = new Random();

    private int getAction() {
        if (random.nextDouble() < EXPLORE_RATE) {
            return getRandomAction();
        } else {
            return getMaxAction(getCurrentState());
        }
    }

    private int getRandomAction() {
        return random.nextInt(ACTIONS_SIZE) + 1;
    }

    private int getMaxAction(String state) {
        int action = 0;
        double actionValue = -Double.MAX_VALUE;
        for (String key : QTable.keySet()) {
            if (key.startsWith(state)) {
                double value = QTable.get(key);
                if (actionValue < value) {
                    action = Character.getNumericValue(key.charAt(key.length() - 1));
                    actionValue = value;
                }
            }
        }
        return action;
    }

    private int quantizePosition(double position) {
        int positionBucket = 0;

        if (position <= 100) {
            positionBucket = 1;
        } else if (position <= 200) {
            positionBucket = 2;
        } else if (position <= 300) {
            positionBucket = 3;
        } else if (position <= 400) {
            positionBucket = 4;
        } else if (position <= 500) {
            positionBucket = 5;
        } else if (position <= 600) {
            positionBucket = 6;
        } else if (position <= 700) {
            positionBucket = 7;
        } else if (position <= 800) {
            positionBucket = 8;
        }
        return positionBucket;

    }

    private int quantizeAngle(double angle) {

        int angleBucket = 0;

        if (angle <= 90) {
            angleBucket = 1;
        } else if (angle <= 180) {
            angleBucket = 2;
        } else if (angle <= 270) {
            angleBucket = 3;
        } else if (angle <= 360) {
            angleBucket = 4;
        }

        return angleBucket;
    }

    private int quantizeDistance(double distance) {

        int distanceBucket = 0;

        if (distance <= 250) {
            distanceBucket = 1;
        } else if (distance <= 500) {
            distanceBucket = 2;
        } else if (distance <= 750) {
            distanceBucket = 3;
        } else if (distance <= 1000) {
            distanceBucket = 4;
        }

        return distanceBucket;
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        distanceToEnemy = e.getDistance();
        angleToEnemy = e.getBearing() + 180;
        enemyEnergy = e.getEnergy();
        robot_x = getX();
        robot_y = getY();
        fire(1);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        setBack(10);
        reward -= 3;
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        setBack(20);
        reward -= 3.5;
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        reward -= 2;
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        reward += 3;
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        System.out.println("Reward of round number " + getRoundNum() + ": " + totalReward);
        totalReward = 0;
        saveQTable();
    }

    private void loadQTable() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getDataFile("QTable.txt")));
        String line = reader.readLine();
        try {
            while (line != null) {
                String splitLine[] = line.split(TAB);
                String key = splitLine[0];
                double value = Double.parseDouble(splitLine[1]);
                QTable.put(key, value);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
    }

    private void saveQTable() {
        PrintStream w = null;
        try {
            w = new PrintStream(new RobocodeFileOutputStream(getDataFile("QTable.txt")));
            for (String key : QTable.keySet()) {
                w.println(key + TAB + QTable.get(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (w != null) {
                w.flush();
                w.close();
            }
        }
    }

    private void initQTable() {
        for (int i = 1; i <= POSITION_BUCKETS; i++) {
            for (int j = 1; j <= POSITION_BUCKETS; j++) {
                for (int k = 1; k <= ANGLE_BUCKETS; k++) {
                    for (int l = 1; l <= DISTANCE_BUCKETS; l++) {
                        for (int m = 1; m <= ACTIONS_SIZE; m++) {
                            QTable.put(i + "" + j + "" + k + "" + l + "" + m, 0.0);
                        }
                    }
                }
            }
        }
    }

    private class CSVWriter {

        private static final String DELIMITER = ",";
        private final String fileName;

        public CSVWriter(String fileName, String... headers) {
            this.fileName = fileName;
            writeLineSeparatedWithDelimiter(headers);
        }

        public void writeLineSeparatedWithDelimiter(String... strings) {
            writeLine(getTextSeparetedByDelimiter(strings));
        }

        private String getTextSeparetedByDelimiter(String... strings) {

            if (strings == null) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            for (String header : strings) {
                sb.append(header).append(DELIMITER);
            }
            return sb.substring(0, sb.length() - 1);
        }

        public void writeLine(String text) {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new RobocodeFileOutputStream(getDataFile(fileName).getName(), true));
                writer.println(text);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
        }
    }
}
