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
    var processActive = false

    init {
        fill = Color.TRANSPARENT
        arcWidth = 10.0
        arcHeight = 10.0
        stroke = Color.ALICEBLUE
        isVisible = false
    }

    /**
     * Starts a selection process. Set the selection anchor point to the mouse-event coordinates and show the selection area.
     * The call to this method reset the bounds of the selection area, so generally you have to call it at the start of the
     * selection process, in order to not lose the last selection bounds.
     */
    fun startSelection(evt: MouseEvent) {
        selectionAnchor = Point2D(evt.x, evt.y)

        x = evt.x
        y = evt.y

        // reset the area size
        width = 0.0
        height = 0.0

        processActive = true
    }

    /**
     * Update the rectangle dimensions based on the mouse coordinates.
     * This method has to be called after the start of the selection event and during the dragging time.
     * @param evt The mouse event obtained by a drag listener
     */
    fun updateSelectionShape(evt: MouseEvent) {
        isVisible = true

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
     * Stop the selection process and hide the selection area, maintaining the area bounds for later use.
     * @see startSelection
     * @see updateSelectionShape
     */
    fun stopSelection() {
        processActive = false
        isVisible = false
    }

    /**
     * Check weather or not the [dot] is located inside the selection rectangle. After the completion of the selection process,
     * the bounds of the rectangle aren't reset, so we can test the dot membership based on the last selection process.
     */
    fun isDotInSelection(dot: Dot) =
        dot.intersects(boundsInParent)
//        dot.centerX + dot.radius > x
//                && dot.centerX - dot.radius < x + width
//                && dot.centerY + dot.radius > y
//                && dot.centerY - dot.radius < y + height
}