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

        for(int i = 0; i < dataCollectors.length; i++){
            dataCollectors[i] = new DataCollector(portIDs[i], dataQueue);
            dataCollectors[i].setup();
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

            handler.setUrl(hostName, dbName, user, password);
        }

        new Thread(handler::start).start();

        in.nextLine();

        handler.deactivate();

        for(DataCollector dataCollector : dataCollectors) dataCollector.deactivate();

        while(!handler.isDone()) try{ Thread.sleep(100); } catch(InterruptedException ignored){}

        in.close();

        handler.disconnect();
    }
}
