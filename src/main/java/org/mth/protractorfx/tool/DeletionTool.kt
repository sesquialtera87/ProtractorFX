package org.mth.protractorfx.tool

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.*
import org.mth.protractorfx.command.Action

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
                    execute(DeleteSingleDotAction(dot))
                break
            }
        }
    }

    fun deleteSelection() {
        execute(DeleteSelectedDotsAction())
    }

    /**
     * Removes the nodes actually selected.
     */
    class DeleteSelectedDotsAction(override val name: String = "delete-selection") : Action {

        data class DotPair(val parent: Dot, val leaf: Dot)

        /**
         * A list containing the deleted nodes, in order of deletion
         */
        val deletionList = ArrayList<DotPair>()

        override fun execute(): Boolean {
            var leaves = Selection.selectedDots().filter { it.isLeaf() }

            while (leaves.isNotEmpty()) {
                leaves.forEach {
                    deletionList.add(DotPair(
                        parent = it.neighbors().first(), // it's a leaf, only one neighborhood
                        leaf = it
                    ))
                    it.removeFromChain()
                    Selection.unselect(it)
                }

                leaves = Selection.selectedDots().filter { it.isLeaf() }
            }

            Selection.clear()
            deletionList.trimToSize() // todo remove???

            return true
        }

        override fun undo() {
            deletionList.forEach {
                val (parent, dot) = it

                with(parent.chain) {
                    addDot(dot)
                    connect(dot, parent)
                }
            }
        }
    }

    private class DeleteSingleDotAction(val dot: Dot, override val name: String = "delete-single") : Action {
        lateinit var parent: Dot

        override fun execute(): Boolean {
            // todo fix single-dot case
            parent = dot.neighbors().first()
            dot.removeFromChain()

            return true
        }

        override fun undo() {
            with(parent.chain) {
                addDot(dot)
                connect(dot, parent)
            }
        }
    }
}