# ⚽ FutTracker & Lineup Generator

App Android nativa moderna desarrollada con **Kotlin** y **Jetpack Compose** (Material 3). Diseñada para registrar partidos de fútbol (Futsal, Fútbol 6 y Fútbol 7), realizar sorteos y alineaciones tácticas equilibradas, analizar estadísticas detalladas, evaluar enfrentamientos cara a cara entre amigos y gestionar tu perfil deportivo.

---

## 📱 Las 7 Pestañas de Navegación

La aplicación se estructura en una barra de navegación inferior con 7 secciones especializadas:

1. 👥 **Jugadores**:
   - Gestión integral de la plantilla de jugadores y amigos.
   - Fotos de perfil con **herramienta interactiva de recorte (Cropping)**: zoom táctil, paneo y rotación antes de guardar.
   - Definición de posiciones primarias y secundarias con insignias visuales diferenciadas (secundarias oscurecidas).
   - Nivel de habilidad (1 a 5 estrellas) y filtro rápido de favoritos.

2. 📋 **Alineaciones**:
   - Pizarra táctica interactiva con campo de fútbol virtual.
   - Formaciones tácticas optimizadas y simétricas (ej. 2-3-1, 2-2-1, 3-2-1) adaptadas a cada modalidad.
   - Visualización de dorsales, nombres y placas de posición en esquina inferior.

3. 🎲 **Sorteos**:
   - Algoritmo de reparto equilibrado de equipos basado en nivel y posiciones polivalentes de los jugadores convocados.
   - Selector de convocados con fondo oscuro de alto contraste y límite estricto según la modalidad seleccionada.
   - **Interacción Drag & Drop táctico**: arrastra jugadores en el campo para intercambiar posiciones y personalizar el once/siete inicial.
   - Exportación y compartición de la alineación como tarjeta gráfica de imagen para WhatsApp u otras redes, con decodificación segura y robusta de avatares locales.

4. 📊 **Estadísticas**:
   - Pantalla centralizada para analizar el rendimiento colectivo e individual.
   - **Filtros por modalidad de juego**: Total (por defecto), Futsal, Fútbol 6 y Fútbol 7.
   - **Filtros temporales**: Histórico total, Temporada deportiva (formato estandarizado **2026/27**, septiembre a agosto), Año natural y selector por rango de fechas inclusivo.
   - **Jerarquía visual optimizada**: El **Balance de Resultados** (Victorias, Empates, Derrotas y porcentaje de éxito) se ubica en primer lugar, seguido de métricas clave (partidos jugados, goles marcados, promedio por partido, asistencias, tiros al palo y balance de goles a favor/en contra).
   - Gráficas evolutivas de goles/asistencias y resumen detallado del tipo de goles.

5. ⚽ **Partidos**:
   - Registro rápido e intuitivo del marcador con centrado visual y controles `+/-`.
   - Preselección automática del usuario propio al crear un partido, resaltado en la lista con el identificador `(Tú)` y borde de acento.
   - Buscador interactivo de participantes por texto, posición y jugadores favoritos.
   - **Contabilidad rigurosa de goles**:
     - Los goles totales se calculan sumando de forma unívoca las partes del cuerpo utilizadas (diestra, zurda, cabeza, otro), evitando sumas dobles.
     - Atributos especiales acumulativos e independientes: tiros al palo, goles fuera del área, tacones y chilenas.
   - Reubicación de notas y crónica táctica inmediatamente debajo de los participantes.

6. ⚔️ **Enfrentamientos**:
   - Módulo exclusivo de análisis cara a cara (Head-to-Head).
   - Consulta el historial y porcentaje de victorias, empates y derrotas cuando juegas **CON** un compañero en tu equipo o **CONTRA** él como rival.
   - Buscador rápido por jugador y selector por modalidad.

7. 👤 **Perfil**:
   - Información esencial del usuario: foto de avatar con recorte interactivo, nombre y posiciones.
   - Sincronización automática del perfil en la plantilla general de jugadores.
   - **Sistema de Copia de Seguridad y Exportación**: empaquetado integral de la base de datos (perfil, plantilla de jugadores y partidos) en formato JSON, con guardado local mediante Storage Access Framework o compartición directa vía el panel de compartir de Android.

---

## 🎨 Identidad Visual y Estilos

- **Color de Acento**: Verde lima suave y semi-transparente (`LimeVolt` = `Color(0xD9BCE324)`), aplicado en botones de acción, bordes destacados y títulos principales.
- **Modo Oscuro Puro**: Superficies en `DarkBackground` y `DarkCard` para máxima legibilidad en campos y estadísticas.
- **Tipografía y Legibilidad**: Textos estandarizados en minúsculas y mayúsculas coherentes ("Nuevo partido", "Resumen de goles", "primarias y secundarias").

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Kotlin 2.0+
- **UI:** Jetpack Compose con Material 3 y Navigation Compose (7 destinos principales)
- **Inyección de Dependencias:** Dagger Hilt
- **Persistencia Local:** Room Database con SQLite (Migraciones de esquemas para nuevas métricas y enfrentamientos)
- **Gestión de Estado:** Coroutines, Flow, StateFlow y Compose Runtime
- **Gráficos y Canvas:** Jetpack Compose Canvas nativo y Android Graphics para renderizado de alineaciones

---

## 🚀 Requisitos de Entorno

- **Android Studio:** Ladybug / Meerkat o superior
- **JDK:** 17
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 34+ (Android 14)