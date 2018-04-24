package datacollector;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class DataCollector implements SerialPortEventListener{
    private SerialPort serialPort;
    private String portID;

    private InputStream input;
    private StringBuilder stringBuilder;

    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    private final DataQueue dataQueue;
    private LocalDateTime currentDateTime;

    private boolean active;

    DataCollector(String portID, DataQueue dataQueue){
        active = true;

        this.portID = portID;
        stringBuilder = new StringBuilder();

       this.dataQueue = dataQueue;

        currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        currentDateTime = currentDateTime.plusSeconds(5 - (currentDateTime.getSecond() % 5));

        //magic line to make ACM work
        System.setProperty("gnu.io.rxtx.SerialPorts", portID);
    }
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent){
        if(serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE){
            try{
                int total = input.available();
                while(total-- > 0){
                    final char c = (char) input.read();
                    if(c == '\n'){
                        handleData(stringBuilder.toString());
                        stringBuilder.delete(0, stringBuilder.length());
                    } else{
                        stringBuilder.append(c);
                    }
                }
            } catch(IOException e){
                terminate();
            }
        }
    }
    void setupPort(){
        while(serialPort == null){
            CommPortIdentifier portIdentifier = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
            while (portEnum.hasMoreElements()) {
                CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                if (currPortId.getName().equals(portID)) {
                    portIdentifier = currPortId;
                    break;
                }
            }

            try {
                if(portIdentifier != null)
                    serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(),
                            TIME_OUT);
            } catch(PortInUseException e){
                e.printStackTrace();
            }

            if (serialPort == null) try{ Thread.sleep(500); } catch(InterruptedException ignored){}
        }

        //setup parameters
        try{
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
        } catch(UnsupportedCommOperationException e){
            e.printStackTrace();
        }

        //create input stream
        try{
            input = serialPort.getInputStream();
        } catch(IOException e){
            e.printStackTrace();
        }

        //add event listener
        try{
            serialPort.addEventListener(this);
        } catch(TooManyListenersException e){
            e.printStackTrace();
        }
        serialPort.notifyOnDataAvailable(true);
    }
    private void handleData(String line){
        float temperature = Float.parseFloat(line);

        LocalDateTime now = LocalDateTime.now();
        if(now.isAfter(currentDateTime) && now.isBefore(currentDateTime.plusSeconds(1))){
            dataQueue.add(temperature, portID, currentDateTime.toLocalTime(), currentDateTime.toLocalDate());

            currentDateTime = currentDateTime.plusSeconds(5);
        }
    }
    private void terminate(){
        serialPort.removeEventListener();
        serialPort.close();

        active = false;
    }
    DataQueue getQueue(){
        return dataQueue;
    }

    boolean isActive() {
        return active;
    }
    public void shutdown(){
        active = false;
    }
}
