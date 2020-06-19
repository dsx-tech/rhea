package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import com.bettercloud.vault.VaultException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

/**
 * [ConfigSource] that reads configuration from Vault.
 *
 * **Note: use [Builder] to build instances of this class.
 */
class VaultConfigSource : ConfigSource {
    var vault: Vault
    private var secretPath: String
    private var reloadTimeMillis: Long = 1000

    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private val map: HashMap<String, Node?> = HashMap()
    private val logger = KotlinLogging.logger {}

    private constructor(vault: Vault, secretPath: String) {
        this.vault = vault
        this.secretPath = secretPath
    }

    private constructor(vault: Vault, secretPath: String, reloadTimeMillis: Long) {
        this.vault = vault
        this.secretPath = secretPath
        this.reloadTimeMillis = reloadTimeMillis
    }
    /**
     * Builder for [VaultConfigSource].
     *
     * **Note: provide builder with secretPath and either configured [Vault] or just address and token.
     */
    class Builder {
        private lateinit var vault: Vault
        private lateinit var address: String
        private lateinit var token: String
        private lateinit var secretPath: String
        private var reloadTimeMillis: Long = 0

        /**
         * Sets Vault address for [VaultConfigSource]s built by the builder.
         *
         * @param address address to use
         * @return this instance of builder with Vault address set to provided parameter
         */
        fun withAddress(address: String): Builder {
            return apply {
                this.address = address
            }
        }

        /**
         * Sets Vault token for [VaultConfigSource]s built by the builder.
         *
         * @param token token to use
         * @return this instance of builder with Vault token set to provided parameter
         */
        fun withToken(token: String): Builder {
            return apply {
                this.token = token
            }
        }

        /**
         * Sets Vault secretPath for [VaultConfigSource]s built by the builder.
         *
         * @param secretPath secretPath to use
         * @return this instance of builder with Vault secretPath set to provided parameter
         */
        fun withSecretPath(secretPath: String): Builder {
            return apply {
                this.secretPath = secretPath
            }
        }

        /**
         * Sets Vault reloadTime for [VaultConfigSource]s built by the builder.
         *
         * @param reloadTime reloadTime that configures intervals of time in which [VaultConfigSource] checks for changes
         * @return this instance of builder with reloadTime set to provided parameter
         */
        fun withReloadTime(reloadTime: Long): Builder {
            return apply {
                this.reloadTimeMillis = reloadTime
            }
        }

        /**
         * Sets Vault for [VaultConfigSource]s built by the builder.
         *
         * @param vault Vault to use
         * @return this instance of builder with Vault set to provided parameter
         */
        fun withVault(vault: Vault): Builder {
            return apply {
                this.vault = vault
            }
        }

        /**
         * Builds a [VaultConfigSource] using this builder
         *
         * @return new instance of [VaultConfigSource]
         */
        fun build(): VaultConfigSource {
            if (!::vault.isInitialized) {
                val vaultConfig = VaultConfig().address(address).token(token).build()
                vault = Vault(vaultConfig)
            }

            if (reloadTimeMillis != 0L) {
                return VaultConfigSource(vault, secretPath, reloadTimeMillis)
            } else {
                return VaultConfigSource(vault, secretPath)
            }
        }
    }

    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        configScope = scope

        configScope.launch {
            try {
                lateinit var data: Map<String, String>
                try {
                    data = vault.logical().read(secretPath).data
                } catch (e: VaultException) {
                    throw error("Could't get values from Vault")
                }
                for (pair in data) {
                    map[pair.key] = StringNode(pair.value)
                }

                while (true) {
                    try {
                        data = vault.logical().read(secretPath).data
                    } catch (e: VaultException) {
                        throw error("Couldn't get values from Vault")
                    }

                    for (pair in data) {
                        with(StringNode(pair.value)) {
                            if (map[pair.key] != this) {
                                map[pair.key] = this
                                channel.send(RawProperty(pair.key, this))
                            }
                        }
                    }

                    delay(reloadTimeMillis)
                }
            } catch (e: Exception) {
                logger.error(e.message)
            }
        }
    }

    override fun getNode(key: String): Node? {
        return map[key]
    }
}