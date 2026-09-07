package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Long = LOCAL_USER_ID,
    val displayName: String? = null,
    val preferredWeightUnit: String = "kg",
    val createdAt: Instant,
    val updatedAt: Instant,
    val notes: String? = null,
) {
    companion object {
        const val LOCAL_USER_ID = 1L
    }
}
