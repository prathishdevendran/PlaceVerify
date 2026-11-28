package com.example.placeverify.data

/** Privileged navigation is based only on an ID-token claim, never a profile field. */
fun roleFromClaims(value: Any?): String = when (value) {
    "PLACEMENT_OFFICER" -> "PLACEMENT_OFFICER"
    "SUPER_ADMIN" -> "SUPER_ADMIN"
    else -> "STUDENT"
}
