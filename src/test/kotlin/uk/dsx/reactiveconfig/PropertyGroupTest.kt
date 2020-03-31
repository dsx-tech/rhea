package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import uk.dsx.reactiveconfig.configsources.FileConfigSource
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertNotNull


object hello : PropertyGroup(){
    val first = "first" of stringType
    val portagain = "portagain" of intType
    val hey = "hey" of object : PropertyGroup() {
        val third = "third" of stringType
        val fourth = "fourth" of stringType
    }
}

object PropertyGroupTest : Spek({
    val config = ReactiveConfig {
        "second" of stringType
        register(hello)
    }
    config.addConfigSource(
        "config.txt",
        FileConfigSource(
            Paths.get("src" + File.separator + "test" + File.separator + "resources").toRealPath(), "config"
        )
    )
    describe("check if the reloadables contain the correct values") {
        while (true){
            if (config["hello.hey.third"] != ""){
                break
            }
        }
        it("check if the reloadable contain the correct values"){
            assert(config["hello.hey.fourth"] == "iamhere")
        }
    }
})