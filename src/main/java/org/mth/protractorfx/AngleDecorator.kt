package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import org.mth.protractorfx.log.LogFactory
import org.mth.protractorfx.tool.MeasureUnit.*
import java.util.logging.Logger
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

/**
 * @param neighbor1 The first vertex of the oriented angle
 * @param neighbor2 The second vertex of the oriented angle
 */
data class AngleDecorator(val neighbor1: Dot, val neighbor2: Dot) {

    inner class MeasureLabel : StackPane() {

        /**
         * [Text] component that displays the measure of the angle
         */
        private val label = Text()

        /**
         * The [Rectangle] used only for coloring the text background
         */
        private val background = Rectangle()

        /**
         * The displacement of the label from the default position, due to user dragging
         * @see T
         */
        val dragTranslation: Translate = Transform.translate(.0, .0)


        /**
         * Angle between the default position vector [T] and the custom displacement, measured in radians
         */
        var dragToTranslationAngle: Double = 0.0

        /**
         * The vector representing the displacement of the measure label from the center of the [arc]
         */
        var T = Point2D(.0, .0)


        var backgroundColor: Paint by background::fill
        var text: String by label::text
        var fill: Paint by label::fill
        var font: Font by label::font

        init {
            label.layoutBoundsProperty().addListener { _, _, bounds ->
                background.width = bounds.width + 4
                background.height = bounds.height + 3
            }

            with(background) {
                arcWidth = 5.0
                arcHeight = 5.0
                fill = Color.BISQUE
            }

            children.addAll(background, label)

            DragSupport(this)
        }

        fun hideBackground(visible: Boolean) {
            if (visible) background.fill = Color.LIGHTGRAY
            else background.fill = Color.TRANSPARENT
        }

        inner class DragSupport(label: MeasureLabel) {

            /**
             * The drag event start point
             */
            private var anchorPoint = Point2D(.0, .0)

            /**
             * The drag displacement before a new drag event
             */
            private var oldDragTranslation = Point2D(.0, .0)
            private var dr = Point2D(.0, .0)

            init {
                label.setOnMousePressed { event ->
                    anchorPoint = Point2D(event.screenX, event.screenY)
                    oldDragTranslation = Point2D(dragTranslation.x, dragTranslation.y)
                    event.consume()

                    log.finest("Mouse pressed. \n\tAnchor point = $anchorPoint \n\tOld translation vector = $oldDragTranslation")
                }

                label.setOnMouseDragged {
                    val currentDragPoint = Point2D(it.screenX, it.screenY)

                    // calculate the delta from the anchor point
                    dr = currentDragPoint.subtract(anchorPoint).add(oldDragTranslation)

                    dragTranslation.x = dr.x
                    dragTranslation.y = dr.y

                    // update the angle
                    dragToTranslationAngle = angleBetween(dr, T, MEASURE_UNIT)

                    it.consume()
                }
            }
        }
    }

    /**
     * The label displaying the angle measure
     */
    val angleLabel = MeasureLabel()
    private val arc: Arc = Arc()

    val chain get() = neighbor1.chain

    private val vectorLine = Line()
    private val dragVector = Line()

    fun dispose(pane: Pane) {
        listOf(
            vectorLine,
            dragVector,
            arc,
            angleLabel
        ).forEach { pane.children.remove(it) }
    }

    fun build(dot: Dot, pane: Pane) {

        // add the nodes to the Pane and move them to background
        pane.children.addAll(arc, angleLabel, vectorLine, dragVector)

        with(vectorLine) {
            startXProperty().bind(dot.centerXProperty())
            startYProperty().bind(dot.centerYProperty())
            stroke = Color.RED
            isVisible = false
        }

        with(dragVector) {
            startXProperty().bind(dot.centerXProperty())
            startYProperty().bind(dot.centerYProperty())
            stroke = Color.CORAL
            isVisible = false
        }


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
            text = format(getMeasure())
            isVisible = true

            // initialize appearance properties
            fill = chain.measureLabelFontColor
            font = Font.font(font.name, chain.measureLabelFontSize)
            backgroundColor = chain.measureLabelBackgroundColor

            relocate(dot.centerX, dot.centerY)
            layoutXProperty().bind(dot.centerXProperty())
            layoutYProperty().bind(dot.centerYProperty())

            hideBackground(chain.measureLabelBackgroundVisibility)
        }

