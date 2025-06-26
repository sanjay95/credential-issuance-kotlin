package credentialissuance.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
        val uuid: String, // Client-provided unique identifier
        val name: String?,
        val did: String? = null,
        val requestTime: String? = null,
        val responseTime: String?,
        val requestStatus: String? = null,
        val payloadDto: Map<String, String>? = null
)
