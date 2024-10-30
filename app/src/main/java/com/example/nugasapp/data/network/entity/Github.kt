package com.example.nugasapp.data.network.entity

data class Github(
    val login: String,
    val id: Int,
    val avatar_url: String,
    val name: String,
    val public_repos: Int,
    val followers: Int,
    val following: Int,
)