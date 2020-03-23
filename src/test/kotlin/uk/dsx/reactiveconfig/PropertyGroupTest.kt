package uk.dsx.reactiveconfig

import org.spekframework.spek2.Spek
import uk.dsx.reactiveconfig.configsources.FileConfigSource
import java.nio.file.Paths
import kotlin.test.assertNotNull

object hello : PropertyGroup(){
    val first = "first" of base.stringType
    object second : PropertyGroup(){
        val third = "third" of base.stringType
        val fourth = "fourth" of base.stringType
    }
}

object PropertyGroupTest : Spek({
    val config = ReactiveConfig {
        val second = "second" of base.stringType
        register(hello)
    }
    config.addConfigSource(FileConfigSource(Paths.get("C:\\Users\\Дмитрий\\Desktop\\Java-Reactive-Configuration\\src\\test\\resources"),"config"));
    assertNotNull(config["second"])
    assertNotNull(config["hello.first"])
    assertNotNull(config["hello.second.third"])
    assertNotNull(config["hello.second.fourth"])
})