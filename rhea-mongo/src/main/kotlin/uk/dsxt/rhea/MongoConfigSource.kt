package uk.dsxt.rhea

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.sql.Connection
import com.mongodb.MongoException
import org.bson.Document

class MongoConfigSource(private val url : String,private val database: String, private val scheme : String) : ConfigSource {
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var configScope: CoroutineScope
    private lateinit var client : MongoClient
    private lateinit var db : MongoDatabase
    private val map: HashMap<String, Node?> = HashMap()
    private val logger = KotlinLogging.logger {}

    init{
        try{
            client = MongoClients.create(url)
        }
        catch(e : Exception){
            logger.error("Couldn't connect to database with this url: \"${url}\". Values from this place are no longer updates")
        }
    }
    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        configScope = scope

        try{
            db = client.getDatabase(database)
            val collection = db.getCollection(scheme)

            configScope.launch {
                collection.find().forEach{
                    it.forEach{
                        map[it.key] = toNode(it.value)
                    }
                }
                while(true){
                    collection.find().forEach{
                        it.forEach{
                            map[it.key] = toNode(it.value)
                        }
                    }
                }
            }
        }
        catch(e : MongoException){
            logger.error("Failed reading from $url, $database database, $scheme scheme. Values from this place are no longer updates\nMore detailed infrmation:\n"
                    + e.message)
        }
    }

    override fun getNode(key: String): Node? {
        return map[key]
    }

    private fun toNode(obj: Any?): Node? {
        return when (obj) {
            is Int -> NumericNode(obj.toString())
            is Long -> NumericNode(obj.toString())
            is Float -> NumericNode(obj.toString())
            is Double -> NumericNode(obj.toString())
            is Boolean -> BooleanNode(obj)
            is ArrayList<*> -> {
                val result = mutableListOf<Node?>()
                obj.forEach{
                    result.add(toNode(it))
                }
                ArrayNode(result)
            }
            is Document -> {
                val result = mutableMapOf<String, Node?>()
                obj.forEach{
                    result[it.key] = toNode(it.value)
                }
                ObjectNode(result)
            }
            is String -> StringNode(obj)
            else -> null
        }
    }

}