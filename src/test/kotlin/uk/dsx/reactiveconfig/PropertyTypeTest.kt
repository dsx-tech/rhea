package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import uk.dsx.reactiveconfig.ConfigManagerBase.*

object PropertyTypeTest : Spek({
    class Manager : ConfigManagerBase()

    val manager = Manager()
    val source = ConfigProvider()
    manager.addSource(source)

    describe("a declaration") {
        val server by StringType()

        it("should contain initial value") {
            assertEquals("", server.get())
        }
    }

    describe("testing IntType") {
        val port by IntType()

        while (true) {
            if (source.channel != null) break
        }

        source.getChanges(listOf("port"))
        source.getChanges(listOf("port"))

        while (true) {
            if (port.get() != 0) break
        }

        it("value of port should have been changed") {
            assertNotEquals(0, port.get())
        }
    }
})