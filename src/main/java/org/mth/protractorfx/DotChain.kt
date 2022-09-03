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

class DotChain(private val container: Pane, displacement: Point2D = Point2D(.0, .0), color: Color? = Color.BLACK) :
    Iterable<Dot> {

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
            stroke = dot1.chainColor.desaturate()

            FadeIn(this).apply {
                setSpeed(2.0)
                play()
            }

            toBack()

            setOnMouseClicked {
                dot1.chain.forEach { it.toFront() }
            }
        }

        /**
         * Returns `true` if this line connect the two graph nodes
         */
        fun match(dot1: Dot, dot2: Dot) =
            (dot1 == this.dot1 && dot2 == this.dot2)
                    || (dot1 == this.dot2 && dot2 == this.dot1)
    }

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
        if (color == null) {
            // choose a random color from the available ones
            var availableColors = availableColors()

            // if no color is available, choose random from all the colors
            if (availableColors.isEmpty())
                availableColors = defaultColors()

            with(Random()) {
                val randomColor = availableColors[nextInt(availableColors.size)]
                chainColor.set(randomColor)
            }
        } else
            chainColor.set(color)

        // update the connector color in response to a change of the chain color
        chainColor.addListener { _, _, chainColor ->
            connections.forEach {
                it.stroke = chainColor.desaturate()
            }
        }


        val dot1 = Dot(50.0 + displacement.x, 50.0 + displacement.y, this)
        val dot2 = Dot(150.0 + displacement.x, 50.0 + displacement.y, this)
        val dot3 = Dot(50.0 + displacement.x, 150.0 + displacement.y, this)

        addDots(dot1, dot2, dot3)
        connect(dot1, dot2)
        connect(dot2, dot3)
    }

    fun setColor(color: Color) {
        chainColor.set(color)
    }

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


    fun removeDot(dot: Dot) {
        if (size == 1) {
            dispose()
        } else if (dot.isLeaf()) {
            val parent = adjacencyList[dot]!!.first() // there's only one node connected (it's a leaf...)

            // remove the dot from its parent's adjacency list and from the adjacency list itself
            adjacencyList[parent]!!.remove(dot)
            adjacencyList.remove(dot)

            // get the connection and removes it from Pane
            val dotConnection = connections.first { it.match(dot, parent) }

            // remove the measures, related to the deleted node, around the angle
            parent.angleDecorators
                .filter { it.neighbor1 == dot || it.neighbor2 == dot }
                .forEach {
                    it.dispose(container)
                    parent.angleDecorators.remove(it)
                }

            // animate the removal
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

    /**
     * Remove all dots and their decorators (connector lines, measures) from the pane.
     */
    fun dispose() {
        forEach { dot ->
            // remove the dot from the pane
            container.children.remove(dot)

            // remove the connection lines
            connections.forEach { container.children.remove(it) }

            // remove all decorators
            dot.angleDecorators.forEach { it.dispose(container) }
        }
    }

    private fun availableColors(): List<Color> {
        val usedColors = chains.map { it.chainColor.get() }
        return defaultColors() - usedColors.toSet()
    }

    override fun iterator() = adjacencyList.keys.iterator()
}