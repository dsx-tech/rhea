package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals

object ReloadableTest : Spek({
    val reloadable1: Reloadable<Int> = Reloadable(
        0,
        flow {
            for (i in 1 until 10) {
                emit(i)
            }
        },
        CoroutineScope(EmptyCoroutineContext),
        {}
    )

    val reloadable2: Reloadable<Int> = Reloadable(
        0,
        flow {
            for (i in 1 until 5) {
                emit(i)
            }
        },
        CoroutineScope(EmptyCoroutineContext),
        {}
    )

    describe("calling onChange()") {
        var sum = 0
        var mul = 1

        runBlocking {
            reloadable1.onChange {
                sum += it
            }
            reloadable2.onChange {
                mul *= it
            }
        }

        it("total sum of emitted values in reloadable1 should be 45") {
            assertEquals(45, sum)
        }

        it("total multiplication of emitted values in reloadable2 should be 24") {
            assertEquals(24, mul)
        }

        it("final value in reloadable1 should be 9") {
            assertEquals(9, reloadable1.get())
        }
    }
})