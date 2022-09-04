package org.mth.protractorfx.tool

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.*
import org.mth.protractorfx.command.Action
import org.mth.protractorfx.command.CommandManager

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
                if (dot.isLeaf())
                    CommandManager.execute(DeleteSingleDotAction(dot))
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

    class DeleteSingleDotAction(val dot: Dot, override val name: String = "delete-single") : Action {
        lateinit var parent: Dot

        override fun execute() {
            parent = dot.neighbors().first()
            dot.removeFromChain()
        }

        override fun undo() {
            with(parent.chain) {
                addDot(dot)
                connect(dot, parent)
            }
        }
    }
}