package org.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.*
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import org.mth.protractorfx.log.LogFactory
import org.mth.protractorfx.tool.MeasureUnit.*
import java.util.logging.Logger
import kotlin.math.*

data class AngleDecorator(val angle: Angle) {

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
         * Angle between the default position vector [T] and the custom displacement, measured in **radians**
         */
        var dragToTranslationAngle: Double = 0.0

        /**
         * The vector representing the displacement of the measure label from the center of the [arc].
         * T = aB+W
         */
        var T = Point2D(.0, .0)

        /**
         * The magnitude of the bisector vector.
         * @see B
         */
        var a: Double = 1.0

        /**
         * The radius of the circumscribed circle to the label
         */
        var R: Double = 0.0

        /**
         * The bisector of the angle. This vector has norm 1.
         */
        var B = Point2D(.0, .0)

        /**
         * The vector that from tol-left corner of the label, to its center of symmetry.
         * @see T
         */
        var W = Point2D(.0, .0)


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
            if (visible) background.fill = Color.LIGHTGRAY // todo ??
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
                    oldDragTranslation = dragTranslation.toVector()
                    event.consume()

                    log.finest("Mouse pressed. \n\tAnchor point = $anchorPoint \n\tOld translation vector = $oldDragTranslation")
                }

                label.setOnMouseDragged {
                    val currentDragPoint = Point2D(it.screenX, it.screenY)

                    // calculate the delta from the anchor point
                    dr = (currentDragPoint sub anchorPoint) sum oldDragTranslation

                    dragTranslation.set(dr)

                    // update the angle
                    dragToTranslationAngle = angleBetween(dr, T, RADIANS)

                    updateDragLines()

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

    val chain get() = angle.vertex.chain

    private val vectorLine = Line()
    private val dragVector = Line()
    private val circle = Circle()


    /**
     * Remove all nodes belonging to this decorator from the [Pane]
     */
    fun dispose(pane: Pane) {
        listOf(
            vectorLine,
            dragVector,
            arc,
            angleLabel
        ).forEach { pane.children.remove(it) }
    }

    fun containsDot(dot: Dot, asVertex: Boolean = false): Boolean {
        return if (asVertex)
            angle.vertex == dot
        else
            angle.extreme1 == dot || angle.extreme2 == dot
    }

    fun build() { // todo remove and put in constructor
        val vertex = angle.vertex

        // add the nodes to the Pane and move them to background
        pane.children.addAll(
//            circle,
            arc,
            angleLabel,
//            dragVector,
//            vectorLine,
        )

        with(circle) {
            stroke = Color.PINK
            fill = Color.CORAL
            isVisible = false
        }

        with(vectorLine) {
            startXProperty().bind(vertex.centerXProperty())
            startYProperty().bind(vertex.centerYProperty())
            stroke = Color.RED
//            isVisible = false
        }

        with(dragVector) {
            startXProperty().bind(vertex.centerXProperty())
            startYProperty().bind(vertex.centerYProperty())
            stroke = Color.CORAL
//            isVisible = false
        }


        // create the arc
        arc.apply {
            centerXProperty().bind(vertex.centerXProperty())
            centerYProperty().bind(vertex.centerYProperty())
            radiusX = 20.0
            radiusY = 20.0
            type = ArcType.ROUND
            fill = Color.TRANSPARENT
            stroke = Color.DARKGREEN
            strokeWidth = 1.5
            length = angle.measure()
            startAngle = getInitialAngle()
        }

        angleLabel.apply {
            text = format(angle.measure())
            isVisible = true

            // initialize appearance properties
            fill = chain.measureLabelFontColor
            font = Font.font(font.name, chain.measureLabelFontSize)
            backgroundColor = chain.measureLabelBackgroundColor

            layoutXProperty().bind(vertex.centerXProperty())
            layoutYProperty().bind(vertex.centerYProperty())

            hideBackground(chain.measureLabelBackgroundVisibility)
        }

        angleLabel.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                // show the label context-menu
                MeasureLabelMenu.show(angleLabel, vertex, it.screenX, it.screenY)
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

        arc.toBack()
        angleLabel.toBack()

        updateLabelPosition()
    }

    fun resetLabelPosition() {
        with(angleLabel) {
            dragTranslation.x = .0
            dragTranslation.y = .0
        }

        updateLabelPosition()
    }

    private fun isLabelPositionCustomized() = angleLabel.dragTranslation.toVector() != Point2D(.0, .0)

    @Suppress("LocalVariableName")
    fun updateLabelPosition() {
        with(angleLabel) {
            val bounds = boundsInParent
            val angleMeasure = angle.measure()

            println(boundsInLocal)
            println(boundsInParent)
            println(layoutBounds)

            // the normal vector to the first side of the angle, pointing to the interior of the angle (the -1)
            val N1 = (angle.L1 sub angle.C).orthogonal(-1)

            // translate to the center of symmetry of the rectangle
            W = Point2D(-bounds.width / 2, -bounds.height / 2)

            B = angle.bisector()

            /* for angle less than 90° correct the position (the minimum space is guaranteed by the circumscribed
            circle to the measure label */
            if (angleMeasure < 90.0 && !isLabelPositionCustomized()) {
                R = 0.5 * sqrt(bounds.width.pow(2) + bounds.height.pow(2)) + 3

                a = max(R / (B dot N1), 46.0)
                a = min(110.0, a)
            } else {
                a = 46.0
            }

            T = B.multiply(a) sum W

            // recalculate the drag-vector position to preserve the angle with the translation vector
            val dragVectorMagnitude = dragTranslation.toVector().magnitude()
            val d = T.normalize()
                .rotate(-dragToTranslationAngle)
                .multiply(dragVectorMagnitude)

            dragTranslation.set(d)

            transforms.clear()
            transforms.addAll(
                Transform.translate(T.x, T.y),
                dragTranslation
            )

            updateDragLines()
        }
    }

    private fun updateDragLines() {
        with(angleLabel) {
            circle.apply {
                centerX = T.x - W.x + arc.centerX
                centerY = T.y - W.y + arc.centerY
                radius = R
                angleLabel.toFront()
            }

            vectorLine.apply {
                endX = T.x + arc.centerX
                endY = T.y + arc.centerY
            }
        }

        dragVector.apply {
            endX = angleLabel.dragTranslation.x + arc.centerX
            endY = angleLabel.dragTranslation.y + arc.centerY
        }
    }


    fun update() {
        // the updated angle measure
        val angleMeasure = angle.measure()

        // update arc properties
        arc.apply {
            length = angleMeasure
            startAngle = getInitialAngle()
        }

        // update label position and text
        if (angleLabel.isVisible) {
            angleLabel.text = format(angleMeasure)

            updateLabelPosition()
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is AngleDecorator)
            false
        else {
            other.angle.extreme1 == angle.extreme1 &&
                    other.angle.extreme2 == angle.extreme2
                    && other.angle.vertex == angle.vertex
        }
    }

    override fun hashCode(): Int {
        var result = angle.hashCode()
        result = 31 * result + arc.hashCode()
        return result
    }

    private fun getInitialAngle() = angleBetween(angle.L1, Point2D(1.0, 0.0))

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