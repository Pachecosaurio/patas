# 📚 BiblioTech - Sistema de Gestión de Biblioteca

Un sistema completo de gestión de biblioteca desarrollado en Java con JavaFX, siguiendo el patrón arquitectónico MVC (Modelo-Vista-Controlador).

## 🚀 Características Principales

### 📖 Gestión de Libros
- ✅ Agregar, modificar y eliminar libros
- 🔍 Búsqueda avanzada por título, autor, categoría o ISBN
- 📊 Visualización con imágenes de portada
- 📋 Categorización de libros
- ✅ Control de disponibilidad

### 👥 Gestión de Usuarios
- 👤 Registro de usuarios (Estudiantes, Profesores, Administrativos, Externos)
- ✏️ Modificación de datos personales
- 🔍 Búsqueda y filtrado de usuarios
- 📊 Estados activo/inactivo

### 📋 Gestión de Préstamos
- 📚 Crear nuevos préstamos
- 🔄 Renovar préstamos existentes
- ✅ Devolver libros
- ⚠️ Control de préstamos vencidos
- 📊 Estados de préstamo (Activo, Devuelto, Vencido, Renovado)

### 📊 Sistema de Reportes
- 📈 Estadísticas generales del sistema
- 📊 Gráficos interactivos (barras y circulares)
- 📋 Libros más prestados
- 👥 Usuarios más activos
- ⚠️ Listado de préstamos vencidos
- 📄 Reportes detallados exportables

## 🏗️ Arquitectura del Proyecto

```
📁 PROYECTO_BIBLIOTECA
├── 📁 gradle/                 # Configuración de Gradle
├── 📁 app/
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/
│   │   │   │   └── 📁 com/gestorbiblioteca/
│   │   │   │       ├── ☕ MainApp.java
│   │   │   │       ├── 📁 controlador/
│   │   │   │       │   ├── ☕ LibrosController.java
│   │   │   │       │   ├── ☕ PrestamosController.java
│   │   │   │       │   ├── ☕ ReportesController.java
│   │   │   │       │   └── ☕ UsuariosController.java
│   │   │   │       └── 📁 modelo/
│   │   │   │           ├── ☕ Libro.java
│   │   │   │           ├── ☕ Prestamo.java
│   │   │   │           ├── ☕ ReporteService.java
│   │   │   │           └── ☕ Usuario.java
│   │   │   └── 📁 resources/
│   │   │       └── 📁 com/gestorbiblioteca/
│   │   │           ├── 📁 vista/
│   │   │           │   ├── 🎨 LibrosView.fxml
│   │   │           │   ├── 🎨 MainView.fxml
│   │   │           │   ├── 🎨 PrestamosView.fxml
│   │   │           │   ├── 🎨 ReportesView.fxml
│   │   │           │   └── 🎨 UsuariosView.fxml
│   │   │           └── 💅 styles.css
│   │   └── 📁 test/
│   │       └── 📁 java/
│   │           └── 📁 com/gestorbiblioteca/
│   │               └── ☕ AppTest.java
│   └── 📄 build.gradle.kts
├── 📄 .gitignore
├── 📄 build.gradle.kts
├── 📄 gradlew
├── 📄 gradlew.bat
├── 📄 README.md
├── 📄 run_app.sh
└── 📄 settings.gradle.kts
```

## 🔧 Requisitos del Sistema

- **Java:** 11 o superior
- **JavaFX:** 21.0.2 (incluido automáticamente)
- **Gradle:** 9.1.0 (incluido wrapper)
- **Sistema Operativo:** Windows, macOS, Linux

## 🚀 Instalación y Ejecución

### Opción 1: Script de Ejecución (Recomendado)
```bash
# En Linux/macOS
./run_app.sh

# En Windows
gradlew.bat run
```

### Opción 2: Gradle Directo
```bash
# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew run

# Ejecutar pruebas
./gradlew test
```

## 🎯 Funcionalidades por Módulo

### 📖 Módulo de Libros
- **Modelo:** `Libro.java` - Entidad con propiedades como título, autor, ISBN, categoría, disponibilidad
- **Vista:** `LibrosView.fxml` - Interfaz con tabla de libros y formulario de detalles
- **Controlador:** `LibrosController.java` - Lógica de negocio para CRUD de libros

### 👥 Módulo de Usuarios
- **Modelo:** `Usuario.java` - Entidad con tipos de usuario y estados
- **Vista:** `UsuariosView.fxml` - Interfaz para gestión de usuarios
- **Controlador:** `UsuariosController.java` - Lógica de negocio para usuarios

### 📋 Módulo de Préstamos
- **Modelo:** `Prestamo.java` - Entidad que relaciona usuarios y libros
- **Vista:** `PrestamosView.fxml` - Interfaz para gestión de préstamos
- **Controlador:** `PrestamosController.java` - Lógica de negocio para préstamos

### 📊 Módulo de Reportes
- **Modelo:** `ReporteService.java` - Servicio para generar estadísticas
- **Vista:** `ReportesView.fxml` - Interfaz con gráficos y tablas
- **Controlador:** `ReportesController.java` - Lógica para reportes y visualizaciones

## 🎨 Características de la Interfaz

### 🖼️ Diseño Visual
- **Tema:** Interfaz moderna con iconos y colores intuitivos
- **Navegación:** Pestañas para fácil acceso a diferentes módulos
- **Tablas:** Vistas organizadas con funciones de búsqueda y filtrado
- **Formularios:** Campos de entrada intuitivos con validación

### 📊 Visualizaciones
- **Gráficos de Barras:** Para libros más prestados y usuarios más activos
- **Gráficos Circulares:** Para distribución de categorías y estados
- **Indicadores:** Barras de progreso y estadísticas visuales
- **Alertas:** Notificaciones para préstamos vencidos

## 🧪 Pruebas

El proyecto incluye pruebas unitarias completas:

```bash
# Ejecutar todas las pruebas
./gradlew test

# Ver reporte de pruebas
./gradlew test jacocoTestReport
```

### Cobertura de Pruebas
- ✅ Creación y validación de entidades
- ✅ Lógica de préstamos y devoluciones
- ✅ Renovaciones y estados
- ✅ Validaciones de negocio

## 🔧 Desarrollo

### Estructura de Paquetes
```
com.gestorbiblioteca/
├── MainApp.java                 # Clase principal
├── controlador/                 # Controladores MVC
├── modelo/                      # Modelos de datos
└── vista/                       # Archivos FXML (en resources)
```

### Tecnologías Utilizadas
- **JavaFX:** Framework de interfaz gráfica
- **FXML:** Descripción declarativa de interfaces
- **CSS:** Estilos personalizados
- **JUnit 5:** Framework de pruebas
- **Gradle:** Herramienta de construcción

## 📈 Métricas del Proyecto

- **Líneas de código:** ~2000+
- **Clases:** 10+
- **Interfaces FXML:** 5
- **Pruebas unitarias:** 6+
- **Controladores:** 4

## 🤝 Contribución

1. Fork el repositorio
2. Crea una rama para tu característica (`git checkout -b feature/nueva-caracteristica`)
3. Commit tus cambios (`git commit -am 'Agregar nueva característica'`)
4. Push a la rama (`git push origin feature/nueva-caracteristica`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia Apache 2.0 - ver el archivo [LICENSE](LICENSE) para detalles.

## 📞 Soporte

Para soporte técnico o preguntas sobre el sistema:

- **Documentación:** Ver comentarios en el código fuente
- **Issues:** Abrir un issue en el repositorio
- **Email:** Contactar al desarrollador

## 🔄 Versiones

- **v1.0.0:** Versión inicial con funcionalidades básicas
- Gestión completa de libros, usuarios y préstamos
- Sistema de reportes con visualizaciones
- Interfaz moderna y responsiva

---

⭐ **¡No olvides dar una estrella al repositorio si te resultó útil!** ⭐