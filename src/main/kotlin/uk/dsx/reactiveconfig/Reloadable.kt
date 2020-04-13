package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import java.util.concurrent.atomic.AtomicBoolean

class Reloadable<T>(
    private val key: String,
    @Volatile private var value: T,
    private var flowOfChanges: Flow<T>,
    private val type: PropertyType<T>,
    private val mapOfProperties: MutableMap<String, Reloadable<*>>,
    private val mapOfSources: MutableMap<String, ConfigSource>,
    private val commonFlow: Flow<RawProperty>,
    private val scope: CoroutineScope,
    private val isValueSet: AtomicBoolean
) {
    init {
        val isLaunched = AtomicBoolean(false)

        scope.launch {
            isLaunched.set(true)
            flowOfChanges.collect { newValue: T ->
                value = newValue
                isValueSet.set(true)
            }
        }

        while (!isLaunched.get()) {
        }

        for (source in mapOfSources.values) {
            source.pushValue(key)
        }

        while (!isValueSet.get()) {
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
        val newInitValue = function(value)
        val newType = type.transform(newInitValue, function)

        return ReloadableFactory.createReloadable(
            key,
            AtomicBoolean(true),
            newType,
            mapOfProperties,
            mapOfSources,
            commonFlow,
            scope
        )
    }
}