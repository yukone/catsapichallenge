package tv.bae.core.domain.usecase

import tv.bae.core.domain.repository.BreedRepository

class ToggleFavouriteUseCase(
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(breedId: String): Result<Unit> =
        repository.toggleFavourite(breedId)
}
