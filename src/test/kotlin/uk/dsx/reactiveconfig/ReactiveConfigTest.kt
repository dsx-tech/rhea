package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertNotNull

object ReactiveConfigTest : Spek({
    val config = ReactiveConfig {
        "property".of(base.stringType)
    }

    val source = ConfigMock()
    config.addConfigSource(source)

    describe("ReactiveConfig") {
        it("creation of ReactiveConfig") {
            assertNotNull(config)
        }
    }

    describe("DSL functions") {
        val variable = config.reloadable("server.port", config.base.intType)

        while (true) {
            if (source.channel != null) break
        }
        source.pushChanges("server.port", "13")

        while (true) {
            if (variable.get() != 0) break
        }

        it("reloadable function") {
            assertNotNull(config.manager.properties["server.port"])
        }

        it("infix function 'of'") {
            assertNotNull(config.manager.properties["property"])
        }
    }
})