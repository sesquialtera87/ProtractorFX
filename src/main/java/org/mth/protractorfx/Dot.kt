package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.effect.DropShadow
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
    private val chainColor: Color by chain.chainColor

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
        // listen to focus changes
        focusedProperty().addListener { _, _, _ ->
            if (isFocused) {
                effect = DropShadow().apply {
                    color = Color.ORANGE
                    width = 10.0
                    height = 10.0
                }
            } else {
                effect = null
            }
        }

        // listen for color changes
        chain.chainColor.addListener { _, _, _ -> fill = chainColor }

        radius = DOT_RADIUS
        centerX = x
        centerY = y
        fill = chainColor

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
        private var dragInitialized = false

        private fun initDrag(dot: Dot) {
            log.fine("Drag detected")

            dot.radius = DOT_RADIUS_SMALL

            if (Selection.size != anchorMap.size)
                anchorMap.keys.filter { it != dot }
                    .forEach {
                        anchorMap.remove(it)
                        it.selected = false
                    }

            if (!dot.selected)
                Selection.addToSelection(dot)

            dragInitialized = true
        }

        init {
            dot.addEventHandler(MOUSE_CLICKED) {
                if (it.button == MouseButton.SECONDARY) {
                    // popup trigger
                    DotMenu.show(dot, it.x, it.y)
                } else if (it.button == MouseButton.PRIMARY) {
                    // With CTRL + Click select the chain this dot belongs to
                    if (it.isControlDown) {
                        log.finest("Chain selection trigger detected")

                        Selection.clear()
                        Selection.addToSelection(dot.chain)
                        dot.requestFocus()
                    } else if (it.isShiftDown) {
                        log.finest("Increment selection trigger detected")

                        Selection.addToSelection(dot)
                        dot.requestFocus()
                    } else {
                        if (it.isDragDetect) {
                            log.finest("Single dot selection trigger detected")

                            Selection.select(dot)
                            dot.requestFocus()
                        }
                    }
                }

                it.consume()
            }

            dot.addEventHandler(MOUSE_PRESSED) { event ->
                // remove the old anchor points of the previous selection
                anchorMap.clear()
                anchorMap[dot] = dot.getCenter()

                // save the coordinates of every node in the selection
                Selection.selectedDots().forEach {
                    anchorMap[it] = it.getCenter()
                }

                anchorPoint = Point2D(event.screenX, event.screenY)
                anchorPointForDrag = dot.getCenter()

                log.fine("Mouse pressed. \n\tAnchor point = $anchorPoint \n\tDrag anchor = $anchorPointForDrag \n\tAnchorMap size = ${anchorMap.size}")
            }

            dot.addEventHandler(DRAG_DETECTED) { event ->
                log.fine("Drag detected")

                if (!dragInitialized)
                    initDrag(dot)

                event.consume()
            }

            dot.addEventHandler(MOUSE_RELEASED) {
                dot.radius = DOT_RADIUS
//                dot.toFront()

                if (!it.isDragDetect)
                    dragInitialized = false

                it.consume()
            }


            dot.addEventHandler(MOUSE_DRAGGED) { mouseEvent ->
                log.fine("Dragging")

                if (!dragInitialized)
                    initDrag(dot)

                val currentDragPoint = Point2D(mouseEvent.screenX, mouseEvent.screenY)

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

                updateList.forEach { it.updateNeighboringAngles() }
            }
        }

        companion object {
            private val log: Logger = LogFactory.configureLog(DragSupport::class.java)
        }
    }

}