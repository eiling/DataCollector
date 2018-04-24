package datacollector;

public class Main {
    public static void main(String[] args){
        System.out.println("start - " + Thread.activeCount());

        DataQueue[] dataQueues = new DataQueue[10];
        dataQueues[0] = new DataQueue();

        String[] portIDs = new String[10];
        portIDs[0] = "/dev/ttyACM0";

        DataCollector[] dataCollectors = new DataCollector[10];
        dataCollectors[0] = new DataCollector(portIDs[0], dataQueues[0]);

        new Thread(() -> dataCollectors[0].setupPort()).start();
        System.out.println("setup port - " + Thread.activeCount());

        Handler.connect();
        Handler[] handlers = new Handler[10];
        handlers[0] = new Handler(dataCollectors[0]);

        new Thread(() -> handlers[0].start()).start();
        System.out.println("start handler - " + Thread.activeCount());

        while (allHandlersAreActive(handlers)){
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

    private static boolean allHandlersAreActive(Handler[] handlers){
        for(Handler handler : handlers){
            if(handler != null && !handler.isActive()) return false;
        }
        return true;
    }
}