        angleLabel.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                // show the label context-menu
                MeasureLabelMenu.show(angleLabel, dot, it.screenX, it.screenY)
                it.consume()
            }
        }

        // listen to changes of the font color
        chain.measureLabelFontColorProperty.addListener { _, _, color ->
            angleLabel.fill = color
        }

        // listen to changes of the font size
        chain.measureLabelFontSizeProperty.addListener { _, _, size ->
            angleLabel.font = Font.font(
                angleLabel.font.name,
                chain.measureLabelFontWeight,
                size.toDouble()
            )
        }

        // listen to changes of the font weight
        chain.measureLabelFontWeightProperty.addListener { _, _, weight ->
            angleLabel.font = Font.font(
                angleLabel.font.name,
                weight,
                chain.measureLabelFontSize
            )
        }

        // listen to changes of the background visibility
        chain.measureLabelBackgroundVisibilityProperty.addListener { _, _, visible ->
            angleLabel.hideBackground(visible)
        }

        // listen to changes of the background color
        chain.measureLabelBackgroundColorProperty.addListener { _, _, color ->
            angleLabel.backgroundColor = color
        }

        // listen to changes of the measure unit
        measureUnitProperty.addListener { _, _, unit ->

        }

        arc.toBack()
        angleLabel.toBack()

        updateAngleLabelPosition()
    }

    fun resetLabelPosition() {
        with(angleLabel.dragTranslation) {
            x = 0.0
            y = 0.0
        }
    }

    @Suppress("LocalVariableName")
    private fun updateAngleLabelPosition() {
        with(angleLabel) {
            // put the center of symmetry of the label on the bisector of the angle
            val alpha = -Math.toRadians(getMeasure()) / 2

            val bounds = angleLabel.boundsInParent

            // translate to the center of symmetry of the rectangle
            val W = Point2D(-bounds.width / 2, -(bounds.height) / 2)

            // normalized direction vector of the first side of the angle
            T = neighbor1.getCenter()
                .subtract(arc.getCenter())
                .normalize()

            // rescale and rotate the direction vector to outdistance the label from the center of the angle
            T = T.multiply(45.0).rotate(alpha)


            // recalculate the drag-vector position to preserve the angle with the translation vector
            val dragVectorMagnitude = sqrt(dragTranslation.x.pow(2) + dragTranslation.y.pow(2))
            val d = T.normalize()
                .rotate(-dragToTranslationAngle)
                .multiply(dragVectorMagnitude)

            dragTranslation.x = d.x
            dragTranslation.y = d.y

            transforms.clear()
            transforms.addAll(
                Transform.translate(W.x, W.y),
                Transform.translate(T.x, T.y),
                dragTranslation
            )

            vectorLine.apply {
                endX = T.x + arc.centerX
                endY = T.y + arc.centerY
            }

            dragVector.apply {
                endX = d.x + arc.centerX
//            endX = angleLabel.userDragTranslation.x + arc.centerX
                endY = d.y + arc.centerY
//            endY = angleLabel.userDragTranslation.y + arc.centerY
            }
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
            angleLabel.text = format(angleMeasure)

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

    /**
     * Calculate the measure of the angle in **degrees**
     */
    private fun getMeasure(): Double {
        val p1 = neighbor1.getCenter()
        val p2 = neighbor2.getCenter()
        val arcCenter = arc.getCenter()

        return angleBetween(p2.subtract(arcCenter), p1.subtract(arcCenter), DECIMAL_DEGREE)
    }

    private fun getInitialAngle(): Double {
        val p1 = neighbor1.getCenter()
        val arcCenter = arc.getCenter()

        return angleBetween(p1.subtract(arcCenter), Point2D(1.0, 0.0), DECIMAL_DEGREE)
    }

    companion object {
        private val log: Logger = LogFactory.configureLog(AngleDecorator::class.java)

        private val DECIMAL_DEGREE_TEMPLATE = "%.${ANGLE_LABEL_PRECISION}f°"
        private val RADIANS_TEMPLATE = "%.${ANGLE_LABEL_PRECISION}f"
        private const val SEXAGESIMAL_DEGREE_TEMPLATE = "%d° %02d' %02d''"
        private val CENTESIMAL_DEGREE_TEMPLATE = "%d° %02d' %02d''"

        /**
         * Format the given measure
         * @param angle The measure in **degrees**
         */
        fun format(angle: Double): String {
            return when (MEASURE_UNIT) {
                RADIANS -> RADIANS_TEMPLATE.format(Math.toRadians(angle))
                SEXAGESIMAL_DEGREES -> {
                    val S = round(angle * 3600).toInt()
                    val seconds = S % 60
                    val minutes = (S / 60) % 60
                    val degrees = (S / 3600)

                    SEXAGESIMAL_DEGREE_TEMPLATE.format(degrees, minutes, seconds)
                }
                DECIMAL_DEGREE -> DECIMAL_DEGREE_TEMPLATE.format(angle)
                CENTESIMAL_DEGREE -> {
                    val S = round(angle * 10 / 9 * 10000).toInt()
                    val seconds = S % 100
                    val minutes = (S / 100) % 100
                    val degrees = (S / 10000)

                    CENTESIMAL_DEGREE_TEMPLATE.format(degrees, minutes, seconds)
                }
            }
        }
    }
}