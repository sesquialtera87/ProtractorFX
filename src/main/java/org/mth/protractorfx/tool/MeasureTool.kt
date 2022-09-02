package org.mth.protractorfx.tool

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import javafx.util.Pair
import org.mth.protractorfx.*

object MeasureTool : AbstractTool() {

    override val cursor: Cursor
        get() = CURSOR_ANGLE

    override val shortcut: KeyCodeCombination
        get() = KeyCodeCombination(KeyCode.M)

    override fun onRelease(mouseEvent: MouseEvent) {
        measureAngle(Point2D(mouseEvent.x, mouseEvent.y))
    }

    /**
     * Put an [AngleDecorator] around the angle nearest to the mouse click
     */
    private fun measureAngle(mousePoint: Point2D) {
        val nearestDot = getNearestDot(mousePoint, chains.flatten(), true)
        val neighbors = nearestDot.neighbors()

        val anglesFromMouse = mutableListOf<Pair<Dot, Double>>()

        if (neighbors.size < 2) // the nearest node is a leaf
            return

        for (neighbor in neighbors) {
            // calculate the angle (measured anticlockwise) from the neighbor node to the mouse point
            val p1 = neighbor.getCenter().subtract(nearestDot.getCenter())
            val p2 = mousePoint.subtract(nearestDot.getCenter())
            val angle = angleBetween(p1, p2, MeasureUnit.DECIMAL_DEGREE)
            anglesFromMouse.add(Pair(neighbor, angle))
        }

        // sort angles in ascending order, from the nearest to the farthest node (counterclockwise)
        anglesFromMouse.sortBy { it.value }

        // get the farthest and the nearest nodes as the delimiters for the user-chosen angle
        val dot1 = anglesFromMouse.last().key
        val dot2 = anglesFromMouse.first().key

        nearestDot.addAngleMeasure(dot1, dot2)
    }
}