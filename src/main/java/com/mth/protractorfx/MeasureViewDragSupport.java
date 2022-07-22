package com.mth.protractorfx;

import javafx.geometry.Point2D;
import javafx.scene.control.TitledPane;

public class MeasureViewDragSupport {

    private Point2D dr = new Point2D(0, 0);

    public MeasureViewDragSupport(TitledPane pane) {

        pane.setOnMousePressed(evt -> {
            dr = new Point2D(evt.getSceneX() - pane.getLayoutX(), evt.getSceneY() - pane.getLayoutY());
        });
        pane.setOnMouseDragged(evt -> {
            // update node position
            System.out.println(evt.getX());
            pane.setLayoutX(evt.getSceneX() - dr.getX());
            pane.setLayoutY(evt.getSceneY() - dr.getY());
        });
    }
}
