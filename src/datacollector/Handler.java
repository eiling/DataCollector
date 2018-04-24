package datacollector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class Handler{
    private final DataCollector dataCollector;
    private final DataQueue dataQueue;

    private boolean active;

    private static Connection connection;

    private static String url;

    Handler(DataCollector dataCollector){
        this.dataCollector = dataCollector;
        dataQueue = dataCollector.getQueue();
        active = true;
    }
    static void setUrl(String hostName, String dbName, String user, String password){
        url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;" +
                        "encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
                hostName, dbName, user, password);
    }
    static boolean connect(){
        try {
            connection = DriverManager.getConnection(url);
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Connection error.");
            return false;
        }
    }
    static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void start(){
        while(dataCollector.isActive()){
            while(dataQueue.ready()){
                checkConnection();
                handleData();
            }

            try{ Thread.sleep(100);} catch(InterruptedException ignored){}
        }

        while(dataQueue.hasElements()){
            checkConnection();
            handleData();
        }

        active = false;
    }

    private void checkConnection(){
        try{
            if(connection.isClosed()) {
                System.out.println("Reconnecting.");
                connect();
            }
        } catch(SQLException e){
            System.out.println("Connection.isClosed error.");
        }
    }
    private void handleData() {
        try{
            Statement st = connection.createStatement();
            st.executeUpdate("INSERT INTO temp (origin, temperature, ttime, tdate) " +
                    "VALUES ("
                    + "'" + dataQueue.getID() + "',"
                    + dataQueue.getTemperature() + ","
                    + "'" + dataQueue.getTime() + "',"
                    + "'" + dataQueue.getDate() + "'" + ")");
            System.out.print("Successful Update! Time: " + dataQueue.getTime() + "\n");

            dataQueue.remove();
            System.out.println("data deleted");
        } catch(SQLException e){
            e.printStackTrace();
            System.out.println("INSERT error");
        }
    }

    boolean isActive(){
        return active;
    }
}
