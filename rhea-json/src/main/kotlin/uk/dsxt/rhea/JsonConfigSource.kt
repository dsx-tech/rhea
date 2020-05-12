package uk.dsxt.rhea

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import mu.KotlinLogging
import uk.dsxt.rhea.*
import uk.dsxt.rhea.ConfigSource
import java.io.File
import java.io.IOException
import java.nio.file.*

class JsonConfigSource(directory: Path, fileName: String) : ConfigSource {
    private val file: File
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private val map: HashMap<String, Node?> = HashMap()

    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private lateinit var watchKey: WatchKey
    private val parser: Parser = Parser.default()
    private val logger = KotlinLogging.logger {}

    init {
        try {
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        } catch (e: IOException) {
            logger.error("Couldn't register WatchService in directory $directory")
        }

        file = File(directory.toAbsolutePath().toString() + File.separator + fileName)
    }

    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        configScope = scope

        if (file.exists()) {
            configScope.launch(newSingleThreadContext("watching thread")) {
                try {
                    var inputStream = file.inputStream()
                    var parsed = parser.parse(inputStream) as? JsonObject
                        ?: throw error("Failed to parse ${file.name}: file is not formatted correctly")

                    for (obj in parsed.map) {
                        map[obj.key] = toNode(obj.value)
                    }
                    inputStream.close()

                    while (true) {
                        watchKey = watchService.take()

                        for (event in watchKey.pollEvents()) {
                            val changed = event.context() as Path

                            if (changed.endsWith(file.name)) {
                                inputStream = file.inputStream()
                                parsed = parser.parse(inputStream) as? JsonObject
                                    ?: throw error("Failed to parse ${file.name}: file is not formatted correctly")

                                for (obj in parsed.map) {
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
                            logger.error("Couldn't reset watch key of ${file.name}. \n Stop watching for changes of this config source")
                            break
                        }
                    }
                } catch (e: IOException) {
                    logger.error("Failed reading ${file.name}: an I/O error occurred. \n Stop watching for changes of this config source")
                } catch (e: InterruptedException) {
                    logger.error("WatchService was interrupted. Stop watching for changes of ${file.name}")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
            }
        } else {
            logger.error("No such file or directory ${file.absolutePath}. \n Subscription to ${file.name} failed")
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