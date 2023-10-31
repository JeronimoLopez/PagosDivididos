package com.android.pagosdivididos.database

import android.os.Parcelable
import java.util.UUID
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Expenses(
    val expensesId:UUID,
    val expenseName: String,
    var memberThatPaid:@RawValue Member,
    val amountToPay: String,
    var expensesMemberList:@RawValue List<Member>
) : Parcelable
