package org.mth.protractorfx.tool

import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.CURSOR_INSERT_DOT
import org.mth.protractorfx.Dot
import org.mth.protractorfx.Selection
import org.mth.protractorfx.log.LogFactory
import org.mth.protractorfx.pane
import java.util.logging.Logger

object InsertionTool : AbstractTool() {

    val log: Logger = LogFactory.configureLog(InsertionTool::class.java)

    override val cursor: Cursor
        get() = CURSOR_INSERT_DOT

    override val shortcut: KeyCodeCombination
        get() = KeyCodeCombination(KeyCode.A)

    override fun onRelease(mouseEvent: MouseEvent) {
        /* the mouse event comes from a listener attached to the scene, so we need a correction in the coordinates, because
        the mouse event x and y coordinates essentially equals the screen coordinates, and not the coordinates relative to the
        pane container
        */
        addNewDot(
            x = mouseEvent.sceneX - pane.boundsInParent.minX,
            y = mouseEvent.sceneY - pane.boundsInParent.minY
        )
    }

    override fun activate() {
        if (Selection.isEmpty().not())
            super.activate()
        else
            log.finest("Cannot activate the insertion tool, because no dot is selected")
    }

    /**
     * Add a new dot at the specified coordinates
     */
    private fun addNewDot(x: Double, y: Double) {
        val selectedDot = Selection.selectedDot()
        println(selectedDot.isPresent)
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
}