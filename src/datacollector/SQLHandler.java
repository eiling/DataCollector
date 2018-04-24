package datacollector;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class Handler{
    private DataQueue dataQueue;

    private JTextArea out;

    private Connection connection;

    private String hostName = "host.database.windows.net";
    private String dbName = "db";
    private String user = "user";
    private String password = "password";
    private String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;" +
                    "encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
            hostName, dbName, user, password);

    Handler(DataQueue q, JTextArea o){
        dataQueue = q;
        out = o;

        loop();
    }
    private void connect(){
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e){
            e.printStackTrace();
            out.append(e.getMessage());
            System.out.println("Connection error.");
        }
    }
    private void loop(){
        while(true){
            while(dataQueue.ready()){
                try{
                    if(connection==null || connection.isClosed()) connect();
                } catch(SQLException e){
                    System.out.println("Connection.isClosed error.");
                }

                try{
                    Statement st = connection.createStatement();
                    st.executeUpdate("INSERT INTO temp (codProduto, temp, peak, valey, hr, dt) " +
                            "VALUES ("
                            + "'" + dataQueue.getID() + "',"
                            + dataQueue.getAverage() + ","
                            + dataQueue.getMax() + ","
                            + dataQueue.getMin() + ","
                            + "'" + dataQueue.getTime() + "'," //end time of the 5 min interval
                            + "'" + dataQueue.getDate() + "'" + ")");
                    out.append("Successful Update! Time: " + dataQueue.getTime() + "\n");

                    dataQueue.remove();
                } catch(SQLException e){
                    e.printStackTrace();
                    System.out.println("INSERT error");
                }
            }

            try{ Thread.sleep(1000);} catch(InterruptedException ignored){}
        }
    }
}