# Registro de assets y licencia

Fecha de revision: 2026-09-08. Este archivo es el inventario obligatorio para todo recurso visual empaquetado por GymQuest.

## Activos propios de GymQuest

Los seis vectores siguientes fueron dibujados para este repositorio como geometria abstracta de un solo color. Su autor es GymQuest contributors y se distribuyen bajo **Apache-2.0**, junto con el codigo del proyecto. No representan personajes, armas, monstruos, logotipos ni composiciones de una franquicia existente.

| Archivo | Uso | Formato/tamano | Procedencia/licencia |
| --- | --- | --- | --- |
| `res/drawable/asset_character.xml` | Personaje y fallback de retrato | Vector, 359 B | Original GymQuest / Apache-2.0 |
| `res/drawable/asset_xp.xml` | XP y nivel | Vector, 269 B | Original GymQuest / Apache-2.0 |
| `res/drawable/asset_mission.xml` | Misiones | Vector, 293 B | Original GymQuest / Apache-2.0 |
| `res/drawable/asset_achievement.xml` | Logros | Vector, 424 B | Original GymQuest / Apache-2.0 |
| `res/drawable/asset_empty_state.xml` | Estados vacios | Vector, 328 B | Original GymQuest / Apache-2.0 |
| `res/drawable/asset_exercise_placeholder.xml` | Multimedia de ejercicio ausente | Vector, 313 B | Original GymQuest / Apache-2.0 |

## Recursos de plataforma

| Recurso | Uso | Procedencia/licencia |
| --- | --- | --- |
| `ic_launcher_background.xml`, `ic_launcher_foreground.xml` y variantes `mipmap-*` | Icono de aplicacion | Plantilla de Android Studio; Apache-2.0 |
| `androidx.compose.material:material-icons-extended` | Acciones Material restantes | Google Material Icons; Apache-2.0 |
| Tipografia | Texto de la aplicacion | Familias del sistema Android; no se empaqueta una fuente de terceros |

Los 58 retratos PNG de rangos y cinturones en `res/drawable-nodpi/` son assets originales del usuario. Su titular y procedencia son el usuario del proyecto; la licencia de uso dentro de GymQuest queda autorizada por el titular. `CharacterCategoryRules` usa los 51 retratos de fuerza según el nivel y `MartialBelt` usa los siete cinturones manuales del Dojo. No se deben optimizar, sustituir ni eliminar sin autorización expresa del propietario. Si faltase un archivo, `CharacterHeader` muestra la copa vectorial como respaldo.

No se empaquetan fotografias, audio, musica, tipografias descargadas ni assets obtenidos de la web. Todo multimedia de ejercicios futuro es contenido local proporcionado por la persona usuaria y debe conservar autoria y licencia en su propia ficha; si falta se usa `QuestExerciseMediaPlaceholder`.

## Reglas de identidad y rendimiento

- No introducir nombres, logos, personajes, armas, monstruos, tipografias, musica ni composiciones reconocibles de Dragon Quest u otra franquicia. La fantasia se limita a vocabulario funcional de entrenamiento y formas abstractas.
- Un nuevo asset requiere fila en este registro antes de fusionarse: origen, licencia, propietario, formato, peso y pantalla que lo usa.
- Preferir `VectorDrawable` monocromo tintado con tokens. Raster solo si aporta informacion que un vector no puede representar; optimizarlo a WebP/AVIF compatible, con dimensiones de presentacion y sin copias por densidad innecesarias.
- Limite de revision: cualquier raster individual superior a 100 KiB o pantalla que decodifique mas de 1 MiB visible a la vez necesita una excepcion documentada en `VISUAL_SYSTEM.md`.
- Las ilustraciones deben ser neutras, geométricas y secundarias al registro de datos: no rasgos faciales expresivos, personajes infantiles, armas ni escenas de combate.

## Verificacion offline

La comprobacion reproducible de empaquetado es `./gradlew :app:assembleDebug --offline`. La APK resultante no necesita descargar assets: los recursos de esta tabla estan dentro de `res/` y las ilustraciones no dependen de red. La validacion final en dispositivo exige activar modo avion, abrir Inicio, Catalogo y un estado vacio, y confirmar que aparecen los vectores y el placeholder sin errores de carga.
