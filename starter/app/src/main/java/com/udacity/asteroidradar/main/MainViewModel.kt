package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

enum class AsteroidApiStatus {ALL, WEEK, TODAY}
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // OVO
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidRepository(database)

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay


    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()
    val navigateToSelectedAsteroid : LiveData<Asteroid>
    get() = _navigateToSelectedAsteroid

    private val _filterAsteroid = MutableLiveData(AsteroidApiStatus.ALL)

    val asteroidLists = Transformations.switchMap(_filterAsteroid) {
        when (it) {
            AsteroidApiStatus.TODAY -> asteroidsRepository.asteroidsOfToday
            AsteroidApiStatus.WEEK -> asteroidsRepository.asteroidsOfWeek
            else -> asteroidsRepository.allAsteroids
        }
    }

    fun asteroidFiltered(filter: AsteroidApiStatus) {
        _filterAsteroid.postValue(filter)
    }

    init {
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids()
            getPictureOfDay()
        }
    }


//    private fun getAsteroidList() {
//        viewModelScope.launch {
//            try {
//                var asteroids = AsteroidApi.retrofitService.getProperties(API_KEY)
//                val asteroidList = parseAsteroidsJsonResult(JSONObject(asteroids))
//                _properties.value = asteroidList
//            } catch (e: Exception) {
//            }
//        }
//    }

    private suspend fun getPictureOfDay() {
        viewModelScope.launch {
            try {
                _pictureOfDay.postValue(
                    AsteroidApi.retrofitService.getPictureOfTheDay(API_KEY)
                )
            } catch (e: Exception) {
                Log.e("getPictureOfDay", e.message.toString())
            }
        }
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }

}