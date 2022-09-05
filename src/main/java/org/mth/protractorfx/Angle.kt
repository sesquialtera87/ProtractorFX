package org.mth.protractorfx

import javafx.geometry.Point2D
import org.mth.protractorfx.tool.MeasureUnit
import org.mth.protractorfx.tool.MeasureUnit.DECIMAL_DEGREE
import org.mth.protractorfx.tool.MeasureUnit.RADIANS

data class Angle(
    val vertex: Dot,
    val extreme1: Dot,
    val extreme2: Dot,
) {
    val L1 get() = extreme1.getCenter() sub vertex.getCenter()

    val L2 get() = extreme2.getCenter() sub vertex.getCenter()

    val C: Point2D get() = vertex.getCenter()

    fun bisector(): Point2D {
        val alpha = -measure(RADIANS) / 2
        return L1.normalize().rotate(alpha)
    }

    fun measure(unit: MeasureUnit = DECIMAL_DEGREE) = angleBetween(L2, L1, unit)

}
