package datacollector;

public class Main {
    public static void main(String[] args){
        DataQueue[] dataQueues = new DataQueue[10];
        dataQueues[0] = new DataQueue();

        String[] portIDs = new String[10];
        portIDs[0] = "/dev/ttyACM0";

        DataCollector[] dataCollectors = new DataCollector[10];
        dataCollectors[0] = new DataCollector(portIDs[0], dataQueues[0]);

        new Thread(() -> dataCollectors[0].setupPort()).start();

        Handler.connect();
        Handler[] handlers = new Handler[10];
        handlers[0] = new Handler(dataCollectors[0]);

        new Thread(() -> handlers[0].start()).start();

        while (allHandlersAreActive(handlers)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Handler.disconnect();
    }

    private static boolean allHandlersAreActive(Handler[] handlers){
        for(Handler handler : handlers){
            if(handler != null && !handler.isActive()) return false;
        }
        return true;
    }
}
