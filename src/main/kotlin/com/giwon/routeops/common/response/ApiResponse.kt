package com.giwon.routeops.common.response

data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T
)
