# Guia Interactiva de Museo 🏛️

**Museum Interactive Guide** es una aplicación móvil nativa para Android diseñada para transformar la experiencia del visitante en los museos. Utilizando tecnología de proximidad **Bluetooth Low Energy (BLE)**, la app detecta automáticamente la ubicación del usuario y despliega información multimedia relevante sobre las obras de arte en tiempo real.

## 🚀 Características principales

* **Detección por Proximidad:** Integración con **Beacons** para identificar piezas de arte cercanas sin intervención del usuario.
* **Contenido Dinámico:** Información gestionada y actualizada al instante mediante **Firebase Firestore**.
* **Interfaz Intuitiva:** Interfaz moderna desarrollada en **Kotlin** para una navegación fluida.
* **Sincronización en Tiempo Real:** Los detalles de las exhibiciones se gestionan centralizadamente, permitiendo actualizaciones rápidas del catálogo.

## 🛠️ Stack Tecnológico

* **Lenguaje:** [Kotlin](https://kotlinlang.org/)
* **Plataforma:** Android SDK (Min SDK: 21+)
* **Base de Datos:** [Firebase Firestore](https://firebase.google.com/docs/firestore)
* **Comunicación:** Bluetooth Low Energy (BLE) / Protocolo iBeacon.
* **Arquitectura:** MVVM (Model-View-ViewModel).

## 📦 Instalación y Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/tu-usuario/nombre-del-repo.git](https://github.com/tu-usuario/nombre-del-repo.git)
    ```

2.  **Configurar Firebase:**
    * Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
    * Descarga el archivo `google-services.json` y colócalo en el directorio `app/`.
    * Habilita **Firestore Database** en la consola.

3.  **Configurar Beacons:**
    * Registra los UUIDs/Minors/Majors de tus Beacons en Firestore para vincularlos con la información de las obras.

4.  **Compilar y Ejecutar:**
    * Abre el proyecto en **Android Studio**.
    * Sincroniza los archivos de Gradle.
    * Ejecuta la app en un dispositivo físico (los emuladores no suelen soportar escaneo BLE).

## 📄 Estructura de Datos (Firestore)

El sistema espera una colección llamada `exhibitions` con documentos que sigan este esquema:

```json
{
  "beacon_id": "UUID-DEL-BEACON",
  "title": "Nombre de la Obra",
  "description": "Historia y detalles técnicos...",
  "image_url": "[https://link-a-la-imagen-en-storage.com](https://link-a-la-imagen-en-storage.com)",
  "author": "Nombre del Artista",
  "year": "Año de creación"
}
