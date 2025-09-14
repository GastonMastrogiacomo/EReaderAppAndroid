package com.ereaderapp.android.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("author")
    val author: String = "",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("imageLink")
    val imageLink: String? = null,

    @SerializedName("releaseDate")
    val releaseDate: String? = null,

    @SerializedName("pageCount")
    val pageCount: Int? = null,

    @SerializedName("score")
    val score: Double? = null,

    @SerializedName("authorBio")
    val authorBio: String? = null,

    @SerializedName("pdfPath")
    val pdfPath: String? = null,

    @SerializedName("averageRating")
    val averageRating: Double = 0.0,

    @SerializedName("reviewCount")
    val reviewCount: Int = 0
) : Parcelable