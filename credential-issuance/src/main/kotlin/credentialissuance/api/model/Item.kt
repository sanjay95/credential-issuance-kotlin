package credentialissuance.api.model

import kotlinx.serialization.Serializable

@Serializable data class Item(val id: String, val name: String, val description: String? = null)
