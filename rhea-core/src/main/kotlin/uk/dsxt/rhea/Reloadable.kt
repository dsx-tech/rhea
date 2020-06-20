package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Contains the latest value of type T and updates it every time the value is changed in configuration.
 *
 * @param T the type of property
 * @param value the latest value of property
 * @param flowOfChanges the flow with typed values
 * @param scope the scope where coroutines will be launched
 */
class Reloadable<T>(
    @Volatile private var value: T,
    private val flowOfValues: Flow<T>,
    private val scope: CoroutineScope
) {
    init {
        scope.launch {
            flowOfValues.collect { newValue: T ->
                value = newValue
            }
        }
    }

    /**
     * @return the latest value.
     */
    fun get() = value

    /** Sets the logic that executes every time the value is changed in configuration.
     *
     * @param function lambda that executes every time the value is changed
     */
    fun onChange(function: (T) -> Unit) {
        scope.launch {
            flowOfValues.collect { newValue: T ->
                function(newValue)
            }
        }
    }

    /** Creates new [Reloadable] which values are constructed from values of explicit parameter by applying [function].
     *
     * @param function the function to apply
     * @return new Reloadable
     */
    fun <F> map(function: (T) -> (F)): Reloadable<F> {
        val newInitValue = function(this.value)
        val newFlow = this.flowOfValues.map { value ->
            function(value)
        }

        return Reloadable(newInitValue, newFlow, this.scope)
    }

    /** Creates new [Reloadable] which values are constructed from combined values of explicit parameter and provided [other] with [function].
     *
     * @param other the Reloadable to combine with
     * @param function the function to apply
     * @return new Reloadable
     */
    fun <F, G> combine(other: Reloadable<F>, function: (T, F) -> (G)): Reloadable<G> {
        val newInitValue = function(this.value, other.value)
        val newFlow = this.flowOfValues.combine(other.flowOfValues) { val1: T, val2: F ->
            function(val1, val2)
        }

        return Reloadable(newInitValue, newFlow, this.scope)
    }
}