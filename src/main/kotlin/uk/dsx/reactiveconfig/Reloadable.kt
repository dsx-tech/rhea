package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Reloadable<T>(@Volatile private var value: T, private var observable: Flow<T>) {
    init {
        ConfigManagerBase.configScope.launch {
            observable.collect {
                value = it
            }
        }
    }

    fun get() = value

    suspend fun onChange(function: (T) -> Unit) {
        observable.collect() {
            function(it)
        }
    }
}