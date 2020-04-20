package uk.dsx.reactiveconfig.configsources

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import uk.dsx.reactiveconfig.*
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import java.nio.file.*
import java.io.*
import java.util.*
import kotlin.collections.HashMap

class PropertiesConfigSource : ConfigSource {
    private val file: File
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private val map: HashMap<String, Node?> = HashMap()

    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private lateinit var watchKey: WatchKey

    constructor(directory: Path, fileName: String) {
        try {
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        file = Paths.get(directory.toAbsolutePath().toString() + File.separator + fileName).toFile()
            ?: error("Cannot open file: $fileName")
    }

    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        configScope = scope

        configScope.launch(newSingleThreadContext("watching thread")) {
            try {
                val properties = Properties()
                var inputStream = file.inputStream()

                properties.load(inputStream)
                for (pair in properties) {
                    map[pair.key.toString()] = StringNode(pair.value.toString())
                }
                properties.clear()
                inputStream.close()

                while (true) {
                    try {
                        watchKey = watchService.take()
                    } catch (e: InterruptedException) {
                        return@launch
                    }
                    for (event in watchKey.pollEvents()) {
                        val changed = event.context() as Path

                        if (changed.endsWith(file.name)) {
                            inputStream = file.inputStream()
                            properties.load(inputStream)

                            for (pair in properties) {
                                val key = pair.key.toString()
                                val value = pair.value.toString()

                                with(StringNode(value)) {
                                    if (map[key] != this) {
                                        map[key] = this
                                        channel.send(RawProperty(key, this))
                                    }
                                }
                            }
                        }
                    }

                    inputStream.close()
                    properties.clear()
                    if (!watchKey.reset()) {
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getNode(key: String): Node? {
        return map[key]
    }
}