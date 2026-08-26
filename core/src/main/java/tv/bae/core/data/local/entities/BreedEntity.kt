package tv.bae.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breeds")
data class BreedEntity(
    @PrimaryKey val id: String,
    val name: String,
    val origin: String,
    val temperament: String,
    val description: String,
    val lifeSpan: String,
    val imageUrl: String?,
    val page: Int,
    val cachedAt: Long,
)
