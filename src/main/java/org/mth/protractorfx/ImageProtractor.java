package org.mth.protractorfx;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.mth.protractorfx.command.Action;
import org.mth.protractorfx.command.CommandManager;
import org.mth.protractorfx.log.LogFactory;
import org.mth.protractorfx.tool.InsertionTool;
import org.mth.protractorfx.tool.MeasureUnit;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.lang.Double.parseDouble;

public class ImageProtractor implements Initializable {

    private static final Logger log = LogFactory.configureLog(ImageProtractor.class);

    private final EventHandler<KeyEvent> cropKeyEventHandler = evt -> {
        if (evt.getCode() == KeyCode.ENTER) {
            cropImage();
        }
    };

    private short backgroundOpacity = 10;

    /**
     * The current zoom value
     */
    SimpleDoubleProperty zoomValue = new SimpleDoubleProperty(1.0);
    Scale zoomScaling = new Scale();
    File imageFile = null;
    Image image = null;
    CropArea cropArea;

    @FXML
    ScrollPane imageScrollPane;
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
    @FXML
    Menu chainColorMenu;
    @FXML
    MenuBar menuBar;
    @FXML
    Menu measureUnitMenu;
    @FXML
    Button maximizeButton;

    DotChain chain;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UtilsKt.stage.maximizedProperty().addListener((observableValue, aBoolean, maximized) -> {
            ImageView graphic = (ImageView) maximizeButton.getGraphic();

            if (maximized) {
                graphic.setImage(new Image(getClass().getResourceAsStream("restore_down_x48.png")));
            } else {
                graphic.setImage(new Image(getClass().getResourceAsStream("maximize_x48.png")));
            }
        });

        container.getChildren().add(SelectionRectangle.INSTANCE);

        // bind zoom update with the related transform
        zoomValue.addListener((observableValue, number, t1) -> {
            zoomScaling.setX(zoomValue.get());
            zoomScaling.setY(zoomValue.get());
        });

        cropArea = new CropArea(imageView, cropKeyEventHandler);
        cropArea = CropArea.getInstance();
        cropArea.install(container, imageView);

//        container.getChildren().add(cropArea);
//        cropArea.setVisible(false);


        container.setOnMouseClicked(evt -> {
            cropArea.show(false);

            if (evt.isStillSincePress() && !InsertionTool.INSTANCE.getActive()) {
                log.fine("Click on empty space");
                Selection.INSTANCE.clear();
                container.requestFocus();
            }

            System.out.println("CLICK");
        });

