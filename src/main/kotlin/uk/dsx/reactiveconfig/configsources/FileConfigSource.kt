package uk.dsx.reactiveconfig.configsources

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import uk.dsx.reactiveconfig.ConfigManager
import uk.dsx.reactiveconfig.RawProperty
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import java.io.File
import java.nio.file.*
import java.util.*

class FileConfigSource(private val directory: Path, private val fileName: String) :
    ConfigSource {
    private var config: List<String>? = null
    private val file = Paths.get(directory.toAbsolutePath().toString() + File.separator + fileName)
    private val watchService: WatchService? = FileSystems.getDefault().newWatchService()
    private var key: WatchKey? = null

    init {
        directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
    }

    @ObsoleteCoroutinesApi
    override suspend fun subscribe(dataStream: Channel<RawProperty>) {
        ConfigManager.configScope.launch(newSingleThreadContext("coroutine"))
        {
            try {
                for (string in Files.readAllLines(file)) {
                    val splitted = string.split('=')
                    dataStream.send(RawProperty(splitted[0], splitted[1]))
                }
                while (true) {
                    var updated = try {
                        key = watchService!!.take()
                        Files.readAllLines(file)
                    } catch (e: NoSuchFileException) {
                        delay(10)
                        Files.readAllLines(file)
                    }
                    for (string in giveMeListOfChanges(config, updated)) {
                        val splitted = string.split('=')
                        dataStream.send(RawProperty(splitted[0], splitted[1]))
                    }
                    key!!.reset();
                }
            } catch (e: Exception) {
                e.printStackTrace();
            }
        }
    }

    private fun giveMeListOfChanges(previous: List<String>?, updated: List<String>): List<String> {
        val changes: LinkedList<String> = LinkedList()
        if (previous == null) {
            config = updated
            return updated
        }
        for (upd in updated) {
            if (!previous.contains(upd)) {
                changes.add(upd)
            }
        }
        config = updated
        return changes
    }
}