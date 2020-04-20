package uk.dsx.reactiveconfig.configsources

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.yaml.snakeyaml.Yaml
import uk.dsx.reactiveconfig.*
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import java.io.File
import java.io.IOException
import java.nio.file.*

class YamlConfigSource : ConfigSource {
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
                val yaml = Yaml()
                var inputStream = file.inputStream()
                var parsed =
                    yaml.load(inputStream) as? Map<String, Any> ?: error("File ${file.name} is not formatted correctly")

                for (obj in parsed) {
                    map[obj.key] = toNode(obj.value)
                }
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
                            parsed = yaml.load(inputStream) as? Map<String, Any>
                                ?: error("File ${file.name} is not formatted correctly")

                            for (obj in parsed) {
                                with(toNode(obj.value)) {
                                    if (map[obj.key] != this) {
                                        map[obj.key] = this
                                        channel.send(RawProperty(obj.key, this))
                                    }
                                }
                            }
                        }
                    }

                    inputStream.close()
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

    private fun toNode(obj: Any?): Node? {
        return when (obj) {
            is Int -> NumericNode(obj.toString())
            is Long -> NumericNode(obj.toString())
            is Float -> NumericNode(obj.toString())
            is Double -> NumericNode(obj.toString())
            is Boolean -> BooleanNode(obj)
            is List<*> -> {
                val result = mutableListOf<Node?>()
                for (el in obj) {
                    result.add(toNode(el))
                }
                ArrayNode(result)
            }
            is Map<*, *> -> {
                val result = mutableMapOf<String, Node?>()
                for (el in obj) {
                    result[el.key.toString()] = toNode(el.value)
                }
                ObjectNode(result)
            }
            is String -> StringNode(obj)
            else -> null
        }
    }
}