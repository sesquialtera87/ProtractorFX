package org.mth.protractorfx;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Math.*;

public class MainViewController implements Initializable {

    public static final int NODE_SIZE = 6;
    private static MainViewController instance;

    private static final ObjectProperty<Color> primaryAngleColor = new SimpleObjectProperty<>(Color.RED);
    private static final ObjectProperty<Color> secondaryAngleColor = new SimpleObjectProperty<>(Color.GRAY);
    static List<Chain> chains = new ArrayList<>();
    static Chain currentChain = null;
    Line newNodeHint = new Line();
    Rectangle chainSelectionRectangle;
    static boolean deletionEnabled = false;
    KeyCode currentlyPressedKeyCode;


    @FXML
    private Canvas canvas;
    @FXML
    private Pane pane;
    @FXML
    private TitledPane measuresPane;
    @FXML
    private TableView<AnchorNode> measureTable;
    @FXML
    private TableColumn<AnchorNode, String> idColumn;
    @FXML
    private TableColumn<AnchorNode, Double> degreeColumn;
    @FXML
    private TableColumn<AnchorNode, Double> secondaryAngleColumn;
    @FXML
    private Menu transparencyMenu;

    EventHandler<KeyEvent> keyPressedHandler = evt -> {
        System.out.println(evt.getCode());
        currentlyPressedKeyCode = evt.getCode();

        if (evt.getCode() == KeyCode.SHIFT) {
            pane.setCursor(Cursor.CROSSHAIR);
        } else if (evt.getCode() == KeyCode.D) {
            deletionEnabled = true;
        }
    };

    EventHandler<KeyEvent> keyReleasedHandler = evt -> {
        System.out.println(evt.getCode());

        if (currentlyPressedKeyCode == evt.getCode())
            currentlyPressedKeyCode = null;

        if (evt.getCode() == KeyCode.SHIFT) {
            pane.setCursor(Cursor.DEFAULT);
        } else if (evt.getCode() == KeyCode.D) {
            deletionEnabled = false;
        }
    };

    @FXML
    void onKeyPressed(KeyEvent evt) {
    }

    @FXML
    void onKeyReleased(KeyEvent evt) {
    }

    @FXML
    void onMouseMoved(MouseEvent evt) {
        if (evt.isShiftDown()) {
            AnchorNode first = currentChain.first();
            AnchorNode last = currentChain.last();
            Point2D mousePoint = new Point2D(evt.getX(), evt.getY());

            double dFirst = mousePoint.distance(first.getCenterX(), first.getCenterY());
            double dLast = mousePoint.distance(last.getCenterX(), last.getCenterY());

            if (dFirst < dLast) {
                newNodeHint.setStartX(first.getCenterX());
                newNodeHint.setStartY(first.getCenterY());
            } else {
                newNodeHint.setStartX(last.getCenterX());
                newNodeHint.setStartY(last.getCenterY());
            }

            newNodeHint.setEndX(evt.getX());
            newNodeHint.setEndY(evt.getY());

            newNodeHint.setVisible(true);
        } else
            newNodeHint.setVisible(false);
    }

    @FXML
    void onMouseClicked(MouseEvent evt) {
        System.out.println("Click");

        if (evt.isShiftDown()) {
            AnchorNode anchorNode = currentChain.addNode(evt.getX(), evt.getY());
            pane.getChildren().add(anchorNode);
            draw(currentChain);
        }

        chainSelectionRectangle.setVisible(false);
    }

    @FXML
    private void clearChains() {
        currentChain.forEach(node -> {
            node.dispose();
            pane.getChildren().remove(node);
        });
        currentChain.getNodes().clear();

        double[] X = {pane.getWidth() / 3, pane.getWidth() * 2 / 3, pane.getWidth() * 3 / 4};
        double[] Y = {pane.getHeight() * 2 / 3, pane.getHeight() / 2, pane.getHeight() * 3 / 4};

        for (int i = 0; i < 3; i++) {
            AnchorNode anchorNode = currentChain.addNode(X[i], Y[i]);
            pane.getChildren().add(anchorNode);
        }
    }

    @FXML
    void quit() {
        Platform.exit();
    }

