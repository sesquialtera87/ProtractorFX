package com.mth.protractorfx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

public class ImageProtractor implements Initializable {


    private final EventHandler<KeyEvent> cropKeyEventHandler = evt -> {
        if (evt.getCode() == KeyCode.ENTER) {
            cropImage();
        }
    };

    SimpleDoubleProperty zoomValue = new SimpleDoubleProperty(1.0);
    File imageFile = null;
    Image image = null;
    CropArea cropArea;

    @FXML
    ImageView imageView;
    @FXML
    Pane container;
    @FXML
    TextField xField;
    @FXML
    TextField yField;
    @FXML
    TextField widthField;
    @FXML
    TextField heightField;

    DotChain chain;
    boolean dotInsertionEnabled = false;
    boolean dotDeletionEnabled = false;
    boolean angleMeasureEnabled = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cropArea = new CropArea(imageView, cropKeyEventHandler);
        cropArea = CropArea.getInstance();
        cropArea.install(container, imageView);

//        container.getChildren().add(cropArea);
//        cropArea.setVisible(false);

        container.setOnMouseClicked(evt -> {
            cropArea.show(false);

            if (angleMeasureEnabled) {
                measureAngle(new Point2D(evt.getX(), evt.getY()));
                container.setCursor(Cursor.DEFAULT);
                angleMeasureEnabled = false;
            } else if (dotInsertionEnabled)
                addDot(evt.getX(), evt.getY());
            else if (dotDeletionEnabled)
                ;
            else
                chain.clearSelection();
        });
        container.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.A) {
                container.setCursor(Cursor.CROSSHAIR);
                dotInsertionEnabled = true;
            } else if (evt.getCode() == KeyCode.X) {
                container.setCursor(Cursor.HAND);
                dotDeletionEnabled = true;
            }
        });
        container.setOnKeyReleased(evt -> {
            if (evt.getCode() == KeyCode.A) {
                container.setCursor(Cursor.DEFAULT);
                dotInsertionEnabled = false;
            } else if (evt.getCode() == KeyCode.X) {
                container.setCursor(Cursor.DEFAULT);
                dotDeletionEnabled = false;
            }
        });
        container.setOnMouseMoved(evt -> {
            Dot nearestDot = chain.getNearestDot(new Point2D(evt.getX(), evt.getY()), true);
            nearestDot.highLight();

            if (chain.neighborsCount(nearestDot) < 2)
                return;

            List<Double> angles = new ArrayList<>(10);

            for (Dot neighbor : chain.neighbors(nearestDot)) {
                double dx1 = neighbor.getCenterX() - nearestDot.getCenterX();
                double dy1 = neighbor.getCenterY() - nearestDot.getCenterY();
                double dx2 = evt.getX() - nearestDot.getCenterX();
                double dy2 = evt.getY() - nearestDot.getCenterY();

                double dot = dx1 * dx2 + dy1 * dy2;
                double det = dx1 * dy2 - dx2 * dy1;
                double angle = toDegrees(atan2(det, dot));

                System.out.print(angle + " ");
                angles.add(angle);
            }
            System.out.println();
        });

        imageView.setPreserveRatio(true);

