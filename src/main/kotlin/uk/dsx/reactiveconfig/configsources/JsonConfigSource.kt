package uk.dsx.reactiveconfig.configsources

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import uk.dsx.reactiveconfig.*
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import java.net.URI
import java.nio.file.*
import java.io.*

class JsonConfigSource : ConfigSource {
    private val file: File
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private val map: HashMap<String, Node?> = HashMap()

    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private lateinit var key: WatchKey
    private val parser: Parser = Parser.default()

    constructor(directory: Path, fileName: String) {
        directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        file = Paths.get(directory.toAbsolutePath().toString() + File.separator + fileName).toFile()
    }

    constructor(uri: URI) {
        Paths.get(uri).register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        file = Paths.get(uri).toFile() ?: error("Cannot open file: $uri")
    }

    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        configScope = scope

        configScope.launch(newSingleThreadContext("watching thread")) {
            try {
                var inputStream = file.inputStream()
                var parsed = parser.parse(inputStream) as JsonObject

                for (obj in parsed.map) {
                    map[obj.key] = toNode(obj.value)

                }

                inputStream.close()

                while (true) {
                    inputStream = file.inputStream()

                    parsed = try {
                        key = watchService.take()
                        parser.parse(inputStream) as JsonObject
                    } catch (e: Exception) {
                        delay(10)
                        parser.parse(inputStream) as JsonObject
                    }

                    for (obj in parsed.map) {
                        with(toNode(obj.value)) {
                            if (map[obj.key] != this) {
                                map[obj.key] = this
                                channel.send(RawProperty(obj.key, this))
                            }
                        }
                    }
                    key.reset()
                    inputStream.close()
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
            is JsonArray<*> -> {
                val result = mutableListOf<Node?>()
                for (el in obj.value) {
                    result.add(toNode(el))
                }
                ArrayNode(result)
            }
            is JsonObject -> {
                val result = mutableMapOf<String, Node?>()
                for (el in obj.map) {
                    result[el.key] = toNode(el.value)
                }
                ObjectNode(result)
            }
            is String -> StringNode(obj)
            else -> null
        }
    }
}