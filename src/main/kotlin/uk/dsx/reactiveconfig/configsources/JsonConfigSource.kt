package uk.dsx.reactiveconfig.configsources

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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
    private var channel: Channel<RawProperty>? = null
    private val map: HashMap<String, Node> = HashMap()

    private val watchService: WatchService? = FileSystems.getDefault().newWatchService()
    private var key: WatchKey? = null

    constructor(directory: Path, fileName: String) {
        directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        file = Paths.get(directory.toAbsolutePath().toString() + File.separator + fileName).toFile()
    }

    constructor(uri: URI) {
        Paths.get(uri).register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        file = Paths.get(uri).toFile() ?: error("Cannot open file: $uri")
    }

    override suspend fun subscribe(dataStream: Channel<RawProperty>, scope: CoroutineScope) {
        channel = dataStream
        val parser: Parser = Parser.default()

        scope.launch(newSingleThreadContext("watching thread")) {
            try {
                var inputStream = file.inputStream()

                var parsed = parser.parse(inputStream) as JsonObject
                for (obj in parsed.map) {
                    toNode(obj.value).also {
                        map[obj.key] = it
                        channel!!.send(RawProperty(obj.key, it))
                    }
                }
                inputStream.close()

                while (true) {
                    inputStream = file.inputStream()

                    parsed = try {
                        key = watchService!!.take()
                        parser.parse(inputStream) as JsonObject
                    } catch (e: Exception) {
                        delay(10)
                        parser.parse(inputStream) as JsonObject
                    }

                    for (obj in parsed.map) {
                        toNode(obj.value).also {
                            if (map[obj.key] != it) {
                                map[obj.key] = it
                                channel!!.send(RawProperty(obj.key, it))
                            }
                        }
                    }
                    key!!.reset();
                    inputStream.close()
                }
            } catch (e: Exception) {
                println(e.stackTrace)
            }
        }

    }

    fun toNode(obj: Any?): Node {
        return when (obj) {
            is Long -> IntNode(obj)
            is Double -> FloatNode(obj)
            is Boolean -> BooleanNode(obj)
            is JsonArray<*> -> {
                val result = mutableListOf<Node>()
                for (el in obj.value) {
                    result.add(toNode(el))
                }
                ArrayNode(result)
            }
            is JsonObject -> {
                val result = mutableMapOf<String, Node>()
                for (el in obj.map) {
                    result[el.key] = toNode(el.value)
                }
                ObjectNode(result)
            }
            else -> {
                if (obj.toString() == "null") NullNode(null)
                else StringNode(obj.toString())
            }
        }
    }
}