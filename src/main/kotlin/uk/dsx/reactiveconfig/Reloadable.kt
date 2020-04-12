package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class Reloadable<T>(
    @Volatile private var value: T,
    private var flowOfChanges: Flow<T>,
    private val scope: CoroutineScope,
    askForInitialValue: () -> Unit
) {
    init {
        val isLaunched = AtomicBoolean(false)
        val isSet = AtomicBoolean(false)

        scope.launch {
            isLaunched.set(true)
            flowOfChanges.collect { newValue: T ->
                value = newValue
                isSet.set(true)
            }
        }

        while (!isLaunched.get()) {
        }

        askForInitialValue()

        while (!isSet.get()) {
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