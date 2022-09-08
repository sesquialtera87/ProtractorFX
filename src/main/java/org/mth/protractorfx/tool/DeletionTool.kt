package org.mth.protractorfx.tool

import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.*
import org.mth.protractorfx.command.Action
import java.util.*

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

    /**
     * Removes the nodes actually selected.
     */
    class DeleteSelectedDotsAction(override val name: String = "delete-selection") : Action {

        data class DotPair(val parent: Optional<Dot>, val leaf: Dot)

        /**
         * A list containing the deleted nodes, in order of deletion
         */
        private val deletionList = Stack<DotPair>()

        override fun execute(): Boolean {
            var leaves = Selection.selectedDots().filter { it.isLeaf() }

            while (leaves.isNotEmpty()) {
                leaves.forEach { dot ->
                    val neighbors = dot.neighbors()

                    if (neighbors.isNotEmpty())
                        deletionList.push(DotPair(
                            parent = Optional.of(neighbors.first()), // it's a leaf, only one neighborhood
                            leaf = dot
                        ))
                    else deletionList.push(DotPair(parent = Optional.empty(), leaf = dot))

                    dot.removeFromChain()
                    Selection.unselect(dot)
                }

                leaves = Selection.selectedDots().filter { it.isLeaf() }
            }

            Selection.clear()

            return true
        }

        override fun undo() {
            deletionList.forEach { dotPair ->
                val (parent, dot) = dotPair

                parent.ifPresentOrElse({
                    it.chain.run {
                        addDot(dot)
                        connect(dot, it)
                    }
                }, {
                    val dotChain = dot.chain
                    chains.add(dotChain)
                    dotChain.addDot(dot)
                })
            }
        }
    }

    private class DeleteSingleDotAction(val dot: Dot, override val name: String = "delete-single") : Action {
        var parent: Dot? = null

        override fun execute(): Boolean {
            val neighbors = dot.neighbors()

            // if it is not an isolated node, then set the parent node
            if (neighbors.isNotEmpty())
                parent = neighbors.first()

            dot.removeFromChain()

            return true
        }

        override fun undo() {
            if (parent == null) {

            } else
                with(parent!!.chain) {
                    addDot(dot)
                    connect(dot, parent!!)
                }
        }
    }
}