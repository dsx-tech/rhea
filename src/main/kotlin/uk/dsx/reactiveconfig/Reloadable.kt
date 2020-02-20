package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Reloadable<T>(@Volatile private var value: T, private var flowOfChanges: Flow<T>) {
    init {
        ConfigManagerBase.configScope.launch {
            flowOfChanges.collect { newValue: T ->
                value = newValue
            }
        }
    }

    fun get() = value

    fun onChange(function: (T) -> Unit) {
        ConfigManagerBase.configScope.launch {
            flowOfChanges.collect { newValue: T ->
                function(newValue)
            }
        }
    }
}