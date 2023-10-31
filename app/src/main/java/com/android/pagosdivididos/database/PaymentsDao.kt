package com.android.pagosdivididos.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID


@Dao
interface PaymentsDao {
    @Query("SELECT * FROM EventData")
    fun getEventList(): Flow<List<EventData>>

    @Query("SELECT * FROM EventData WHERE id=(:id)")
    suspend fun getEvent(id: UUID): EventData?

    @Insert
    suspend fun addEvent(event: EventData)

    @Update
    suspend fun updateEvent(event: EventData)

    @Delete
    suspend fun deleteEvent(event: EventData)

}