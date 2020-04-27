package uk.dsxt.rhea

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

object PropertyTypeTest : Spek({
    val source = ConfigMock()

    val config = ReactiveConfig.Builder()
        .addSource("configMock", source)
        .build()

    describe("calling nullable()") {
        source.addToMap("nullableStringProperty", null)
        val nullableStringProperty = config.get("nullableStringProperty", stringType.nullable())

        it("value of property should have been changed to null") {
            assertNull(nullableStringProperty!!.get())
        }
    }

    describe("accessing one property twice") {
        source.addToMap("property", "something")

        val property1 = config.get("property", stringType)
        val property2 = config.get("property", stringType)

        it("the same reloadable should be in both: property1 and property2") {
            assertEquals(property1, property2)
        }
        it("value of property1 should have been changed to 'something'") {
            assertEquals("something", property1!!.get())
        }
        it("value of property2 should also have been changed to 'something'") {
            assertEquals("something", property2!!.get())
        }
    }

    describe("stringType") {
        source.addToMap("stringProperty", "something1")
        val stringProperty = config.get("stringProperty", stringType)

        Thread.sleep(10)
        source.pushChanges("stringProperty", "something2")

            it("value of property should have been changed to 'something2'") {
                assertEquals("something2", stringProperty!!.get())
            }
    }


    describe("intType") {
        source.addToMap("intProperty", 1313)
        val intProperty = config.get("intProperty", intType)

        it("value of property should have been changed to 1313") {
            assertEquals(1314, intProperty!!.get() + 1)
        }
    }

    describe("longType") {
        source.addToMap("longProperty", 1212L)
        val longProperty = config.get("longProperty", longType)

        it("value of property should have been changed to 1212") {
            assertEquals(1212L, longProperty!!.get())
        }
    }

    describe("floatType") {
        source.addToMap("floatProperty", 1414F)
        val floatProperty = config.get("floatProperty", floatType)

        it("value of property should have been changed to 1414") {
            assertEquals(1414F, floatProperty!!.get())
        }
    }

    describe("doubleType") {
        source.addToMap("doubleProperty", 1515.0)
        val doubleProperty = config.get("doubleProperty", doubleType)

        it("value of property should have been changed to 1515") {
            assertEquals(1515.0, doubleProperty!!.get())
        }
    }

    describe("booleanType") {
        source.addToMap("booleanProperty", true)
        val booleanProperty = config.get("booleanProperty", booleanType)

        it("value of property should have been changed to true") {
            assertTrue(booleanProperty!!.get())
        }
    }
})