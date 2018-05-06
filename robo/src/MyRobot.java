//package iwium.asia;

import robocode.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

public class MyRobot extends AdvancedRobot {

    //learning params
    final double alpha = 0.1;
    final double gamma = 0.9;

    //robot state
    double robot_x = 0;
    double robot_y = 0;
    double angleToEnemy = 0;
    double distance = 0;

    double enemyEnergy = 0;


    int count = 0;

    // q table params
    static int ACTIONS_COUNT = 4;
    static int ANGLE_BUCKETS = 4;
    static int POSITION_BUCKETS = 8;
    static int DISTANCE_BUCKETS = 4;

    int[] action = new int[4];
    int[] totalStateActions = new int[POSITION_BUCKETS*POSITION_BUCKETS*ANGLE_BUCKETS*DISTANCE_BUCKETS*action.length];
    int[] totalActions = new int[4];
    String[][] QTable = new String[totalStateActions.length][2];
    double totalReward = 0.0;
    static double[] rewardsArray = new double[1000];

    double reward = 0;

    boolean explore = true;
    boolean greedy = false;

    String stateAction = "";
    int stateActionIndex = 0;
    int afterStateActionIndex = 0;


    public void run() {

        if(count==0){

            //For initializing text file in the first run use the three lines of code. once the text file is generated in \Rl_check comment this out
//            initQTable();
//            saveQTable();
            try {
                loadQTable();
            }
            catch (IOException e) {
                e.printStackTrace();

            }
        }
        count=count+1;

        while (true) {
            turnGunRight(360);

            //if explore
            if (explore) {
                saveQTable();
                //generate random action
                int action = getRandomAction();

                stateAction = getCurrentState() + "" + action;
                //look into the table
                for(int i=0;i<QTable.length;i++){
                    if(QTable[i][0].equals(stateAction))
                    {
                        stateActionIndex = i;
                        break;
                    }
                }

                double beforeQvalue = Double.parseDouble(QTable[stateActionIndex][1]);

                double beforeEnergy = getEnergy();
                double beforeEnemyEnergy = enemyEnergy;
                //perform action
                performAction(action);
//                turnGunRight(360);

                //look what happened, insert the reward
                double afterEnergy = getEnergy();
                double afterEnemyEnergy = enemyEnergy;
                double actionReward = (afterEnergy - beforeEnergy) - (afterEnemyEnergy - beforeEnemyEnergy);
                //TODO where does it go?

                //look for current state in table
                String state = getCurrentState();
                String nextStateAction = state + getMaxAction(state);

                for(int i=0;i<QTable.length;i++){
                    if(QTable[i][0].equals(nextStateAction))
                    {
                        afterStateActionIndex = i;
                        break;
                    }
                }
                double afterQvalue = Double.parseDouble(QTable[afterStateActionIndex][1]);

                //update the table
                beforeQvalue = beforeQvalue+alpha*(reward+gamma*afterQvalue - beforeQvalue);
                QTable[stateActionIndex][1]=Double.toString(beforeQvalue);

            }

            //if greedy
            if (greedy) {
                //look for actions for this state in q table
                String state = getCurrentState();
                // get one with max reward
                int maxAction = getMaxAction(state);
                String stateAction = state + maxAction;

                for(int i=0;i<QTable.length;i++){
                    if(QTable[i][0].equals(stateAction))
                    {
                        stateActionIndex = i;
                        break;
                    }
                }

                double beforeQvalue = Double.parseDouble(QTable[stateActionIndex][1]);

                double beforeEnergy = getEnergy();
                double beforeEnemyEnergy = enemyEnergy;
                //perform action
                performAction(maxAction);
//                turnGunRight(360);

                //look what happened, insert the reward
                double afterEnergy = getEnergy();
                double afterEnemyEnergy = enemyEnergy;
                double actionReward = (afterEnergy - beforeEnergy) - (afterEnemyEnergy - beforeEnemyEnergy);
                //TODO where does it go?

                //look for current state in table
                state = getCurrentState();
                String nextStateAction = state + getMaxAction(state);

                for(int i=0;i<QTable.length;i++){
                    if(QTable[i][0].equals(nextStateAction))
                    {
                        afterStateActionIndex = i;
                        break;
                    }
                }
                double afterQvalue = Double.parseDouble(QTable[afterStateActionIndex][1]);

                //update the table
                beforeQvalue = beforeQvalue+alpha*(reward+gamma*afterQvalue - beforeQvalue);
                QTable[stateActionIndex][1]=Double.toString(beforeQvalue);

            }
//            ahead(100);
//            turnGunRight(360);
//            back(100);
//            turnGunRight(360);
        }
    }

    public void performAction(int action) {

        System.out.println("Performing action: " + action);

        switch(action) {
            case 1:
                //move left
                setTurnLeft(90);
                setAhead(150);
                break;
            case 2:
                //move right
                setTurnRight(90);
                setAhead(150);
                break;
            case 3:
                //turn gun left
                setTurnGunLeft(90);
                break;
            case 4:
                //turn gun right
                setTurnGunRight(90);
                break;
        }
    }

