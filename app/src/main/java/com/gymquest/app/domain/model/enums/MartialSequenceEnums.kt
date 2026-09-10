package com.gymquest.app.domain.model.enums

/** Direction faced by a step in a martial sequence. */
enum class MartialDirection {
    FRONT,
    BACK,
    LEFT,
    RIGHT,
    FRONT_LEFT,
    FRONT_RIGHT,
    BACK_LEFT,
    BACK_RIGHT,
}

enum class MartialSide {
    LEFT,
    RIGHT,
    BOTH,
}

enum class MartialTechniqueFamily {
    PUNCH,
    KICK,
    BLOCK,
    OPEN_HAND,
    ARM_POSTURE,
    CONTROL,
    OTHER,
}

enum class MartialMediaType {
    IMAGE,
    GIF,
    VIDEO,
}
