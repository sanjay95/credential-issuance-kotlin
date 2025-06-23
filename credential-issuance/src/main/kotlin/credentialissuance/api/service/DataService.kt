package credentialissuance.api.service // Adjust package as needed

import credentialissuance.api.model.Item
import jakarta.annotation.PostConstruct
import java.io.File
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service

@Service
class DataService {

    @Serializable
    data class CreateItemRequest(
            val name: String,
            val uuid: String, // Client-provided UUID
            val did: String? = null,
            val requestTime: String? = null,
            val requestStatus: String? = null,
            val payloadDto: JsonObject? = null // Remains optional
    )

    @Serializable
    data class UpdateItemRequest(
            val uuid: String, // UUID is mandatory for updates
            val did: String,
            val payloadDto: JsonObject,
            val name: String? = null,
            val requestTime: String? = null,
            val requestStatus: String? = null
    )

    private val dataFilePath = "data/local_db.json" // Store in a 'data' subdirectory
    private lateinit var dataFile: File

    // Configure Json for pretty printing and to ignore unknown keys if your schema evolves
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    @PostConstruct
    fun init() {
        val projectDir = System.getProperty("user.dir")
        val fullPath = java.nio.file.Paths.get(projectDir, dataFilePath)
        dataFile = fullPath.toFile()

        // Create directory and file if they don't exist
        dataFile.parentFile.mkdirs() // Create parent directories if they don't exist
        if (!dataFile.exists()) {
            dataFile.createNewFile()
            dataFile.writeText("[]") // Initialize with an empty JSON array
        }
    }

    fun getAllItems(): List<Item> {
        val content = dataFile.readText()
        return if (content.isNotBlank()) {
            try {
                json.decodeFromString<List<Item>>(content)
            } catch (e: Exception) {
                // Log error or handle corrupted file
                println("Error reading or parsing data file: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun getItemByDid(did: String): Item? {
        return getAllItems().find { it.did == did }
    }

    fun getItemsByName(name: String): List<Item> { // Name is mandatory in Item
        return getAllItems().filter { it.name.equals(name, ignoreCase = true) }
    }

    fun getItemByUuid(uuid: String): Item? { // UUID is mandatory in Item
        return getAllItems().find { it.uuid == uuid }
    }

    fun addItem(itemRequest: CreateItemRequest): Item? {
        // Check for DID uniqueness
        itemRequest.did?.let {
            if (getItemByDid(it) != null) {
                println("Error: Item with DID $it already exists.")
                return null
            }
        }

        // Check for UUID uniqueness (uuid is mandatory in CreateItemRequest)
        if (getItemByUuid(itemRequest.uuid) != null) {
            // Or throw a custom exception, e.g., DuplicateDidException
            println("Error: Item with UUID ${itemRequest.uuid} already exists.")
            return null
        }

        val items = getAllItems().toMutableList()
        val newItem =
                Item(
                        uuid = itemRequest.uuid,
                        name = itemRequest.name,
                        did = itemRequest.did,
                        requestTime = itemRequest.requestTime,
                        responseTime =
                                null, // responseTime is typically set later, so null on creation
                        requestStatus = itemRequest.requestStatus,
                        payloadDto = itemRequest.payloadDto
                )
        items.add(newItem)
        saveItems(items)
        return newItem
    }

    /**
     * Updates an item if it exists, or creates it if it does not (upsert).
     * @return A Pair containing the resulting Item and a Boolean which is 'true' if the item was
     * created, and 'false' if it was updated. Returns null if there is a conflict (e.g., duplicate
     * DID).
     */
    fun updateItem(uuid: String, updateRequest: UpdateItemRequest): Pair<Item, Boolean>? {
        // The 'uuid' in the path identifies the item to update.
        val items = getAllItems().toMutableList()
        val index = items.indexOfFirst { it.uuid == uuid }

        if (index != -1) {
            // UPDATE logic
            val existingItem = items[index]

            // If the new DID in the request is different from the existing item's DID,
            // check uniqueness for the new DID.
            if (updateRequest.did != existingItem.did && getItemByDid(updateRequest.did) != null) {
                println("Error: Another item with the new DID ${updateRequest.did} already exists.")
                return null
            }

            val updatedItem =
                    existingItem.copy(
                            name = updateRequest.name
                                            ?: existingItem.name, // Keep existing if not provided
                            did = updateRequest.did,
                            payloadDto = updateRequest.payloadDto,
                            requestTime = updateRequest.requestTime ?: existingItem.requestTime,
                            responseTime =
                                    Instant.now().toString(), // Automatically set responseTime to
                            // current time
                            requestStatus = updateRequest.requestStatus
                                            ?: existingItem.requestStatus,
                    )
            items[index] = updatedItem
            saveItems(items)
            return Pair(updatedItem, false) // false indicates an update
        } else {
            // INSERT logic (Upsert)
            if (getItemByDid(updateRequest.did) != null) {
                println("Error: Another item with the new DID ${updateRequest.did} already exists.")
                return null
            }

            val newItem =
                    Item(
                            uuid = uuid,
                            name = updateRequest.name
                                            ?: uuid, // Default name to uuid if not provided
                            did = updateRequest.did,
                            requestTime = updateRequest.requestTime,
                            responseTime =
                                    Instant.now().toString(), // Set response time on creation
                            requestStatus = updateRequest.requestStatus,
                            payloadDto = updateRequest.payloadDto
                    )
            items.add(newItem)
            saveItems(items)
            return Pair(newItem, true) // true indicates a creation
        }
    }

    fun deleteItem(did: String): Boolean { // 'did' here is the identifier for deletion
        // If 'did' in Item model can be null, this method might need adjustment
        // or we assume we only delete items that have a non-null DID.
        val items = getAllItems().toMutableList()
        val removed = items.removeIf { it.did == did }
        if (removed) {
            saveItems(items)
        }
        return removed
    }

    private fun saveItems(items: List<Item>) {
        try {
            dataFile.writeText(json.encodeToString(items))
        } catch (e: Exception) {
            // Log error or handle write failure
            println("Error writing to data file: ${e.message}")
        }
    }
}
