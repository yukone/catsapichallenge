package tv.bae.core.data.remote.mapper

import tv.bae.core.data.remote.dto.BreedDto
import tv.bae.core.domain.model.Breed

fun BreedDto.toDomain(): Breed {
    return Breed(
        id = id,
        name = name,
        origin = origin,
        temperament = temperament,
        description = description,
        lifeSpan = lifeSpan,
        imageUrl = image?.url,
    )
}
