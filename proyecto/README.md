# BiblioTech - Biblioteca Digital

Este proyecto implementa una aplicación moderna de biblioteca digital usando JavaFX con Java 21.

## Características

- 📚 **Gestión de Biblioteca**: Interfaz completa para explorar libros digitales
- 🔍 **Búsqueda Avanzada**: Campo de búsqueda con filtros por categorías
- � **Libros Destacados**: Sección horizontal con libros recomendados
- 🎨 **Diseño Moderno**: Interfaz elegante con efectos visuales y animaciones
- 📱 **Navegación Intuitiva**: Barra inferior con acceso rápido a secciones
- 🎯 **Componentes de Biblioteca**: 
  - Header con logo y perfil de usuario
  - Barra de búsqueda y filtros por categoría
  - Scroll horizontal de libros destacados
  - Grid responsivo de biblioteca personal
  - Navegación inferior temática

## Componentes de la Aplicación

### Header Principal
- Logo de biblioteca con emoji 📚
- Título "BiblioTech" y subtítulo
- Botón de perfil de usuario

### Sección de Búsqueda
- Campo de búsqueda amplio con placeholder
- Botón de búsqueda con ícono 🔍
- Filtros de categorías (Todos, Novelas, Ciencia, Historia, Tecnología)

### Libros Destacados
- Scroll horizontal con libros recomendados
- Portadas coloridas con información del libro
- Efectos hover para interactividad

### Biblioteca Personal
- Grid responsivo de 3 columnas
- Tarjetas de libros con:
  - Portada simulada con colores únicos
  - Título y autor del libro
  - Botón "Leer ahora"
- Efectos de sombra y hover

### Navegación Inferior
- Inicio 🏠 (activo)
- Buscar 🔍
- Biblioteca 📚
- Favoritos ❤️
- Perfil 👤

## Requisitos

- Java 21 o superior
- Gradle 7.0 o superior
- JavaFX 21.0.2

## Cómo ejecutar

1. **Compilar el proyecto:**
   ```bash
   ./gradlew build
   ```

2. **Ejecutar la aplicación:**
   ```bash
   ./gradlew run
   ```

3. **Ejecutar tests:**
   ```bash
   ./gradlew test
   ```

## Estructura del Proyecto

```
proyecto/
├── app/
│   ├── build.gradle.kts          # Configuración de Gradle con JavaFX
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── org/example/
│       │   │       └── App.java   # Clase principal con interfaz JavaFX
│       │   └── resources/
│       │       └── styles.css     # Estilos CSS adicionales
│       └── test/
│           └── java/
│               └── org/example/
│                   └── AppTest.java # Tests unitarios
├── gradle.properties              # Configuración de Gradle
└── README.md                      # Este archivo
```

## Funcionalidades Implementadas

### Estilos Visuales
- **Gradientes**: Fondo con gradiente de colores modernos
- **Efectos de Sombra**: DropShadow en tarjetas y botones
- **Hover Effects**: Cambios visuales al pasar el mouse
- **Transparencias**: Elementos semitransparentes para profundidad

### Interactividad
- **Botones Animados**: Efectos hover con transiciones suaves
- **Campos de Formulario**: Validación visual y efectos de foco
- **Alertas Modernas**: Diálogos estilizados para feedback

### Layout Responsivo
- **BorderPane**: Layout principal adaptable
- **GridPane**: Formulario organizado en grilla
- **HBox/VBox**: Contenedores flexibles para alineación

## Personalización

Para personalizar la interfaz, puedes modificar:

1. **Colores**: Cambiar los gradientes en la clase `App.java`
2. **Tipografía**: Ajustar fuentes y tamaños en los métodos de estilo
3. **Efectos**: Modificar los efectos de sombra y transparencias
4. **Layout**: Reorganizar componentes cambiando los contenedores

## Tecnologías Utilizadas

- **JavaFX 21.0.2**: Framework para interfaz gráfica
- **Java 21**: Lenguaje de programación
- **Gradle**: Sistema de construcción
- **JUnit 5**: Framework de testing

## Próximas Mejoras

- [ ] Animaciones CSS más avanzadas
- [ ] Temas oscuro/claro intercambiables
- [ ] Más componentes interactivos
- [ ] Integración con bases de datos
- [ ] Internacionalización (i18n)

---

¡Disfruta explorando esta interfaz moderna con JavaFX! 🚀
