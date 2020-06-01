package uk.dsxt.rhea

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object JDBCConfigSourceTest : Spek({
    val connection = DriverManager.getConnection("jdbc:h2:~/test", "test", "test")
    val statement = connection.createStatement()

    statement.execute("CREATE TABLE IF NOT EXISTS testtable1('key' VARCHAR(50), value VARCHAR(50), update_time VARCHAR(50)")
    statement.execute("CREATE TABLE IF NOT EXISTS testtable2('key' VARCHAR(50), value INT")

    statement.execute("INSERT INTO testtable1 VALUES(\"tableOne\", \"first\", 5)")
    statement.execute("INSERT INTO testtable2 VALUES(\"tableTwo\", 1)")

    val jdbcSourceOne = JDBCConfigSource(
        "jdbc:h2:~/test",
    "test",
    "test",
    "testtable1"
    )
    val jdbcSourceTwo = JDBCConfigSource(
    "jdbc:h2:~/test",
    "test",
    "test",
    "testtable2"
)

    val config = ReactiveConfig.Builder()
        .addSource("jdbcConfigOne", jdbcSourceOne)
        .addSource("jdbcConfigTwo", jdbcSourceTwo)
        .build()

    describe("checks reading properly StringNode from database") {
        val firstTableFirst = config["tableOne", stringType]

        it("should contain value 'first' sent from database") {
            assertEquals("first", firstTableFirst!!.get())
        }
    }

    describe("checks reading properly IntNode from database") {
        val firstTableSecond = config["tableTwo", intType]

        it("should contain value '1' sent from database") {
            assertEquals(1, firstTableSecond!!.get())
        }
    }

    statement.execute("DROP TABLE IF EXISTS testtable1")
    statement.execute("DROP TABLE IF EXISTS testtable2")
})