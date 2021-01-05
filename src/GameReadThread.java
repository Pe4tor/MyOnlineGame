import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * This class is based off of the 'ReadThread' example given at www.codejava.net, it has been altered for this project.
 *
 * @author www.codejava.net
 * @author pamat
 */
public class GameReadThread extends Thread {
    private BufferedReader gameReader;
    private Socket gameSocket;
    private Client client;

    public GameReadThread(Socket gameSocket, Client client) {
        this.gameSocket = gameSocket;
        this.client = client;

        try {
            InputStream gameInput = gameSocket.getInputStream();
            gameReader = new BufferedReader(new InputStreamReader(gameInput));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String tokens = gameReader.readLine();
                System.out.println("Received data: " + tokens);
                StringTokenizer gameUpdate = new StringTokenizer(tokens);
                /**
                 * How to understand these tokens:
                 * 1 token = A user's name. This adds a new user to the server
                 * 2 tokens = A user's name + 'delete', broadcasted in UserGameThread. This deletes a user
                 * 3 tokens = username + 'x' + 'y', this updates a user's coordinates.
                 */
                if(gameUpdate.countTokens() == 1){
                    client.addNewUser(gameUpdate.nextToken());
                    continue;
                }else if (gameUpdate.countTokens() == 2){
                    client.deleteUser(gameUpdate.nextToken());
                    continue;
                }else if (gameUpdate.countTokens() == 3){ ;
                    String userName = gameUpdate.nextToken();
                    int newX = Integer.parseInt(gameUpdate.nextToken());
                    int newY = Integer.parseInt(gameUpdate.nextToken());
                    client.updateUser(userName, newX, newY);
                }
            } catch (IOException ex) {
                client.addAsServer("Error reading from game server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }
}