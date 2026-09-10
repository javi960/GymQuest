package com.gymquest.app.data.local

import androidx.room.TypeConverter
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.MasteryRank
import com.gymquest.app.domain.model.enums.MartialDirection
import com.gymquest.app.domain.model.enums.MartialSide
import com.gymquest.app.domain.model.enums.MartialTechniqueFamily
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.model.enums.SetType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import com.gymquest.app.domain.model.enums.Weekday
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
    fun weekdayToStorageValue(value: Weekday?): String? = value?.name

    @TypeConverter
    fun storageValueToWeekday(value: String?): Weekday? = value?.let(Weekday::valueOf)

    @TypeConverter
    fun masteryRankToStorageValue(value: MasteryRank?): String? = value?.name

    @TypeConverter
    fun storageValueToMasteryRank(value: String?): MasteryRank? = value?.let(MasteryRank::valueOf)

    @TypeConverter
    fun martialDirectionToStorageValue(value: MartialDirection?): String? = value?.name

    @TypeConverter
    fun storageValueToMartialDirection(value: String?): MartialDirection? = value?.let(MartialDirection::valueOf)

    @TypeConverter
    fun martialSideToStorageValue(value: MartialSide?): String? = value?.name

    @TypeConverter
    fun storageValueToMartialSide(value: String?): MartialSide? = value?.let(MartialSide::valueOf)

    @TypeConverter
    fun martialTechniqueFamilyToStorageValue(value: MartialTechniqueFamily?): String? = value?.name

    @TypeConverter
    fun storageValueToMartialTechniqueFamily(value: String?): MartialTechniqueFamily? = value?.let(MartialTechniqueFamily::valueOf)
}
