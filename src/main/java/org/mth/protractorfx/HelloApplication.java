package org.mth.protractorfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("MainView.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("ImageProtractor.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 600);
//        scene.setFill(Color.TRANSPARENT);
//        scene.setFill(Color.rgb(255, 255, 255, 0.1));
//        MainViewController controller = fxmlLoader.getController();
        stage.setTitle("Hello!");
        stage.setScene(scene);
//        stage.initStyle(StageStyle.TRANSPARENT);
//        stage.setMaximized(true);
//        stage.addEventHandler(KeyEvent.KEY_PRESSED, controller.keyPressedHandler);
//        stage.addEventHandler(KeyEvent.KEY_RELEASED, controller.keyReleasedHandler);
        stage.show();
//        new ImageProtractor().show();
    }

    public static void main(String[] args) {
        launch();
    }
}