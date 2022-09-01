package org.mth.protractorfx.tool

import javafx.scene.Cursor
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import org.mth.protractorfx.*

object SelectionTool : AbstractTool() {

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