    public String getCurrentState() {

        //quantizied
        int x = quantizePosition(robot_x);
        int y = quantizePosition(robot_y);
        int angle = quantizeAngle(angleToEnemy);
        int distanceToEnemy = quantizeDistance(distance);

        return x+""+y+""+angle+""+distanceToEnemy;
    }

    public int getMaxAction(String state) {
        int maxAction = 0;
        double maxActionValue = 0;
        String[][] possibleStateActions = new String[ACTIONS_COUNT][2];
        int index = 0;
        for(int i=0;i<QTable.length;i++){
            if(QTable[i][0].startsWith(state)) {
                possibleStateActions[index][0] = QTable[i][0];
                possibleStateActions[index][1] = QTable[i][1];
                if (Double.parseDouble(possibleStateActions[index][1]) > maxActionValue) {
                    maxAction = Integer.parseInt(possibleStateActions[index][0])%10;
                    maxActionValue = Double.parseDouble(possibleStateActions[index][1]);
                }
                index++;
            }
        }
        return maxAction;
    }

    public int quantizePosition(double position) {
        int q_position = 0;
        if((position > 0) && (position<=100)){
            q_position=1;
        }
        else if((position > 100) && (position<=200)){
            q_position=2;
        }
        else if((position > 200) && (position<=300)){
            q_position=3;
        }
        else if((position > 300) && (position<=400)){
            q_position=4;
        }
        else if((position > 400) && (position<=500)){
            q_position=5;
        }
        else if((position > 500) && (position<=600)){
            q_position=6;
        }
        else if((position > 600) && (position<=700)){
            q_position=7;
        }
        else if((position > 700) && (position<=800)){
            q_position=8;
        }
        return q_position;

    }

    public int quantizeAngle(double angle) {

        int q_angle = 0;

        if((angle > 0) && (angle<=90)){
            q_angle=1;
        }
        else if((angle > 90) && (angle<=180)){
            q_angle=2;
        }
        else if((angle > 180) && (angle<=270)){
            q_angle=3;
        }
        else if((angle > 270) && (angle<=360)){
            q_angle=4;
        }
        return q_angle;
    }

    public int quantizeDistance(double distance) {
        int qdistancetoenemy = 0;

        if((distance > 0) && (distance<=250)){
            qdistancetoenemy = 1;
        }
        else if((distance > 250) && (distance<=500)){
            qdistancetoenemy = 2;
        }
        else if((distance > 500) && (distance<=750)){
            qdistancetoenemy = 3;
        }
        else if((distance > 750) && (distance<=1000)){
            qdistancetoenemy = 4;
        }

        return qdistancetoenemy;
    }

    public int getRandomAction() {
        Random rand = new Random();
        int randomNum = rand.nextInt(totalActions.length) + 1;

        return randomNum;
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        distance = e.getDistance();
        angleToEnemy = e.getBearingRadians()+getHeadingRadians();
        enemyEnergy = e.getEnergy();
        robot_x = getX();
        robot_y = getY();
        fire(1);
    }


    public void onHitByBullet(HitByBulletEvent e) {
        back(10);
        reward -= 3;
    }

    public void onHitWall(HitWallEvent e) {
        back(20);
        reward -= 3.5;
    }

    public void onHitRobot(HitRobotEvent event) {
        reward-=2;
    }

    public void onBulletHit(BulletHitEvent event) {
        reward+=3;
    }

    public void onRoundEnded(RoundEndedEvent e) {
        System.out.println("cumulative reward of one full battle is ");
        System.out.println(totalReward);
        System.out.println("index number ");
        System.out.println(getRoundNum());
        rewardsArray[getRoundNum()]=totalReward;
        for(int i=0;i<rewardsArray.length;i++){
            System.out.println(rewardsArray[i]);
            System.out.println();
        }

//        saveQTable();


//        index1=index1+1;
//        save1();
    }

    public void loadQTable() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getDataFile("QTable.txt")));
        String line = reader.readLine();
        try {
            int index=0;
            while (line != null) {
                String splitLine[] = line.split("    ");
                QTable[index][0]=splitLine[0];
                QTable[index][1]=splitLine[1];
                index = index+1;
                line= reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
    }

    public void saveQTable() {
        PrintStream w = null;
        try {
            w = new PrintStream(new RobocodeFileOutputStream(getDataFile("QTable.txt")));
            for (int i=0;i<QTable.length;i++) {
                w.println(QTable[i][0]+"    "+QTable[i][1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            w.flush();
            w.close();
        }
    }

    public void initQTable() {
        int[] totalStateActions = new int[POSITION_BUCKETS*POSITION_BUCKETS*ANGLE_BUCKETS*DISTANCE_BUCKETS*action.length];
        QTable = new String[totalStateActions.length][2];
        int z=0;
        for(int i=1;i<=POSITION_BUCKETS;i++){
            for(int j=1;j<=POSITION_BUCKETS;j++){
                for(int k=1;k<=ANGLE_BUCKETS;k++){
                    for(int l=1;l<=DISTANCE_BUCKETS;l++){
                        for(int m=1;m<=action.length;m++){
                            QTable[z][0]=i+""+j+""+k+""+l+""+m;
                            QTable[z][1]="0";
                            z=z+1;
                        }
                    }
                }
            }
        }
    }
}
