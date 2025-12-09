# Sistema ECG con Base de Datos Normalizada

## Normalización de Base de Datos - 3 Formas Normales (3FN)

Este sistema implementa una base de datos relacional que cumple con las **tres formas normales** para garantizar la integridad, eficiencia y mantenibilidad de los datos.

---

## ✅ Primera Forma Normal (1FN)

**Requisitos:**
- Cada columna debe contener valores atómicos (indivisibles)
- No debe haber grupos repetitivos
- Cada registro debe ser único
- Debe existir una clave primaria

**Implementación en el Sistema:**

### Tabla `Paciente`
```sql
CREATE TABLE Paciente (
    id_paciente INTEGER PRIMARY KEY,  -- Clave primaria única
    nombre VARCHAR(100),               -- Valor atómico
    edad INTEGER,                      -- Valor atómico
    estatura DECIMAL(5,2),             -- Valor atómico
    fecha_registro TIMESTAMP           -- Valor atómico
);
```

✅ **Cumple 1FN porque:**
- Cada campo contiene un solo valor
- No hay listas o arrays en las columnas
- Cada paciente tiene un ID único (clave primaria)
- No hay grupos repetitivos

---

## ✅ Segunda Forma Normal (2FN)

**Requisitos:**
- Cumplir con 1FN
- Todos los atributos no-clave deben depender completamente de la clave primaria
- Eliminar dependencias parciales

**Implementación en el Sistema:**

### Separación de `Lectura_ECG` y `Sesion_Monitoreo`

**❌ Incorrecto (viola 2FN):**
```sql
-- Dependencia parcial: fecha_sesion depende de id_sesion, no de id_lectura
CREATE TABLE Lectura (
    id_lectura INTEGER,
    id_sesion INTEGER,
    fecha_sesion TIMESTAMP,  -- ⚠️ Depende solo de id_sesion
    valor_senal DECIMAL,
    PRIMARY KEY (id_lectura, id_sesion)
);
```

**✅ Correcto (cumple 2FN):**
```sql
-- Tabla Sesion_Monitoreo: datos de la sesión
CREATE TABLE Sesion_Monitoreo (
    id_sesion INTEGER PRIMARY KEY,
    id_paciente INTEGER,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    estado VARCHAR(20),
    FOREIGN KEY (id_paciente) REFERENCES Paciente(id_paciente)
);

-- Tabla Lectura_ECG: solo datos de la lectura
CREATE TABLE Lectura_ECG (
    id_lectura INTEGER PRIMARY KEY,
    id_sesion INTEGER,
    valor_senal DECIMAL(10,4),
    timestamp TIMESTAMP,
    FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion)
);
```

✅ **Cumple 2FN porque:**
- Cada atributo depende completamente de su clave primaria
- Los datos de sesión están separados de los datos de lectura
- No hay dependencias parciales

---

## ✅ Tercera Forma Normal (3FN)

**Requisitos:**
- Cumplir con 2FN
- Eliminar dependencias transitivas
- Los atributos no-clave NO deben depender de otros atributos no-clave

**Implementación en el Sistema:**

### Separación de `Tipo_Evento`

**❌ Incorrecto (viola 3FN):**
```sql
-- Dependencia transitiva: severidad depende de tipo_evento
CREATE TABLE Evento_Detectado (
    id_evento INTEGER PRIMARY KEY,
    id_lectura INTEGER,
    tipo_evento VARCHAR(50),
    severidad VARCHAR(20),  -- ⚠️ Depende de tipo_evento (transitiva)
    descripcion TEXT
);
```

**✅ Correcto (cumple 3FN):**
```sql
-- Tabla de referencia para tipos de eventos
CREATE TABLE Tipo_Evento (
    id_tipo_evento INTEGER PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE,
    descripcion TEXT,
    severidad VARCHAR(20)  -- ✓ Depende directamente de id_tipo_evento
);

-- Tabla de eventos detectados
CREATE TABLE Evento_Detectado (
    id_evento INTEGER PRIMARY KEY,
    id_lectura INTEGER,
    id_tipo_evento INTEGER,  -- ✓ Referencia a la tabla Tipo_Evento
    descripcion TEXT,
    FOREIGN KEY (id_lectura) REFERENCES Lectura_ECG(id_lectura),
    FOREIGN KEY (id_tipo_evento) REFERENCES Tipo_Evento(id_tipo_evento)
);
```

✅ **Cumple 3FN porque:**
- Eliminamos la dependencia transitiva (severidad → tipo_evento → id_evento)
- Cada atributo depende directamente de la clave primaria
- Los tipos de eventos están en una tabla de referencia separada
- No hay redundancia de datos

### Otro ejemplo: `Calculo_BPM`

**❌ Incorrecto (viola 3FN):**
```sql
CREATE TABLE Lectura_ECG (
    id_lectura INTEGER PRIMARY KEY,
    valor_senal DECIMAL,
    bpm INTEGER,           -- ⚠️ BPM se calcula de múltiples lecturas
    timestamp TIMESTAMP
);
```

**✅ Correcto (cumple 3FN):**
```sql
-- Tabla separada para cálculos de BPM
CREATE TABLE Calculo_BPM (
    id_calculo INTEGER PRIMARY KEY,
    id_sesion INTEGER,
    bpm INTEGER,
    num_latidos INTEGER,
    intervalo_segundos INTEGER,
    timestamp TIMESTAMP,
    FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion)
);
```

---

## 📊 Estructura Completa de la Base de Datos

