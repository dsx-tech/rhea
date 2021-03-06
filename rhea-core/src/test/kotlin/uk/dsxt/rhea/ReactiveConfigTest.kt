package uk.dsxt.rhea

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

object ReactiveConfigTest : Spek({
    val source = ConfigMock()

    val config = ReactiveConfig.Builder()
        .addSource("configMock", source)
        .build()

    describe("ReactiveConfig creation") {
        it("should be not null") {
            assertNotNull(config)
        }
    }

    describe("typed access to mapOfProperties") {
        source.addToMap("prop", 3)
        config["prop", intType]

        it("sum of prop's value of reloadable from map and 7 should be 10") {
            assertEquals(10, config["prop", intType]!!.get() + 7)
        }

        it("null should be returned because property with key='propp' doesn't exist") {
            assertNull(config["propp", intType])
        }

        source.addToMap("prop1", true)
        config["prop1", booleanType]

        it("value of reloadable with key='prop1' from map should be 'true'") {
            assertTrue(config["prop1", booleanType]!!.get())
        }
    }
})