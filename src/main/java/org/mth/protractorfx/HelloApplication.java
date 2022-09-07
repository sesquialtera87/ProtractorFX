package org.mth.protractorfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.JMetro;
import org.mth.protractorfx.tool.Tool;

import java.awt.*;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        if (true) {
            TestKt.test();
//            return;
        }

        UtilsKt.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("ImageProtractor1.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),
                Toolkit.getDefaultToolkit().getScreenSize().width,
                Toolkit.getDefaultToolkit().getScreenSize().height * 0.7);

        scene.setFill(Color.rgb(255, 255, 255, 0.1));
        scene.getStylesheets().add(HelloApplication.class.getResource("style.css").toExternalForm());

        JMetro style = new JMetro();
        style.setScene(scene);

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setMaximized(true);

        stage.show();
        stage.requestFocus();

        UtilsKt.scene = scene;

        Tool.Companion.initialize();

        GlobalKeyListener.INSTANCE.install();
    }

    public static void main(String[] args) {
        launch();
    }
}