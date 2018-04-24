package datacollector;

import java.util.Scanner;

public class Main {
    private static final int N = 1;

    public static void main(String[] args){
        System.out.println("start - " + Thread.activeCount());

        Scanner in = new Scanner(System.in);
        System.out.print("Host Name: ");
        String hostName = in.nextLine();
        System.out.print("Database Name: ");
        String dbName = in.nextLine();
        System.out.print("User: ");
        String user = in.nextLine();
        System.out.print("Password: ");
        String password = in.nextLine();
        in.close();
        Handler.setUrl(hostName, dbName, user, password);
        Handler.connect();

        DataQueue[] dataQueues = new DataQueue[N];
        dataQueues[0] = new DataQueue();

        String[] portIDs = new String[N];
        portIDs[0] = "/dev/ttyACM0";

        DataCollector[] dataCollectors = new DataCollector[N];
        dataCollectors[0] = new DataCollector(portIDs[0], dataQueues[0]);

        Handler[] handlers = new Handler[N];
        handlers[0] = new Handler(dataCollectors[0]);

        new Thread(() -> {
            dataCollectors[0].setupPort();
            handlers[0].start();
        }).start();
        System.out.println("setup/start - " + Thread.activeCount());

        while (handlersAreActive(handlers)){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("loop - " + Thread.activeCount());
            dataCollectors[0].shutdown(); //for test only; remove this method later
        }

        Handler.disconnect();
        System.out.println("end - " + Thread.activeCount());
    }

    private static boolean handlersAreActive(Handler[] handlers){
        for(Handler handler : handlers){
            if(handler != null && handler.isActive()) return true;
        }
        return false;
    }
}
