package org.mth.protractorfx.command

import org.mth.protractorfx.log.LogFactory
import java.util.*
import java.util.function.Consumer


object CommandManager {

    private val log = LogFactory.configureLog(CommandManager::class.java)
    private val queueStackNormal: QueueStack<List<Action>> = QueueStack()

    private val queueStackReverse: QueueStack<List<Action>> = QueueStack()

    var MAX_HISTORY_SIZE = 100
    val actionHistory = LinkedList<Action>()

    fun execute(vararg actions: Action) {
        actions.forEach {
            if (it.execute()) {
                log.fine("Action ${it.name} successfully executed. Pushing it to history")
                actionHistory.push(it)

                if (actionHistory.size > MAX_HISTORY_SIZE) {
                    val action = actionHistory.removeLast()
                    log.fine("The maximum numbero of actions that can be stored has been reached. Deleting the oldest: ${action.name}")
                }
            }
        }
    }

    fun undo() {
        if (actionHistory.isNotEmpty()) {
            val action = actionHistory.pop()
            action.undo()
            log.fine("Undoing " + action.name)
            return
        }
    }

    fun redo() {
        val optionalActions = queueStackReverse.pop()
        optionalActions.ifPresent { aList: List<Action> ->
            aList.forEach(Consumer { obj: Action -> obj.execute() })
            queueStackNormal.push(aList)
//            aList.forEach(Consumer { a: Action ->
//                actionHistory.add(a.name + " - redo")
//            })
        }
    }

    fun clearNormal() {
        queueStackNormal.clear()
    }

    fun clearReverse() {
        queueStackReverse.clear()
    }

}