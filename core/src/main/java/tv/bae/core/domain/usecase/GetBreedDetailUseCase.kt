package tv.bae.core.domain.usecase

import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository

class GetBreedDetailUseCase(
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(id: String): Result<Breed> =
        repository.getBreedById(id)
}
