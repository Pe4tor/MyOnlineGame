import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A simple infinite loop that will spit out output from our server. This allows us to have systems like chat be
 * possible, and lets us send more than one message at a time to the client. Java also makes it super easy to take
 * advantage of threading by extending from the Thread superclass.
 *
 * This is from an old group project in 2019 that I was a part of. It's not used in this project but I'm keeping it here
 * in case any ideas from it (such as the sleeping) are useful.
 */

public class ServerThread extends Thread{

    private BufferedReader fromServer;  // Our BufferedReader object receiving data from the server.
    private VBox feed;

    public ServerThread(VBox feed){
        this.feed = feed;
    }

    /**
     * Our method that will continously run while the client is handling other things. All we want to do is
     * run an infinite loop that prints out input from the server
     */
    @Override
    public void run() {

        while (true){

            try {
                String serverOutput = fromServer.readLine();  // Attempt to read from the server
                if (serverOutput != null && !serverOutput.trim().equals("\n") && !serverOutput.trim().equals("")){
                    //System.out.println("[server] " + serverOutput);  // If we have something that's not nonsense, print it
                    Text output = new Text("[server] "+ serverOutput);
                    feed.getChildren().add(output);
                }
                sleep(200);  // Wait .2 seconds to print something again
            } catch (InterruptedException e){
                e.printStackTrace();
            } catch (IOException e){
            }
            catch (NullPointerException e){
            }
        }
    }

    /**
     * A setter to config our BufferedReader to spit out from the server setup in the main client class
     *
     * @param fromServer - A BufferedReader to read from
     */
    public void setFromServer(BufferedReader fromServer){
        this.fromServer = fromServer;
    }

    /**
     * Handles any cleanup we should do before we terminate our program.
     */
    public void closeFromServer(){
        try {
            this.fromServer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
