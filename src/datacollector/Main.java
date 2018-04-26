package datacollector;

import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);

        System.out.println("Enter port names separated by space:");
        String ports = in.nextLine();

        String[] portIDs = ports.split(" ");

        DataQueue dataQueue = new DataQueue();

        DataCollector[] dataCollectors = new DataCollector[portIDs.length];

        boolean running = true;
        while(running){
            for(int i = 0; i < dataCollectors.length; i++){
                dataCollectors[i] = new DataCollector(portIDs[i], dataQueue);
            }

            Handler handler = new Handler(dataCollectors, dataQueue);

            System.out.print("Host Name: ");
            String hostName = in.nextLine();
            System.out.print("Database Name: ");
            String dbName = in.nextLine();
            System.out.print("User: ");
            String user = in.nextLine();
            System.out.print("Password: ");
            String password = in.nextLine();

            handler.setUrl(hostName, dbName, user, password);
            while(!handler.connect()){
                System.out.println("Connection failed");
                System.out.print("Host Name: ");
                hostName = in.nextLine();
                System.out.print("Database Name: ");
                dbName = in.nextLine();
                System.out.print("User: ");
                user = in.nextLine();
                System.out.print("Password: ");
                password = in.nextLine();

                in.close();

                handler.setUrl(hostName, dbName, user, password);
            }

            new Thread(handler::start).start();

            while(handler.isActive()){
                try{
                    Thread.sleep(1000);
                } catch(InterruptedException ignored){
                }
            }

            System.out.print("All devices disconnected. Stop? (y/n): ");
            in = new Scanner(System.in);
            String answer = in.nextLine();
            while(!answer.equalsIgnoreCase("y") || !answer.equalsIgnoreCase("n")){
                System.out.print("All devices disconnected. Stop? (y/n): ");
                answer = in.nextLine();
            }
            in.close();
            if(answer.equalsIgnoreCase("y")){
                handler.disconnect();
                running = false;
            }
        }
    }
}
