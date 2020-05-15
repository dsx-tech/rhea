package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VaultConfigSource (private val address: String, private val token: String, private val secretPath: String) : ConfigSource {
    val vault: Vault
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private val map: HashMap<String, Node?> = HashMap()

    init {
        val vaultConfig = VaultConfig().address(address).token(token).build()
        vault = Vault(vaultConfig)
    }

    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        configScope = scope

        configScope.launch {
            var data = vault.logical().read(secretPath).data
            for (pair in data) {
                map[pair.key] = StringNode(pair.value)
            }

            while (true) {
                data = vault.logical().read(secretPath).data

                for (pair in data) {
                    with(StringNode(pair.value)) {
                        if (map[pair.key] != this) {
                            map[pair.key] = this
                            channel.send(RawProperty(pair.key, this))
                        }
                    }
                }
                delay(100)
            }
        }
    }

    override fun getNode(key: String): Node? {
        return map[key]
    }
}