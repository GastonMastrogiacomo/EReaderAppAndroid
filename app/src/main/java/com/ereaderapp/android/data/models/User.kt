package com.ereaderapp.android.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("role")
    val role: String = "User",

    @SerializedName("createdAt")
    val createdAt: String? = null
) : Parcelable