package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object PropertyTypeTest : Spek({
    val config = ReactiveConfig {}
    val source = ConfigMock()
    config.addConfigSource(source)

    describe("a declaration") {
        val server by config.base.stringType

        it("should contain initial value") {
            assertEquals("", server.get())
        }
    }

    describe("testing IntType") {
        val port by config.base.intType

        while (true) {
            if (source.channel != null) break
        }
        source.pushChanges("port", "1313")
        while (true) {
            if (port.get() != 0) break
        }

        it("value of port should have been changed to 1313") {
            assertEquals(1313, port.get())
        }
    }
})