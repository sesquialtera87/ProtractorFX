package org.mth.protractorfx.tool

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.CURSOR_REMOVE_DOT
import org.mth.protractorfx.Selection
import org.mth.protractorfx.chains
import org.mth.protractorfx.getCenter

object DeletionTool : AbstractTool() {

    override val cursor: Cursor
        get() = CURSOR_REMOVE_DOT

    override val shortcut: KeyCodeCombination
        get() = KeyCodeCombination(KeyCode.X)

    override fun onRelease(mouseEvent: MouseEvent) {
        val coordinates =
            Point2D(mouseEvent.x, mouseEvent.y).subtract(pane.boundsInParent.minX, pane.boundsInParent.minY)

        for (dot in chains.flatten()) {
            if (dot.getCenter().subtract(coordinates).magnitude() < dot.radius) {
                dot.chain.removeDot(dot)
                break
            }
        }

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