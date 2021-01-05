import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the interface that the user will interact with. It allows the user to connect to a server and chat with
 * other users as well as play a 'game' with them.
 *
 * @author pamat
 */

public class Client extends Application {

    /**
     * Networking variables*/

    private String hostname;
    private int port;
    public String name = "";
    private ChatReadThread read;
    private VBox chatMessages;
    private boolean isTyping = false;

    public AtomicBoolean isConnectedChat = new AtomicBoolean(false);
    public AtomicBoolean isConnectedGame = new AtomicBoolean(false);

    private Socket socket;
    private Socket gameSocket;
    private PrintWriter gameWriter;

    private HashMap<String, Group> others = new HashMap<>();

    /**
     * Game variables*/

    private int WORLD_WIDTH = 16;
    private int WORLD_HEIGHT = 16;

    private int MAP_WIDTH = WORLD_WIDTH * 16;
    private int MAP_HEIGHT = WORLD_HEIGHT * 16;

    Group stuff = new Group();
    private World world;
    private BorderPane gp;

    Rectangle rect = new Rectangle(5, 5, Color.RED);
    public BooleanProperty isUp = new SimpleBooleanProperty();
    public BooleanProperty isDown = new SimpleBooleanProperty();
    public BooleanProperty isLeft = new SimpleBooleanProperty();
    public BooleanProperty isRight = new SimpleBooleanProperty();

    public BooleanBinding anyPressed = isUp.or(isDown.or(isLeft.or(isRight)));
    Label playerName = new Label();

    /**
     * What the program does when launching
     */

    public void start(Stage stage){
        /**
         * Creating simple chat window*/
        Group root = new Group();
        Scene scene = new Scene(root, 600, 400);

        ScrollPane chatWindow = new ScrollPane();
        chatWindow.setPrefSize(400, 300);
        chatWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatWindow.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        chatMessages = new VBox();
        chatWindow.setContent(chatMessages);

        TextField chatBar = new TextField();
        chatBar.setText("Message");

        BorderPane bp = new BorderPane();
        bp.setCenter(chatWindow);
        bp.setBottom(chatBar);

        /**
         * Setting up server-client stuff*/

        TextField ipAddressField = new TextField();
        ipAddressField.setText("ip");
        TextField portField = new TextField();
        portField.setText("port");
        TextField nameField = new TextField();
        nameField.setText("name");

        Button connectToServer = new Button("Connect");
        VBox serverPane = new VBox();
        serverPane.getChildren().addAll(ipAddressField, portField, nameField, connectToServer);
        bp.setRight(serverPane);

        /**
         * This big lambda connects the client to the server*/

        connectToServer.setOnMouseClicked(event -> {
            try {
                port = Integer.parseInt(portField.getText());
                hostname = ipAddressField.getText();
                name = nameField.getText();
                playerName.setText(name);

                socket = new Socket(hostname, port);
                Text connected = new Text("Connected to server");
                chatMessages.getChildren().add(connected);
                isConnectedChat.set(true);

                read = new ChatReadThread(socket, this);
                read.start();

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output,true);
                writer.println(name);



                gameSocket = new Socket(hostname, 2112);
                chatMessages.getChildren().add(new Text("Connected to game server"));
                //Showing game area once connected

                world = new World();
                gp = world.buildWorld(MAP_WIDTH, MAP_HEIGHT, stuff, this, rect);
                initializePlayerMovement(scene, rect, world.getPosX(), world.getPosY());
                bp.setLeft(gp);

                OutputStream gameOutput = gameSocket.getOutputStream();
                gameWriter = new PrintWriter(gameOutput, true);
                gameWriter.println(name);
                isConnectedGame.set(true);
                GameReadThread gameRead = new GameReadThread(gameSocket, this);
                gameRead.start();
                sendPlayerData();

                //Restructure some stuff on the server pane, we're not using them anymore
                serverPane.getChildren().removeAll(connectToServer);
                ipAddressField.setDisable(true);
                portField.setDisable(true);
                nameField.setDisable(true);

                // lambda to send messages to chat server
                chatBar.setOnKeyPressed(event1 -> {
                    KeyCode kc = event1.getCode();
                    if (kc.equals(KeyCode.ENTER)){
                        if(!chatBar.getText().equals("")){
                            writer.println(chatBar.getText());
                            chatBar.setText("");
                        }
                    }
                });
                System.out.println("finished try");

            } catch (UnknownHostException e){
                e.printStackTrace();
                Text output = new Text("Chat server not found");
                chatMessages.getChildren().add(output);


            } catch (IOException e){
                e.printStackTrace();
                Text output = new Text("I/O Error: " + e.getMessage());
                isConnectedChat.set(false);
                isConnectedGame.set(false);
                chatMessages.getChildren().add(output);

            }


        });

