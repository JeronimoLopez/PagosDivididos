package com.android.pagosdivididos.database

data class Member(
    val memberName:String,
    var payment:Int,
    var hasToPay:Boolean = true
)
