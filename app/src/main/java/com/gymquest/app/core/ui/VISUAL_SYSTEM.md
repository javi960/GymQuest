# Sistema visual de GymQuest

Este archivo es el contrato único de UI. Es propiedad compartida de `core/ui`; quien cambia un token o componente común revisa sus consumidores. Quien añade una feature reutiliza el contrato y aporta pruebas/previews. La persona revisora valida este documento en cada cambio Compose.

## Convenciones y admisión

- Tokens: `GymQuestColors`, `GymQuestSpacing`, `GymQuestRadii` y `GymQuestSizes` en `theme/`. Ninguna feature define colores, radios, sombras, tipografías o tamaños de icono equivalentes.
- Componentes: prefijo `Quest` para controles y estructura (`QuestButton`, `QuestPanel`); `Mission`, `Character`, `Stat` y `Xp` para semántica de producto.
- Acciones: solo `QuestAction` puede decidir icono, rol y descripción. Iconos decorativos usan `QuestSymbol` y `contentDescription = null` cuando su texto vecino ya transmite el dato.
- Un componente nuevo necesita: necesidad no cubierta por uno existente, API inmutable, estados loading/disabled/error aplicables, objetivos táctiles de 48 dp, descripción accesible, preview y prueba semántica si responde a interacción.
- Regla de revisión: un PR Compose se rechaza si introduce `Color(...)`, `RoundedCornerShape(...)`, `Button`, `IconButton` o `OutlinedButton` en una feature cuando `QuestTheme` o un componente `Quest*` ya cubre el caso. Una excepción exige enlace a esta sección, motivo, alcance y fecha de retirada.

## Jerarquía móvil

| Nivel | Componente y posición | Regla |
|---|---|---|
| Primaria | `QuestActionButton` con rol `Primary` | Una visible por estado; inicia o continúa la tarea de mayor frecuencia. |
| Positiva | `QuestActionButton` con rol `Positive` | Guarda o completa después de validar; no comparte fila inmediata con borrar. |
| Secundaria | `QuestButton` u `QuestIconButton` | Edita, pausa, filtra o cancela sin competir con la primaria. |
| Contextual | `QuestIconButton` en `QuestTopBar` | Solo una acción relacionada con el destino actual. |
| Navegación | `QuestBottomNavigation` / volver | Nunca se usa para mutar datos; conserva etiqueta o descripción cuando se oculta por fuente ampliada. |
| Destructiva | `QuestActionButton` rol `Destructive` + `QuestConfirmationDialog` | Siempre confirma y explica qué se conserva; separado al menos 8 dp de guardar. |

## Inventario y hallazgos actuales

| Zona | Acciones existentes | Componente actual | Hallazgo |
|---|---|---|---|
| Home | iniciar/continuar, accesos, reintentar | `QuestActionButton`, `QuestButton` | Conforme. |
| Sesión | iniciar, añadir, guardar, editar, borrar, completar, cancelar, descanso | `QuestActionButton`, diálogo común | Revisar que borrar siempre llegue mediante confirmación desde la pantalla raíz. |
| Catálogo | crear, editar, archivar, filtrar, buscar, reintentar | controles `Quest*` | Conforme; los filtros usan `QuestFilterChip`. |
| Historial/Progreso | abrir detalle, reintentar | `QuestButton` | Sin duplicados observados. |
| Ajustes/Backup/Dojo | lectura y futuros accesos | sin mutación todavía | No introducir botones Material locales al activarlos. |
| Navegación | destinos y ajustes | `QuestBottomNavigation`, `QuestTopBar` | Conforme; `Icon` directo queda encapsulado en navegación/paneles. |

No se detectaron botones Material directos dentro de features; los `Icon` directos restantes son decoración o composición interna de componentes comunes. Las excepciones se revisan en cada PR mediante `rg 'Button\\(|IconButton\\(|Color\\(|RoundedCornerShape\\(' app/src/main/java/com/gymquest/app/feature`.

