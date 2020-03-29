package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object ConfigManagerTest : Spek({
    val manager = ConfigManager()

    describe("creation") {
        it("should be not null") {
            assertNotNull(manager)
        }
    }

    describe("subscription") {
        val source = ConfigMock()
        manager.addSource(source)

        manager.configScope.launch {
            manager.flowOfChanges.collect {
                it("flow should contain RawProperty with key=port from subscribed source") {
                    assertEquals("port", it.key)
                }
            }
        }
        source.pushChanges("port", "1234")
    }
})