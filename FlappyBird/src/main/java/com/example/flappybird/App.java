pack  age com.example.flappybird;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {

        int boardWidth = 360;
        int boardHeight = 640;
        FlappyBird flappyBird = new FlappyBird();
        Scene scene = new Scene(flappyBird, boardWidth, boardHeight);

        stage.setTitle("Flappy Bird");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setResizable(false);
        flappyBird.requestFocus();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
