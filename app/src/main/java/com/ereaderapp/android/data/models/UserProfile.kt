package com.ereaderapp.android.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
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
    val createdAt: String? = null,

    @SerializedName("statistics")
    val statistics: UserStatistics = UserStatistics()
) : Parcelable

@Parcelize
data class UserStatistics(
    @SerializedName("totalBooksRead")
    val totalBooksRead: Int = 0,

    @SerializedName("totalPagesRead")
    val totalPagesRead: Int = 0,

    @SerializedName("totalReadingHours")
    val totalReadingHours: Double = 0.0,

    @SerializedName("totalReviews")
    val totalReviews: Int = 0,

    @SerializedName("totalLibraries")
    val totalLibraries: Int = 0
) : Parcelable

@Parcelize
data class ReadingActivity(
    @SerializedName("bookId")
    val bookId: Int = 0,

    @SerializedName("book")
    val book: BookInfo = BookInfo(),

    @SerializedName("firstAccess")
    val firstAccess: String = "",

    @SerializedName("lastAccess")
    val lastAccess: String = "",

    @SerializedName("accessCount")
    val accessCount: Int = 0,

    @SerializedName("totalPagesRead")
    val totalPagesRead: Int = 0,

    @SerializedName("lastPageRead")
    val lastPageRead: Int = 0,

    @SerializedName("totalReadingTimeMinutes")
    val totalReadingTimeMinutes: Int = 0,

    @SerializedName("readingProgress")
    val readingProgress: Double = 0.0
) : Parcelable

@Parcelize
data class BookInfo(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("author")
    val author: String = "",

    @SerializedName("imageLink")
    val imageLink: String? = null,

    @SerializedName("pageCount")
    val pageCount: Int? = null
) : Parcelable