    void initTransparencyMenu() {
        double[] transparencyValues = {0.05, 0.1, 0.15, 0.2, 0.25, 0.3};
        final ToggleGroup group = new ToggleGroup();

        for (double value : transparencyValues) {
            RadioMenuItem item = new RadioMenuItem(Double.toString(value));
            item.setSelected(value == 0.1);
            item.setOnAction(evt -> {
                if (item.isSelected())
                    canvas.getScene().setFill(Color.rgb(255, 255, 255, value));
            });
            transparencyMenu.getItems().add(item);
            group.getToggles().add(item);
        }

        MenuItem increaseMenuItem = new MenuItem("Increase transparency");
        increaseMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        increaseMenuItem.setOnAction(evt -> {
            RadioMenuItem selectedMenuItem = (RadioMenuItem) group.getSelectedToggle();
            int index = transparencyMenu.getItems().indexOf(selectedMenuItem) - 1;

            if (index >= 0) {
                RadioMenuItem menuItem = (RadioMenuItem) transparencyMenu.getItems().get(index);
                menuItem.setSelected(true);
                canvas.getScene().setFill(Color.rgb(255, 255, 255, Double.parseDouble(menuItem.getText())));
            }
        });

        MenuItem decreaseMenuItem = new MenuItem("Decrease transparency");
        decreaseMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        decreaseMenuItem.setOnAction(evt -> {
            RadioMenuItem selectedMenuItem = (RadioMenuItem) group.getSelectedToggle();
            int index = transparencyMenu.getItems().indexOf(selectedMenuItem) + 1;

            if (index < transparencyValues.length) {
                RadioMenuItem menuItem = (RadioMenuItem) transparencyMenu.getItems().get(index);
                menuItem.setSelected(true);
                canvas.getScene().setFill(Color.rgb(255, 255, 255, Double.parseDouble(menuItem.getText())));
            }
        });

        transparencyMenu.getItems().addAll(new SeparatorMenuItem(), increaseMenuItem, decreaseMenuItem);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        initTransparencyMenu();

        newNodeHint.setFill(Color.GRAY);
        newNodeHint.toBack();

        chainSelectionRectangle = new Rectangle();
        chainSelectionRectangle.setStroke(Color.ORANGE);
        chainSelectionRectangle.setFill(Color.TRANSPARENT);
        chainSelectionRectangle.toBack();

        pane.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        pane.addEventFilter(KeyEvent.KEY_RELEASED, this::onKeyReleased);

        pane.getChildren().addAll(newNodeHint, chainSelectionRectangle);

        Chain chain = new Chain(pane);
        chains.add(chain);
        currentChain = chain;

        int[] X = {60, 100, 300};
        int[] Y = {100, 100, 210};

        for (int i = 0; i < 3; i++) {
            AnchorNode anchorNode = chain.addNode(X[i], Y[i]);
            pane.getChildren().add(anchorNode);
        }

        FilteredList<AnchorNode> nodeFilteredList = new FilteredList<>(currentChain.getNodes());
        nodeFilteredList.setPredicate(AnchorNode::isInner);
        measureTable.setItems(nodeFilteredList);

        idColumn.setCellValueFactory(dataFeatures -> dataFeatures.getValue().chainIndex.asString());
        degreeColumn.setCellValueFactory(dataFeatures -> dataFeatures.getValue().principalAngle.asObject());
        degreeColumn.setCellFactory(c -> new MeasureCell(primaryAngleColor));
        secondaryAngleColumn.setCellValueFactory(dataFeatures -> dataFeatures.getValue().secondaryAngle.asObject());
        secondaryAngleColumn.setCellFactory(c -> new MeasureCell(secondaryAngleColor));

        new MeasureViewDragSupport(measuresPane);
    }

    public static MainViewController getInstance() {
        return instance;
    }

    public void selectChain(Chain chain) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (AnchorNode node : chain.getNodes()) {
            minX = min(minX, node.getCenterX());
            minY = min(minY, node.getCenterY());
            maxX = max(maxX, node.getCenterX());
            maxY = max(maxY, node.getCenterY());
        }

