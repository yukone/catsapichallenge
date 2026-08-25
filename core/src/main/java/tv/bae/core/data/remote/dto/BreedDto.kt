package tv.bae.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BreedDto(
    val id: String,
    val name: String,
    val origin: String,
    val temperament: String,
    val description: String,
    @SerialName("life_span") val lifeSpan: String,
    @SerialName("image") val image: BreedImageDto?,
)

@Serializable
data class BreedImageDto(
    val url: String?,
)
