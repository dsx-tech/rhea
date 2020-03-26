package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

object PropertyTypeTest : Spek({
    val config = ReactiveConfig {}
    val source = ConfigMock()
    config.addConfigSource(source)

    describe("declaration") {
        val server = config.reloadable("server", config.base.stringType)

        it("should contain initial value") {
            assertEquals("", server.get())
        }
    }

    // todo: test delegation

    describe("calling nullable()") {
        val nullableStringProperty = config.reloadable("nullableStringProperty", config.base.stringType.nullable())

        source.pushChanges("nullableStringProperty", "notNull")
        while (true) {
            if (nullableStringProperty.get() != "") break
        }

        source.pushChanges("nullableStringProperty", null)
        while (true) {
            if (nullableStringProperty.get() != "notNull") break
        }

        it("value of property should have been changed to null") {
            assertNull(nullableStringProperty.get())
        }
    }


    describe("stringType") {
        val stringProperty = config.reloadable("stringProperty", config.base.stringType)

        source.pushChanges("stringProperty", "something1")
        while (true) {
            if (stringProperty.get() != "") break
        }

        source.pushChanges("stringProperty", "something2")
        while (true) {
            if (stringProperty.get() != "something1") break
        }

        it("value of property should have been changed to 'something2'") {
            assertEquals("something2", stringProperty.get())
        }
    }

    describe("intType") {
        val intProperty = config.reloadable("intProperty", config.base.intType)

        source.pushChanges("intProperty", 1313)
        while (true) {
            if (intProperty.get() != 0) break
        }

        it("value of property should have been changed to 1313") {
            assertEquals(1313, intProperty.get())
        }
    }

    describe("longType") {
        val longProperty = config.reloadable("longProperty", config.base.longType)

        source.pushChanges("longProperty", 1212L)
        while (true) {
            if (longProperty.get() != 0L) break
        }

        it("value of property should have been changed to 1212") {
            assertEquals(1212L, longProperty.get())
        }
    }

    describe("floatType") {
        val floatProperty = config.reloadable("floatProperty", config.base.floatType)

        source.pushChanges("floatProperty", 1414F)
        while (true) {
            if (floatProperty.get() != 0F) break
        }

        it("value of property should have been changed to 1414") {
            assertEquals(1414F, floatProperty.get())
        }
    }

    describe("doubleType") {
        val doubleProperty = config.reloadable("doubleProperty", config.base.doubleType)

        source.pushChanges("doubleProperty", 1515.0)
        while (true) {
            if (doubleProperty.get() != 0.0) break
        }

        it("value of property should have been changed to 1515") {
            assertEquals(1515.0, doubleProperty.get())
        }
    }

    describe("booleanType") {
        val booleanProperty = config.reloadable("booleanProperty", config.base.booleanType)

        source.pushChanges("booleanProperty", true)
        while (true) {
            if (booleanProperty.get()) break
        }

        it("value of property should have been changed to true") {
            assertTrue(booleanProperty.get())
        }
    }
})