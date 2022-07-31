package org.mth.protractorfx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

class Chain implements Iterable<MainViewController.AnchorNode> {

    IntegerProperty sizeProperty = new SimpleIntegerProperty(0);

    private final ObservableList<MainViewController.AnchorNode> nodes = FXCollections.observableArrayList();
    private final Pane pane;

    public Chain(Pane pane) {
        this.pane = pane;

        nodes.addListener((ListChangeListener<MainViewController.AnchorNode>) change -> sizeProperty.set(nodes.size()));
    }

    public ObservableList<MainViewController.AnchorNode> getNodes() {
        return nodes;
    }

    public MainViewController.AnchorNode addNode(double x, double y) {
        MainViewController.AnchorNode node = new MainViewController.AnchorNode(this, size(), pane);
        node.setCenterX(x);
        node.setCenterY(y);

        if (!nodes.isEmpty()) {
            MainViewController.AnchorNode lastNode = nodes.get(nodes.size() - 1);
            lastNode.nextNodeConnector.endXProperty().bind(node.centerXProperty());
            lastNode.nextNodeConnector.endYProperty().bind(node.centerYProperty());
        }

        nodes.add(node);

        if (node.hasPrevious())
            node.getPrevious().updateAngleMeasure();

        return node;
    }

    public MainViewController.AnchorNode first() {
        return nodes.get(0);
    }

    public MainViewController.AnchorNode last() {
        return nodes.get(size() - 1);
    }

    public MainViewController.AnchorNode get(int index) {
        return nodes.get(index);
    }

    int size() {
        return nodes.size();
    }

    public void remove(MainViewController.AnchorNode node) {
        if (node.isInner()) {
            MainViewController.AnchorNode previous = node.getPrevious();
            MainViewController.AnchorNode next = node.getNext();
            previous.nextNodeConnector.endXProperty().bind(next.centerXProperty());
            previous.nextNodeConnector.endYProperty().bind(next.centerYProperty());


        }

        nodes.remove(node.getChainIndex());
        node.dispose();

        // update the indices of each node next to the deleted one
        for (int i = node.getChainIndex(); i < nodes.size(); i++) {
            int nodeIndex = nodes.get(i).getChainIndex();
            nodes.get(i).chainIndexProperty().set(nodeIndex - 1);
        }

        // update the angles for the new connected angles (necessary only during the deletion of an inner node)
        Stream.of(node.getChainIndex() - 1, node.getChainIndex())
                .filter(index -> index >= 0 && index < nodes.size())
                .forEach(index -> nodes.get(index).updateAngleMeasure());

        // remove the node from the container
        pane.getChildren().remove(node);
    }

    @Override
    public Iterator<MainViewController.AnchorNode> iterator() {
        return nodes.iterator();
    }

    @Override
    public void forEach(Consumer<? super MainViewController.AnchorNode> action) {
        nodes.forEach(action);
    }

    @Override
    public Spliterator<MainViewController.AnchorNode> spliterator() {
        return nodes.spliterator();
    }
}
