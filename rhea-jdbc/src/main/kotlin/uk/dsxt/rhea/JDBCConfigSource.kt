package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import mu.KotlinLogging
import java.sql.*

class JDBCConfigSource(private val url : String,private val scheme : String) : ConfigSource {

    private lateinit var connection : Connection
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private val map: HashMap<String, Node?> = HashMap()
    private val logger = KotlinLogging.logger {}

    init{
        try{
            connection = DriverManager.getConnection(url)
        } catch (e : SQLException) {
            logger.error("Couldn't connect to database with this url: \"${url}\". Values from this place are no longer updates")
        }
    }

    constructor(url : String, login : String, password : String, scheme: String) : this(url, scheme){
        try{
            connection = DriverManager.getConnection(url, login, password)
        } catch (e : SQLException) {
            logger.error("Couldn't connect to database with this url: \"${url}\". Values from this place are no longer updates")
        }
    }

    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        configScope = scope

        if (!connection.isClosed){
            configScope.launch(newSingleThreadContext("watching thread")) {
                try{
                    if (scheme.contains(" ")){
                        throw error("Incorrect name of scheme: \"${scheme}\". Values from this place are no longer updates")
                    }
                    var query = "SELECT * FROM $scheme"
                    val statement = connection.createStatement()
                    var result = statement.executeQuery(query)

                    val columnNumber = result.metaData.columnCount
                    val updateColumnName : String
                    var latestUpdate = 3

                    while (result.next()){
                        map[result.getString(1)] = toNode(result.getObject(2))
                        if (columnNumber == 3){
                            with(result.getInt(3)) {
                                if (this > latestUpdate) {
                                    latestUpdate = this
                                }
                            }
                        }
                    }

                    if (columnNumber == 3) {
                        updateColumnName = result.metaData.getColumnName(3)
                        query += " WHERE $updateColumnName > "
                    }

                    while(true){
                        delay(1000)
                        if (columnNumber == 3){
                            result = statement.executeQuery(query + "$latestUpdate")
                        }
                        while (result.next()){
                            val first = result.getString(1)
                            val second = toNode(result.getObject(2))
                            if(map[first] != second){
                                map[first] = second
                                channel.send(RawProperty(first, second))
                            }
                            if (columnNumber == 3){
                                with(result.getInt(3)) {
                                    if (this > latestUpdate) {
                                        latestUpdate = this
                                    }
                                }
                            }
                        }
                    }
                }
                catch(e : SQLException){
                    logger.error("Failed reading from $scheme scheme. Values from this place are no longer updates")
                }
                catch(e : Exception){
                    logger.error(e.message)
                }
            }
        }
    }

    override fun getNode(key: String): Node? = map[key]

    private fun toNode(obj: Any?): Node? {
        return when (obj) {
            is Int -> NumericNode(obj.toString())
            is Long -> NumericNode(obj.toString())
            is Float -> NumericNode(obj.toString())
            is Double -> NumericNode(obj.toString())
            is Boolean -> BooleanNode(obj)
            is String -> StringNode(obj)
            else -> null
        }
    }
}