# ⚽ FutTracker & Lineup Generator

App Android nativa moderna desarrollada con **Kotlin** y **Jetpack Compose** (Material 3). Diseñada para registrar partidos de fútbol (Futsal, Fútbol 6 y Fútbol 7), gestionar plantillas con fotos interactivas, realizar sorteos y alineaciones tácticas equilibradas por posiciones, analizar estadísticas detalladas individuales y globales, evaluar enfrentamientos y sinergias (H2H y Dúos) y realizar copias de seguridad de todos tus datos.

---

## 📱 Estructura de Navegación y Funcionalidades

La aplicación se organiza en una barra de navegación inferior con secciones clave optimizadas:

### 1. 📁 **Datos** (Pestaña Combinada Principal)
Permite gestionar la base de datos de la app dividida en dos subpestañas:
- **Jugadores**:
  - Plantilla completa de amigos y compañeros.
  - Fotos de perfil con **herramienta interactiva de recorte (Cropping)** y **visualizador con zoom a pantalla completa** (gestos táctiles de pinza y doble toque).
  - Configuración de posiciones principales y secundarias (con chips visuales diferenciados y contraste mejorado).
  - Marcado rápido de jugadores favoritos y buscador por nombre/posición.
  - Distintivo visual especial `(Tú)` para el usuario propio.
- **Partidos**:
  - Registro de partidos por fecha, modalidad (Futsal, Fútbol 6, Fútbol 7) y duración personalizada (**60' por defecto, 90' o 120' min**).
  - Marcador simétrico con controles intuitivos `+/-`.
  - **Soporte para partidos externos**: Selector *"¿He jugado yo este partido?"* que permite registrar partidos de terceros sin que computen para las estadísticas personales del usuario propio.
  - Distintivo visual **Externo** en las tarjetas de partidos no jugados, ocultando las estadísticas personales y etiquetando a los participantes como *Equipo 1* y *Equipo 2*.
  - **Desglose exhaustivo de goles y jugadas**:
    - Distribución anatómica rigurosa: diestra, zurda, cabeza, tacón, chilena y otro.
    - Atributos especiales: goles fuera del área, asistencias y tiros al palo.
  - Registro de notas y crónicas tácticas.

### 2. 📋 **Pizarra (Alineaciones)**
- Campo táctico interactivo para diseñar alineaciones ideales.
- Formaciones tácticas optimizadas y simétricas según la modalidad (ej. 2-3-1, 2-2-1, 3-2-1).
- Selector de formación sugerida automática.
- Visualización de dorsales, nombres y placas de posición elevadas para máxima legibilidad.
- Filtro por posiciones principales o secundarias ("Ambas posiciones").

### 3. 🎲 **Sorteos**
- Generador de equipos equilibrados: modo **Equilibrado por posiciones** o modo **Aleatorio puro**.
- Selector de convocados con límite estricto según la modalidad de juego.
- **Pizarra interactiva tras el sorteo**:
  - Botón de **Alineación Sugerida** para colocar a los jugadores en su posición ideal automáticamente.
  - Posibilidad de cambiar la formación de cada equipo.
  - **Intercambio táctico entre jugadores**: arrastra y suelta para permutar jugadores sin romper la formación.
- Compartición gráfica del resultado como imagen en alta resolución lista para WhatsApp o redes sociales, reflejando fielmente cambios de formación y posiciones.

### 4. 📊 **Stats (Estadísticas)**
- **Individual**:
  - Balance de resultados (victorias, empates, derrotas) con porcentaje de éxito coloreado dinámicamente mediante un **degradado de verde a rojo** según el rendimiento.
  - Filtros por modalidad y filtros temporales ampliados: Total, Temporadas (ej. 2026/27), Año natural, **Últimas 4 semanas**, **Últimos 3 meses** y rango de fechas personalizado.
  - Gráficas de minutos totales y por semana, desglose de tipología de goles y promedios por encuentro.
- **General**:
  - Ranking global de jugadores de la plantilla ordenado por partidos, victorias, empates, derrotas o minutos jugados.
  - Buscador integrado con filtros por nombre, favoritos y posiciones.
  - Acceso prioritario al usuario propio situado al inicio de la lista.

### 5. ⚔️ **Versus (Enfrentamientos y Sinergias)**
Organizado en tres subpestañas especializadas:
- **General (Destacados)**:
  - Tarjetas destacadas de roles cruzados:
    - 🐐 **La cabra** (mayor % de victorias jugando como compañeros).
    - 💀 **La lacra** (mayor % de derrotas jugando como compañeros).
    - 🍬 **El caramelito** (rival contra el que más se gana).
    - 👹 **La bestia** (rival contra el que más se pierde).
  - **Rotación automática ante empates**: Si múltiples jugadores comparten el récord y porcentaje más alto, la tarjeta rota suavemente con animaciones de desvanecimiento (*fade-in / fade-out*) cada pocos segundos e indica el índice del empate (ej. 1/3).
  - Posibilidad de consultar estos destacados para cualquier jugador de la plantilla.
- **Dúos**:
  - Análisis de duplas y sinergias entre compañeros de equipo, con balance y porcentaje de victorias coloreado con degradado dinámico.
- **H2H (Cara a Cara)**:
  - Historial directo frente a frente: estadísticas detalladas jugando juntos o como rivales.
  - El usuario propio aparece como primera opción en el selector de jugador.

### 6. 👤 **Perfil**
- Configuración de la ficha de jugador del usuario: nombre, posiciones principales y secundarias, y fotografía.
- Sincronización transparente con la plantilla de jugadores identificada como `(Tú)`.
- **Copia de seguridad en formato JSON**:
  - **Exportar**: Genera un archivo `.json` completo con perfil, jugadores y partidos (compatible con Storage Access Framework y compartir directo).
  - **Importar / Cargar**: Carga copias de seguridad existentes restaurando de forma íntegra toda la base de datos de la app.

---

## 🎨 Identidad Visual y Estilo

- **Color de Acento**: Verde lima eléctrico (`LimeVolt`), presente en botones de acción, bordes activos y acentos clave.
- **Superficies Dark Mode**: Fondos en `DarkBackground` y tarjetas en `DarkCard` diseñadas para un contraste nítido con el césped táctico y los textos.
- **Codificación Semántica de Colores**:
  - Porcentajes de victoria representados mediante un degradado continuo desde verde (máximo éxito) hasta rojo (bajo rendimiento).
  - Compañeros identificados con tonos azules y el usuario propio resaltado en verde lima.
  - Partidos externos señalizados con distintivo gris neutro (*Slate*).

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Kotlin 2.0+
- **UI:** Jetpack Compose con Material 3 y Navigation Compose.
- **Inyección de Dependencias:** Dagger Hilt.
- **Base de Datos Local:** Room Database con SQLite (versión 8 con migraciones automáticas).
- **Gestión Asíncrona:** Kotlin Coroutines, Flow, StateFlow.
- **Renderizado Gráfico:** Jetpack Compose Canvas nativo y Android Graphics para exportación de alineaciones en mapa de bits.
- **Almacenamiento:** Android Storage Access Framework para importación y exportación de backups JSON.

---

## 🚀 Requisitos del Entorno

- **Android Studio:** Ladybug / Meerkat o superior
- **JDK:** 17
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 34+ (Android 14)