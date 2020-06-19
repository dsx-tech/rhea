package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

/**
 * Provides access to configuration store and watches changes of configuration values.
 */
interface ConfigSource {
    /**
     * Provides initial values from configuration store for [getNode] and starts a new coroutine that watches changes of configuration values.
     *
     * @param channelOfChanges the channel where changes of values will be sent
     * @param scope the scope where a new coroutine that watches changes of configuration should be launched
     */
    suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope)

    /**
     * Provides initial value of property to construct [Reloadable].
     *
     * @param key the key of property
     * @return the Node that holds [RawProperty]
     */
    fun getNode(key: String): Node?
}