        /**
         *  Starting Application*/

        root.getChildren().addAll(bp);
        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Getter/Setter methods
     */


    public String getUserName() {
        return  name;
    }

    /**
     * Tasks
     */

    public synchronized void addAsServer(String msg) {
        Task<Text> task = new Task<Text>() {
            @Override
            public Text call() throws Exception {
                Text text = new Text(msg);
                return text;
            }
        };
        task.setOnSucceeded(event -> {
            chatMessages.getChildren().add(task.getValue());
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    public synchronized void addNewUser(String userName) {
        System.out.println("Adding new user: " + userName);
        Task<Group> task = new Task<Group>() {
            @Override
            protected Group call() throws Exception {
                Group user = new Group();
                Rectangle userSprite = new Rectangle(5, 5, Color.BLUE); //edit later for random colors
                Text text = new Text(userName);

                text.layoutXProperty().bind(userSprite.xProperty());
                text.layoutYProperty().bind(userSprite.yProperty());

                user.getChildren().addAll(userSprite, text); //Rectangle is added at index 0
                others.put(userName, user);
                return user; //Change to void? why are we returning this
            }
        };

        task.setOnSucceeded(event -> {
            stuff.getChildren().add(task.getValue());
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    public synchronized void deleteUser(String userName){
        Task<Group> task = new Task<Group>() {
            @Override
            protected Group call() throws Exception {
                Group user = others.get(userName);
                return user;
            }
        };

        task.setOnSucceeded(event -> {
            stuff.getChildren().remove(task.getValue());
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    public synchronized void updateUser(String userName, int newX, int newY){
        System.out.println("Updating user location: " + userName);
        Task<Rectangle> task = new Task<Rectangle>() {
            @Override
            protected Rectangle call() throws Exception {
                Group playerGroup = others.get(userName);
                if (playerGroup == null){
                    System.out.println(userName + " not found. Adding new user");
                    addNewUser(userName);
                }
                Rectangle playerRect = (Rectangle)playerGroup.getChildren().get(0);
                return playerRect;
            }
        };

        task.setOnSucceeded(event -> {
            Rectangle playerRect = task.getValue();
            playerRect.setX(newX);
            playerRect.setY(newY);
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    public synchronized void sendPlayerData(){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String result = "";
                while(isConnectedGame.get() == true){
                    result = name;
                    result += " " + (int)rect.getX();
                    result += " " + (int)rect.getY();
                    gameWriter.println(result);
                    Thread.sleep(250);
                }
                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }


    /**
     * Helper methods
     */

    public void initializePlayerMovement(Scene scene, Rectangle rect, Label xPos, Label yPos){

        scene.setOnKeyPressed(event -> {
            KeyCode kc = event.getCode();
                if (kc.equals(KeyCode.W)) {
                    isUp.set(true);
                }
                if (kc.equals(KeyCode.S)) {
                    isDown.set(true);
                }
                if (kc.equals(KeyCode.A)) {
                    isLeft.set(true);
                }
                if (kc.equals(KeyCode.D)) {
                    isRight.set(true);
                }
        });

        scene.setOnKeyReleased(event -> {
            KeyCode kc = event.getCode();
                if (kc.equals(KeyCode.S)){
                    isDown.set(false);
                }
                if (kc.equals(KeyCode.W)){
                    isUp.set(false);
                }
                if (kc.equals(KeyCode.A)){
                    isLeft.set(false);
                }
                if (kc.equals(KeyCode.D)){
                    isRight.set(false);
                }
        });


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isUp.get() == true){
                    rect.setY(rect.getY() - 2);
                    yPos.setText(String.valueOf(rect.getY()));
                }
                if (isDown.get() == true){
                    rect.setY(rect.getY() + 2);
                    yPos.setText(String.valueOf(rect.getY()));
                }
                if (isLeft.get() == true){
                    rect.setX(rect.getX() - 2);
                    xPos.setText(String.valueOf(rect.getX()));
                }
                if (isRight.get() == true){
                    rect.setX(rect.getX() + 2);
                    xPos.setText(String.valueOf(rect.getX()));
                }

            }
        };

        anyPressed.addListener((obs, wasPressed, isNowPressed) -> {
            if (isConnectedGame.get() == true){
                timer.start();
            } if (anyPressed.get() == false || isConnectedGame.get() == false){
                timer.stop();
            }
        });
    }

}
