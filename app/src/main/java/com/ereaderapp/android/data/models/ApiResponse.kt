package com.ereaderapp.android.data.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("token")
    val token: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("expiresIn")
    val expiresIn: Long = 0,

    @SerializedName("message")
    val message: String? = null
)

data class BooksResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<Book> = emptyList(),

    @SerializedName("pagination")
    val pagination: Pagination? = null,

    @SerializedName("message")
    val message: String? = null
)

data class Pagination(
    @SerializedName("currentPage")
    val currentPage: Int = 1,

    @SerializedName("pageSize")
    val pageSize: Int = 10,

    @SerializedName("totalItems")
    val totalItems: Int = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 0,

    @SerializedName("hasNextPage")
    val hasNextPage: Boolean = false,

    @SerializedName("hasPreviousPage")
    val hasPreviousPage: Boolean = false
)