        container.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            switch (evt.getCode()) {
                case LEFT:
                    moveSelectedDots(-2, Adjustable.HORIZONTAL);
                    evt.consume();
                    break;
                case RIGHT:
                    moveSelectedDots(2, Adjustable.HORIZONTAL);
                    evt.consume();
                    break;
                case UP:
                    moveSelectedDots(-2, Adjustable.VERTICAL);
                    evt.consume();
                    break;
                case DOWN:
                    moveSelectedDots(2, Adjustable.VERTICAL);
                    evt.consume();
                    break;
            }
        });

        imageView.setPreserveRatio(true);
        imageView.getTransforms().add(zoomScaling);

        imageScrollPane.toBack();

        imageScrollPane.minWidthProperty().

                bind(container.widthProperty());
        imageScrollPane.maxWidthProperty().

                bind(container.widthProperty());

        imageScrollPane.minHeightProperty().

                bind(container.heightProperty());
        imageScrollPane.maxHeightProperty().

                bind(container.heightProperty());

        // color menu initialization
        UtilsKt.defaultColors().forEach(color -> {
            MenuItem colorMenuItem = new MenuItem();
            colorMenuItem.setGraphic(new Rectangle(14, 14, color));
            colorMenuItem.setOnAction(evt -> chain.setColor(color));
            chainColorMenu.getItems().add(colorMenuItem);
        });


        ToggleGroup group = new ToggleGroup();

        for (MeasureUnit unit : MeasureUnit.values()) {
            RadioMenuItem item = new RadioMenuItem(unit.getDisplay());
            item.setSelected(UtilsKt.measureUnitProperty.getValue() == unit);
            item.setOnAction(actionEvent -> UtilsKt.measureUnitProperty.set(unit));
            measureUnitMenu.getItems().add(item);
            group.getToggles().add(item);
        }

        container.getChildren().add(MouseCoordinateLabel.INSTANCE);

        chain = DotChain.randomChain(container);
        UtilsKt.setChain(chain);
        UtilsKt.getChains().add(chain);

        container.requestFocus();
    }

    private void moveSelectedDots(double dr, int direction) {
        HashSet<Dot> updateSet = new HashSet<>();

        if (direction == Adjustable.VERTICAL) {
            Selection.INSTANCE.forEach(dot -> {
                dot.setCenterY(dot.getCenterY() + dr);
                updateSet.add(dot);
                updateSet.addAll(dot.neighbors());
            });

            log.fine(String.format("Moving selected dots [Direction=%s, dr=%.2f]", direction, dr));
        } else if (direction == Adjustable.HORIZONTAL) {
            Selection.INSTANCE.forEach(dot -> {
                dot.setCenterX(dot.getCenterX() + dr);
                updateSet.add(dot);
                updateSet.addAll(dot.neighbors());
            });

            log.fine(String.format("Moving selected dots [Direction=%s, dr=%.2f]", direction, dr));
        } else
            log.warning("Something strange happened");

        updateSet.forEach(Dot::updateNeighboringAngles);
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
    void undo() {
        log.fine("Undo");
        CommandManager.INSTANCE.undo();
    }

    @FXML
    void increaseOpacity() {
        if (backgroundOpacity <= 90) {
            backgroundOpacity += 10;
            UtilsKt.scene.setFill(Color.rgb(255, 255, 255, backgroundOpacity / 100.0));

            log.fine("Opacity increased: " + backgroundOpacity);
        } else
            log.fine("Cannot increase opacity, because it has reached its maximum value");
    }

    @FXML
    void reduceOpacity() {
        if (backgroundOpacity >= 20) {
            backgroundOpacity -= 10;
            UtilsKt.scene.setFill(Color.rgb(255, 255, 255, backgroundOpacity / 100.0));

            log.fine("Opacity reduced: " + backgroundOpacity);
        } else
            log.fine("Cannot reduce opacity, because it has reached its minimum value");
    }

    @FXML
    void takeScreenshot() throws AWTException {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            File output = new File(UtilsKt.SNAPSHOT_DIR, "screenshot_" + timestamp + ".jpg");

            // exclude from the area the top menu bar
            java.awt.Rectangle screenshotArea = new java.awt.Rectangle(
                    0,
                    (int) menuBar.getHeight(),
                    (int) screenSize.getWidth(),
                    (int) (screenSize.height - menuBar.getHeight())
            );

            Robot robot = new Robot();
            BufferedImage screenCapture = robot.createScreenCapture(screenshotArea);
            ImageIO.write(screenCapture, "jpg", output);

            log.info("Screenshot taken and saved to " + output.getAbsolutePath());
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }

    @FXML
    void closeApp() {
        Platform.exit();
    }

    @FXML
    void minimizeApp() {
        UtilsKt.stage.setIconified(true);
    }

    @FXML
    void maximizeWindow() {
        Stage stage = UtilsKt.stage;
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    void fullScreenMode() {
        UtilsKt.scene.setFill(Color.rgb(255, 255, 255, 0.1));
        UtilsKt.stage.setMaximized(true);
        UtilsKt.stage.initStyle(StageStyle.TRANSPARENT);
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
                zoomValue.set(1.0);

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
        log.fine("zoom in");

        zoomValue.set(zoomValue.getValue() + 0.1);
    }

    @FXML
    void zoomOut() {
        log.fine("zoom out");

        if (zoomValue.get() > 0.1) {
            zoomValue.set(zoomValue.getValue() - 0.1);
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
    }

    @FXML
    void newChain() {
        CommandManager.INSTANCE.execute(new NewChainAction());
    }

    public static double getImageScalingFactor(ImageView imageView) {
        double scaleX = imageView.getFitWidth() / imageView.getViewport().getMaxX();
        double scaleY = imageView.getFitHeight() / imageView.getViewport().getMaxY();

        return Math.min(scaleX, scaleY);
    }

    static class NewChainAction implements Action {

        private DotChain chain;

        @Override
        public boolean execute() {
            chain = DotChain.randomChain(UtilsKt.getPane());
            UtilsKt.getChains().add(chain);
            return true;
        }

        @Override
        public void undo() {
            chain.dispose();
        }

        @Override
        public String getName() {
            return "new-chain";
        }
    }

}
