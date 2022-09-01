package org.mth.protractorfx.tool

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.*

object DeletionTool : AbstractTool() {

    override val cursor: Cursor
        get() = CURSOR_REMOVE_DOT

    override val shortcut: KeyCodeCombination
        get() = KeyCodeCombination(KeyCode.X)

    override fun onRelease(mouseEvent: MouseEvent) {
        val coordinates =
            Point2D(mouseEvent.x, mouseEvent.y).subtract(pane.boundsInParent.minX, pane.boundsInParent.minY)

        // find the dot under the cursor and remove it
        for (dot in chains.flatten()) {
            if (dot.getCenter().subtract(coordinates).magnitude() < dot.radius) {
                dot.removeFromChain()
                break
            }
        }

    }

    fun deleteSelection() {
        var leaves = Selection.selectedDots().filter { it.isLeaf() }

        while (leaves.isNotEmpty()) {
            leaves.forEach {
                it.removeFromChain()
                Selection.unselect(it)
            }

            leaves = Selection.selectedDots().filter { it.isLeaf() }
        }

        Selection.clear()
    }
}