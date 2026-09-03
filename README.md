# ⚽ FutTracker & Lineup Generator

App Android nativa para uso personal desarrollada con **Kotlin** y **Jetpack Compose**. Diseñada para llevar el registro individual de partidos de fútbol 6 y fútbol 7, estadísticas por temporada y generación de alineaciones tácticas automáticas.

---

## ✨ Características Principales

- 📊 **Estadísticas Personalizadas:** Seguimiento de goles y asistencias por temporada deportiva (septiembre - agosto) y por año natural.
- 📋 **Registro de Partidos:** Historial detallado de encuentros de fútbol 6 y fútbol 7.
- 👥 **Gestión de Plantilla:** Ficha de jugadores/amigos con avatar personalizado y definición de posiciones (principales y secundarias).
- 🏟️ **Pizarra Táctica:** Visualización e interacción con alineaciones tácticas en campo virtual.
- 🤖 **Generador de Alineaciones:** Algoritmo automático que asigna la plantilla óptima según la formación elegida (ej. 2-3-1, 2-2-1) y la polivalencia de cada jugador.

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Base de Datos Local:** Room Database (SQLite)
- **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Inyección de Dependencias:** Hilt / Koin *(opcional según implementación)*

---

## 🚀 Requisitos de Entorno

- **Android Studio:** Ladybug / Jellyfish o superior
- **JDK:** 17
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34+

---

## 📝 Próximas Funcionalidades (Roadmap)

- [ ] Exportación de resúmenes de partido en formato imagen/card para compartir por WhatsApp.
- [ ] Integración con la API de Gemini para la generación de resúmenes tácticos.
- [ ] Soporte para estadísticas avanzadas (porcentaje de victorias por formación).