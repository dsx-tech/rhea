package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object ManagerTest : Spek({
    val manager = ConfigManager

    describe("a creation") {
        it("should be not null") {
            assertNotNull(manager)
        }
    }

    describe("a subscription") {
        val source = ConfigMock()
        manager.addSource(source)

        while (true) {
            if (source.channel != null) break
        }
        source.pushChanges("port", "1234")

        ConfigManager.configScope.launch {
            ConfigManager.flowOfChanges.collect {
                it("flow should contain RawProperty with key=port from subscribed source") {
                    assertEquals("port", it.key)
                }
            }
        }
    }
})