## Matriz global de acciones

| Acción | Componente | Icono Material | Color/rol | Confirmación | Descripción accesible |
|---|---|---|---|---|---|
| Añadir | `QuestActionButton` | Add | Primary / azul estructural | No | Añadir elemento |
| Editar | `QuestButton` | Edit | Secondary / pergamino | No | Editar elemento |
| Guardar | `QuestActionButton` | Save | Positive / positivo | No | Guardar cambios |
| Cancelar | `QuestButton` | Close | Secondary | No | Cancelar acción |
| Descartar sesión | `QuestActionButton` + diálogo | Close | Destructive / error | Sí | Cancelar sesión y descartar progreso |
| Eliminar/archivar | `QuestActionButton` + diálogo | Delete | Destructive / error | Sí | Eliminar elemento |
| Volver | `QuestIconButton` | ArrowBack | Secondary | No | Volver atrás |
| Buscar | `QuestSearchField` | Search | Secondary | No | Buscar |
| Filtrar/ordenar | `QuestFilterChip` / menú | FilterList / Sort | Secondary | No | Filtrar resultados / Ordenar resultados |
| Abrir detalle | `QuestButton` | OpenInNew | Secondary | No | Abrir detalle |
| Iniciar/reanudar | `QuestActionButton` | PlayArrow / Replay | Primary | No | Iniciar sesión / Reanudar |
| Pausar | `QuestActionButton` | Pause | Secondary | No | Pausar |
| Completar | `QuestActionButton` + diálogo si hay series | Check | Positive | Condicional | Completar sesión |
| Navegar | `QuestBottomNavigation` | acción del destino | Secondary; Sesión Primary | No | Ir a [destino] |
| Ajustes | `QuestIconButton` | Settings | Secondary | No | Ir a ajustes |

## Criterios medibles de aceptación

1. Registro de una serie: desde sesión activa, un usuario puede introducir peso y repeticiones, guardar y ver confirmación en **máximo 5 toques** y **menos de 30 s**, sin hacer scroll para encontrar la CTA en móvil de 320 dp.
2. Legibilidad: las pruebas/previews cubren 320 dp, 412 dp, horizontal y fuente al 200 %; no hay texto truncado en la acción primaria, unidades ni mensajes de error.
3. Accesibilidad: cada control interactivo tiene 48 × 48 dp mínimo y nombre semántico; ningún estado se comunica solo por color, icono, sonido o vibración.
4. Consistencia: el 100 % de acciones de la matriz usa componente, icono, rol y confirmación documentados; cero estilos locales equivalentes tras la búsqueda de revisión.
5. Calidad: antes de aceptar una UI se ejecutan `:app:testDebugUnitTest`, `:app:lintDebug`, previews relevantes y la comprobación manual de la matriz.

## Contraste, movimiento y presets

`ColorContrastTest` verifica AA (4,5:1) para texto principal/secundario y las acciones primaria, positiva y destructiva en Classic claro y oscuro. Los estados siempre añaden etiqueta, icono, forma o mensaje: éxito usa Check, error texto/diálogo, y acciones destructivas confirmación; el color nunca es la única señal.

`GymQuestMotion` limita feedback a 100 ms, cambio de estado a 180 ms y celebraciones a 300 ms. Durante registro solo se permite feedback funcional. La futura preferencia de reducción de movimiento debe poner las tres duraciones a cero y desactivar háptica, sin afectar estado o resultado.

Los presets declarados (`MinimalGym`, `DarkDungeon`, `MartialDojo`) son contratos, no temas implementados: mientras no tengan tokens propios, resuelven a ClassicQuest. `ThemePresetPreferenceStore` delimita la futura persistencia local; solo la capa de aplicación podrá leerlo y pasar `preset` a `GymQuestTheme`.

## Botones, iconos y confirmación

