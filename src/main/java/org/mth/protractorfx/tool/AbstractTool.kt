package org.mth.protractorfx.tool

import javafx.scene.Cursor
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.pane


abstract class AbstractTool {

    /**
     * Flag indicating the state of the tool
     */
    var active: Boolean = false

    /**
     * The [Cursor] that has to be shown when the tool is activated
     */
    abstract val cursor: Cursor

    /**
     * The shortcut used to activate the tool
     */
    abstract val shortcut: KeyCodeCombination

    /**
     * Activate the tool
     */
    open fun activate() {
        active = true
        pane.cursor = cursor
    }

    /**
     * Deactivate the tool, restoring the default cursor
     */
    open fun deactivate() {
        active = false
        pane.cursor = Cursor.DEFAULT
    }

    open fun onPress(mouseEvent: MouseEvent) {}

    open fun onDrag(mouseEvent: MouseEvent) {}

    open fun onRelease(mouseEvent: MouseEvent) {}
}