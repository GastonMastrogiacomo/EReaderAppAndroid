package com.ereaderapp.android.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Library(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("bookCount")
    val bookCount: Int = 0,

    @SerializedName("books")
    val books: List<Book> = emptyList()
) : Parcelable