package com.example.jetpackcompose.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.jetpackcompose.data.ForecastItem
import com.example.jetpackcompose.storage.Keys
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpackcompose.viewmodel.WeatherViewModel
import com.example.jetpackcompose.ui.components.SearchBarSample
import com.example.jetpackcompose.ui.components.WeatherCard

/**
 * A composable function that displays the weather forecast for a city.
 * It includes a search bar to search for forecasts and displays the fetched forecast data.
 * The city name and API key are fetched from the DataStore, and if the data is available,
 * it fetches the forecast data using the [WeatherViewModel].
 *
 * The view shows:
 * - A search bar for users to input a city name.
 * - A list of weather forecast items for the specified city.
 * - An error message if there was an issue fetching the data.
 * - A message prompting the user to set their hometown if no search query is provided.
 *
 * @param forecast A list of [ForecastItem] containing the weather forecast data to be displayed.
 */
@Composable
fun ForecastWeatherView(forecast: List<ForecastItem>) {
    val context = LocalContext.current
    var hometown by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    val weatherViewModel: WeatherViewModel = viewModel()
    val errorMessage by weatherViewModel.errorMessage.collectAsState()

    // Retrieve hometown and apiKey from DataStore
    LaunchedEffect(Unit) {
        context.dataStore.data.collect { preferences ->
            hometown = preferences[Keys.HOMETOWN_KEY] ?: ""
            apiKey = preferences[Keys.API_TOKEN_KEY] ?: ""

            if (hometown.isNotEmpty() && apiKey.isNotEmpty()) {
                weatherViewModel.fetchForecastData(hometown, apiKey)
            }
        }
    }

    val searchQuery = rememberSaveable { mutableStateOf("") }

    // Search Bar for querying the forecast data
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SearchBarSample(
            selectedMenu = "Forecast",
            apiKey = apiKey,
            onQueryChanged = { query ->
                searchQuery.value = query
                if (query.isNotEmpty()) {
                    weatherViewModel.fetchForecastData(query, apiKey)
                } else {
                    if (hometown.isNotEmpty() && apiKey.isNotEmpty()) {
                        weatherViewModel.fetchForecastData(hometown, apiKey)
                    }
                }
            }
        )
    }

    // Display error message if there was an issue fetching the forecast
    errorMessage?.let {
        Text(
            text = it,
            color = Color.Red,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 25.sp),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Display message if no city or forecast data is available
        if (searchQuery.value.isEmpty() && hometown.isEmpty()) {
            Text(
                text = "Set your hometown in settings",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.padding(16.dp)
            )
        } else if (forecast.isNotEmpty()) {
            Text(
                text = "Forecast for ${searchQuery.value.takeIf { it.isNotEmpty() } ?: hometown}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    color = Color.Black
                ),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Display the list of forecast items in a LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(forecast.size) { index ->
                    WeatherCard(forecastItem = forecast[index])
                }
            }

        }

    }
}
