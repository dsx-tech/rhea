package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Reloadable<T>(
    @Volatile private var value: T,
    private val flowOfChanges: Flow<T>,
    private val scope: CoroutineScope
) {
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

    fun <F> map(function: (T) -> (F)): Reloadable<F> {
        val newInitValue = function(this.value)
        val newFlow = this.flowOfChanges.map { value ->
            function(value)
        }

        return Reloadable(newInitValue, newFlow, this.scope)
    }

    fun <F, G> combine(other: Reloadable<F>, function: (T, F) -> (G)): Reloadable<G> {
        val newInitValue = function(this.value, other.value)
        val newFlow = this.flowOfChanges.combine(other.flowOfChanges) { val1: T, val2: F ->
            function(val1, val2)
        }

        return Reloadable(newInitValue, newFlow, this.scope)
    }
}