```
┌─────────────────┐
│   Paciente      │
│  (1FN, 2FN, 3FN)│
└────────┬────────┘
         │ 1
         │
         │ N
┌────────▼──────────┐
│ Sesion_Monitoreo  │
│  (1FN, 2FN, 3FN)  │
└────────┬──────────┘
         │ 1
         │
         ├─────────────────┬─────────────────┐
         │ N               │ N               │ N
┌────────▼────────┐ ┌──────▼──────┐ ┌───────▼──────────┐
│  Lectura_ECG    │ │ Calculo_BPM │ │ Historial_Motor  │
│ (1FN, 2FN, 3FN) │ │(1FN,2FN,3FN)│ │ (1FN, 2FN, 3FN)  │
└────────┬────────┘ └─────────────┘ └──────────────────┘
         │ 1
         │
         │ N
┌────────▼──────────┐       ┌──────────────┐
│ Evento_Detectado  │───────│ Tipo_Evento  │
│  (1FN, 2FN, 3FN)  │ N   1 │(1FN,2FN,3FN) │
└───────────────────┘       └──────────────┘
```

---

## 🎯 Beneficios de la Normalización

### 1. **Eliminación de Redundancia**
- Los datos se almacenan una sola vez
- Reduce el espacio de almacenamiento
- Ejemplo: Los tipos de eventos se definen una vez en `Tipo_Evento`

### 2. **Integridad de Datos**
- Las relaciones están garantizadas por claves foráneas
- Previene datos huérfanos
- Mantiene consistencia

### 3. **Facilidad de Mantenimiento**
- Cambios en un solo lugar
- Ejemplo: Cambiar la severidad de "Taquicardia" solo requiere actualizar `Tipo_Evento`

### 4. **Prevención de Anomalías**
- **Anomalía de Inserción:** Evitada
- **Anomalía de Actualización:** Evitada
- **Anomalía de Eliminación:** Evitada

---

## 🚀 Instalación y Uso

### 1. Descargar SQLite JDBC Driver

```powershell
# Descargar el driver SQLite
Invoke-WebRequest -Uri "https://github.com/xerial/sqlite-jdbc/releases/download/3.44.1.0/sqlite-jdbc-3.44.1.0.jar" -OutFile "sqlite-jdbc.jar"
```

### 2. Compilar el Sistema

```powershell
# Compilar con el driver en el classpath
javac -cp ".;sqlite-jdbc.jar" DatabaseManager.java SistemaECG.java
```

### 3. Ejecutar el Sistema

```powershell
# Ejecutar con el driver en el classpath
java -cp ".;sqlite-jdbc.jar" SistemaECG
```

### 4. Verificar la Base de Datos

La base de datos `ecg_database.db` se creará automáticamente. Puedes inspeccionarla con:

```powershell
# Usar SQLite CLI (si está instalado)
sqlite3 ecg_database.db

# Ver todas las tablas
.tables

# Ver estructura de una tabla
.schema Paciente

# Consultar datos
SELECT * FROM Paciente;
```

---

## 📝 Ejemplos de Consultas

### Obtener historial completo de un paciente
```sql
SELECT 
    p.nombre,
    s.fecha_inicio,
    l.valor_senal,
    l.timestamp,
    te.nombre AS tipo_evento,
    te.severidad
FROM Paciente p
JOIN Sesion_Monitoreo s ON p.id_paciente = s.id_paciente
JOIN Lectura_ECG l ON s.id_sesion = l.id_sesion
LEFT JOIN Evento_Detectado e ON l.id_lectura = e.id_lectura
LEFT JOIN Tipo_Evento te ON e.id_tipo_evento = te.id_tipo_evento
WHERE p.id_paciente = 1
ORDER BY l.timestamp DESC;
```

### Obtener estadísticas de BPM por paciente
```sql
SELECT 
    p.nombre,
    AVG(cb.bpm) as bpm_promedio,
    MIN(cb.bpm) as bpm_minimo,
    MAX(cb.bpm) as bpm_maximo
FROM Paciente p
JOIN Sesion_Monitoreo s ON p.id_paciente = s.id_paciente
JOIN Calculo_BPM cb ON s.id_sesion = cb.id_sesion
GROUP BY p.id_paciente;
```

---

## ✅ Verificación de Normalización

| Forma Normal | Requisito | Estado | Evidencia |
|--------------|-----------|--------|-----------|
| **1FN** | Valores atómicos | ✅ | Todas las columnas son atómicas |
| **1FN** | Clave primaria | ✅ | Todas las tablas tienen PK |
| **1FN** | Sin grupos repetitivos | ✅ | No hay arrays ni listas |
| **2FN** | Cumple 1FN | ✅ | ✓ |
| **2FN** | Sin dependencias parciales | ✅ | Sesiones y lecturas separadas |
| **3FN** | Cumple 2FN | ✅ | ✓ |
| **3FN** | Sin dependencias transitivas | ✅ | Tipo_Evento en tabla separada |

---

## 📚 Documentación Adicional

- **schema.sql**: Definición completa del esquema de base de datos
- **DatabaseManager.java**: Capa de acceso a datos con operaciones CRUD
- **SistemaECG.java**: Sistema principal integrado con la base de datos

---

## 🎓 Conclusión

Este sistema demuestra una implementación completa de las **3 Formas Normales**, garantizando:
- ✅ Integridad referencial
- ✅ Eliminación de redundancia
- ✅ Facilidad de mantenimiento
- ✅ Escalabilidad
- ✅ Consistencia de datos
