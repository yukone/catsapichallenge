package tv.bae.core.domain.usecase

import tv.bae.core.domain.repository.BreedRepository

class GetAverageLifespanUseCase(
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(): Result<Double?> {
        return repository.getFavouriteBreeds().map { breeds ->
            if (breeds.isEmpty())
                return@map null
            val values = breeds.mapNotNull { parseUpperBound(it.lifeSpan) }
            if (values.isEmpty())
                null
            else
                values.average()
        }
    }

    private fun parseUpperBound(lifeSpan: String): Int? {
        val lifespanTrimmed = lifeSpan.trim()
        if (lifespanTrimmed.isEmpty()) return null
        val parts = lifespanTrimmed.split("-").map { it.trim() }
        return parts.lastOrNull()?.toIntOrNull()
    }
}
