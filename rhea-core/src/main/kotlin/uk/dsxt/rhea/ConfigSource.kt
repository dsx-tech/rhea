package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

/**
 * Provides access to configuration store and watches changes of configuration values.
 */
interface ConfigSource {
    /**
     * Provides initial values from configuration store for [getNode] and starts a new coroutine in [scope],
     * watches changes of configuration values and transfers them through [channelOfChanges] to [ReactiveConfig].
     *
     * @param channelOfChanges the channel where changes of values will be sent to
     * @param scope the scope where a new coroutine that watches changes of configuration should be launched
     */
    suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope)

    /**
     * Provides initial value of property with given [key] to construct [Reloadable].
     *
     * @return [Node] that holds [RawProperty] with initial value
     */
    fun getNode(key: String): Node?
}