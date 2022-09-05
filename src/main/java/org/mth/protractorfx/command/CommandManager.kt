package org.mth.protractorfx.command

import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList


object CommandManager {

    private val queueStackNormal: QueueStack<List<Action>>
    private val queueStackReverse: QueueStack<List<Action>>
    private val actionHistory: MutableList<String?>

    private val stack = LinkedList<Action>()

    init {
        queueStackNormal = QueueStack()
        queueStackReverse = QueueStack()
        actionHistory = ArrayList()
    }

    fun execute(vararg actions: Action) {
        actions.forEach { it.execute() }
        actions.forEach { stack.push(it) }
        println(stack.toSet())

//        queueStackNormal.push(actions.asList())
//        actions.forEach { actionHistory.add(it.name) }
    }

    fun undo() {
        if (stack.isNotEmpty()) {
            val action = stack.pop()
            action.undo()
            println("Undoing " + action)
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
            aList.forEach(Consumer { a: Action ->
                actionHistory.add(a.name + " - redo")
            })
        }
    }

    fun clearNormal() {
        queueStackNormal.clear()
    }

    fun clearReverse() {
        queueStackReverse.clear()
    }

    fun getActionHistory(): List<String?> {
        return actionHistory
    }
}