package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Reloadable<T>(@Volatile private var value: T, private var flowOfChanges: Flow<T>, private val scope: CoroutineScope) {
    init {
        scope.launch {
            flowOfChanges.collect { newValue: T ->
                value = newValue
            }
        }
    }

    fun get() = value

    fun onChange(function: (T) -> Unit) {
        scope.launch {
            flowOfChanges.collect { newValue: T ->
                function(newValue)
            }
        }
    }
}