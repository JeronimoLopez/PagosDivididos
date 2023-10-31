package com.android.pagosdivididos.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity
data class EventData(
    @PrimaryKey val id: UUID,
    val title:String = "",
    val expensesList: List<Expenses>,
    val memberList:List<Member>,
    val timeStamp:Instant
)
