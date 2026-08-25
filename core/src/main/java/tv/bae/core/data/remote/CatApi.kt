package tv.bae.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import tv.bae.core.data.remote.dto.BreedDto

class CatApi(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.thecatapi.com/v1"
    }
    suspend fun getBreeds(page: Int = 0, limit: Int = 20): List<BreedDto> {

        return client.get("$BASE_URL/breeds") {
            parameter("page",page)
            parameter("limit",limit)
        }.body()
    }
}