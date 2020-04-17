package uk.dsx.reactiveconfig.configsources

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsx.reactiveconfig.*
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object PropertiesConfigSourceTest : Spek({
    val propertiesSource =
        PropertiesConfigSource(Paths.get("src" + File.separator + "test" + File.separator + "resources"), "propertiesSource.properties")

    val config = ReactiveConfig.Builder()
        .addSource("propertiesConfig", propertiesSource)
        .build()

    describe("resource check") {
        it("should be initialised") {
            assertNotNull(propertiesSource)
        }
    }

    describe("checks reading properly string property") {
        val name = config.get("login", stringType)

        it("should contain value 'Felix' sent from PropertiesConfigSource") {
            assertEquals("Felix", name!!.get())
        }
    }

    describe("checks reading properly integer property") {
        val number = config.get("password", intType)

        it("should contain value 1234 sent from PropertiesConfigSource") {
            assertEquals(1234, number!!.get())
        }
    }
})