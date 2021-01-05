import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is the game server program.
 * This class is heavily based off of the 'ChatServer' sample given at www.codejava.net, and has been altered slightly
 * for this project.
 *
 * @author www.codejava.net
 * @author pamat
 */
public class GameServer {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<UserGameThread> userThreads = new HashSet<>();

    public GameServer(int port) {
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Game Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected to game");

                UserGameThread newUser = new UserGameThread(socket, this);
                userThreads.add(newUser);
                newUser.start();

            }

        } catch (IOException ex) {
            System.out.println("Error in the game server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port;
        if (args.length < 1) {
            System.out.println("Syntax: java ChatServer <port-number>");
            System.out.println("Defaulting to port 2112");
            port = 2112;
        }else {
            port = Integer.parseInt(args[0]);
        }



        GameServer server = new GameServer(port);
        server.execute();
    }

    /**
     * Delivers a message from one user to others (broadcasting)
     */
    void broadcast(String message, UserGameThread excludeUser) {
        for (UserGameThread aUser : userThreads) {
            if (aUser != excludeUser){
                aUser.sendMessage(message);
            }
        }
    }


    /**
     * Stores username of the newly connected client.
     */
    void addUserName(String userName) {
        userNames.add(userName);
    }

    /**
     * When a client is disconneted, removes the associated username and UserChatThread
     */
    void removeUser(String userName, UserGameThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(aUser);
            System.out.println("The user " + userName + " quitted");
        }
    }

    Set<String> getUserNames() {
        return this.userNames;
    }

    /**
     * Returns true if there are other users connected (not count the currently connected user)
     */
    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }
}