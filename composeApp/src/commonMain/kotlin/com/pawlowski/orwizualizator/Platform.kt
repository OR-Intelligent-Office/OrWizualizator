package com.pawlowski.orwizualizator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform