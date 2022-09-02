package org.mth.protractorfx

import org.mth.protractorfx.log.LogFactory

object GlobalKeyListener {
    val log = LogFactory.configureLog(GlobalKeyListener::class.java)

    fun install() {

        scene.setOnKeyPressed {
//            log.finest("Key pressed on scene: $it")

            if (MouseCoordinateLabel.combination.match(it)) {
                showMouseCoordinates.value = true
            }
        }

        scene.setOnKeyReleased {
//            log.finest("Key released on scene: $it")

            if (MouseCoordinateLabel.combination.match(it)) {
                showMouseCoordinates.value = false
            }
        }

        scene.setOnKeyTyped {
//            log.finest("Key typed on scene: $it")
        }
    }
}