package uk.dsxt.rhea.configsources

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsxt.rhea.*
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals

object YamlConfigSourceTest : Spek({

    val yamlSource =
        YamlConfigSource(
            Paths.get("src" + File.separator + "test" + File.separator + "resources"),
            "yamlSource.yml"
        )

    val config = ReactiveConfig.Builder()
        .addSource("yamlConfig", yamlSource)
        .build()

    describe("checks reading properly string property") {
        val job = config["job", stringType]

        it("should contain value 'Developer' sent from YamlConfigSource") {
            assertEquals("Developer", job!!.get())
        }
    }

    describe("checks reading properly integer property") {
        val number = config["age", intType]

        it("should contain value 13 sent from YamlConfigSource") {
            assertEquals(27, number!!.get())
        }
    }
})