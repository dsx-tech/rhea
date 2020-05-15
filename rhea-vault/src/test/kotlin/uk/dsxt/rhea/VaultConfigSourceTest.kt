package uk.dsxt.rhea

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object VaultConfigSourceTest : Spek({
    val secretPath = "secret/hello"
    val vaultConfigSource =
        VaultConfigSource("http://127.0.0.1:8200", "s.gRq9zyaNMFkfceaGAWhU5SEX", secretPath)

    val config = ReactiveConfig.Builder()
        .addSource("vaultSource", vaultConfigSource)
        .build()

    describe("checks reading properly string property") {
        val newProperties = HashMap<String, Any>()
        newProperties["foo"] = "world"
        vaultConfigSource.vault.logical().write(secretPath, newProperties)

        Thread.sleep(150)
        val reloadable = config["foo", stringType]

        it("should contain value 'world' sent from VaultConfigSource") {
            assertEquals("world", reloadable!!.get())
        }
    }

    describe("checks reading properly integer property") {
        val newProperties = HashMap<String, Any>()
        newProperties["num"] = 14
        vaultConfigSource.vault.logical().write(secretPath, newProperties)

        Thread.sleep(150)
        val reloadable = config["num", intType]

        it("should contain value '14' sent from VaultConfigSource") {
            assertEquals(14, reloadable!!.get())
        }
    }
})