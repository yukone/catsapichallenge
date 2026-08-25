package tv.bae.core.domain.usecase

import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository

class GetFavouriteBreedsUseCase(
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(): Result<List<Breed>> =
        repository.getFavouriteBreeds()
}
