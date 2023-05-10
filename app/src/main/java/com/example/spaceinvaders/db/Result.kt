package com.example.spaceinvaders.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
class Result(
    @ColumnInfo(name = "score") var score: Int?,
    @ColumnInfo(name = "dateString") var dateString: String?,
    @ColumnInfo(name = "player") var player: String?,
    @PrimaryKey(autoGenerate = true) var id:Int=0
) : Parcelable