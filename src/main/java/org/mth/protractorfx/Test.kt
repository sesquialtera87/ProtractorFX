package org.mth.protractorfx

import javafx.application.Platform
import javafx.geometry.Point2D
import org.mth.protractorfx.tool.MeasureUnit
import kotlin.math.PI


fun test() {
    val p1 = Point2D(10.0, 0.0)
    val p2 = Point2D(0.0, -20.0)
    println(angleBetween(p1, p2, MeasureUnit.DECIMAL_DEGREE, true))
    println(p1.orthogonal())
//    Platform.exit()
}
