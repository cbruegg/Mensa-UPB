package com.cbruegg.mensaupb.model

enum class UserType(internal val id: String) {
    STUDENT("student"), WORKER("worker"), GUEST("guest")

    companion object {
        fun findById(id: String): UserType? = values().firstOrNull { it.id == id }
    }
}