package com.gymquest.app.domain.model

data class Achievement(
    val id: Long = 0,
    val code: String,
    val name: String,
    val description: String,
    val category: String? = null,
    val xpReward: Long = 0,
    val isBuiltIn: Boolean = true,
) {
    init {
        require(code.isNotBlank() && name.isNotBlank() && description.isNotBlank() && xpReward >= 0) {
            "El logro no es valido."
        }
    }
}
