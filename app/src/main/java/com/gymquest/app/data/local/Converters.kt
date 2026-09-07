package com.gymquest.app.data.local

import androidx.room.TypeConverter
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.MasteryRank
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.model.enums.SetType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant

class Converters {
    @TypeConverter
    fun instantToStorageValue(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun storageValueToInstant(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun equipmentTypeToStorageValue(value: EquipmentType?): String? = value?.name

    @TypeConverter
    fun storageValueToEquipmentType(value: String?): EquipmentType? = value?.let(EquipmentType::valueOf)

    @TypeConverter
    fun sessionStatusToStorageValue(value: SessionStatus?): String? = value?.name

    @TypeConverter
    fun storageValueToSessionStatus(value: String?): SessionStatus? = value?.let(SessionStatus::valueOf)

    @TypeConverter
    fun setTypeToStorageValue(value: SetType?): String? = value?.name

    @TypeConverter
    fun storageValueToSetType(value: String?): SetType? = value?.let(SetType::valueOf)

    @TypeConverter
    fun weightComparisonTypeToStorageValue(value: WeightComparisonType?): String? = value?.name

    @TypeConverter
    fun storageValueToWeightComparisonType(value: String?): WeightComparisonType? =
        value?.let(WeightComparisonType::valueOf)

    @TypeConverter
    fun masteryRankToStorageValue(value: MasteryRank?): String? = value?.name

    @TypeConverter
    fun storageValueToMasteryRank(value: String?): MasteryRank? = value?.let(MasteryRank::valueOf)
}