        chainSelectionRectangle.setWidth(maxX - minX);
        chainSelectionRectangle.setHeight(maxY - minY);
        chainSelectionRectangle.setLayoutX(minX);
        chainSelectionRectangle.setLayoutY(minY);
        chainSelectionRectangle.setVisible(true);
    }

    void draw(Chain chain) {
        GraphicsContext g2d = canvas.getGraphicsContext2D();

        g2d.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 1; i < chain.size(); i++) {
//            g2d.strokeLine(chain.get(i - 1).getCenterX(), chain.get(i - 1).getCenterY(), chain.get(i).getCenterX(), chain.get(i).getCenterY());

        }
    }

    static class AnchorNode extends Circle {

        private Point2D dr = new Point2D(0, 0);

        private final Chain ownerChain;

        private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
        private final SimpleDoubleProperty principalAngle = new SimpleDoubleProperty();
        private final SimpleDoubleProperty secondaryAngle = new SimpleDoubleProperty();
        private final IntegerProperty chainIndex = new SimpleIntegerProperty();
        private final BooleanBinding isInnerNode;
        private final BooleanBinding isLastNode;
        private final BooleanBinding isFirstNode;
        private final Arc angle1;
        private final Arc angle2;
        Line nextNodeConnector;
        Text idText;
        DropShadow shadow;


        public AnchorNode(Chain owner, int chainIndex, Pane parent) {
            super(NODE_SIZE);

            this.ownerChain = owner;
            this.chainIndex.set(chainIndex);

            secondaryAngle.bind(principalAngle.subtract(360).multiply(-1));

            isLastNode = this.chainIndex.isEqualTo(ownerChain.sizeProperty.subtract(1));
            isFirstNode = this.chainIndex.isEqualTo(0);
            isInnerNode = isFirstNode.not().and(isLastNode.not());

            shadow = new DropShadow();
            shadow.setBlurType(BlurType.GAUSSIAN);
            shadow.setHeight(50);
            shadow.setWidth(50);
            setEffect(shadow);

            idText = new Text();
            idText.setTextAlignment(TextAlignment.CENTER);
            idText.layoutXProperty().bind(centerXProperty().subtract(idText.getLayoutBounds().getWidth() / 2 + 3));
            idText.layoutYProperty().bind(centerYProperty().add(-10));
            idText.textProperty().bind(chainIndexProperty().asString());

            nextNodeConnector = new Line();
            nextNodeConnector.startXProperty().bind(centerXProperty());
            nextNodeConnector.startYProperty().bind(centerYProperty());
            nextNodeConnector.setFill(Color.RED);
            nextNodeConnector.setVisible(false);
            nextNodeConnector.visibleProperty().bind(isLastNode.not());

            angle1 = new Arc();
            angle1.centerXProperty().bind(centerXProperty());
            angle1.centerYProperty().bind(centerYProperty());
            angle1.setRadiusX(20);
            angle1.setRadiusY(20);
            angle1.setType(ArcType.ROUND);
            angle1.setFill(Color.TRANSPARENT);
            angle1.strokeProperty().bind(primaryAngleColor);
            angle1.setStrokeWidth(2);
            angle1.visibleProperty().bind(isInnerNode);

            angle2 = new Arc();
            angle2.centerXProperty().bind(centerXProperty());
            angle2.centerYProperty().bind(centerYProperty());

            angle2.setRadiusX(20);
            angle2.setRadiusY(20);
            angle2.setType(ArcType.ROUND);
            angle2.setFill(Color.TRANSPARENT);
            angle2.strokeProperty().bind(secondaryAngleColor);
            angle2.setStrokeWidth(2);
            angle2.toBack();
            angle2.visibleProperty().bind(isInnerNode);

            parent.getChildren().add(angle1);
            parent.getChildren().add(angle2);
            parent.getChildren().addAll(nextNodeConnector, idText);


            setOnMouseEntered(evt -> {
                if (!evt.isPrimaryButtonDown())
                    setCursor(Cursor.MOVE);
            });
            setOnMouseExited(evt -> {
            });
            setOnMouseClicked(evt -> {
                System.out.println("Click on node");
                System.out.println(this);

                if (deletionEnabled) {
                    if (currentChain.size() > 3)
                        currentChain.remove(this);
                    else System.out.println("Minimal chain. Cannot remove anything");
                } else if (MainViewController.getInstance().currentlyPressedKeyCode == KeyCode.A) {
                    System.out.println("Select chain");
                    MainViewController.instance.selectChain(ownerChain);
                    evt.consume();
                } else {
                    shadow.setColor(Color.ORANGE);
                }
            });
            setOnMousePressed(evt -> {
                dr = new Point2D(evt.getX() - getCenterX(), evt.getY() - getCenterY());
            });
            setOnMouseReleased(evt -> {
                setRadius(NODE_SIZE);
                setCursor(Cursor.CROSSHAIR);
            });
            setOnDragDetected(evt -> {
                System.out.println("Drag detected");
                setCursor(Cursor.NONE);
                setRadius(3);
            });
            setOnDragDropped(evt -> {
                System.out.println("drag dropped");
            });
            setOnDragDone(evt -> {
                System.out.println("drag done");
            });
            setOnMouseDragged(evt -> {


                // update node position
                setCenterX(evt.getX() - dr.getX());
                setCenterY(evt.getY() - dr.getY());


                if (hasNext()) {
                    getNext().updateAngleMeasure();
                }

                if (hasPrevious()) {
                    getPrevious().updateAngleMeasure();
                }

                updateAngleMeasure();
//                chains.forEach(MainViewController.this::draw);
            });
        }

        public int getChainIndex() {
            return chainIndex.get();
        }

        public IntegerProperty chainIndexProperty() {
            return chainIndex;
        }

        /**
         * To be called after the node removal. Removes all the associated nodes and shapes from the {@link #pane}
         */
        public void dispose() {
            Pane parent = (Pane) getParent();
            parent.getChildren().removeAll(angle1, angle2, nextNodeConnector, idText);
        }

        public double getAngles1() {
            if (!hasNext() || !hasPrevious())
                throw new UnsupportedOperationException("Cannot calculate the angle of a border node");

            AnchorNode previous = getPrevious();
            AnchorNode next = getNext();

            double dx1 = next.getCenterX() - getCenterX();
            double dy1 = next.getCenterY() - getCenterY();
            double dx2 = previous.getCenterX() - getCenterX();
            double dy2 = previous.getCenterY() - getCenterY();

            double dot = dx1 * dx2 + dy1 * dy2;
            double det = dx1 * dy2 - dx2 * dy1;

            System.out.println("PPPPPP: " + toDegrees(atan2(det, dot)));

            return atan2(det, dot);
        }

        public double[] getAngles() {
            if (!hasNext() || !hasPrevious())
                throw new UnsupportedOperationException("Cannot calculate the angle of a border node");

            AnchorNode previous = getPrevious();
            AnchorNode next = getNext();

            double dx = next.getCenterX() - getCenterX();
            double dy = next.getCenterY() - getCenterY();
            double r = pow(dx * dx + dy * dy, 0.5);
            double nextAngle = acos(dx / r);

            if (dy < 0)
                nextAngle = 2 * PI - nextAngle;

            dx = previous.getCenterX() - getCenterX();
            dy = previous.getCenterY() - getCenterY();
            r = pow(dx * dx + dy * dy, 0.5);

            double previousAngle = acos(dx / r);

            if (dy < 0)
                previousAngle = 2 * PI - previousAngle;

            System.out.printf("[%.2f - %.2f]%n", toDegrees(previousAngle), toDegrees(nextAngle));
            System.out.printf("alpha=%.2f\n", toDegrees(previousAngle - nextAngle));

            return new double[]{previousAngle, nextAngle};
        }

        public boolean isLast() {
            return isLastNode.get();
        }

        public boolean isFirst() {
            return isFirstNode.get();
        }

        public boolean isInner() {
            return isInnerNode.get();
        }

        public boolean hasNext() {
            return !isLast();
        }

        public boolean hasPrevious() {
            return chainIndex.get() > 0;
        }

        public AnchorNode getNext() {
            if (hasNext())
                return ownerChain.get(chainIndex.get() + 1);
            else return null;
        }

        public AnchorNode getPrevious() {
            if (hasPrevious())
                return ownerChain.get(chainIndex.get() - 1);
            else return null;
        }

        public void updateAngleMeasure() {
            if (isFirst() || isLast())
                return;

            double[] angles = getAngles();
            double degrees = toDegrees(getAngles1());

            if (degrees < 0)
                degrees += 360;

            principalAngle.set(degrees);

            angle1.setStartAngle(-toDegrees(angles[1]));
            angle1.setLength(-degrees);

            angle2.setStartAngle(-toDegrees(angles[1]));
            angle2.setLength(360 - degrees);
        }

        @Override
        public String toString() {
            return String.format("[chainIndex=%d, inner=%s, last=%s, first=%s]", chainIndex.get(), isInnerNode.get(), isLastNode.get(), isFirstNode.get());
        }
    }

    static class MeasureCell extends TableCell<AnchorNode, Double> {
        private int decimalPrecision;
        private String numberFormat;

        public MeasureCell(ObjectProperty<Color> color) {
            this(2, color);
        }

        public MeasureCell(int decimalPrecision, ObjectProperty<Color> color) {
            this.decimalPrecision = decimalPrecision;
            numberFormat = "%." + decimalPrecision + "f";

            textFillProperty().bind(color);
        }

        @Override
        protected void updateItem(Double value, boolean empty) {
            super.updateItem(value, empty);

            if (empty)
                setText(null);
            else if (value != null)
                setText(String.format(numberFormat, value));
        }
    }

}