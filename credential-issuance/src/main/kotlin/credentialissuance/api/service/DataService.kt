package credentialissuance.api.service // Adjust package as needed

import credentialissuance.api.model.Item
import jakarta.annotation.PostConstruct
import java.io.File
import java.nio.file.Paths
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class DataService {

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
        val fullPath = Paths.get(projectDir, dataFilePath)
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

    fun getItemById(id: String): Item? {
        return getAllItems().find { it.id == id }
    }

    fun addItem(itemRequest: OmitIdItem): Item {
        val items = getAllItems().toMutableList()
        val newItem =
                Item(
                        id = UUID.randomUUID().toString(), // Generate a unique ID
                        name = itemRequest.name,
                        description = itemRequest.description
                )
        items.add(newItem)
        saveItems(items)
        return newItem
    }

    fun updateItem(id: String, itemUpdate: OmitIdItem): Item? {
        val items = getAllItems().toMutableList()
        val index = items.indexOfFirst { it.id == id }
        return if (index != -1) {
            val updatedItem =
                    items[index].copy(name = itemUpdate.name, description = itemUpdate.description)
            items[index] = updatedItem
            saveItems(items)
            updatedItem
        } else {
            null
        }
    }

    fun deleteItem(id: String): Boolean {
        val items = getAllItems().toMutableList()
        val removed = items.removeIf { it.id == id }
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

// Helper data class for POST/PUT requests where ID is auto-generated or path-provided
// Consider moving to a dto package like credentialissuance.api.dto if you have many DTOs
@Serializable data class OmitIdItem(val name: String, val description: String? = null)
