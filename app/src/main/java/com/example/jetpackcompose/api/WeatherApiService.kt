package com.example.jetpackcompose.api

import android.util.Log
import com.example.jetpackcompose.data.ForecastData
import com.example.jetpackcompose.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * A singleton object that provides the weather API service to fetch weather data and forecasts.
 * This object handles the creation of the Retrofit instance and performs network requests
 * to fetch weather-related information from the OpenWeather API.
 */
object WeatherApiService {

    /**
     * The base URL for the OpenWeather API.
     */
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    /**
     * OkHttpClient used to perform network operations.
     */
    private val client = OkHttpClient.Builder().build()

    /**
     * Retrofit instance configured with the base URL, OkHttpClient, and Gson converter to
     * handle JSON responses.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * The API interface that defines the network calls to fetch weather and forecast data.
     */
    private val api = retrofit.create(WeatherApi::class.java)

    /**
     * Interface defining the methods to interact with the OpenWeather API.
     * These methods are used for fetching current weather data and forecast data for a given city.
     */
    interface WeatherApi {

        /**
         * Fetches the current weather data for a given city.
         *
         * @param city The name of the city for which the weather is fetched.
         * @param apiKey The API key required to authenticate the request to the OpenWeather API.
         * @param units The unit system used for the weather data. Defaults to "metric".
         * @return A [retrofit2.Response] containing the [WeatherData] for the city.
         */
        @GET("weather")
        suspend fun fetchWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): retrofit2.Response<WeatherData>

        /**
         * Fetches the weather forecast for a given city.
         *
         * @param city The name of the city for which the weather forecast is fetched.
         * @param apiKey The API key required to authenticate the request to the OpenWeather API.
         * @param units The unit system used for the weather forecast. Defaults to "metric".
         * @return A [retrofit2.Response] containing the [ForecastData] for the city.
         */
        @GET("forecast")
        suspend fun fetchForecast(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): retrofit2.Response<ForecastData>
    }

    /**
     * Fetches the current weather for a given city using the OpenWeather API.
     * If the request is successful, the corresponding [WeatherData] object is returned.
     * Otherwise, `null` is returned, and an error is logged.
     *
     * @param city The name of the city for which to fetch the weather.
     * @param apiKey The API key required for the request.
     * @return The [WeatherData] object for the city, or `null` if the request fails.
     */
    suspend fun fetchWeather(city: String, apiKey: String): WeatherData? {
        return try {
            withContext(Dispatchers.Default) {
                val response = api.fetchWeather(city, apiKey)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("WeatherApiService", "Failed to fetch data: ${response.code()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherApiService", "Error fetching data: ${e.message}")
            null
        }
    }

    /**
     * Fetches the weather forecast for a given city using the OpenWeather API.
     * If the request is successful, the corresponding [ForecastData] object is returned.
     * Otherwise, `null` is returned, and an error is logged.
     *
     * @param city The name of the city for which to fetch the forecast.
     * @param apiKey The API key required for the request.
     * @return The [ForecastData] object for the city, or `null` if the request fails.
     */
    suspend fun fetchForecast(city: String, apiKey: String): ForecastData? {
        return try {
            // Using Dispatchers.IO instead of Default since this is an I/O operation
            withContext(Dispatchers.IO) {
                val response = api.fetchForecast(city, apiKey)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("WeatherApiService", "Failed to fetch forecast: ${response.code()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherApiService", "Error fetching forecast: ${e.message}")
            null
        }
    }
}
