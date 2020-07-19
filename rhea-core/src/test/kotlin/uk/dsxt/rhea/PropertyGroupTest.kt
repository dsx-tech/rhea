package uk.dsxt.rhea

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertNotNull
import kotlin.test.assertNull

object outer : PropertyGroup() {
    val first by booleanType

    object inside : PropertyGroup() {
        object deeper : PropertyGroup() {
            val anotherone by intType.nullable()
        }

        val third by stringType
    }
}

object PropertyGroupTest : Spek({
    val source = ConfigMock()
    val config = ReactiveConfig.Builder()
        .addSource("configMock", source)
        .build()

    describe("emitting values with our keys") {
        source.addToMap("outer.inside.deeper.anotherone", null)
        source.addToMap("outer.inside.third", "three")
        source.addToMap("outer.first", true)
    }

    val reloadable1 = config[outer.inside.deeper.anotherone]
    val reloadable2 = config[outer.inside.third]
    val reloadable3 = config[outer.first]

    describe("wait until reloadables will be created") {
        while (reloadable3 == null) {
        }
    }

    describe("asserting that right keys created")
    {
        it("should be not null") {
            assertNotNull(reloadable1)
            assertNotNull(reloadable2)
            assertNotNull(reloadable3)
        }
        it("keys should be added to the map") {
            assertNotNull(config["outer.inside.deeper.anotherone", intType.nullable()])
            assertNotNull(config["outer.inside.third", stringType])
            assertNull(config["outer.firs", booleanType])
        }
    }
})
