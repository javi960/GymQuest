package com.gymquest.app.data.seed

import com.gymquest.app.domain.model.enums.MartialTechniqueFamily
import java.text.Normalizer

/** Built-in, reusable technical library for Shito-Ryu Karate. */
internal object ShitoRyuCatalog {
    data class Stance(val name: String, val translation: String, val description: String)
    data class Technique(
        val name: String,
        val translation: String,
        val description: String,
        val family: MartialTechniqueFamily,
    )

    fun matchesStyle(name: String): Boolean = normalize(name) == "shito ryu"

    fun normalizedEntryName(name: String): String = normalize(name)

    val stances = listOf(
        Stance("Musubi Dachi", "Posición de los talones juntos", "Posición formal en la que los talones permanecen unidos y los pies se abren formando una V. Se emplea especialmente en saludos, atención y momentos formales. La espalda permanece recta y el peso se reparte de manera equilibrada."),
        Stance("Heisoku Dachi", "Posición de los pies juntos", "Los pies permanecen completamente juntos, paralelos y alineados. Es una posición de preparación y concentración que puede aparecer al comienzo, final o durante transiciones de un kata."),
        Stance("Heiko Dachi", "Posición de los pies paralelos", "Los pies se separan aproximadamente al ancho de los hombros y apuntan hacia delante. Es una postura neutral, estable y versátil, habitual en kihon y como posición preparatoria."),
        Stance("Nainfanchin Dachi / Naifanchi Dachi", "Posición de la montura", "Posición amplia y estable, con los pies aproximadamente paralelos y las rodillas flexionadas. Favorece el trabajo lateral, el fortalecimiento de piernas y el control del centro de gravedad."),
        Stance("Shiko Dachi", "Posición del jinete", "Posición muy amplia con las rodillas flexionadas y los pies orientados hacia fuera. Ofrece una base sólida y baja, muy útil en movimientos que necesitan estabilidad y potencia desde el suelo."),
        Stance("Neko Ashi Dachi", "Posición del pie de gato", "La mayor parte del peso se concentra en la pierna trasera y el pie delantero mantiene un apoyo ligero. Facilita cambios rápidos, desplazamientos y transiciones."),
        Stance("Moto Dachi", "Posición corta", "Posición parecida a Zenkutsu Dachi, pero más corta y compacta. Permite una mayor movilidad y facilita cambios rápidos de dirección o técnicas explosivas."),
        Stance("Zenkutsu Dachi", "Posición adelantada", "Posición larga con una pierna adelantada y flexionada y la trasera extendida. Proporciona una base sólida para acciones lineales y desplazamientos hacia delante."),
        Stance("Koukutsu Dachi", "Posición atrás", "La mayor parte del peso se sitúa sobre la pierna trasera, mientras la delantera permanece más ligera. Favorece transiciones, cambios de distancia y movimientos hacia atrás."),
        Stance("Kosa Dachi", "Posición cruzada", "Las piernas se cruzan para facilitar giros, cambios de dirección o transiciones. Requiere control del equilibrio y de la distribución del peso."),
        Stance("Kake Dachi", "Posición de enganche", "Posición cruzada en la que el peso se mantiene principalmente sobre una pierna. Se emplea en giros y transiciones rápidas dentro de secuencias técnicas."),
        Stance("Tsuru Ashi Dachi", "Posición de la grulla", "Posición sobre una sola pierna, mientras la otra queda elevada junto a la pierna de apoyo. Requiere equilibrio y control corporal y puede preparar técnicas de pierna."),
        Stance("Sanchin Dachi", "Posición de las tres batallas", "Posición corta, compacta y sólida, con rodillas flexionadas y estructura corporal firme. Se utiliza para trabajar estabilidad, respiración, tensión corporal y fortalecimiento."),
    )

