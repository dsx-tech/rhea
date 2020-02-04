package uk.dsx.reactiveconfig

import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import uk.dsx.reactiveconfig.configsources.FileConfigSource
import java.io.File
import java.nio.file.Paths
import uk.dsx.reactiveconfig.ConfigManagerBase.StringType

object ManagerTest : Spek({
    class Manager : ConfigManagerBase() {}

    val manager = Manager()

    ConfigManagerBase.configScope.launch {
        manager.addSource(FileConfigSource(
                Paths.get(".", "src" + File.separator + "test" + File.separator +
                    "kotlin" + File.separator + "uk" + File.separator + "dsx" + File.separator + "reactiveconfig"),
            "config"))
    }

    val property by StringType()
    while (true){
        print(property.get())
        Thread.sleep(5000)
    }
})