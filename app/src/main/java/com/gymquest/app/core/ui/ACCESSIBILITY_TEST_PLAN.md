# Validacion de accesibilidad de GymQuest

## Contrato implementado

- Las acciones iconicas se describen mediante recursos `string` y el icono interior es decorativo.
- Tarjetas de estadisticas, filas densas y progreso agrupan sus descendientes para que TalkBack anuncie una frase util. Los controles que permanezcan dentro de una fila conservan su propio foco.
- El progreso anuncia nivel, XP actual y objetivo; el descanso anuncia tiempo registrado, estado, transcurrido y objetivo. Ningun estado funcional depende solamente del color.
- Valores comparables de peso, repeticiones, tiempo y volumen usan una familia monoespaciada en las filas densas.
- Las ilustraciones sin informacion adicional tienen `contentDescription = null`; toda informacion esencial se muestra como texto Compose.

## Matriz de verificacion manual antes de beta

Ejecutar estas comprobaciones en un dispositivo o emulador estable, con datos reales y tambien con los textos largos del caso de prueba:

| Escenario | Pasos | Resultado aceptable |
| --- | --- | --- |
| TalkBack | Recorrer Inicio, Sesion, Historial, Progreso y Catalogo; abrir una tarjeta, una serie y el descanso. | Orden: encabezado, resumen, controles. Cada accion se entiende sin el icono; progreso y temporizador se anuncian completos. |
| Teclado e interruptor | Navegar con Tab/Shift+Tab y activar con Enter/Espacio; repetir con Switch Access. | Foco visible, orden logico, objetivo de 48 dp y ninguna accion solo mediante gesto. |
| Fuente al 200 % | Revisar movil compacto 320 dp, movil grande, horizontal y tablet; abrir teclado en Sesion. | No hay solapamiento, corte de texto ni accion inaccesible. La barra inferior puede ocultar etiquetas visuales, nunca las descripciones accesibles. |
| Contraste y daltonismo | Ejecutar contraste automatizado y simular deuteranopia, protanopia y tritanopia. | Texto/fondo e interactivos pasan AA; seleccionado, error, exito y destructivo se distinguen ademas por texto, icono o forma. |
| Movimiento y haptica | Activar reduccion de movimiento y desactivar vibracion del sistema. | Las acciones siguen comunicando el resultado sin depender de animacion, sonido ni haptica. |

## Prueba de comprension con usuarios

Esta prueba no se puede sustituir por una comprobacion de codigo. Antes de cerrar el bloque, realizar cinco sesiones breves con personas que no hayan usado GymQuest y registrar resultado, cita y punto de confusion:

1. Pedir que identifiquen Inicio, Sesion, Historial, Progreso y Catalogo solo por el icono y su etiqueta accesible/visible.
2. Pedir que guarden una serie de 40 kg y 8 repeticiones, inicien un descanso y la corrijan.
3. No dar instrucciones de interfaz; observar primero y preguntar despues que significaba cada accion.

Se acepta cuando al menos cuatro de cinco personas completan el registro sin ayuda y reconocen los destinos globales. Cualquier icono que cause confusion se reemplaza o gana una etiqueta visible: un tooltip nunca es la unica explicacion.
