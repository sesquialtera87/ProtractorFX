package com.mth.protractorfx;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CropArea
//        extends Rectangle
        implements Initializable {

    @FXML
    Rectangle NW;
    @FXML
    Rectangle NE;
    @FXML
    Rectangle SE;
    @FXML
    Rectangle SW;
    @FXML
    Pane N, S, E, W;

    double resizeAreaInset = 5;
    Rectangle2D initialRegion;
    Point2D dragAnchorPoint;
    Point2D delta;
    Side mouseLocation;
    ImageView imageView;

    @FXML
    GridPane cropArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cropArea.setOpacity(0.5);

        // Prevent the crop area to close when user click over itself
        cropArea.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);

        List<Pane> panes = Arrays.asList(N, S, W, E);
        List<Rectangle> rectangles = Arrays.asList(NE, NW, SE, SW);
        List<Cursor> cursors = Arrays.asList(Cursor.N_RESIZE, Cursor.S_RESIZE, Cursor.W_RESIZE, Cursor.E_RESIZE);
        List<Cursor> angleCursors = Arrays.asList(Cursor.NE_RESIZE, Cursor.NW_RESIZE, Cursor.SE_RESIZE, Cursor.SW_RESIZE);

        for (int i = 0; i < panes.size(); i++) {
            panes.get(i).setCursor(cursors.get(i));
            rectangles.get(i).setCursor(angleCursors.get(i));
        }

        cropArea.setCursor(Cursor.MOVE);

        cropArea.setOnMousePressed(evt -> {
            dragAnchorPoint = new Point2D(evt.getSceneX(), evt.getSceneY());
            initialRegion = new Rectangle2D(getX(), getY(), getWidth(), getHeight());

            System.out.println("Drag anchor = " + dragAnchorPoint);
            System.out.println("Crop region = " + initialRegion);
        });
        cropArea.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            double newX = initialRegion.getMinX() - delta.getX();
            double newY = initialRegion.getMinY() - delta.getY();

            newX = Math.max(newX, 0);
            newY = Math.max(newY, 0);

            newX = Math.min(newX, imageView.getFitWidth() - getWidth());
            newY = Math.min(newY, imageView.getFitHeight() - getHeight());

            setX(newX);
            setY(newY);

            evt.consume();
        });
        N.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setY(initialRegion.getMinY() - delta.getY());
            setHeight(initialRegion.getHeight() + delta.getY());
            evt.consume();
        });
        S.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setHeight(initialRegion.getHeight() - delta.getY());
            evt.consume();
        });
        E.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setWidth(initialRegion.getWidth() - delta.getX());
            evt.consume();
        });
        W.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setX(initialRegion.getMinX() - delta.getX());
            setWidth(initialRegion.getWidth() + delta.getX());
            evt.consume();
        });
        SE.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setHeight(initialRegion.getHeight() - delta.getY());
            setWidth(initialRegion.getWidth() - delta.getX());
            evt.consume();
        });
        SW.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setHeight(initialRegion.getHeight() - delta.getY());
            setX(initialRegion.getMinX() - delta.getX());
            setWidth(initialRegion.getWidth() + delta.getX());
            evt.consume();
        });
        NE.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setY(initialRegion.getMinY() - delta.getY());
            setHeight(initialRegion.getHeight() + delta.getY());
            setWidth(initialRegion.getWidth() - delta.getX());
            evt.consume();
        });
        NW.setOnMouseDragged(evt -> {
            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
            setY(initialRegion.getMinY() - delta.getY());
            setHeight(initialRegion.getHeight() + delta.getY());
            setX(initialRegion.getMinX() - delta.getX());
            setWidth(initialRegion.getWidth() + delta.getX());
            evt.consume();
        });
    }

    public CropArea() {
    }

    public CropArea(ImageView imageView, EventHandler<KeyEvent> cropHandler) {
//        super(100, 100);
        this.imageView = imageView;

//        setFill(Color.ALICEBLUE);
//        setOpacity(0.5);
//
//        // Prevent the crop area to close when user click over itself
//        addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
//
//        setOnKeyPressed(cropHandler);
//
//        setOnMouseMoved(evt -> {
//            mouseLocation = getAreaIndicator(evt);
//
//            if (mouseLocation == null) {
//                setCursor(Cursor.MOVE);
//            } else
//                switch (mouseLocation) {
//                    case TOP:
//                        setCursor(Cursor.N_RESIZE);
//                        break;
//                    case BOTTOM:
//                        setCursor(Cursor.S_RESIZE);
//                        break;
//                    case LEFT:
//                        setCursor(Cursor.W_RESIZE);
//                        break;
//                    case RIGHT:
//                        setCursor(Cursor.E_RESIZE);
//                        break;
//                }
//        });
//        setOnMousePressed(evt -> {
//            dragAnchorPoint = new Point2D(evt.getSceneX(), evt.getSceneY());
//            initialRegion = new Rectangle2D(getX(), getY(), getWidth(), getHeight());
//
//            System.out.println("Drag anchor = " + dragAnchorPoint);
//        });
//        setOnMouseDragged(evt -> {
//            delta = dragAnchorPoint.subtract(evt.getSceneX(), evt.getSceneY());
//
//            if (mouseLocation == null) {
//                double newX = initialRegion.getMinX() - delta.getX();
//                double newY = initialRegion.getMinY() - delta.getY();
//
//                newX = Math.max(newX, 0);
//                newY = Math.max(newY, 0);
//
//                newX = Math.min(newX, imageView.getFitWidth() - getWidth());
//                newY = Math.min(newY, imageView.getFitHeight() - getHeight());
//
//                setX(newX);
//                setY(newY);
//            } else switch (mouseLocation) {
//                case TOP:
//                    setY(initialRegion.getMinY() - delta.getY());
//                    setHeight(initialRegion.getHeight() + delta.getY());
//                    break;
//                case BOTTOM:
//                    setHeight(initialRegion.getHeight() - delta.getY());
//                    break;
//                case RIGHT:
//                    setWidth(initialRegion.getWidth() - delta.getX());
//                    break;
//                case LEFT:
//                    setX(initialRegion.getMinX() - delta.getX());
//                    setWidth(initialRegion.getWidth() + delta.getX());
//                    break;
//            }
//        });
    }

    public void install(Pane parent, ImageView imageView) {
        this.imageView = imageView;

        parent.getChildren().add(cropArea);
        cropArea.setLayoutX(0);
        cropArea.setLayoutY(0);
        cropArea.setPrefWidth(100);
        cropArea.setPrefHeight(100);
        cropArea.setVisible(false);
    }

    public void show(boolean isVisible) {
        cropArea.setVisible(isVisible);
    }

    public double getWidth() {
        return cropArea.getWidth();
    }

    public void setWidth(double width) {
        cropArea.setPrefWidth(width);
    }

    public double getHeight() {
        return cropArea.getHeight();
    }

    public void setHeight(double h) {
        cropArea.setPrefHeight(h);
    }

    public void requestFocus() {
        cropArea.requestFocus();
    }

    public void setX(double x) {
        cropArea.setLayoutX(x);
    }

    public void setY(double y) {
        cropArea.setLayoutY(y);
    }

    public double getX() {
        return cropArea.getLayoutX();
    }

    public double getY() {
        return cropArea.getLayoutY();
    }

    public static CropArea getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("CropArea.fxml"));
            Parent pane = loader.load();
            CropArea controller = loader.getController();
            controller.cropArea = (GridPane) pane;

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Side getAreaIndicator(MouseEvent evt) {
//        System.out.println(new Point2D(evt.getX(), evt.getY()));
//        System.out.println(new Point2D(evt.getSceneX(), evt.getSceneY()));
//        System.out.println(new Point2D(evt.getScreenX(), evt.getScreenY()));
//        System.out.println(new Point2D(getX(), getY()));

        double x = evt.getX() - getX();
        double y = evt.getY() - getY();

        if (x <= resizeAreaInset) {
            return Side.LEFT;
        } else if (getWidth() - x <= resizeAreaInset)
            return Side.RIGHT;
        else {
            if (y <= resizeAreaInset)
                return Side.TOP;
            else if (getHeight() - y <= resizeAreaInset)
                return Side.BOTTOM;
            else return null;
        }
    }


}
