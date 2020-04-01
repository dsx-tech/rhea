package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    describe("typed access to mapOfProperties") {
        val prop = config.reloadable("prop", intType)
        source.pushChanges("prop", 3)

        it("sum of prop's value of reloadable from map and 7 should be 10") {
            assertEquals(10, config.getReloadable<Int>("prop")!!.get() + 7)
        }

        it ("null should be returned because of a wrong type specified") {
            assertNull(config.getReloadable<String>("prop"))
        }

        it ("null should be returned because property with key='propp' doesn't exist") {
            assertNull(config.getReloadable<Int>("propp"))
        }

        val prop1 = config.reloadable("prop1", booleanType)
        source.pushChanges("prop1", true)

        it("value of reloadable with key='prop1' from map should be 'true'") {
            assertTrue(config.getReloadable("prop1", booleanType)!!.get())
        }
    }

    describe("reloadable creation with infix function 'of'") {
        source.pushChanges("property", "something")

        it("reloadable from map should contain a new value of updated property with key=property") {
            assertEquals("something", (config.getReloadable<String>("property")!!.get()))
        }
    }

    describe("reloadable creation with function reloadable() where name can be changed") {
        val reloadable = config.reloadable("server.port", intType)
        source.pushChanges("server.port", 1313)

        it("reloadable from map should contain a new value of updated property with key=server.port") {
            assertEquals(1313, (reloadable.get()))
        }

        it("calling get() on Reloadable directly should return the same new value") {
            assertEquals(1313, (reloadable.get()))
        }
    }
})