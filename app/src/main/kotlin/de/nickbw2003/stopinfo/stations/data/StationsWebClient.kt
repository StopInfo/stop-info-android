package de.nickbw2003.stopinfo.stations.data

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import de.nickbw2003.stopinfo.common.data.OkHttpClientFactory
import de.nickbw2003.stopinfo.common.data.models.Error
import de.nickbw2003.stopinfo.stations.data.models.Station
import de.nickbw2003.stopinfo.common.data.WebException
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

class StationsWebClient(baseUrl: String) {
    private interface StationsApi {
        @GET("stations/byName/{name}")
        fun findByNameAsync(@Path("name") name: String, @Header("x-network") network: String): Deferred<Response<List<Station>>>

        @GET("stations/byLatLng/{lat}/{lng}")
        fun findByLatLngAsync(@Path("lat") lat: String, @Path("lng") lng: String, @Header("x-network") network: String): Deferred<Response<List<Station>>>
    }

    private val stationsApi: StationsApi = Retrofit.Builder()
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .client(OkHttpClientFactory.client)
        .build()
        .create()

    suspend fun findByName(name: String, network: String): List<Station>? {
        return findStations(Error.DATA_LOADING_ERROR_STATIONS_BY_NAME) {
            stationsApi.findByNameAsync(
                name,
                network
            )
        }
    }

    suspend fun findByLatLng(lat: Double, lng: Double, network: String): List<Station>? {
        val formattedLat = (lat * 1E6).toLong().toString()
        val formattedLng = (lng * 1E6).toLong().toString()

        return findStations(Error.DATA_LOADING_ERROR_STATIONS_BY_LAT_LNG) {
            stationsApi.findByLatLngAsync(formattedLat, formattedLng, network)
        }
    }

    private suspend fun findStations(
        error: Error,
        findOperation: () -> Deferred<Response<List<Station>>>
    ): List<Station>? {
        val response: Response<List<Station>>?

        try {
            response = findOperation().await()
        } catch (ex: Exception) {
            throw WebException(
                error,
                WebException.UNKNOWN_RESPONSE_CODE,
                ex
            )
        }

        if (response.isSuccessful) {
            return response.body()
        } else {
            throw WebException(error, response.code())
        }
    }
}