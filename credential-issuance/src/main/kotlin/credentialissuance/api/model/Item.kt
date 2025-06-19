package credentialissuance.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Item(
        val uuid: String, // Client-provided unique identifier
        val name: String,
        val did: String,
        val requestTime: String,
        val responseTime: String?,
        val requestStatus: String,
        val payloadDto: JsonObject? = null
)
