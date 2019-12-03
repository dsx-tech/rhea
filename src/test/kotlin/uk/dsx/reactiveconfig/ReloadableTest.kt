package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object ReloadableTest : Spek({
    val reloadable: Reloadable<Int> = Reloadable(0, flow {
        for (i in 1 until 10) {
            emit(i)
        }
    })

    describe("calling onChange()") {
        var sum = 0

        runBlocking {
            reloadable.onChange {
                sum += it
            }
        }

        it("total sum of emitted values should be 45") {
            assertEquals(45, sum)
        }

        it("final value should be 9") {
            assertEquals(9, reloadable.get())
        }
    }
})