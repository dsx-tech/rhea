package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsx.reactiveconfig.configsources.FileConfigSource
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertNotNull
object hello : PropertyGroup({
    "first" of stringType
    "portagain" of intType
    "hey" of {
        "deeper" of {
            "deepenough" of intType
            "anotherone" of stringType
        }
        "third" of stringType
        "fourth" of stringType
    }
})

object PropertyGroupTest : Spek({
    val config = ReactiveConfig {
        val second = "second" of stringType
        register(hello)
    }
    config.addConfigSource(
        "config",
        FileConfigSource(
            Paths.get("src" + File.separator + "test" + File.separator + "resources").toRealPath(), "config"
        )
    )
    assertNotNull(config["second"])
    assertNotNull(config["hello.first"])
    assertNotNull(config["hello.hey.third"])
    assertNotNull(config["hello.hey.fourth"])
})