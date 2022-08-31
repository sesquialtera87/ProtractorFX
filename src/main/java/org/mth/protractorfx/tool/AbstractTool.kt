package org.mth.protractorfx.tool

import javafx.scene.Cursor
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent

// todo comment
abstract class AbstractTool {

    var active: Boolean = false
    abstract val cursor: Cursor
    abstract val shortcut: KeyCodeCombination

    open fun activate() {
        active = true
        Tools.pane.cursor = cursor
    }

    open fun deactivate() {
        active = false
        Tools.pane.cursor = Cursor.DEFAULT
    }

    open fun onPress(mouseEvent: MouseEvent) {}

    open fun onDrag(mouseEvent: MouseEvent) {}

    open fun onRelease(mouseEvent: MouseEvent) {}
}