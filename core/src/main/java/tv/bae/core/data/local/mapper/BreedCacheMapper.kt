package tv.bae.core.data.local.mapper

import tv.bae.core.data.local.entities.BreedEntity
import tv.bae.core.data.remote.dto.BreedDto
import tv.bae.core.domain.model.Breed

fun BreedDto.toCacheEntity(page: Int): BreedEntity {
    return BreedEntity(
        id = id,
        name = name,
        origin = origin,
        temperament = temperament,
        description = description,
        lifeSpan = lifeSpan,
        imageUrl = image?.url,
        page = page,
        cachedAt = System.currentTimeMillis(),
    )
}

fun BreedEntity.toDomain(): Breed {
    return Breed(
        id = id,
        name = name,
        origin = origin,
        temperament = temperament,
        description = description,
        lifeSpan = lifeSpan,
        imageUrl = imageUrl,
    )
}
