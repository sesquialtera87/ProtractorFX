package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyEvent.KEY_PRESSED
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseEvent.*
import javafx.scene.layout.Pane
import javafx.util.Pair
import org.mth.protractorfx.log.LogFactory
import java.util.logging.Logger


object Tools {

    val log: Logger = LogFactory.configureLog(Tools::class.java)

    lateinit var pane: Pane

    private val deletionTool = object : Tool() {
        override val cursor: Cursor
            get() = CURSOR_REMOVE_DOT

        override val shortcut: KeyCodeCombination
            get() = KeyCodeCombination(KeyCode.X)

        override fun onRelease(mouseEvent: MouseEvent) {
            deleteDot()
        }
    }

    private val insertionTool = object : Tool() {
        override val cursor: Cursor
            get() = CURSOR_INSERT_DOT

        override val shortcut: KeyCodeCombination
            get() = KeyCodeCombination(KeyCode.A)

        override fun onRelease(mouseEvent: MouseEvent) {
            addNewDot(mouseEvent.x, mouseEvent.y)
        }
    }

    private val measureTool = object : Tool() {
        override val cursor: Cursor
            get() = CURSOR_ANGLE

        override val shortcut: KeyCodeCombination
            get() = KeyCodeCombination(KeyCode.M)

        override fun onRelease(mouseEvent: MouseEvent) {
            measureAngle(Point2D(mouseEvent.x, mouseEvent.y))
        }
    }

    private val selectionTool = object : Tool() {

        override val cursor: Cursor
            get() = CURSOR_RECT_SELECTION

        override val shortcut: KeyCodeCombination
            get() = SHORTCUT_RECT_SELECTION


        override fun onPress(mouseEvent: MouseEvent) {
            SelectionRectangle.startSelection(mouseEvent)
        }

        override fun onRelease(mouseEvent: MouseEvent) {
            // with shift down, maintain the previous selected dots
            if (!mouseEvent.isShiftDown)
                chain.clearSelection()

            chain.filter { SelectionRectangle.isDotInSelection(it) }
                .forEach { chain.addToSelection(it) }

            SelectionRectangle.stopSelection()
            deactivate()
        }

        override fun onDrag(mouseEvent: MouseEvent) {
            // reshape the selection
            SelectionRectangle.updateSelectionShape(mouseEvent)
        }

    }

    abstract class Tool {
        var active: Boolean = false
        abstract val cursor: Cursor
        abstract val shortcut: KeyCodeCombination

        open fun activate() {
            active = true
            pane.cursor = cursor
        }

        open fun deactivate() {
            active = false
            pane.cursor = Cursor.DEFAULT
        }

        open fun onPress(mouseEvent: MouseEvent) {}

        open fun onDrag(mouseEvent: MouseEvent) {}

        open fun onRelease(mouseEvent: MouseEvent) {}
    }

    fun initialize(pane: Pane) {
        this.pane = pane

        val tools = listOf(
            selectionTool,
            measureTool,
            insertionTool,
            deletionTool
        )

        pane.addEventHandler(KEY_PRESSED) { event ->
            when (event.code) {
                KeyCode.ESCAPE -> tools.forEach { it.deactivate() }
                KeyCode.DELETE -> deleteDot()
                in listOf(KeyCode.SHIFT, KeyCode.CONTROL, KeyCode.ALT) -> return@addEventHandler
                else -> {
                    val partition = tools.partition { it.shortcut.match(event) }
                    partition.second.forEach { it.deactivate() }
                    partition.first.forEach { it.activate() }

                    event.consume()
                }
            }
        }

        pane.addEventHandler(MOUSE_PRESSED) { event ->
            if (event.isPrimaryButtonDown) {
                tools.filter { it.active }
                    .forEach { it.onPress(event) }

                event.consume()
            }
        }

        pane.addEventHandler(MOUSE_RELEASED) { event ->
            if (event.button == MouseButton.PRIMARY) {
                tools.filter { it.active }
                    .forEach { it.onRelease(event) }

                event.consume()
            }
        }

        pane.addEventHandler((MOUSE_DRAGGED)) { event ->
            if (event.button == MouseButton.PRIMARY) {
                tools.filter { it.active }
                    .forEach { it.onDrag(event) }
            }
        }
    }

    /**
     * Add a new dot at the specified coordinates
     */
    fun addNewDot(x: Double, y: Double) {
        val selectedDot = chain.getSelectedDot()

        selectedDot.ifPresent { dot: Dot ->
            val newDot = Dot(x, y, chain)

            chain.apply {
                addDot(newDot)
                connect(newDot, dot)
                select(newDot)
            }
        }
    }

    fun measureAngle(mousePoint: Point2D) {
        val nearestDot = chain.getNearestDot(mousePoint, true)
        val neighbors = nearestDot.neighbors()

        val anglesFromMouse = mutableListOf<Pair<Dot, Double>>()

        if (neighbors.size < 2) // the nearest node is a leaf
            return

        for (neighbor in neighbors) {
            // calculate the angle (measured anticlockwise) from the neighbor node to the mouse point
            val p1 = neighbor.getCenter().subtract(nearestDot.getCenter())
            val p2 = mousePoint.subtract(nearestDot.getCenter())
            val angle = angleBetween(p1, p2, true)
            anglesFromMouse.add(Pair(neighbor, angle))
        }

        // sort angles in ascending order, from the nearest to the farthest node (counterclockwise)
        anglesFromMouse.sortBy { it.value }

        // get the farthest and the nearest nodes as the delimiters for the user-chosen angle
        val dot1 = anglesFromMouse.last().key
        val dot2 = anglesFromMouse.first().key

        nearestDot.addAngleMeasure(dot1, dot2)
    }

    fun deleteDot() {
        with(chain) {
            var leaves = selection.filter { it.isLeaf() }

            while (leaves.isNotEmpty()) {
                leaves.forEach {
                    removeDot(it)
                    selection.remove(it)
                }

                leaves = selection.filter { it.isLeaf() }
            }

            clearSelection()
        }
    }
}