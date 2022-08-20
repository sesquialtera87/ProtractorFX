package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle


object SelectionRectangle : Rectangle() {

    /**
     * Coordinates of the mouse click point, at which the selection event started
     */
    private var selectionAnchor = Point2D(.0, .0)

    init {
        fill = Color.TRANSPARENT
        arcWidth = 10.0
        arcHeight = 10.0
        stroke = Color.ALICEBLUE
        isVisible = false
    }

    /**
     * Starts a selection process. Set the selection anchor point to the mouse-event coordinates and show the selection area
     */
    fun show(evt: MouseEvent) {
        selectionAnchor = Point2D(evt.x, evt.y)

        x = evt.x
        y = evt.y

        // reset the area size
        width = 0.0
        height = 0.0

        isVisible = true
    }

    /**
     * Update the rectangle dimensions based on the mouse coordinates.
     * This method has to be called after the start of the selection event and during the dragging time.
     * @param evt The mouse event obtained by a drag listener
     */
    fun updateSelectionShape(evt: MouseEvent) {
        if (evt.x >= selectionAnchor.x) {
            width = evt.x - selectionAnchor.x
        } else {
            x = evt.x
            width = selectionAnchor.x - evt.x
        }

        if (evt.y >= selectionAnchor.y)
            height = evt.y - selectionAnchor.y
        else {
            y = evt.y
            height = selectionAnchor.y - evt.y
        }
    }

    /**
     * Check weather or not the [dot] is located inside the selection rectangle
     */
    fun isDotInSelection(dot: Dot) =
        dot.centerX + dot.radius > x
                && dot.centerX - dot.radius < x + width
                && dot.centerY + dot.radius > y
                && dot.centerY - dot.radius < y + height
}