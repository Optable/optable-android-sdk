package co.optable.sdk.core.network

import co.optable.sdk.OptableConfig
import co.optable.sdk.core.network.data.TraitsRequest
import co.optable.sdk.core.network.edge.EdgeService
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class NetworkClient(
    config: OptableConfig,
    requestInterceptor: RequestInterceptor,
    responseInterceptor: ResponseInterceptor,
) {

    private val edgeService: EdgeService

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(requestInterceptor)
            .addInterceptor(responseInterceptor)
            .build()

        val gsonFactory = GsonConverterFactory.create()

        val retrofit = Retrofit.Builder()
            .baseUrl(config.getBaseUrl())
            .addConverterFactory(gsonFactory)
            .client(client)
            .build()

        edgeService = retrofit.create(EdgeService::class.java)
    }

    suspend fun identify(ids: List<String>): NetworkResponse<Unit> {
        return runSafe {
            edgeService.identify(ids)
        }
    }

    suspend fun profile(
        traits: Map<String, Any>,
        id: String?,
        neighbors: Set<String>,
    ): NetworkResponse<JsonObject> {
        return runSafe {
            val request = TraitsRequest.Companion.from(traits, id, neighbors)
            edgeService.profile(request)
        }
    }

    suspend fun targeting(ids: List<String>): NetworkResponse<JsonObject> {
        return runSafe {
            edgeService.targeting(ids)
        }
    }

    suspend fun witness(
        event: String,
        properties: HashMap<String, Any>,
    ): NetworkResponse<Unit> {
        return runSafe {
            val evtBody = HashMap<String, Any>()
            evtBody["event"] = event
            evtBody["properties"] = properties
            edgeService.witness(evtBody)
        }
    }

    private suspend fun <T> runSafe(responseBuilder: suspend () -> Response<T>): NetworkResponse<T> {
        val result = runCatching {
            return withContext(Dispatchers.IO) {
                val response = responseBuilder()
                if (response.isSuccessful) {
                    return@withContext NetworkResponse.Success(response.body()!!)
                } else {
                    return@withContext NetworkResponse.Error("Request failed with status code $response: ${response.errorBody()}")
                }
            }
        }
        return result.getOrNull() ?: NetworkResponse.Error(result.exceptionOrNull()?.message ?: "Unknown error")
    }

}
