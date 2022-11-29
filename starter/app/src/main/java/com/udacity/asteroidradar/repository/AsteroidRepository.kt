package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AsteroidRepository(private val database: AsteroidsDatabase) {

    private val startDate = LocalDateTime.now()

    private val endDate = LocalDateTime.now().plusDays(7)

    // All asteroids
    val allAsteroids: LiveData<List<Asteroid>> = Transformations.map(
        database.asteroidDao.getAsteroids(startDate.format(DateTimeFormatter.ISO_DATE),
        endDate.format(DateTimeFormatter.ISO_DATE))) {
        it.asDomainModel()
    }

    // Asteroids of Today
    val asteroidsOfToday: LiveData<List<Asteroid>> = Transformations.map(
        database.asteroidDao.getAsteroidsOfDay(startDate.format(DateTimeFormatter.ISO_DATE))) {
        it.asDomainModel()
    }

    // Asteroids of the Week
    val asteroidsOfWeek: LiveData<List<Asteroid>> = Transformations.map(
        database.asteroidDao.getAsteroidsOfWeek(startDate.format(DateTimeFormatter.ISO_DATE),
        endDate.format(DateTimeFormatter.ISO_DATE))) {
        it.asDomainModel()
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val asteroidList = AsteroidApi.retrofitService.getProperties(API_KEY)
                val result = parseAsteroidsJsonResult(JSONObject(asteroidList))
                database.asteroidDao.insertAll(*result.asDatabaseModel())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}