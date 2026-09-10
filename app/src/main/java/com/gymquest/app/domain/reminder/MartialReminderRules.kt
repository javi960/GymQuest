package com.gymquest.app.domain.reminder

object MartialReminderRules {
    private val time = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun canSchedule(missionsToday: Int, missionsPerDay: Int): Boolean =
        missionsPerDay in 1..5 && missionsToday < missionsPerDay

    fun validateWindow(start: String, end: String, existing: List<Pair<String, String>>) {
        require(time.matches(start) && time.matches(end)) { "Usa el formato HH:mm." }
        require(start < end) { "La ventana debe terminar después de empezar." }
        require(existing.none { (currentStart, currentEnd) -> start < currentEnd && currentStart < end }) {
            "La ventana se solapa con otra ya configurada."
        }
    }
}
