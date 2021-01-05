import java.io.*;
import java.net.*;

/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * This code is based off of the 'ReadThread' example given at www.codejava.net, it has been altered for this project
 *
 * @author www.codejava.net
 * @author pamat
 */
public class ChatReadThread extends Thread {
    private BufferedReader chatReader;
    private Socket chatSocket;
    private Client client;

    public ChatReadThread(Socket chatSocket,Client client) {
        this.chatSocket = chatSocket;
        this.client = client;

        try {
            InputStream chatInput = chatSocket.getInputStream();
            chatReader = new BufferedReader(new InputStreamReader(chatInput));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                //update chat first
                String response = chatReader.readLine();
                System.out.println("\n + [CHAT]" + response);

                // prints the username after displaying the server's message
                if (client.getUserName() != null) {
                    //System.out.print("[" + client.getUserName() + "]: ");
                    client.addAsServer(response); // "[" + client.getUserName() + "]: " +
                }
            } catch (IOException ex) {
                client.addAsServer("Error reading from chat server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }
}