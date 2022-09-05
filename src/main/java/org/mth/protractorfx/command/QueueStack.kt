package org.mth.protractorfx.command

import java.util.*


internal class QueueStack<T> {
    private val dataCollection: LinkedList<T> = LinkedList()

    fun push(item: T) {
        dataCollection.push(item)
    }

    fun pop(): Optional<T> {
        return if (dataCollection.size > 0)
            Optional.ofNullable(dataCollection.removeLast())
        else Optional.empty()
    }

    fun clear() {
        dataCollection.clear()
    }
}