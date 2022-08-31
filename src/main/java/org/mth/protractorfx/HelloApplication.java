package org.mth.protractorfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.labs.util.Util;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("MainView.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("ImageProtractor1.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 600);

        scene.setFill(Color.rgb(255, 255, 255, 0.1));
        scene.getStylesheets().add(HelloApplication.class.getResource("style.css").toExternalForm());

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setMaximized(true);
//        stage.addEventHandler(KeyEvent.KEY_PRESSED, controller.keyPressedHandler);
//        stage.addEventHandler(KeyEvent.KEY_RELEASED, controller.keyReleasedHandler);

        stage.show();
        stage.requestFocus();

        UtilsKt.scene = scene;
        UtilsKt.stage = stage;

        Tool.Companion.initialize();

//        GlobalKeyListener.INSTANCE.install();
    }

    public static void main(String[] args) {
        launch();
    }
}