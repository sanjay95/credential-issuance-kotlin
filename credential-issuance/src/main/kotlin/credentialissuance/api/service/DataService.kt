package credentialissuance.api.service // Adjust package as needed

import credentialissuance.api.model.Item
import jakarta.annotation.PostConstruct
import java.io.File
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
            // All fields are optional for update. Client sends only what they want to change.
            val name: String? = null,
            val did: String? = null, // If client wants to update the DID
            val requestTime: String? = null,
            val responseTime: String? = null,
            val requestStatus: String? = null,
            val payloadDto: JsonObject? = null // Can be explicitly set to null to clear it
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

    fun updateItem(did: String, updateRequest: UpdateItemRequest): Item? {
        // The 'did' in the path identifies the item to update.
        val items = getAllItems().toMutableList()
        val index = items.indexOfFirst { it.did == did }

        return if (index != -1) {
            val existingItem = items[index]

            // If the request includes a new DID, check its uniqueness (if it's different from the
            // current one)
            updateRequest.did?.let { newDid ->
                if (newDid != existingItem.did && getItemByDid(newDid) != null) {
                    println("Error: Another item with the new DID $newDid already exists.")
                    return null
                }
            }

            val updatedItem =
                    items[index].copy(
                            name = updateRequest.name
                                            ?: existingItem.name, // Keep existing if not provided
                            did = updateRequest.did ?: existingItem.did,
                            requestTime = updateRequest.requestTime ?: existingItem.requestTime,
                            responseTime = updateRequest.responseTime
                                            ?: existingItem
                                                    .responseTime, // Allows setting responseTime to
                            // null if explicitly passed as
                            // null
                            requestStatus = updateRequest.requestStatus
                                            ?: existingItem.requestStatus,
                            payloadDto = updateRequest.payloadDto ?: existingItem.payloadDto
                    )
            items[index] = updatedItem
            saveItems(items)
            updatedItem
        } else {
            null
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
