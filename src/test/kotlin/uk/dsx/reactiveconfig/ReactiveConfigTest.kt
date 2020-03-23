package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object ReactiveConfigTest : Spek({
    val config = ReactiveConfig {
        "property".of(base.stringType)
    }
    val source = ConfigMock()
    config.addConfigSource(source)

    describe("ReactiveConfig creation") {
        it("should be not null") {
            assertNotNull(config)
        }
    }

    describe("reloadable creation with infix function 'of'") {
        source.pushChanges("property", "something")

        it("reloadable from map should contain a new value of updated property with key=property") {
            assertEquals("something", (config.manager.properties["property"] as Reloadable<String>).get())
        }
    }

    describe("reloadable creation with function reloadable() where name can be changed") {
        val reloadable = config.reloadable("server.port", config.base.intType)
        source.pushChanges("server.port", 1313)
        while (true) {
            if (reloadable.get() != 0) break
        }

        it("reloadable from map should contain a new value of updated property with key=server.port") {
            assertEquals(1313, (config.manager.properties["server.port"] as Reloadable<Int>).get())
        }

        it("calling get() on Reloadable directly should return the same new value") {
            assertEquals(1313, (reloadable.get()))
        }
    }
})