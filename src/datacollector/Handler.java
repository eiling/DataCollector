package datacollector;

import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class Handler{
    private final DataCollector[] dataCollectors;
    private final DataQueue dataQueue;

    private boolean active;

    //private Label lastUpdate = new Label("Last updated: ");

    private String url;
    private Connection connection;

    Handler(DataCollector[] dataCollectors, DataQueue dataQueue){
        this.dataCollectors = dataCollectors;
        this.dataQueue = dataQueue;
        active = true;
    }
    void setUrl(String hostName, String dbName, String user, String password){
        url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;" +
                        "encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
                hostName, dbName, user, password);
    }
    boolean connect(){
        try {
            connection = DriverManager.getConnection(url);
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
        while(aDataCollectorIsActive()){
            while(dataQueue.hasElements()){
                handleData();
            }

            try{ Thread.sleep(100);} catch(InterruptedException ignored){}
        }

        while(dataQueue.hasElements()){
            handleData();
        }

        active = false;
    }

    private void handleData() {
        DataQueue.Node temp = dataQueue.getFirst();

        if(temp != null){
            checkConnection();

            try{
                Statement st = connection.createStatement();
                st.executeUpdate("INSERT INTO temperature (portid, temperature, ttime, tdate) " +
                        "VALUES ("
                        + "'" + temp.id + "',"
                        + temp.temperature + ","
                        + "'" + temp.time + "',"
                        + "'" + temp.date + "'" + ")");
                System.out.print("Successful Update! " + temp.id + " - " + temp.time + "\n");

                /*lastUpdate.setText(
                        "Last updated: " + temp.temperature + "(" + temp.time + ")"
                );*/

                dataQueue.remove();
                System.out.println("data deleted");
            } catch(SQLException e){
                System.out.println(e.getMessage());
            }
        }
    }

    boolean isActive(){
        return active;
    }

    private boolean aDataCollectorIsActive(){
        boolean aDataCollectorIsActive = false;
        for(int i = 0; i < dataCollectors.length; i++){
            if(dataCollectors[i].isActive()){
                aDataCollectorIsActive = true;
                updateStatus(dataCollectors[i].getPortID(), 1);
            }
            else{
                final int index = i;
                new Thread(
                        () -> dataCollectors[index] = new DataCollector(dataCollectors[index].getPortID(), dataQueue)
                ).start();
                updateStatus(dataCollectors[i].getPortID(), 0);
            }
        }
        return aDataCollectorIsActive;
    }

    private void updateStatus(String portID, int active){
        if(active != 1 && active != 0) throw new RuntimeException("Active value must be 1 or 0");

        checkConnection();

        try{
            Statement st = connection.createStatement();
            st.executeUpdate("UPDATE produto SET active=" + (active == 1 ? 1 : 0) + " WHERE portid='" + portID + "'");
            System.out.println("Successful Update! " + portID + " now " + (active == 1 ? "active" : "inactive"));
        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void checkConnection(){
        try{
            if(connection.isClosed()){
                System.out.println("Reconnecting.");
                connect();
            }
        } catch(SQLException e){
            System.out.println("Connection.isClosed error.");
            System.out.println(e.getMessage());
        }
    }

    /*Label getLastUpdate(){
        return lastUpdate;
    }*/
}
