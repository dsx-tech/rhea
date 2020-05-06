package uk.dsxt.rhea.configsources

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsxt.rhea.*
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object JsonConfigSourceTest : Spek({
    val jsonSource =
        JsonConfigSource(
            Paths.get("src" + File.separator + "test" + File.separator + "resources"),
            "jsonSource.json"
        )

    val config = ReactiveConfig.Builder()
        .addSource("jsonConfig", jsonSource)
        .build()

    describe("checks reading properly BooleanNode from json") {
        val isSomethingOn = config["isSomethingOn", booleanType]

        it("should contain value 'true' sent from JsonConfigSource") {
            assertTrue(isSomethingOn!!.get())
        }
    }

    describe("checks reading properly StringNode from json") {
        val property = config["property", stringType]

        it("should contain value 'someInfo' sent from JsonConfigSource") {
            assertEquals("someInfo", property!!.get())
        }
    }
})