package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

object outer : PropertyGroup(){
    val first by booleanType
    object inside : PropertyGroup() {
        object deeper : PropertyGroup() {
            val anotherone by intType
        }
        val third by stringType
    }
}

object PropertyGroupTest : Spek({
    val source = ConfigMock()
    val config = ReactiveConfig.Builder()
        .addSource("configMock", source)
        .build()
    describe("emitting values with our keys"){
        source.addToMap("outer.inside.deeper.anotherone", 3)
        source.addToMap("outer.inside.third", "three")
        source.addToMap("outer.first", true)
    }
    describe("asserting that right keys created")
    {
        it("should be not null") {
            assertNotNull(config[outer.inside.deeper.anotherone])
            assertNotNull(config[outer.inside.third])
            assertNotNull(config[outer.first])
        }
        it("keys should be added to the map"){
            assertTrue(config.manager.mapOfProperties.containsKey("outer.inside.deeper.anotherone"))
            assertTrue(config.manager.mapOfProperties.containsKey("outer.inside.third"))
            assertTrue(config.manager.mapOfProperties.containsKey("outer.first"))
        }
    }
})
