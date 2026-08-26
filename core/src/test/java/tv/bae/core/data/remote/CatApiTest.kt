package tv.bae.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class CatApiTest {

    private lateinit var api: CatApi

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun buildClient(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(this@CatApiTest.json)
            }
            expectSuccess = true
        }
    }

    @Test
    fun `getBreeds returns list of breeds`() = runTest {
        val responseBody = """
            [
                {
                    "id": "stcat",
                    "name": "Street cat",
                    "origin": "Valbom",
                    "temperament": "Active, energetic, noisy",
                    "description": "The Street cat aren't too friendly but they love food.",
                    "life_span": "8 - 15",
                    "image": { "url": "https://example.com/stcat.jpg" }
                },
                {
                    "id": "hcat",
                    "name": "Home cat",
                    "origin": "Valbom",
                    "temperament": "Passive, lazy, quiet",
                    "description": "The Home cat love to sit on windows and watch the street.",
                    "life_span": "8 - 15",
                    "image": null
                }
            ]
        """.trimIndent()

        val engine = MockEngine { request ->
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }
        api = CatApi(buildClient(engine))

        val result = api.getBreeds(page = 0)

        assertEquals(2, result.size)
        assertEquals("stcat", result[0].id)
        assertEquals("Street cat", result[0].name)
        assertEquals("Valbom", result[0].origin)
        assertEquals("8 - 15", result[0].lifeSpan)
        assertEquals("https://example.com/stcat.jpg", result[0].image?.url)
        assertEquals("hcat", result[1].id)
        assertNull(result[1].image)
    }

    @Test
    fun `getBreeds sends correct query params`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("0", request.url.parameters["page"])
            assertEquals("50", request.url.parameters["limit"])
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }
        api = CatApi(buildClient(engine))

        api.getBreeds(page = 0, limit = 50)
    }

    @Test
    fun `getBreeds sends custom page and limit`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("3", request.url.parameters["page"])
            assertEquals("10", request.url.parameters["limit"])
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }
        api = CatApi(buildClient(engine))

        api.getBreeds(page = 3, limit = 10)
    }

    @Test
    fun `getBreeds handles empty array`() = runTest {
        val engine = MockEngine {
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }
        api = CatApi(buildClient(engine))

        val result = api.getBreeds()

        assertEquals(0, result.size)
    }

    @Test
    fun `getBreeds ignores unknown JSON fields`() = runTest {
        val responseBody = """
            [
                {
                    "id": "stcat",
                    "name": "Street cat",
                    "origin": "Valbom",
                    "temperament": "Active",
                    "description": "Test",
                    "life_span": "8 - 15",
                    "unknown_field": "should be ignored",
                    "nested_unknown": { "foo": "bar" }
                }
            ]
        """.trimIndent()

        val engine = MockEngine {
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }
        api = CatApi(buildClient(engine))

        val result = api.getBreeds()

        assertEquals(1, result.size)
        assertEquals("stcat", result[0].id)
    }

    @Test
    fun `getBreeds coerces missing image to null`() = runTest {
        val responseBody = """
            [
                {
                    "id": "stcat",
                    "name": "Street cat",
                    "origin": "Valbom",
                    "temperament": "Active",
                    "description": "Test",
                    "life_span": "8 - 15"
                }
            ]
        """.trimIndent()

        val engine = MockEngine {
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }
        api = CatApi(buildClient(engine))

        val result = api.getBreeds()

        assertEquals(1, result.size)
        assertNull(result[0].image)
    }

    @Test
    fun `getBreeds handles breed with null image`() = runTest {
        val responseBody = """
            [
                {
                    "id": "stcat",
                    "name": "Street cat",
                    "origin": "Valbom",
                    "temperament": "Active",
                    "description": "Test",
                    "life_span": "8 - 15",
                    "image": null
                }
            ]
        """.trimIndent()

        val engine = MockEngine {
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }
        api = CatApi(buildClient(engine))

        val result = api.getBreeds()

        assertEquals(1, result.size)
        assertNull(result[0].image)
    }

    @Test
    fun `getBreeds throws on server error`() = runTest {
        val engine = MockEngine {
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
            )
        }
        api = CatApi(buildClient(engine))

        try {
            api.getBreeds()
            fail("Expected exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("500") == true || e.message?.contains("Internal") == true)
        }
    }

    @Test
    fun `getBreeds throws on client error`() = runTest {
        val engine = MockEngine {
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound,
            )
        }
        api = CatApi(buildClient(engine))

        try {
            api.getBreeds()
            fail("Expected exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("404") == true || e.message?.contains("Not Found") == true)
        }
    }
}
