package org.mth.protractorfx

import javafx.scene.input.KeyEvent
import org.mth.protractorfx.log.LogFactory

object GlobalKeyListener {
    val log = LogFactory.configureLog(GlobalKeyListener::class.java)

    fun install() {
        scene.setOnTouchPressed {
            log.finest("Key pressed on scene: $it")
        }

        scene.setOnKeyReleased {
            log.finest("Key released on scene: $it")
        }

        scene.setOnKeyTyped {
            log.finest("Key typed on scene: $it")
        }
    }
}