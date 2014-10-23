package dao

import com.mongodb.casbah.Imports._

object PasteDao {
    def createPaste() = {
        val mongoConnection = MongoConnection()
        val collection = mongoConnection("lecarton")("pastes")
        val newObject = MongoDBObject(
            "pasteId" -> "1sgfsgf2",
            "owner" -> new ObjectId("54485f901adee7b53870bacb"),
            "title" -> "Sample",
            "content" -> "A Sample message",
            "isPrivate" -> false
        )
        collection += newObject
        collection.find()
        for ( x <- collection.find()) println(x)
    }
}