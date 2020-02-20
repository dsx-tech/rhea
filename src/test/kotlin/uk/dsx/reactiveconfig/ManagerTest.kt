package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import uk.dsx.reactiveconfig.ConfigManagerBase.*
import kotlin.test.assertNotEquals

object ManagerTest : Spek({
    class Manager : ConfigManagerBase()

    val manager = Manager()

    val source = ConfigProvider()
    manager.addSource(source)

    describe("Manager") {
        describe("a creation") {
            it("should be not null") {
                assertNotNull(manager)
            }
        }

        describe("a subscription") {
            while (true) {
                if (source.channel != null) break
            }
            source.getChanges(listOf("port"))

            ConfigManagerBase.configScope.launch {
                ConfigManagerBase.flowOfChanges.collect {
                    it("flow should contain RawProperty with key=port from subscribed source") {
                        assertEquals("prt", it.key)
                    }
                }
            }
        }
    }

    describe("Reloadable") {
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
                println(port.get())
                Thread.sleep(1000)
            }

            it("value of port should have been changed") {
                assertNotEquals(0, port.get())
            }
        }
    }
})