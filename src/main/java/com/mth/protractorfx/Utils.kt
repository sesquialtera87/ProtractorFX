package com.mth.protractorfx

import javafx.geometry.Point2D
import javafx.scene.ImageCursor
import javafx.scene.image.Image
import java.io.File
import kotlin.math.atan2

@JvmField
val CURSOR_ANGLE = ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("angle_x32.png")))

@JvmField
val SNAPSHOT_DIR = File("C:\\Users\\matti\\OneDrive\\Documenti\\Java\\ProtractorFX\\src\\main\\resources\\snapshot")

fun angleBetween(p1: Point2D, p2: Point2D, degree: Boolean = false): Double {
    val dot = p1.x * p2.x + p1.y * p2.y
    val det = p1.x * p2.y - p2.x * p1.y
    var angle = atan2(det, dot)

    if (degree) {
        // converto to degree
        angle = Math.toDegrees(angle)

        // put angle measure in range [0,360]
        if (angle < 0)
            angle += 360
    }

    return angle
}