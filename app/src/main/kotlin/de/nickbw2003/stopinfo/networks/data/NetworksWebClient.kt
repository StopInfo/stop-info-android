package de.nickbw2003.stopinfo.networks.data

import de.nickbw2003.stopinfo.common.data.OkHttpClientFactory
import de.nickbw2003.stopinfo.common.data.WebException
import de.nickbw2003.stopinfo.common.data.models.Error
import de.nickbw2003.stopinfo.networks.data.models.NetworkInfo
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET

class NetworksWebClient(baseUrl: String) {
    private interface NetworksApi {
        @GET("networks")
        suspend fun getAvailableNetworksAsync(): Response<List<NetworkInfo>>
    }

    private val networksApi: NetworksApi = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .client(OkHttpClientFactory.client)
        .build()
        .create()

    suspend fun getAvailableNetworks(): List<NetworkInfo>? {
        val response: Response<List<NetworkInfo>>?

        try {
            response = networksApi.getAvailableNetworksAsync()
        } catch (ex: Exception) {
            throw WebException(
                Error.DATA_LOADING_ERROR_AVAILABLE_NETWORKS,
                WebException.UNKNOWN_RESPONSE_CODE,
                ex
            )
        }

        if (response.isSuccessful) {
            return response.body()
        } else {
            throw WebException(
                Error.DATA_LOADING_ERROR_AVAILABLE_NETWORKS,
                response.code()
            )
        }
    }
}