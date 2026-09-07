package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant

@Entity(
    tableName = "gym_machines",
    foreignKeys = [
        ForeignKey(entity = GymEntity::class, parentColumns = ["id"], childColumns = ["gymId"]),
        ForeignKey(
            entity = ExerciseVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseVariantId"],
        ),
    ],
    indices = [Index(value = ["gymId"]), Index(value = ["exerciseVariantId"])],
)
data class GymMachineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gymId: Long,
    val exerciseVariantId: Long? = null,
    val name: String,
    val brand: String? = null,
    val loadType: String? = null,
    val weightComparisonType: WeightComparisonType,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
