package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.util.Pair
import org.mth.protractorfx.log.LogFactory
import java.util.logging.Logger


object Tools {

    val log: Logger = LogFactory.configureLog(Tools::class.java)

    val pane: Pane by lazy { scene.lookup("#container") as Pane }

    val deletionTool = object : AbstractTool() {
        override val cursor: Cursor
            get() = CURSOR_REMOVE_DOT

        override val shortcut: KeyCodeCombination
            get() = KeyCodeCombination(KeyCode.X)

        override fun onRelease(mouseEvent: MouseEvent) {
            deleteDot()
        }
    }

    val insertionTool = object : AbstractTool() {
        override val cursor: Cursor
            get() = CURSOR_INSERT_DOT

        override val shortcut: KeyCodeCombination
            get() = KeyCodeCombination(KeyCode.A)

        override fun onRelease(mouseEvent: MouseEvent) {
            addNewDot(mouseEvent.x, mouseEvent.y)
        }
    }

    val measureTool = object : AbstractTool() {
        override val cursor: Cursor
            get() = CURSOR_ANGLE

        override val shortcut: KeyCodeCombination
            get() = KeyCodeCombination(KeyCode.M)

        override fun onRelease(mouseEvent: MouseEvent) {
            measureAngle(Point2D(mouseEvent.x, mouseEvent.y))
        }
    }

    val selectionTool = object : AbstractTool() {

        override val cursor: Cursor
            get() = CURSOR_RECT_SELECTION

        override val shortcut: KeyCodeCombination
            get() = SHORTCUT_RECT_SELECTION


        override fun onPress(mouseEvent: MouseEvent) {
            SelectionRectangle.startSelection(mouseEvent)
        }

        override fun onRelease(mouseEvent: MouseEvent) {
            // with shift down, maintain the previous selected dots
            if (!mouseEvent.isShiftDown) {
                Selection.clear()
            }

            chains.flatten()
                .filter { SelectionRectangle.isDotInSelection(it) }
                .forEach { Selection.addToSelection(it) }

            SelectionRectangle.stopSelection()
            deactivate()
        }

        override fun onDrag(mouseEvent: MouseEvent) {
            // reshape the selection
            SelectionRectangle.updateSelectionShape(mouseEvent)
        }

    }

    /**
     * Add a new dot at the specified coordinates
     */
    fun addNewDot(x: Double, y: Double) {
        val selectedDot = Selection.selectedDot()

        selectedDot.ifPresent { dot: Dot ->
            val chain = dot.chain
            val newDot = Dot(x, y, chain)

            chain.apply {
                addDot(newDot)
                connect(newDot, dot)
                Selection.select(newDot)
            }

            newDot.requestFocus()
        }
    }

    fun measureAngle(mousePoint: Point2D) {
        val nearestDot = getNearestDot(mousePoint, chains.flatten(), true)
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
        var leaves = Selection.selectedDots().filter { it.isLeaf() }

        while (leaves.isNotEmpty()) {
            leaves.forEach {
                it.chain.removeDot(it)
                Selection.unselect(it)
            }

            leaves = Selection.selectedDots().filter { it.isLeaf() }
        }

        Selection.clear()
    }
}