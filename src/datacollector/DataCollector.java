package datacollector;

import gnu.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class DataCollector extends JFrame implements SerialPortEventListener{
    private SerialPort serialPort;

    private InputStream input;
    private StringBuilder stringBuilder;

    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    private String portID;

    private volatile DataQueue dataQueue; //Does this need to be volatile?
    private LocalTime currentTime;

    private float max, min;

    private JLabel temp;
    private JTextArea out;

    private DataCollector(String portID){
        super("DataCollector - Port: " + portID);

        addWindowListener(new WindowListener(){
            @Override
            public void windowOpened(WindowEvent e){}

            @Override
            public void windowClosing(WindowEvent e){
                if(serialPort != null){
                    serialPort.removeEventListener();
                    serialPort.close();
                }
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e){}

            @Override
            public void windowIconified(WindowEvent e){}

            @Override
            public void windowDeiconified(WindowEvent e){}

            @Override
            public void windowActivated(WindowEvent e){}

            @Override
            public void windowDeactivated(WindowEvent e){}
        });

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;

        temp = new JLabel();
        temp.setPreferredSize(new Dimension(400, 40));
        temp.setText("Searching for device.");
        out = new JTextArea();
        out.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(out);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        p.add(temp, c);
        c.gridy = 1;
        p.add(scrollPane, c);

        getContentPane().add(p);
        pack();
        setVisible(true);

        //setup
        this.portID = portID;
        stringBuilder = new StringBuilder();

        dataQueue = new DataQueue();
        dataQueue.add();

        currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        currentTime = currentTime.plusMinutes(5 - (currentTime.getMinute() % 5));

        max = 200f; min = -100f; //sensor range: (-50°C, 150°C)

        //magic line to make ACM work
        System.setProperty("gnu.io.rxtx.SerialPorts", portID);

        //open the port
        setupPort();

        //SQLHandler thread
        new Thread(() -> new Handler(dataQueue, out)).start();
    }
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent){
        if(serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE){
            try{
                int total = input.available();
                while(total-- > 0){
                    char ch = (char) input.read();
                    if(ch == '\n'){
                        handleData(stringBuilder.toString());
                        stringBuilder.delete(0, stringBuilder.length());
                    } else{
                        stringBuilder.append(ch);
                    }
                }
            } catch(IOException e){
                temp.setText("Device disconnected.");
                input = null;
                serialPort.removeEventListener();
                serialPort.close();
                serialPort = null;
                setupPort();
            }
        }
    }
    private void setupPort(){
        //open the port
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

            if (serialPort == null){
                try{
                    Thread.sleep(500);
                } catch(InterruptedException ignored){
                }
            }
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
        float te = Float.parseFloat(line);

        if(LocalTime.now().isBefore(currentTime)){
            dataQueue.addToSum(te);
            if(te > max) max = te;
            else if(te < min) min = te;
        } else{
            dataQueue.setID(portID);
            dataQueue.setMax(max);
            dataQueue.setMin(min);
            dataQueue.setTime(currentTime);
            dataQueue.setDate(System.currentTimeMillis());

            currentTime = currentTime.plusMinutes(5);
            dataQueue.add();
            dataQueue.addToSum(te);
        }

        temp.setText("Temperature: " + line);
    }
    public static void main(String[] args){
        new DataCollector("/dev/ttyACM0"); //use invokeLater!
    }
}
