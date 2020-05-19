package uk.dsxt.rhea

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object VaultConfigSourceTest : Spek({
    val secretPath = "secret/hello"

    val vaultConfigSource =
        VaultConfigSource.Builder()
            .withToken("s.gRq9zyaNMFkfceaGAWhU5SEX")
            .withAddress("http://127.0.0.1:8200")
            .withSecretPath(secretPath)
            .build()

    val config = ReactiveConfig.Builder()
        .addSource("vaultSource", vaultConfigSource)
        .build()

    val newProperties = HashMap<String, Any>()
    newProperties["foo"] = "world"
    newProperties["num"] = 14
    vaultConfigSource.vault.logical().write(secretPath, newProperties)

    describe("checks reading properly string property") {
        Thread.sleep(500)
        val reloadable = config["foo", stringType]

        it("should contain value 'world' sent from VaultConfigSource") {
            assertEquals("world", reloadable!!.get())
        }
    }

    describe("checks reading properly integer property") {
        val reloadable = config["num", intType]

        it("should contain value '14' sent from VaultConfigSource") {
            assertEquals(14, reloadable!!.get())
        }
    }
})