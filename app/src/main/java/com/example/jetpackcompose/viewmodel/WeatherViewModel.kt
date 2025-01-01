package com.example.jetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcompose.api.WeatherApiService
import com.example.jetpackcompose.data.ForecastItem
import com.example.jetpackcompose.data.WeatherData
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing weather data and forecasts.
 *
 * This class contains the logic for fetching weather and forecast data from the
 * [WeatherApiService] and provides this data through StateFlow objects to update the UI.
 *
 * It also includes StateFlows for error messages and fetching weather icons.
 * The ViewModel performs the fetch operations for weather and forecast data in the background
 * using [viewModelScope] for coroutine management.
 */
class WeatherViewModel : ViewModel() {

    /**
     * The current weather data fetched from the API.
     */
    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather: StateFlow<WeatherData?> = _currentWeather

    /**
     * The list of forecast data for the coming days.
     */
    private val _forecast = MutableStateFlow<List<ForecastItem>>(emptyList())
    val forecast: StateFlow<List<ForecastItem>> = _forecast

    /**
     * The URL of the weather icon fetched from the current weather data.
     */
    private val _iconUrl = MutableStateFlow<String?>(null)
    val iconUrl: StateFlow<String?> get() = _iconUrl

    /**
     * A possible error message that occurs when fetching weather or forecast data.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    /**
     * Fetches the current weather data for a given city and API key.
     *
     * This function makes a request to the API and stores the result in [currentWeather].
     * If an error occurs, it sets an appropriate error message.
     *
     * @param city The name of the city for which the weather is being fetched.
     * @param apiKey The API key to access the weather data.
     */
    fun fetchWeatherData(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val weatherResponse = WeatherApiService.fetchWeather(city, apiKey)
                if (weatherResponse != null) {
                    _currentWeather.value = weatherResponse
                    fetchWeatherIcon(weatherResponse.weather.firstOrNull()?.icon.orEmpty())
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to fetch weather. Please check your API key or city name."
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Fetches the weather forecast data for a given city and API key.
     *
     * This function makes a request to the API and stores the result in [forecast].
     * If an error occurs, it sets an appropriate error message.
     *
     * @param city The name of the city for which the forecast is being fetched.
     * @param apiKey The API key to access the forecast data.
     */
    fun fetchForecastData(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val forecastResponse = WeatherApiService.fetchForecast(city, apiKey)
                if (forecastResponse != null) {
                    _forecast.value = forecastResponse.list
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to fetch forecast. Please check your API key or city name."
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Fetches the URL of the weather icon based on the icon ID from the current weather data.
     *
     * @param iconId The ID of the weather icon.
     */
    private fun fetchWeatherIcon(iconId: String) {
        if (iconId.isNotEmpty()) {
            _iconUrl.value = "https://openweathermap.org/img/wn/$iconId@2x.png"
        }
    }
}
