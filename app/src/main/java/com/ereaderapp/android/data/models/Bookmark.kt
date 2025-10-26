package com.ereaderapp.android.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bookmark(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("pageNumber")
    val pageNumber: Int = 0,

    @SerializedName("createdAt")
    val createdAt: String = ""
) : Parcelable

data class CreateBookmarkRequest(
    val bookId: Int,
    val pageNumber: Int,
    val title: String
)