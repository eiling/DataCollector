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
    private boolean settingUp;

    DataCollector(String portID, DataQueue dataQueue){
        active = false;
        settingUp = true;

        this.portID = portID;
        stringBuilder = new StringBuilder();

       this.dataQueue = dataQueue;
    }
    void setup(){
        //magic line to make ACM work
        System.setProperty("gnu.io.rxtx.SerialPorts", portID);

        setupPort();

        //set current target time
        currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        currentDateTime = currentDateTime.plusSeconds(5 - (currentDateTime.getSecond() % 5));

        active = true;
        settingUp = false;
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
                deactivate();
            }
        }
    }
    private void setupPort(){
        while(serialPort == null){
            if(!settingUp) return;

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
        LocalDateTime now = LocalDateTime.now();
        if(now.isAfter(currentDateTime) && now.isBefore(currentDateTime.plusSeconds(1))){
            String[] data = line.split(" ");
            dataQueue.add(data[1], data[0], currentDateTime);

            currentDateTime = currentDateTime.plusSeconds(5);
        } else { //reset if something went wrong
            currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            currentDateTime = currentDateTime.plusSeconds(5 - (currentDateTime.getSecond() % 5));
        }
    }
    void deactivate(){
        if(serialPort != null){
            serialPort.removeEventListener();
            serialPort.close();
        }

        active = false;
        settingUp = false;
    }

    boolean isActive() {
        return active;
    }
    boolean isSettingUp(){
        return settingUp;
    }

    String getPortID(){
        return portID;
    }
}
