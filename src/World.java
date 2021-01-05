import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is a helper class that builds all of the game and packages it into a BorderPane for the Client class
 *
 * @author pamat
 */
public class World {

    /**
     * Variables needed to function. We need to use some stuff in the Client class, so those nodes are initialized
     * and given getter methods.
     */

    private Client client;
    private Label xPos;
    private Label yPos;

    public BorderPane buildWorld(int width, int height, Group stuff, Client client, Rectangle rect){
        this.client = client;
        String name = client.name;
        Label playerName = client.playerName;

        FileInputStream stream1 = null;
        FileInputStream stream2 = null;
        FileInputStream stream3 = null;
        try {
            stream1 = new FileInputStream("C:\\Users\\pamat\\IdeaProjects\\TopDownGame\\src\\Tiles\\Grass1.png");
            stream2 = new FileInputStream("C:\\Users\\pamat\\IdeaProjects\\TopDownGame\\src\\Tiles\\Grass2.png");
            stream3 = new FileInputStream("C:\\Users\\pamat\\IdeaProjects\\TopDownGame\\src\\Tiles\\Grass3.png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Image grass1 = new Image(stream1);
        Image grass2 = new Image(stream2);
        Image grass3 = new Image(stream3);
        Image[] tiles = {grass1, grass2, grass3};

        Image[][] tileSet = new Image[width][height];

        //Check if all tiles are 16x16, delete ones that are not
        ArrayList<Image> newTiles = new ArrayList<Image>();
        for (int i = 0; i < tiles.length; i++){
            Image current = tiles[i];
            if (current.getWidth()  == 16.00 && current.getHeight() == 16.00){
                newTiles.add(current);
            }
        }

        //Fill in the result array with the tiles, randomly
        Random rand = new Random();
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
                int randTile = rand.nextInt(newTiles.size());
                tileSet[i][j] = newTiles.get(randTile);
            }
        }

        ImageView[][] tileSetView = new ImageView[width][height];
        for (int i = 0; i < tileSetView.length; i++){
            for (int j = 0; j < tileSetView.length; j++){
                tileSetView[i][j] = new ImageView();
                tileSetView[i][j].setImage(tileSet[i][j]);
                tileSetView[i][j].setFitWidth(64.00);
                tileSetView[i][j].setFitHeight(64.00);

                tileSetView[i][j].setX(64 * i - 120);
                tileSetView[i][j].setY(64 * j - 120);
                stuff.getChildren().add(tileSetView[i][j]);
            }
        }

        /**
         * Potentially separate into different methods here, because there should be some additional handling
         * for that catch block above
         */


        SubScene gameStuff = new SubScene(stuff, width, height); //200x200 for testing
        BorderPane gp = new BorderPane();



        ScrollPane gameTest = new ScrollPane();
        //disallowing scrolling
        gameTest.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        gameTest.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //fixing pane at desired dimensions
        gameTest.setVmax(200);
        gameTest.setVmax(200);
        gameTest.setHmax(200);
        gameTest.setHmin(200);



        //Set up camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-100);
        camera.setFieldOfView(90); //Determines how far the user can 'see'
        gameStuff.setCamera(camera);

        playerName.layoutXProperty().bind(rect.xProperty());
        playerName.layoutYProperty().bind(rect.yProperty());

        camera.layoutXProperty().bind(rect.xProperty());
        camera.layoutYProperty().bind(rect.yProperty());

        //Organizing game window
        Label xLabel = new Label("X: ");
        xPos = new Label();
        Label yLabel = new Label("Y: ");
        yPos = new Label();

        xPos.setText(String.valueOf(rect.getX()));
        yPos.setText(String.valueOf(rect.getY()));

        HBox coords = new HBox();
        coords.getChildren().addAll(xLabel,xPos,yLabel,yPos);

        stuff.getChildren().addAll(rect, playerName);





        gameTest.setContent(gameStuff);
        gp.setLeft(gameTest);
        gp.setTop(coords);

        return gp;
    }

    public Label getPosX(){
        return xPos;
    }

    public Label getPosY(){
        return yPos;
    }



}
