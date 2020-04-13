package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsx.reactiveconfig.configsources.JsonConfigSource
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals

object ReloadableTest : Spek({
    val jsonSource =
        JsonConfigSource(Paths.get("src" + File.separator + "test" + File.separator + "resources"), "jsonSource.json")

    val config = ReactiveConfig.Builder()
        .addSource("jsonConfig", jsonSource)
        .build()

    describe("calling map") {
        val property: Reloadable<String> = config.reloadable("number", intType).map { value ->
            value.toString()
        }

        it("should contain string value 14") {
            assertEquals("14", property.get())
        }
    }
})