package uk.dsxt.rhea.configsources

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import mu.KotlinLogging
import uk.dsxt.rhea.*
import uk.dsxt.rhea.interfaces.ConfigSource
import java.nio.file.*
import java.io.*
import java.util.*
import kotlin.collections.HashMap

class PropertiesConfigSource(directory: Path, fileName: String) : ConfigSource {
    private var file: File
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private val map: HashMap<String, Node?> = HashMap()

    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private lateinit var watchKey: WatchKey
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
                    val properties = Properties()
                    var inputStream = file.inputStream()

                    properties.load(inputStream)
                    for (pair in properties) {
                        map[pair.key.toString()] = StringNode(pair.value.toString())
                    }
                    properties.clear()
                    inputStream.close()

                    while (true) {
                        watchKey = watchService.take()

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
                            logger.error("Couldn't reset watch key of ${file.name}. \n Stop watching for changes of this config source")
                            break
                        }
                    }
                } catch (e: IOException) {
                    logger.error("Failed reading ${file.name}: an I/O error occurred. \n Stop watching for changes of this config source")
                } catch (e: InterruptedException) {
                    logger.error("WatchService was interrupted. Stop watching for changes of ${file.name}")
                }
            }
        } else {
            logger.error("No such file or directory ${file.absolutePath}. \n Subscription to ${file.name} failed")
        }
    }

    override fun getNode(key: String): Node? {
        return map[key]
    }
}