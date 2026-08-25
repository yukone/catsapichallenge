package tv.bae.core.domain.usecase

import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository

class GetBreedsUseCase(
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(): Result<List<Breed>> =
        repository.getBreeds()
}
