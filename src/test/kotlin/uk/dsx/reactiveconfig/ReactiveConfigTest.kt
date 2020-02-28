package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsx.reactiveconfig.ReactiveConfig.Companion.reloadable
import kotlin.test.assertNotNull

object ReactiveConfigTest : Spek({
    val config = ReactiveConfig {
        "property" of StringType
    }
    val source = ConfigMock()
    config.manager.addSource(source)

    describe("ReactiveConfig") {
        it ("creation of ReactiveConfig") {
            assertNotNull(config)
        }
    }

    describe("DSL functions") {
        val variable = reloadable("server.port", IntType)

        while (true) {
            if (source.channel != null) break
        }
        source.pushChanges("server.port", "13")

        while (true) {
            if (variable.get() != 0) break
        }

        it("reloadable function") {
            println(variable.get())
            assertNotNull(ConfigManager.properties["server.port"])
        }

        it("infix function 'of'") {
            assertNotNull(ConfigManager.properties["property"])
        }
    }
})