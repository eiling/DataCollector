package datacollector;

import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

class Handler{
    private final DataCollector[] dataCollectors;
    private final DataQueue dataQueue;

    private boolean active;
    private boolean done;

    //private Label lastUpdate = new Label("Last updated: ");

    private String url;
    private Connection connection;

    Handler(DataCollector[] dataCollectors, DataQueue dataQueue){
        this.dataCollectors = dataCollectors;
        this.dataQueue = dataQueue;
        active = true;
        done = true;
    }
    void setUrl(String hostName, String dbName, String user, String password){
        url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;" +
                        "encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=5;",
                hostName, dbName, user, password);
    }
    boolean connect(){
        try {
            connection = DriverManager.getConnection(url);
            connection.setNetworkTimeout(null, 1000);
            return true;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return false;
        }
    }
    void disconnect(){
        if(connection == null) return;

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void start(){
        done = false;

        while(active){
            for(int i = 0; i < dataCollectors.length; i++){
                if(!(dataCollectors[i].isActive() || dataCollectors[i].isSettingUp())){
                    System.out.println("New thread to set up " + dataCollectors[i].getPortID());

                    dataCollectors[i] = new DataCollector(dataCollectors[i].getPortID(), dataQueue);
                    new Thread(dataCollectors[i]::setup).start();
                }
            }

            while(dataQueue.hasElements()){
                handleData();
            }

            try{ Thread.sleep(100);} catch(InterruptedException ignored){}
        }

        while(dataQueue.hasElements()){
            handleData();
        }

        done = true;
    }

    private void handleData() {
        DataQueue.Node temp = dataQueue.getFirst();

        try{
            if(connection.isClosed()){
                System.out.println("Reconnecting.");
                connect();
            }
        } catch(SQLException e){
            System.out.println("Connection.isClosed error.");
            System.out.println(e.getMessage());
        }

        try{
            LocalDateTime datetime = temp.datetime;

            String dtString = String.format("%04d-%02d-%02dT%02d:%02d:%02d",
                    datetime.getYear(),
                    datetime.getMonthValue(),
                    datetime.getDayOfMonth(),
                    datetime.getHour(),
                    datetime.getMinute(),
                    datetime.getSecond()
            );

            String temperature = String.format("%.1f", Float.parseFloat(temp.temperature));

            Statement st = connection.createStatement();
            st.setQueryTimeout(1);
            st.executeUpdate("INSERT INTO reading (datetime, id, temperature) " +
                    "VALUES ("
                    + "'" + dtString + "',"
                    + "'" + temp.id + "',"
                    + temperature + ")"
            );
            System.out.println("Successful Update! " + temp.id + " - " + temperature + " - " + dtString);

            /*lastUpdate.setText(
                    "Last updated: " + temp.temperature + "(" + temp.time + ")"
            );*/

            dataQueue.remove();
        } catch(SQLException e){
            System.out.print(e.getMessage() + " || Date: ");
            System.out.println(temp.datetime);
        }
    }

    boolean isDone(){
        return done;
    }

    void deactivate(){
        active = false;
    }

    /*Label getLastUpdate(){
        return lastUpdate;
    }*/
}
