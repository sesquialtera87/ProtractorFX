package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import org.mth.protractorfx.log.LogFactory
import java.util.logging.Logger

/**
 * @param neighbor1 The first vertex of the oriented angle
 * @param neighbor2 The second vertex of the oriented angle
 */
data class AngleDecorator(val neighbor1: Dot, val neighbor2: Dot) {

    inner class MeasureLabel : StackPane() {
        private val label = Text()
        private val background = Rectangle()
        val userDragTranslation: Translate = Transform.translate(.0, .0)

        var text: String by label::text
        var fill: Paint by label::fill
        var font: Font by label::font

        init {
            label.layoutX = 0.0
            label.layoutY = 0.0
            label.layoutBoundsProperty().addListener { _, _, bounds ->
                background.width = bounds.width
                background.height = bounds.height
            }

            background.layoutX = 0.0
            background.layoutY = 0.0
            background.fill = Color.LIGHTGRAY

            children.addAll(background, label)

            DragSupport(this)
        }

        fun hideBackground(visible: Boolean) {
            if (visible) background.fill = Color.LIGHTGRAY
            else background.fill = Color.TRANSPARENT
        }

        inner class DragSupport(label: MeasureLabel) {

            private var anchorPoint = Point2D(.0, .0)
            private var oldDragTranslation = Point2D(.0, .0)
            private var dr = Point2D(.0, .0)

            init {
                label.setOnMousePressed { event ->
                    anchorPoint = Point2D(event.screenX, event.screenY)
                    oldDragTranslation = Point2D(userDragTranslation.x, userDragTranslation.y)
                    event.consume()

                    log.finest("Mouse pressed. \n\tAnchor point = $anchorPoint \n\tOld translation vector = $oldDragTranslation")
                }

                label.setOnMouseDragged {
                    val currentDragPoint = Point2D(it.screenX, it.screenY)

                    // calculate the delta from the anchor point
                    dr = currentDragPoint.subtract(anchorPoint).add(oldDragTranslation)

                    userDragTranslation.x = dr.x
                    userDragTranslation.y = dr.y

                    log.finest("Translation vector = $dr")
                }
            }
        }
    }

    /**
     * The label displaying the angle measure
     */
    private val angleLabel = MeasureLabel()
    private val arc: Arc = Arc()

    fun build(dot: Dot, pane: Pane) {
        // create the arc
        arc.apply {
            centerXProperty().bind(dot.centerXProperty())
            centerYProperty().bind(dot.centerYProperty())
            radiusX = 20.0
            radiusY = 20.0
            type = ArcType.ROUND
            fill = Color.TRANSPARENT
            stroke = Color.DARKGREEN
            strokeWidth = 1.5
            length = getMeasure()
            startAngle = getInitialAngle()
        }

        angleLabel.apply {
            layoutXProperty().bind(dot.centerXProperty())
            layoutYProperty().bind(dot.centerYProperty())
            text = "%.${ANGLE_LABEL_PRECISION}f".format(getMeasure())
            isVisible = true
            fill = dot.chain.measureLabelFontColor
            font = Font.font(font.name, dot.chain.measureLabelFontSize)

            hideBackground(dot.chain.measureLabelBackgroundVisibility)
        }

        angleLabel.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                // show the label context-menu
//                MeasureLabelPopup.show(angleLabel, dot, it.screenX, it.screenY)
                MeasureLabelMenu.Companion.show(angleLabel, dot, it.screenX, it.screenY)
            }
        }

        // listen to changes of the font color
        dot.chain.measureLabelFontColorProperty.addListener { _, _, color ->
            angleLabel.fill = color
        }

        // listen to changes of the font size
        dot.chain.measureLabelFontSizeProperty.addListener { _, _, size ->
            angleLabel.font = Font.font(
                angleLabel.font.name,
                dot.chain.measureLabelFontWeight,
                size.toDouble()
            )
        }

        // listen to changes of the font weight
        dot.chain.measureLabelFontWeightProperty.addListener { _, _, weight ->
            angleLabel.font = Font.font(
                angleLabel.font.name,
                weight,
                dot.chain.measureLabelFontSize
            )
        }

        // listen to changes of the background visibility
        dot.chain.measureLabelBackgroundVisibilityProperty.addListener { _, _, visible ->
            angleLabel.hideBackground(visible)
        }

        // add the nodes to the Pane and move them to background
        pane.children.addAll(arc, angleLabel)

        arc.toBack()
        angleLabel.toBack()

        updateAngleLabelPosition()
    }

    fun resetLabelPosition() {
        with(angleLabel.userDragTranslation) {
            x = 0.0
            y = 0.0
        }
    }

    @Suppress("LocalVariableName")
    private fun updateAngleLabelPosition() {
        // put the center of symmetry of the label on the bisector of the angle
        val alpha = -Math.toRadians(getMeasure()) / 2

        val bounds = angleLabel.boundsInParent

        // translate to the center of symmetry of the rectangle
        val W = Point2D(-bounds.width / 2, -(bounds.height + 4) / 2)

        // direction vector of the first side of the angle
        var T = neighbor1.getCenter().subtract(arc.getCenter()).normalize()

        // rescale and rotate the direction vector to outdistance the label from the center of the angle
        T = T.multiply(43.0).rotate(alpha)

        angleLabel.apply {
            transforms.clear()
            transforms.addAll(
                Transform.translate(W.x, W.y),
                Transform.translate(T.x, T.y),
                userDragTranslation
            )
        }
    }

    fun update() {
        // the updated angle measure
        val angleMeasure = getMeasure()

        // update arc properties
        arc.apply {
            length = angleMeasure
            startAngle = getInitialAngle()
        }

        // update label position and text
        if (angleLabel.isVisible) {
            angleLabel.text = "%.${ANGLE_LABEL_PRECISION}f°".format(angleMeasure)

            updateAngleLabelPosition()
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is AngleDecorator)
            false
        else {
            other.neighbor1 == neighbor1 && other.neighbor2 == neighbor2
        }
    }

    override fun hashCode(): Int {
        var result = neighbor1.hashCode()
        result = 31 * result + neighbor2.hashCode()
        result = 31 * result + arc.hashCode()
        return result
    }

    private fun getMeasure(): Double {
        val p1 = neighbor1.getCenter()
        val p2 = neighbor2.getCenter()
        val arcCenter = arc.getCenter()

        return angleBetween(p2.subtract(arcCenter), p1.subtract(arcCenter), degree = true)
    }

    private fun getInitialAngle(): Double {
        val p1 = neighbor1.getCenter()
        val arcCenter = arc.getCenter()

        return angleBetween(p1.subtract(arcCenter), Point2D(1.0, 0.0), degree = true)
    }

    companion object {
        private val log: Logger = LogFactory.configureLog(AngleDecorator::class.java)
    }
}