package co.optable.android_sdk.core.network

import co.optable.android_sdk.*
import co.optable.android_sdk.core.network.edge.EdgeResponse
import co.optable.android_sdk.core.network.edge.EdgeResponseAdapterFactory
import co.optable.android_sdk.core.network.edge.EdgeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.awaitResponse
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
            .addCallAdapterFactory(EdgeResponseAdapterFactory())
            .addConverterFactory(gsonFactory)
            .client(client)
            .build()

        edgeService = retrofit.create(EdgeService::class.java)
    }

    suspend fun identify(idList: OptableIdentifyInput): NetworkResponse<Unit> {
        val result = runCatching {
            return withContext(Dispatchers.IO) {
                val call = edgeService.identify(idList)
                val response = call.awaitResponse()
                if (response.isSuccessful) {
                    return@withContext NetworkResponse.Success(Unit)
                } else {
                    return@withContext NetworkResponse.Error("Request failed with status code $response: ${response.errorBody()}")
                }
            }
        }
        return result.getOrNull() ?: NetworkResponse.Error(result.exceptionOrNull()?.message ?: "Unknown error")
    }

    suspend fun profile(traits: OptableProfileTraits): EdgeResponse<OptableProfileResponse, OptableResponse.Error> {
        val profileBody = HashMap<String, Any>()
        profileBody.put("traits", traits)
        return edgeService.profile(profileBody)
    }

    suspend fun targeting(): EdgeResponse<OptableTargetingResponse, OptableResponse.Error> {
        return edgeService.targeting()
    }

    suspend fun targeting(idList: OptableIdentifyInput): NetworkResponse<OptableTargeting> {
        val result = runCatching {
            return withContext(Dispatchers.IO) {
                val call = edgeService.targeting(idList)
                val response = call.awaitResponse()
                if (response.isSuccessful) {
                    val result = OptableTargeting(response.body()?.ortb2.toString())
                    return@withContext NetworkResponse.Success(result)
                } else {
                    return@withContext NetworkResponse.Error("Request failed with status code $response: ${response.errorBody()}")
                }
            }
        }
        return result.getOrNull() ?: NetworkResponse.Error(result.exceptionOrNull()?.message ?: "Unknown error")

    }

    suspend fun witness(
        event: String,
        properties: OptableWitnessProperties,
    ): EdgeResponse<OptableWitnessResponse, OptableResponse.Error> {
        val evtBody = HashMap<String, Any>()
        evtBody.put("event", event)
        evtBody.put("properties", properties)
        return edgeService.witness(evtBody)
    }

}