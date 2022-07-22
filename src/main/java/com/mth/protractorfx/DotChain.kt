package com.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import java.util.*

class DotChain(private val container: Pane) {

    /**
     * A connection line between two graph nodes
     */
    class Connection(dot1: Dot, dot2: Dot) {
        private val line = Line()

        init {
            line.apply {
                startXProperty().bind(dot1.centerXProperty())
                startYProperty().bind(dot1.centerYProperty())

                endXProperty().bind(dot2.centerXProperty())
                endYProperty().bind(dot2.centerYProperty())

                val pane = dot1.parent as Pane
                pane.children.add(line)
                isVisible = true
                toBack()
            }
        }
    }

    val selection: HashSet<Dot> = HashSet()
    private val adjacencyList: MutableMap<Dot, HashSet<Dot>> = mutableMapOf()
    private val connections: HashSet<Connection> = HashSet()

    val size: Int get() = adjacencyList.size

    init {
        val dot1 = Dot(50.0, 50.0, this)
        val dot2 = Dot(150.0, 50.0, this)
        val dot3 = Dot(250.0, 150.0, this)

        addDots(dot1, dot2, dot3)
        connect(dot1, dot2)
        connect(dot2, dot3)
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
            true
        }
    }

    fun connect(dot1: Dot, dot2: Dot) {
        adjacencyList[dot1]?.add(dot2)
        adjacencyList[dot2]?.add(dot1)

        connections.add(Connection(dot1, dot2))
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
}