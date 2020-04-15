package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals

object ReloadableTest : Spek({
    val source = ConfigMock()

    val config = ReactiveConfig.Builder()
        .addSource("configMock", source)
        .build()

    describe("calling onChange()") {
        val reloadable1: Reloadable<Int> = Reloadable(
            0,
            flow {
                for (i in 1 until 10) {
                    emit(i)
                }
            },
            CoroutineScope(EmptyCoroutineContext)
        )

        val reloadable2: Reloadable<Int> = Reloadable(
            0,
            flow {
                for (i in 1 until 5) {
                    emit(i)
                }
            },
            CoroutineScope(EmptyCoroutineContext)
        )

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

    describe("calling map") {
        source.addToMap("number", 14)
        val property = config.getReloadable("number", intType) as Reloadable<Int>

        val mappedProperty: Reloadable<String> = property.map { value ->
            value.toString()
        }
        it("should contain 14") {
            assertEquals(14, property.get())
        }

        it("should contain string value of 14") {
            assertEquals("14", mappedProperty.get())
        }
    }

    describe("calling combine") {
        source.addToMap("number1", 1)
        source.addToMap("number2", 2)

        val property1 = config.getReloadable("number1", intType) as Reloadable<Int>
        val property2 = config.getReloadable("number2", intType) as Reloadable<Int>

        val combinedProperty = property1.combine(property2) {i1, i2 ->
            i1 + i2
        }

        Thread.sleep(100)
        source.pushChanges("number1", 11)
        source.pushChanges("number2", 16)
        Thread.sleep(100)

        it("should have changed to 27") {
            assertEquals(27, combinedProperty.get())
        }
    }
})