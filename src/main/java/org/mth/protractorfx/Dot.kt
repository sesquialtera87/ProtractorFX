package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import org.mth.protractorfx.command.Action
import org.mth.protractorfx.command.CommandManager
import org.mth.protractorfx.log.LogFactory
import org.mth.protractorfx.tool.Tool
import java.util.logging.Logger
import kotlin.math.max

class Dot(x: Double, y: Double, val chain: DotChain) : Circle() {

    /**
     * Get the color of the parent chain
     */
    val chainColor: Color by chain.chainColor

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
        val decorator = AngleDecorator(Angle(this, dot1, dot2))

        log.finest("Angle centre: $this \nAngle sides: \n\tP1 = $dot1 \n\tP2 = $dot2")

        if (!angleDecorators.contains(decorator)) {
            runLater {
                angleDecorators.add(decorator)
                decorator.build()
            }
        } else {
            log.info("Angle already measured")
        }
    }

    /**
     * Simply call the [DotChain.removeDot] method apply to this dot
     */
    fun removeFromChain() {
        chain.removeDot(this)
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
            dot.toFront()

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
                    DotMenu.configureBeforeShow(dot, it.x, it.y)
                    it.consume()
                } else if (it.button == MouseButton.PRIMARY) {
                    if (Tool.activeTools().isNotEmpty()) {
                        return@addEventHandler
                    }

                    if (it.isControlDown) {
                        // With CTRL + Click select the chain this dot belongs to
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

                    it.consume()
                }
            }

            dot.addEventHandler(MOUSE_PRESSED) { event ->
                dot.chain.forEach { it.toFront() }

                // remove the old anchor points of the previous selection
                anchorMap.clear()
                anchorMap[dot] = dot.getCenter()

                // save the coordinates of every node in the selection
                Selection.forEach {
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

                if (!it.isDragDetect) {
                    dragInitialized = false

                    CommandManager.execute(MoveAction(anchorMap))
                }

                if (Tool.activeTools().isEmpty())
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
                    val newCenter = dr sum oldCenter

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

    class MoveAction(
        private val dotLocations: Map<Dot, Point2D>,
        override val name: String = "move-dots",
    ) : Action {

        override fun execute() = true

        override fun undo() {
            // set of nodes for which update the angle measures
            val updateList: HashSet<Dot> = HashSet()

            dotLocations.forEach { (dot, point) ->
                dot.centerX = point.x
                dot.centerY = point.y

                updateList.add(dot)
                updateList.addAll(dot.neighbors())
            }

            updateList.forEach { it.updateNeighboringAngles() }
        }
    }
}