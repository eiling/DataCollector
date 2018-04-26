/*
package datacollector;

import gnu.io.CommPortIdentifier;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.Enumeration;

public class App extends Application {
    private final static int WIDTH = 600;
    private final static int HEIGHT = 550;

    private Stage stage;

    private ArrayList<String> portIDList = new ArrayList<>();
    private DataCollector[] dataCollectors;
    private DataQueue dataQueue = new DataQueue();
    private Handler handler;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        stage.setTitle("Setup connection - DataCollector");
        stage.setScene(setupConnectionScene());

        stage.show();
    }
    @Override
    public void stop(){
        stage.setScene(shutdownScene());

        if(dataCollectors != null) for(DataCollector dataCollector : dataCollectors) dataCollector.deactivate();

        if(handler != null){
            while (handler.isActive()){
                try{
                    Thread.sleep(100);
                } catch(InterruptedException ignored){
                }
            }
            handler.disconnect();
        }
    }
    private Scene setupConnectionScene(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label hostName = new Label("Host Name:");
        grid.add(hostName, 0, 0);

        TextField hostTextField = new TextField();
        grid.add(hostTextField, 1, 0);

        Label dbName = new Label("Database Name:");
        grid.add(dbName, 0, 1);

        TextField dbTextField = new TextField();
        grid.add(dbTextField, 1, 1);

        Label userName = new Label("User Name:");
        grid.add(userName, 0, 2);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 2);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 3);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 3);

        Label log = new Label();
        Button btn = new Button("Sign in");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(log);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 5);

        btn.setOnAction(event -> {
            handler = new Handler(dataCollectors, dataQueue);

            handler.setUrl(
                    hostTextField.getText(),
                    dbTextField.getText(),
                    userTextField.getText(),
                    pwBox.getText());
            if(handler.connect()){
                stage.setTitle("Setup devices - DataCollector");
                stage.setScene(setupDevicesScene());
            }
        });

        return new Scene(grid, WIDTH, HEIGHT);
    }
    private Scene setupDevicesScene(){
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);

        Label portIDLabel = new Label("Port ID:");
        box.getChildren().add(portIDLabel);

        for (String portID : portIDList) {
            HBox line = new HBox();
            line.setAlignment(Pos.CENTER);

            portIDLabel = new Label(portID);
            line.getChildren().add(portIDLabel);

            Button remove = new Button("Remove");
            line.getChildren().add(remove);

            remove.setOnAction(event -> {
                portIDList.remove(portID);
                stage.setScene(setupDevicesScene());
            });

            box.getChildren().add(line);
        }

        HBox line = new HBox();
        line.setAlignment(Pos.CENTER);

        TextField idTextField = new TextField();
        line.getChildren().add(idTextField);

        Button add = new Button("Add");
        line.getChildren().add(add);

        add.setOnAction(event -> {
            portIDList.add(idTextField.getText());
            stage.setScene(setupDevicesScene());
        });

        box.getChildren().add(line);

        Button done = new Button("Done");
        box.getChildren().add(done);

        done.setOnAction(event -> {
            final int N = portIDList.size();

            dataCollectors = new DataCollector[N];

            for(int i = 0; i < N; i++){
                dataCollectors[i] = new DataCollector(portIDList.get(i), new DataQueue());

                final int temp = i;

                new Thread(() -> {
                    dataCollectors[temp].setupPort();
                }, "Port " + i + " setup").start();
            }

            new Thread(() -> handler.start()).start();

            stage.setScene(monitorScene());
        });

        return new Scene(box, WIDTH, HEIGHT);
    }
    private Scene monitorScene(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(handler.getLastUpdate(), 0, 0);
        for(int j = 0; j < dataCollectors.length/4; j++){
            for(int i = 0; i < 4; i++){
                //grid.add(new Label(dataCollectors[i].getPortID()), i, j);
                grid.add(dataCollectors[i].getLastTemperature(), i, j+1);
            }
        }

        return new Scene(grid, WIDTH, HEIGHT);
    }
    private Scene shutdownScene(){
        StackPane root = new StackPane();
        root.getChildren().add(new Label("Shutting down"));
        return new Scene(root, WIDTH, HEIGHT);
    }
    public static void main(String[] args) {
        launch(args);
    }
}*/
