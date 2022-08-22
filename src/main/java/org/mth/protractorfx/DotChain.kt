package org.mth.protractorfx

import animatefx.animation.FadeIn
import animatefx.animation.FadeOut
import animatefx.util.ParallelAnimationFX
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.FontWeight
import java.util.*

class DotChain(private val container: Pane) : Iterable<Dot> {

    /**
     * A connection line between two graph nodes
     */
    data class ConnectorLine(val dot1: Dot, val dot2: Dot) : Line() {

        // The Pane container this Line belongs to
        private val parent get() = dot1.parent as Pane

        init {
            startXProperty().bind(dot1.centerXProperty())
            startYProperty().bind(dot1.centerYProperty())

            endXProperty().bind(dot2.centerXProperty())
            endYProperty().bind(dot2.centerYProperty())

            // add the line to the parent Pane
            parent.children.add(this)
            isVisible = true

            FadeIn(this).apply {
                setSpeed(2.0)
                play()
            }

            toBack()
        }

        /**
         * Returns `true` if this line connect the two graph nodes
         */
        fun match(dot1: Dot, dot2: Dot) =
            (dot1 == this.dot1 && dot2 == this.dot2)
                    || (dot1 == this.dot2 && dot2 == this.dot1)
    }

    /**
     * Contains the nodes of this chain that are actually selected
     */
    val selection: HashSet<Dot> = HashSet()

    private val adjacencyList: MutableMap<Dot, HashSet<Dot>> = mutableMapOf()
    private val connections: HashSet<ConnectorLine> = HashSet()

    /**
     * The color of the nodes of this chain
     */
    val chainColor = SimpleObjectProperty(Color.BLACK)

    val measureLabelFontColorProperty = SimpleObjectProperty(Color.BLACK)
    val measureLabelFontSizeProperty = SimpleDoubleProperty(12.0)
    val measureLabelFontWeightProperty = SimpleObjectProperty(FontWeight.NORMAL)
    val measureLabelBackgroundVisibilityProperty = SimpleBooleanProperty(true)
    val measureLabelBackgroundColorProperty = SimpleObjectProperty(Color.BISQUE)

    var measureLabelFontColor: Color by measureLabelFontColorProperty
    var measureLabelFontSize: Double by measureLabelFontSizeProperty
    var measureLabelFontWeight: FontWeight by measureLabelFontWeightProperty
    var measureLabelBackgroundVisibility: Boolean by measureLabelBackgroundVisibilityProperty
    var measureLabelBackgroundColor: Color by measureLabelBackgroundColorProperty

    /**
     * Get the total number of nodes in this chain
     */
    val size: Int get() = adjacencyList.size

    init {
        val dot1 = Dot(50.0, 50.0, this)
        val dot2 = Dot(150.0, 50.0, this)
        val dot3 = Dot(250.0, 150.0, this)

        addDots(dot1, dot2, dot3)
        connect(dot1, dot2)
        connect(dot2, dot3)
    }

    fun setColor(color: Color) {
        chainColor.set(color)
    }

    fun neighborsCount(dot: Dot) = adjacencyList[dot]!!.size

    fun neighbors(dot: Dot): HashSet<Dot> = adjacencyList[dot]!!

    fun addDots(vararg dots: Dot) {
        dots.forEach { dot ->
            if (!adjacencyList.containsKey(dot))
                adjacencyList[dot] = HashSet()
            container.children.add(dot)
            dot.isVisible = true
            dot.toFront()
        }
    }

    fun clearSelection() {
        selection.forEach { dot -> dot.selected = false }
        selection.clear()
    }

    fun addDot(dot: Dot): Boolean {
        return if (adjacencyList.containsKey(dot))
            false
        else {
            adjacencyList[dot] = HashSet()
            container.children.add(dot)
            dot.isVisible = true
            dot.toFront()

            FadeIn(dot).apply {
                setSpeed(2.5)
                play()
            }

            true
        }
    }

    fun addToSelection(dot: Dot) {
        selection.add(dot)
        dot.selected = true
    }

    fun select(dot: Dot) {
        clearSelection()
        selection.add(dot)
        dot.selected = true
    }

    fun removeDot(dot: Dot) {
        if (dot.isLeaf()) {
            val parent = adjacencyList[dot]!!.first()
            adjacencyList[parent]!!.remove(dot)
            adjacencyList.remove(dot)

            // get the connection and removes it from Pane
            val dotConnection = connections.first { it.match(dot, parent) }

            val descriptors = parent.angleDecorators.filter { descriptor ->
                descriptor.neighbor1 == dot || descriptor.neighbor2 == dot
            }.toList()

            descriptors.forEach {
                // todo
            }

            ParallelAnimationFX(
                FadeOut(dotConnection).apply {
                    setSpeed(2.0)
                },
                FadeOut(dot).apply {
                    setSpeed(2.0)
                    setOnFinished { container.children.removeAll(dot, dotConnection) }
                }
            ).run { play() }


            // Move focus on the Pane, to handle correctly the key-released event (X)
            container.requestFocus()
        }
    }

    fun connect(dot1: Dot, dot2: Dot) {
        adjacencyList[dot1]?.add(dot2)
        adjacencyList[dot2]?.add(dot1)

        connections.add(ConnectorLine(dot1, dot2))
    }

    fun getSelectedDot(): Optional<Dot> {
        return if (selection.isEmpty())
            Optional.empty()
        else Optional.of(selection.last())
    }

    /**
     * Find the node with the minim distance from the given point.
     * @param excludeLeaves If `true` the nodes with only one incoming connections (leaves) are ignored from the search
     */
    fun getNearestDot(point: Point2D, excludeLeaves: Boolean = false): Dot {
        var nearestDot = Dot(0.0, 0.0, this)
        var minDistance: Double = Double.MAX_VALUE
        var currentDistance: Double

        adjacencyList.keys.forEach { dot ->
            if (!(excludeLeaves && dot.isLeaf())) {
                currentDistance = point.distance(Point2D(dot.centerX, dot.centerY))

                if (currentDistance < minDistance) {
                    minDistance = currentDistance
                    nearestDot = dot
                }
            }
        }

        return nearestDot
    }

    override fun iterator() = adjacencyList.keys.iterator()
}