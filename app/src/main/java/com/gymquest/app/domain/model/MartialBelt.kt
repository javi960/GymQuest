package com.gymquest.app.domain.model

/** Manual dojo grade; it is independent from general-training XP. */
enum class MartialBelt(
    val label: String,
    val avatarFileName: String,
) {
    WHITE("Blanco", "cinturon_blanco.png"),
    YELLOW("Amarillo", "cinturon_amarillo.png"),
    ORANGE("Naranja", "cinturon_naranja.png"),
    GREEN("Verde", "cinturon_verde.png"),
    BLUE("Azul", "cinturon_azul.png"),
    BROWN("Marrón", "cinturon_marron.png"),
    BLACK("Negro", "cinturon_negro.png"),
}
