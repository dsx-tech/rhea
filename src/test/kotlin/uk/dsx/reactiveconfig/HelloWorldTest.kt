package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object HelloWorldTest: Spek({
    describe("A hello world test") {
        it("Shall pass") {
            println("Hello world test!")
            assert(true)
        }
    }
})
