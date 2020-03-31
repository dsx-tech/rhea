package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object ReactiveConfigTest : Spek({
    val config = ReactiveConfig {
        "property" of stringType
    }
    val source = ConfigMock()
    config.addConfigSource("ConfigMock", source)

    describe("ReactiveConfig creation") {
        it("should be not null") {
            assertNotNull(config)
        }
    }

    describe("reloadable creation with infix function 'of'") {
        source.pushChanges("property", "something")

        it("reloadable from map should contain a new value of updated property with key=property") {
            assertEquals("something", (config.manager.mapOfProperties["property"] as Reloadable<String>).get())
        }
    }

    describe("reloadable creation with function reloadable() where name can be changed") {
        val reloadable = config.reloadable("server.port", intType)
        source.pushChanges("server.port", 1313)
        while (true) {
            if (reloadable.get() != 0) break
        }

        it("reloadable from map should contain a new value of updated property with key=server.port") {
            assertEquals(1313, (config.manager.mapOfProperties["server.port"] as Reloadable<Int>).get())
        }

        it("calling get() on Reloadable directly should return the same new value") {
            assertEquals(1313, (reloadable.get()))
        }
    }
})