    val techniques = listOf(
        Technique("Oi Zuki", "Golpe de puño en persecución", "Golpe directo ejecutado acompañando el movimiento con un avance. El desplazamiento corporal contribuye a generar potencia y permite cubrir distancia. Puede integrarse en combinaciones y secuencias de kata.", MartialTechniqueFamily.PUNCH),
        Technique("Gyaku Zuki", "Golpe de puño inverso", "Golpe directo realizado con el brazo contrario a la pierna adelantada. La rotación de la cadera es fundamental para transmitir fuerza desde la base hasta el puño.", MartialTechniqueFamily.PUNCH),
        Technique("Kizami Zuki", "Golpe de puño adelantado", "Técnica rápida realizada con el brazo adelantado. Tiene poco recorrido y resulta útil para controlar la distancia, anticiparse o crear una apertura para otra técnica.", MartialTechniqueFamily.PUNCH),
        Technique("Mae Geri", "Patada frontal", "Patada ejecutada directamente hacia delante. Su trayectoria sencilla permite realizarla con rapidez y utilizarla a diferentes alturas.", MartialTechniqueFamily.KICK),
        Technique("Yoko Geri", "Patada lateral", "Patada realizada hacia un lateral utilizando la alineación corporal y el movimiento de la cadera para generar fuerza. Proporciona un buen alcance.", MartialTechniqueFamily.KICK),
        Technique("Mawashi Geri", "Patada circular", "Patada ejecutada describiendo una trayectoria circular. Permite alcanzar desde ángulos distintos a las técnicas lineales y puede realizarse a diferentes alturas.", MartialTechniqueFamily.KICK),
        Technique("Ura Zuki", "Golpe de puño hacia arriba", "Golpe corto y ascendente diseñado para distancias reducidas. Su recorrido compacto facilita una ejecución rápida cuando existe poco espacio.", MartialTechniqueFamily.PUNCH),
        Technique("Tate Zuki", "Golpe de puño vertical", "Golpe directo en el que el puño permanece en posición vertical. Tiene una trayectoria compacta y resulta sencillo de combinar con otras técnicas.", MartialTechniqueFamily.PUNCH),
        Technique("Shuto Uchi", "Golpe con mano de cuchillo", "Técnica ejecutada utilizando el canto exterior de la mano. Puede aplicarse desde distintos ángulos y su movimiento puede tener funciones de impacto, control o aplicación dentro del bunkai.", MartialTechniqueFamily.OPEN_HAND),
        Technique("Uraken Uchi", "Golpe de revés con el puño", "Técnica rápida realizada con el dorso del puño. Utiliza un recorrido relativamente corto y puede ejecutarse desde diferentes ángulos.", MartialTechniqueFamily.PUNCH),
        Technique("Jodan Uke", "Recepción o bloqueo alto", "Movimiento ascendente del brazo destinado a trabajar sobre la línea alta del cuerpo. Puede interceptar, desviar o controlar una acción y también puede tener aplicaciones ofensivas según el bunkai.", MartialTechniqueFamily.BLOCK),
        Technique("Chudan Uke", "Recepción o bloqueo medio", "Movimiento del brazo que trabaja principalmente sobre la zona media del cuerpo. Permite interceptar o desviar y enlazar posteriormente con otras acciones.", MartialTechniqueFamily.BLOCK),
        Technique("Gedan Barai", "Barrido descendente o recepción baja", "Movimiento descendente y de barrido realizado con el brazo. Aunque habitualmente se aprende como técnica frente a acciones bajas, también puede emplearse para golpear, liberar, desplazar o controlar.", MartialTechniqueFamily.BLOCK),
        Technique("Soto Uke", "Recepción exterior", "Movimiento de antebrazo con trayectoria circular o lateral que permite modificar la dirección de una acción que llega hacia el cuerpo.", MartialTechniqueFamily.BLOCK),
        Technique("Uchi Uke", "Recepción interior", "Técnica ejecutada mediante un movimiento del brazo desde el interior hacia el exterior. Puede utilizarse para desviar, controlar o generar una apertura.", MartialTechniqueFamily.BLOCK),
        Technique("Shuto Uke", "Recepción con mano de cuchillo", "Técnica ejecutada con la mano abierta en forma de shuto. Puede utilizarse para recibir, desviar, controlar, sujetar o incluso golpear, dependiendo de la aplicación.", MartialTechniqueFamily.OPEN_HAND),
        Technique("Kake Uke", "Recepción en gancho", "Movimiento curvo o de enganche utilizado para acompañar, desviar o controlar una extremidad. Es especialmente útil para enlazar con controles o técnicas posteriores.", MartialTechniqueFamily.CONTROL),
        Technique("Morote Uke", "Recepción reforzada o a dos manos", "Técnica en la que ambos brazos participan. Un brazo realiza la acción principal mientras el segundo refuerza la estructura o colabora en el movimiento.", MartialTechniqueFamily.BLOCK),
        Technique("Nagashi Uke", "Recepción deslizante", "Técnica basada en acompañar y redirigir una acción en lugar de detenerla directamente. Busca mantener el movimiento y aprovechar la trayectoria para continuar con otra técnica.", MartialTechniqueFamily.CONTROL),
        Technique("Tetsui Uchi", "Golpe de martillo", "Golpe realizado utilizando la zona inferior o lateral del puño cerrado como si fuera un martillo. Puede ejecutarse desde diferentes trayectorias y aparece en secuencias de kata.", MartialTechniqueFamily.PUNCH),
        Technique("Age Uke", "Recepción ascendente", "Técnica en la que el antebrazo asciende para trabajar sobre la línea alta. En la descripción de Pinan Nidan aparece realizada de forma sucesiva derecha–izquierda–derecha.", MartialTechniqueFamily.BLOCK),
        Technique("Yoko Uchi", "Golpe lateral", "Técnica ejecutada lateralmente con el brazo. Puede aparecer asociada a posiciones laterales como Heiko Dachi y cumplir diferentes funciones según la aplicación.", MartialTechniqueFamily.OTHER),
        Technique("Shuto Barai", "Barrido con mano de cuchillo", "Movimiento de barrido ejecutado utilizando una configuración de shuto. En el ejemplo de Pinan Nidan aparece asociado a Shiko Dachi y desplazamientos diagonales.", MartialTechniqueFamily.OPEN_HAND),
    )

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .replace("[^a-z0-9]+".toRegex(), " ")
            .trim()
}
