package org.mth.protractorfx.command

import java.util.*
import java.util.function.Consumer


object CommandManager {

    private val queueStackNormal: QueueStack<List<Action>>
    private val queueStackReverse: QueueStack<List<Action>>

    val actionHistory = LinkedList<Action>()

    init {
        queueStackNormal = QueueStack()
        queueStackReverse = QueueStack()
    }

    fun execute(vararg actions: Action) {
        actions.forEach {
            it.execute()
            actionHistory.push(it)
        }
    }

    fun undo() {
        if (actionHistory.isNotEmpty()) {
            val action = actionHistory.pop()
            action.undo()
            println("Undoing " + action.name)
            return
        }

//        val optionalActions = queueStackNormal.pop()
//        optionalActions.ifPresent { aList: List<Action> ->
//            aList.forEach(Consumer { obj: Action -> obj.undo() })
//            queueStackReverse.push(aList)
//            aList.forEach(Consumer { a: Action ->
//                actionHistory.add(a.name + " - undo")
//            })
//        }
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