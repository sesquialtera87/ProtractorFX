package org.mth.protractorfx.command

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.stage.Popup
import javafx.util.Callback

object CommandPopup : Popup() {

    init {
        isAutoHide = true

        val listView = ListView<Action>()
        listView.prefWidth = 150.0
        listView.prefHeight = 200.0
        listView.cellFactory = Callback {
            object : ListCell<Action>() {
                override fun updateItem(item: Action?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (item != null && !empty) {
                        text = item.name
                    } else
                        text = null
                }
            }
        }
        listView.setOnMouseClicked {
            if (it.clickCount == 2) {
                if (listView.selectionModel.selectedIndex == 0) {
                    CommandManager.undo()
                    hide()
                }
            }
        }
        content.add(listView)

        setOnShowing {
            listView.items.clear()
            listView.items.addAll(CommandManager.actionHistory)
        }
    }

}