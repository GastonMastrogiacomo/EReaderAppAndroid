package com.ereaderapp.android.data.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = ""
) : Parcelable