package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent.*
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import org.mth.protractorfx.log.LogFactory
import java.util.logging.Logger
import kotlin.math.max

class Dot(x: Double, y: Double, val chain: DotChain) : Circle() {

    /**
     * Get the color of the parent chain
     */
    private val chainColor: Color get() = chain.chainColor.get()

    val angleDecorators = mutableListOf<AngleDecorator>()

    var selected: Boolean = false
        set(value) {
            field = value
            fill = if (value)
                SELECTED_COLOR
            else
                chainColor
        }


    init {
        // listen for color changes
        chain.chainColor.addListener { _, _, _ -> fill = chainColor }

        radius = DOT_RADIUS
        centerX = x
        centerY = y
        fill = chainColor


        addEventHandler(MOUSE_PRESSED) {
            if (it.button == MouseButton.PRIMARY) {
                log.fine("Click on dot with primary button")

                if (selected) {
                    it.consume()
                    return@addEventHandler
                }

                requestFocus()

                if (it.isShiftDown) {
                    Selection.addToSelection(this)
                    it.consume()
                } else {
                    Selection.select(this)
                    it.consume()
                }
            }
        }

        DragSupport(this)
    }


    fun isLeaf() = neighbors().size < 2

    fun neighbors() = chain.neighbors(this)

    fun updateNeighboringAngles() {
        angleDecorators.forEach { it.update() }
    }

    fun addAngleMeasure(dot1: Dot, dot2: Dot) {
        val pane = parent as Pane
        val angleDescriptor = AngleDecorator(dot1, dot2)

        log.finest("Angle centre: $this \nAngle sides: \n\tP1 = $dot1 \n\tP2 = $dot2")

        if (!angleDecorators.contains(angleDescriptor)) {
            angleDecorators.add(angleDescriptor)
            angleDescriptor.build(this, pane)
        } else {
            log.info("Angle already measured")
        }
    }

    companion object {
        val log: Logger = LogFactory.configureLog(Dot::class.java)

        const val DOT_RADIUS = 6.0
        const val DOT_RADIUS_SMALL = 2.5
        val SELECTED_COLOR: Color = Color.GRAY
    }

    class DragSupport(dot: Dot) {

        private val anchorMap: MutableMap<Dot, Point2D> = mutableMapOf()
        private var anchorPoint = Point2D(.0, .0)
        private var anchorPointForDrag = Point2D(.0, .0)
        private var dr = Point2D(.0, .0)

        init {
            dot.addEventHandler(MOUSE_CLICKED) {
                if (it.button == MouseButton.SECONDARY) {
                    DotMenu.show(dot, it.x, it.y)
                }

                it.consume()
            }

            dot.addEventHandler(DRAG_DETECTED) { event ->
                log.fine("Drag detected")

                dot.radius = DOT_RADIUS_SMALL
                dot.toBack()

                if (Selection.size != anchorMap.size)
                    anchorMap.keys.filter { it != dot }
                        .forEach { anchorMap.remove(it) }

                event.consume()
            }

            dot.addEventHandler(MOUSE_RELEASED) {
                dot.radius = DOT_RADIUS
                dot.toFront()

                it.consume()
            }

            dot.addEventHandler(MOUSE_PRESSED) { event ->
                // remove the old anchor points of the previous selection
                anchorMap.clear()
                anchorMap[dot] = dot.getCenter()

                log.fine("Selection size = ${Selection.size}")

                Selection.selectedDots().forEach {
                    anchorMap[it] = it.getCenter()
                }

                anchorPoint = Point2D(event.screenX, event.screenY)
                anchorPointForDrag = dot.getCenter()

                log.fine("Mouse pressed. \n\tAnchor point = $anchorPoint \n\tDrag anchor = $anchorPointForDrag")
            }

            dot.addEventHandler(MOUSE_DRAGGED) {
                log.fine("Dragging")

                val currentDragPoint = Point2D(it.screenX, it.screenY)

                // set of nodes for which update the angle measures
                val updateList: HashSet<Dot> = HashSet()

                anchorMap.forEach { (dot, oldCenter) ->
                    // calculate the delta from the anchor point
                    dr = currentDragPoint.subtract(anchorPoint)

                    // get the new center coordinates of the node
                    val newCenter = oldCenter.add(dr)

                    dot.apply {
                        centerX = max(newCenter.x, DOT_RADIUS)
                        centerY = max(newCenter.y, DOT_RADIUS)
                    }

                    // add the node and its neighbors to the update-set
                    updateList.add(dot)
                    updateList.addAll(dot.neighbors())
                }

                updateList.forEach { dot ->
                    dot.updateNeighboringAngles()
                }
            }
        }

        companion object {
            private val log: Logger = LogFactory.configureLog(DragSupport::class.java)
        }
    }

}