package uk.dsx.reactiveconfig

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Reloadable<T>(@Volatile private var value: T, private var observable: Flow<T>) {
    init {
        GlobalScope.launch {
            observable.collect { newValue ->
                value = newValue
            }
        }
    }

    fun get(): T = value

    suspend fun onChange(function: (T) -> Unit) {
        observable.collect() {
            function(it)
        }
    }
}