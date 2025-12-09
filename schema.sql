-- ============================================
-- ESQUEMA DE BASE DE DATOS NORMALIZADO (3FN)
-- Sistema de Monitoreo ECG
-- ============================================

-- PRIMERA FORMA NORMAL (1FN):
-- - Todos los atributos son atómicos (no hay grupos repetitivos)
-- - Cada columna contiene valores atómicos
-- - Cada registro es único

-- SEGUNDA FORMA NORMAL (2FN):
-- - Cumple con 1FN
-- - No hay dependencias parciales (todos los atributos no-clave dependen completamente de la clave primaria)

-- TERCERA FORMA NORMAL (3FN):
-- - Cumple con 2FN
-- - No hay dependencias transitivas (los atributos no-clave no dependen de otros atributos no-clave)

-- ============================================
-- TABLA: Paciente
-- Almacena información básica de los pacientes
-- ============================================
CREATE TABLE IF NOT EXISTS Paciente (
    id_paciente INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(100) NOT NULL,
    edad INTEGER NOT NULL CHECK (edad > 0 AND edad < 150),
    estatura DECIMAL(5,2) NOT NULL CHECK (estatura > 0 AND estatura < 300),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_paciente_nombre UNIQUE (nombre, fecha_registro)
);

-- ============================================
-- TABLA: Sesion_Monitoreo
-- Registra sesiones de monitoreo ECG
-- Separada de Paciente para evitar redundancia (3FN)
-- ============================================
CREATE TABLE IF NOT EXISTS Sesion_Monitoreo (
    id_sesion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente INTEGER NOT NULL,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'FINALIZADA', 'CANCELADA')),
    notas TEXT,
    FOREIGN KEY (id_paciente) REFERENCES Paciente(id_paciente) ON DELETE CASCADE
);

-- ============================================
-- TABLA: Lectura_ECG
-- Almacena las lecturas individuales del ECG
-- Cumple con 3FN: cada lectura depende únicamente de id_lectura
-- ============================================
CREATE TABLE IF NOT EXISTS Lectura_ECG (
    id_lectura INTEGER PRIMARY KEY AUTOINCREMENT,
    id_sesion INTEGER NOT NULL,
    valor_senal DECIMAL(10,4) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion) ON DELETE CASCADE
);

-- ============================================
-- TABLA: Tipo_Evento
-- Catálogo de tipos de eventos (3FN - tabla de referencia)
-- ============================================
CREATE TABLE IF NOT EXISTS Tipo_Evento (
    id_tipo_evento INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    severidad VARCHAR(20) CHECK (severidad IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA'))
);

-- ============================================
-- TABLA: Evento_Detectado
-- Registra eventos anormales detectados
-- Cumple con 3FN: no hay dependencias transitivas
-- ============================================
CREATE TABLE IF NOT EXISTS Evento_Detectado (
    id_evento INTEGER PRIMARY KEY AUTOINCREMENT,
    id_lectura INTEGER NOT NULL,
    id_tipo_evento INTEGER NOT NULL,
    descripcion TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_lectura) REFERENCES Lectura_ECG(id_lectura) ON DELETE CASCADE,
    FOREIGN KEY (id_tipo_evento) REFERENCES Tipo_Evento(id_tipo_evento)
);

-- ============================================
-- TABLA: Calculo_BPM
-- Almacena cálculos de BPM en intervalos de tiempo
-- Separada de Lectura_ECG para evitar redundancia (3FN)
-- ============================================
CREATE TABLE IF NOT EXISTS Calculo_BPM (
    id_calculo INTEGER PRIMARY KEY AUTOINCREMENT,
    id_sesion INTEGER NOT NULL,
    bpm INTEGER NOT NULL CHECK (bpm >= 0 AND bpm <= 300),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    num_latidos INTEGER,
    intervalo_segundos INTEGER DEFAULT 10,
    FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion) ON DELETE CASCADE
);

-- ============================================
-- TABLA: Configuracion_MQTT
-- Configuraciones de conexión MQTT
-- Separada para cumplir con 3FN
-- ============================================
CREATE TABLE IF NOT EXISTS Configuracion_MQTT (
    id_config INTEGER PRIMARY KEY AUTOINCREMENT,
    broker_url VARCHAR(255) NOT NULL,
    puerto INTEGER DEFAULT 1883,
    topico_datos VARCHAR(100) DEFAULT 'ecg/datos',
    topico_motor VARCHAR(100) DEFAULT 'esp8266/motor',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT 1
);

-- ============================================
-- TABLA: Historial_Motor
-- Registra comandos enviados al motor ESP8266
-- ============================================
CREATE TABLE IF NOT EXISTS Historial_Motor (
    id_comando INTEGER PRIMARY KEY AUTOINCREMENT,
    id_sesion INTEGER,
    comando VARCHAR(10) CHECK (comando IN ('on', 'off', 'ON', 'OFF')),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion) ON DELETE SET NULL
);

-- ============================================
-- ÍNDICES PARA MEJORAR RENDIMIENTO
-- ============================================
CREATE INDEX IF NOT EXISTS idx_lectura_sesion ON Lectura_ECG(id_sesion);
CREATE INDEX IF NOT EXISTS idx_lectura_timestamp ON Lectura_ECG(timestamp);
CREATE INDEX IF NOT EXISTS idx_evento_lectura ON Evento_Detectado(id_lectura);
CREATE INDEX IF NOT EXISTS idx_sesion_paciente ON Sesion_Monitoreo(id_paciente);
CREATE INDEX IF NOT EXISTS idx_sesion_estado ON Sesion_Monitoreo(estado);
CREATE INDEX IF NOT EXISTS idx_bpm_sesion ON Calculo_BPM(id_sesion);

-- ============================================
-- DATOS INICIALES - Tipos de Eventos
-- ============================================
INSERT INTO Tipo_Evento (nombre, descripcion, severidad) VALUES
    ('Taquicardia', 'Frecuencia cardíaca superior a 100 BPM', 'MEDIA'),
    ('Bradicardia', 'Frecuencia cardíaca inferior a 60 BPM', 'MEDIA'),
    ('Arritmia', 'Ritmo cardíaco irregular', 'ALTA'),
    ('Fibrilación', 'Fibrilación auricular o ventricular', 'CRITICA'),
    ('Normal', 'Lectura dentro de parámetros normales', 'BAJA');