`QuestSymbol` es el inventario permitido: fuerza, técnica, energía, descanso, XP, misión, personaje, logro, catálogo y descubrimiento. `QuestAction` conserva los iconos de navegación y mutación. Los estados `Normal`, `Selected`, `Loading` y `Disabled` son comunes; la acción destructiva usa siempre el rol `Destructive`.

Los iconos compactos muestran ayuda por pulsación prolongada mediante `QuestIconButton`, pero su `contentDescription` continúa siendo el nombre accesible. Cualquier acción cuyo `QuestAction.requiresConfirmation` sea verdadero debe abrir `QuestConfirmationDialog` antes de mutar datos; el texto explica la consecuencia y ofrece volver. Los previews `QuestButtonPreviews` cubren los estados en fondo claro, oscuro, pergamino y panel azul.

## Navegación y ergonomía móvil

`QuestBottomNavigation` admite exactamente hasta cinco destinos estables: Inicio, Sesión, Historial, Progreso y Catálogo. Dojo y Ajustes son accesos visibles desde Inicio; no se ocultan tras gestos. El grafo usa `launchSingleTop`, `restoreState` y `saveState` para conservar destino y scroll al alternar zonas. El `Scaffold` aplica `WindowInsets.safeDrawing`; Sesión añade `navigationBarsPadding` e `imePadding` para que el teclado no cubra el registro.

Pantallas compactas usan 20 dp de margen de contenido y controles de 48 dp; en 320 dp las filas densas permiten flujo en varias líneas y fuente ampliada. En horizontal/tablet el contenido no debe exceder 720 dp por columna: cuando se cree una pantalla de dos paneles, el panel secundario se mueve debajo de 720 dp. Guardar y borrar no pueden ser gestos: son controles visibles, con separación mínima de 8 dp y confirmación destructiva. Durante una sesión, la CTA debe seguir el final del contenido o la barra inferior, nunca quedar exclusivamente en la cabecera desplazable.

## Estados, feedback y lenguaje

El vocabulario común es `sesión`, `misión`, `aventura`, `descubrimiento`, `XP`, `nivel` y `dominio`. Nunca reemplaza las etiquetas técnicas `kg`, `reps`, `segundos`, `series` o `volumen`. `QuestVocabulary` es la fuente de estas etiquetas base.

`QuestFeedbackEvent` normaliza los mensajes de serie guardada, descanso iniciado, sesión completada, nivel, récord y descubrimiento. Los resultados transitorios usan `QuestSnackbar`; resultado o error que debe permanecer visible junto al control usa `QuestStatusMessage`, que comunica icono y texto además del color. Los campos muestran validación junto a la entrada; los mensajes vacíos explican qué falta y dan una sola acción siguiente.

`QuestSkeleton` conserva la geometría de paneles durante carga. Las animaciones siguen `GymQuestMotion`: nada durante registro salvo feedback de hasta 100 ms; celebraciones de hasta 300 ms solo para hitos. La reducción de movimiento deja duración y háptica a cero. Háptica solo puede acompañar confirmación correcta, récord, nivel o descubrimiento mediante los ajustes del sistema; nunca es requisito para entender un estado.

Cancelar sesión, borrar serie, archivar ejercicio y descartar cambios usan `QuestConfirmationDialog`, con consecuencia textual y retorno seguro.

## Sesión, catálogo, historial y progreso

Home no muestra una acción Add; si se añade en el futuro, será exactamente `QuestActionButton(QuestAction.Add)`. Catálogo usa ese mismo componente para crear grupo, ejercicio y variante, dentro del contexto que define el destino. Sesión usa Add para añadir ejercicio, Save para serie, Edit para corrección y Delete solo tras confirmación.

Los estados de sesión son: sin iniciar, activa, descanso (con temporizador y texto), completando, completada, cancelada y error recuperable. Historial presenta cada serie con peso en kg, reps, descanso, volumen y récords textuales comparables. Progreso no usa gráficos hasta que cada uno incluya escala, leyenda y alternativa textual; el resumen actual es completamente textual.
