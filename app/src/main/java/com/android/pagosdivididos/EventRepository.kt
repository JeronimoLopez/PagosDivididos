package com.android.pagosdivididos

import android.content.Context
import androidx.room.Room
import com.android.pagosdivididos.database.EventData
import com.android.pagosdivididos.database.PaymentsDatabase
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val TAG = "EventRepository"
private const val DATABASE_NAME = "payment-database"

//TODO remove globalScope
class EventRepository private constructor(
    context: Context
) {

    private val database: PaymentsDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            PaymentsDatabase::class.java,
            DATABASE_NAME
        ).build()

    fun getEventList(): Flow<List<EventData>> = database.paymentsDao().getEventList()

    suspend fun getEvent(id: UUID): EventData? = database.paymentsDao().getEvent(id)

    suspend fun addEvent(event: EventData) = database.paymentsDao().addEvent(event)

    suspend fun updateEvent(event: EventData) {
        database.paymentsDao().updateEvent(event)
    }

    suspend fun deleteEvent(eventData: EventData) {
        database.paymentsDao().deleteEvent(eventData)
    }


    companion object {
        private var INSTANCE: EventRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = EventRepository(context)
            }
        }

        fun get(): EventRepository {
            return INSTANCE ?: throw IllegalStateException("EventyRepository must be initialized")
        }
    }
}