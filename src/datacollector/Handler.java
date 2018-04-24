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

    private static String hostName = "eiling.database.windows.net";
    private static String dbName = "lily";
    private static String user = "eiling";
    private static String password = "$senha123";
    private static String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;" +
                    "encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
            hostName, dbName, user, password);

    Handler(DataCollector dataCollector){
        this.dataCollector = dataCollector;
        dataQueue = dataCollector.getQueue();
        active = true;
    }
    static void connect(){
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e){
            e.printStackTrace();
            System.out.append(e.getMessage());
            System.out.println("Connection error.");
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
                try{
                    if(connection==null || connection.isClosed()) connect();
                } catch(SQLException e){
                    System.out.println("Connection.isClosed error.");
                }

                try{
                    Statement st = connection.createStatement();
                    st.executeUpdate("INSERT INTO temp (origin, temperature, ttime, tdate) " +
                            "VALUES ("
                            + "'" + dataQueue.getID() + "',"
                            + dataQueue.getTemperature() + ","
                            + "'" + dataQueue.getTime() + "',"
                            + "'" + dataQueue.getDate() + "'" + ")");
                    System.out.append("Successful Update! Time: " + dataQueue.getTime() + "\n");

                    dataQueue.remove();
                } catch(SQLException e){
                    e.printStackTrace();
                    System.out.println("INSERT error");
                }
            }

            try{ Thread.sleep(100);} catch(InterruptedException ignored){}
        }

        active = false;
    }
    boolean isActive(){
        return active;
    }
}