import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 *
 * This class is based off of the example given at www.codejava.net. It has been altered for this project.
 *
 * @author www.codejava.net
 * @author pamat
 */
public class UserGameThread extends Thread {
    private Socket socket;
    private GameServer server;
    private PrintWriter writer;

    public UserGameThread(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            String userName = reader.readLine();
            server.addUserName(userName);

            String clientMessage;

            do {
                clientMessage = reader.readLine();
                System.out.println("Broadcasting: " + clientMessage);
                server.broadcast(clientMessage, this);

            } while (!clientMessage.equals(""));

            server.removeUser(userName, this);
            socket.close();

            String serverMessage = userName + " delete";
            server.broadcast(serverMessage, this);

        } catch (IOException ex) {
            System.out.println("Error in UserGameThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (server.hasUsers()) {
            writer.println("Connected users: " + server.getUserNames());
        } else {
            writer.println("No other users connected");
        }
    }

    void printNewUser(String userName){
        writer.print(userName);
    }

    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }
}