//        imageView.fitWidthProperty().bind(container.widthProperty());
//        imageView.fitHeightProperty().bind(container.heightProperty());

        chain = new DotChain(container);
    }

    private void measureAngle(Point2D mouseLocation) {
        Dot nearestDot = chain.getNearestDot(mouseLocation, true);
        HashSet<Dot> neighbors = nearestDot.neighbors();

        ArrayList<Pair<Dot, Double>> anglesFromMouse = new ArrayList<>(neighbors.size());

        if (neighbors.size() < 2)
            // the nearest node is a leaf
            return;

        for (Dot neighbor : neighbors) {
            // calculate the angle (measured anticlockwise) from the neighbor node to the mouse point
            Point2D p1 = new Point2D(neighbor.getCenterX(), neighbor.getCenterY());
            p1 = p1.subtract(nearestDot.getCenterX(), nearestDot.getCenterY());
            Point2D p2 = mouseLocation.subtract(nearestDot.getCenterX(), nearestDot.getCenterY());

            double angle = UtilsKt.angleBetween(p1, p2, true);
            anglesFromMouse.add(new Pair<>(neighbor, angle));
            System.out.println(angle);
        }

        // sort angles in ascending order, from the nearest to the farthest node (anticlockwise)
        anglesFromMouse.sort(Comparator.comparing(Pair::getValue));

        // get the farthest and the nearest nodes as the delimiters for the user-chosen angle
        Dot dot1 = anglesFromMouse.get(anglesFromMouse.size() - 1).getKey();
        Dot dot2 = anglesFromMouse.get(0).getKey();

        nearestDot.addAngleMeasure(dot1, dot2);
    }

    private void addDot(double x, double y) {
        Optional<Dot> selectedDot = chain.getSelectedDot();

        selectedDot.ifPresent(dot -> {
            Dot newDot = new Dot(x, y, chain);
            chain.addDot(newDot);
            chain.connect(newDot, dot);
        });
    }

    private void cropImage() {
        Rectangle2D viewport = imageView.getViewport();

        System.out.println("Viewport = " + viewport);
        System.out.println(imageView.fitWidthProperty());
        System.out.println(imageView.fitHeightProperty());

        double scale = getImageScalingFactor(imageView);
        System.out.println("scale = " + scale);

        System.out.println("Crop area = " + new Rectangle2D(cropArea.getX(), cropArea.getY(), cropArea.getWidth(), cropArea.getHeight()));

        double cropWidth = cropArea.getWidth() / scale;
        double cropHeight = cropArea.getHeight() / scale;

        try {
            BufferedImage original = ImageIO.read(imageFile);
//            BufferedImage original = SwingFXUtils.fromFXImage(image, null);
            BufferedImage croppedImage = original.getSubimage((int) (cropArea.getX() / scale), (int) (cropArea.getY() / scale), (int) cropWidth, (int) cropHeight);

            imageFile = new File(UtilsKt.SNAPSHOT_DIR, "sdfsdf.jpg");
            ImageIO.write(croppedImage, "JPEG", imageFile);

//            image = SwingFXUtils.toFXImage(croppedImage, new WritableImage((int) cropWidth, (int) cropHeight));
            image = new Image(new FileInputStream(imageFile));
            imageView.setImage(image);
            imageView.setViewport(new Rectangle2D(0, 0, image.getWidth(), image.getHeight()));
            imageView.setFitWidth(image.getWidth());
            imageView.setFitHeight(image.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        cropArea.setVisible(false);
        cropArea.show(false);
    }

    @FXML
    void openImage() {
        FileChooser fileChooser = new FileChooser();
        ExtensionFilter jpegFilter = new ExtensionFilter("JPEG", "*.jpg", "*.jpeg");
        ExtensionFilter pngFilter = new ExtensionFilter("PNG", "*.png");
        fileChooser.getExtensionFilters().addAll(jpegFilter, pngFilter);
        fileChooser.setSelectedExtensionFilter(jpegFilter);

        imageFile = fileChooser.showOpenDialog(null);

        if (imageFile != null) {
            try {
                image = new Image(new FileInputStream(imageFile));

                imageView.setImage(image);
                imageView.setViewport(new Rectangle2D(0, 0, image.getWidth(), image.getHeight()));
                imageView.setFitWidth(image.getWidth());
                imageView.setFitHeight(image.getHeight());

                System.out.println("Image(" + image.getWidth() + ", " + image.getHeight() + ")");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void showCropArea() {
        cropArea.setX(50);
        cropArea.setY(50);
        cropArea.setWidth(150);
        cropArea.setHeight(150);
//        cropArea.setVisible(true);
        cropArea.show(true);
        cropArea.requestFocus();
    }

    @FXML
    void zoomIn() {
        System.out.println("zoom in");

        zoomValue.set(zoomValue.getValue() + 0.1);

        imageView.setScaleX(zoomValue.get());
        imageView.setScaleY(zoomValue.getValue());
    }

    @FXML
    void zoomOut() {
        System.out.println("zoom out");

        if (zoomValue.get() > 0.1) {
            zoomValue.set(zoomValue.getValue() - 0.1);

            imageView.setScaleX(zoomValue.get());
            imageView.setScaleY(zoomValue.getValue());
        }
    }

    @FXML
    void viewport() {
        Rectangle2D viewport = new Rectangle2D(parseDouble(xField.getText()),
                parseDouble(yField.getText()),
                parseDouble(widthField.getText()),
                parseDouble(heightField.getText()));
        imageView.setViewport(viewport);
        System.out.println(viewport);
    }

    @FXML
    void putAngleMeasure() {
        angleMeasureEnabled = true;
        container.setCursor(UtilsKt.CURSOR_ANGLE);
    }

    private double clamp(double value, double min, double max) {

        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    // convert mouse coordinates in the imageView to coordinates in the actual image:
    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

    public static double getImageScalingFactor(ImageView imageView) {
        double scaleX = imageView.getFitWidth() / imageView.getViewport().getMaxX();
        double scaleY = imageView.getFitHeight() / imageView.getViewport().getMaxY();

        return Math.min(scaleX, scaleY);
    }

    private static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;

        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }

        return new ImageView(wr).getImage();
    }
}