package tv.bae.core.domain.model

data class Breed(
    val id: String,
    val name: String,
    val origin: String,
    val temperament: String,
    val description: String,
    val lifeSpan: String,
    val imageUrl: String?,
    val isFavourite: Boolean = false,
)
