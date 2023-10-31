package com.android.pagosdivididos.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [EventData::class], version = 1, exportSchema = true)
@TypeConverters(ListExpensesConverter::class, ListMemberConverter::class, InstantTypeConverter::class)
abstract class PaymentsDatabase : RoomDatabase() {
    abstract fun paymentsDao(): PaymentsDao
}