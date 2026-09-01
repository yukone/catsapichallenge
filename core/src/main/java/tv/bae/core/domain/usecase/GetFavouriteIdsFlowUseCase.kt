package tv.bae.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tv.bae.core.domain.repository.BreedRepository

class GetFavouriteIdsFlowUseCase(
    private val repository: BreedRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavouriteIds()
}
