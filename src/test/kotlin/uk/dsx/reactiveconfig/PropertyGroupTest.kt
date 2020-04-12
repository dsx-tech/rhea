package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsx.reactiveconfig.configsources.FileConfigSource
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertNotNull
object outer : PropertyGroup({
    "first" of stringType
    "inside" of {
        "deeper" of {
            "anotherone" of stringType
        }
        "third" of stringType
    }
})

object PropertyGroupTest : Spek({
    val config = ReactiveConfig {
        val second = "second" of stringType
        register(outer)
    }
    config.addConfigSource(
        "config",
        FileConfigSource(
            Paths.get("src" + File.separator + "test" + File.separator + "resources").toRealPath(),
            "config"
        )
    )
    describe("checks if keys contains in config manager")
    {
        val map = config.manager.mapOfProperties
        it ("should contain these keys"){
            assertNotNull(map.containsKey("second"))
            assertNotNull(map.containsKey("outer.portagain"))
            assertNotNull(map.containsKey("outer.inside.third"))
            assertNotNull(map.containsKey("outer.inside.deeper.anotherone"))
        }
    }
})