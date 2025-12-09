import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de Base de Datos para el Sistema ECG
 * Implementa operaciones CRUD cumpliendo con las 3 Formas Normales
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:ecg_database.db";
    private Connection connection;

    public DatabaseManager() {
        conectar();
        inicializarBaseDatos();
    }

    // ============================================
    // CONEXIÓN Y CONFIGURACIÓN
    // ============================================

    private void conectar() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("✓ Conexión a base de datos establecida");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Driver SQLite no encontrado");
            System.err.println("Ejecuta: Descarga sqlite-jdbc-3.x.x.jar y agrégalo al classpath");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    private void inicializarBaseDatos() {
        try {
            Statement stmt = connection.createStatement();
            
            // Leer y ejecutar el archivo schema.sql
            String schema = leerSchema();
            String[] statements = schema.split(";");
            
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        // Ignorar errores de tablas que ya existen
                        if (!e.getMessage().contains("already exists")) {
                            System.err.println("Error ejecutando: " + sql.substring(0, Math.min(50, sql.length())));
                        }
                    }
                }
            }
            
            System.out.println("✓ Base de datos inicializada correctamente");
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error inicializando base de datos: " + e.getMessage());
        }
    }

    private String leerSchema() {
        // Schema SQL embebido para facilitar distribución
        return """
            CREATE TABLE IF NOT EXISTS Paciente (
                id_paciente INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre VARCHAR(100) NOT NULL,
                edad INTEGER NOT NULL CHECK (edad > 0 AND edad < 150),
                estatura DECIMAL(5,2) NOT NULL CHECK (estatura > 0 AND estatura < 300),
                fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS Sesion_Monitoreo (
                id_sesion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_paciente INTEGER NOT NULL,
                fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                fecha_fin TIMESTAMP NULL,
                estado VARCHAR(20) DEFAULT 'ACTIVA',
                notas TEXT,
                FOREIGN KEY (id_paciente) REFERENCES Paciente(id_paciente) ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS Lectura_ECG (
                id_lectura INTEGER PRIMARY KEY AUTOINCREMENT,
                id_sesion INTEGER NOT NULL,
                valor_senal DECIMAL(10,4) NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion) ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS Tipo_Evento (
                id_tipo_evento INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre VARCHAR(50) NOT NULL UNIQUE,
                descripcion TEXT,
                severidad VARCHAR(20)
            );
            
            CREATE TABLE IF NOT EXISTS Evento_Detectado (
                id_evento INTEGER PRIMARY KEY AUTOINCREMENT,
                id_lectura INTEGER NOT NULL,
                id_tipo_evento INTEGER NOT NULL,
                descripcion TEXT,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (id_lectura) REFERENCES Lectura_ECG(id_lectura) ON DELETE CASCADE,
                FOREIGN KEY (id_tipo_evento) REFERENCES Tipo_Evento(id_tipo_evento)
            );
            
            CREATE TABLE IF NOT EXISTS Calculo_BPM (
                id_calculo INTEGER PRIMARY KEY AUTOINCREMENT,
                id_sesion INTEGER NOT NULL,
                bpm INTEGER NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                num_latidos INTEGER,
                intervalo_segundos INTEGER DEFAULT 10,
                FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion) ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS Configuracion_MQTT (
                id_config INTEGER PRIMARY KEY AUTOINCREMENT,
                broker_url VARCHAR(255) NOT NULL,
                puerto INTEGER DEFAULT 1883,
                topico_datos VARCHAR(100) DEFAULT 'ecg/datos',
                topico_motor VARCHAR(100) DEFAULT 'esp8266/motor',
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                activo BOOLEAN DEFAULT 1
            );
            
            CREATE TABLE IF NOT EXISTS Historial_Motor (
                id_comando INTEGER PRIMARY KEY AUTOINCREMENT,
                id_sesion INTEGER,
                comando VARCHAR(10),
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (id_sesion) REFERENCES Sesion_Monitoreo(id_sesion) ON DELETE SET NULL
            );
            
            INSERT OR IGNORE INTO Tipo_Evento (nombre, descripcion, severidad) VALUES
                ('Taquicardia', 'Frecuencia cardíaca superior a 100 BPM', 'MEDIA');
            INSERT OR IGNORE INTO Tipo_Evento (nombre, descripcion, severidad) VALUES
                ('Bradicardia', 'Frecuencia cardíaca inferior a 60 BPM', 'MEDIA');
            INSERT OR IGNORE INTO Tipo_Evento (nombre, descripcion, severidad) VALUES
                ('Arritmia', 'Ritmo cardíaco irregular', 'ALTA');
            INSERT OR IGNORE INTO Tipo_Evento (nombre, descripcion, severidad) VALUES
                ('Fibrilación', 'Fibrilación auricular o ventricular', 'CRITICA');
            INSERT OR IGNORE INTO Tipo_Evento (nombre, descripcion, severidad) VALUES
                ('Normal', 'Lectura dentro de parámetros normales', 'BAJA');
            """;
    }

    // ============================================
    // OPERACIONES PACIENTE (CRUD)
    // ============================================

    public int insertarPaciente(String nombre, int edad, double estatura) {
        String sql = "INSERT INTO Paciente (nombre, edad, estatura) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, edad);
            pstmt.setDouble(3, estatura);
            pstmt.executeUpdate();
            
            // Obtener el ID generado usando last_insert_rowid()
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error insertando paciente: " + e.getMessage());
        }
        return -1;
    }

    public Paciente obtenerPaciente(int idPaciente) {
        String sql = "SELECT * FROM Paciente WHERE id_paciente = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idPaciente);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Paciente p = new Paciente(
                    rs.getString("nombre"),
                    rs.getInt("edad"),
                    rs.getDouble("estatura")
                );
                p.setIdPaciente(rs.getInt("id_paciente"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo paciente: " + e.getMessage());
        }
        return null;
    }

    public List<Paciente> obtenerTodosPacientes() {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT * FROM Paciente ORDER BY fecha_registro DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Paciente p = new Paciente(
                    rs.getString("nombre"),
                    rs.getInt("edad"),
                    rs.getDouble("estatura")
                );
                p.setIdPaciente(rs.getInt("id_paciente"));
                pacientes.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    public void actualizarPaciente(int idPaciente, String nombre, int edad, double estatura) {
        String sql = "UPDATE Paciente SET nombre = ?, edad = ?, estatura = ? WHERE id_paciente = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, edad);
            pstmt.setDouble(3, estatura);
            pstmt.setInt(4, idPaciente);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error actualizando paciente: " + e.getMessage());
        }
    }

    public void eliminarPaciente(int idPaciente) {
        String sql = "DELETE FROM Paciente WHERE id_paciente = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idPaciente);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error eliminando paciente: " + e.getMessage());
        }
    }

    // ============================================
    // OPERACIONES SESIÓN DE MONITOREO
    // ============================================

    public int crearSesionMonitoreo(int idPaciente, String notas) {
        String sql = "INSERT INTO Sesion_Monitoreo (id_paciente, notas, estado) VALUES (?, ?, 'ACTIVA')";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idPaciente);
            pstmt.setString(2, notas);
            pstmt.executeUpdate();
            
            // Obtener el ID generado usando last_insert_rowid()
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creando sesión: " + e.getMessage());
        }
        return -1;
    }

    public void finalizarSesion(int idSesion) {
        String sql = "UPDATE Sesion_Monitoreo SET estado = 'FINALIZADA', fecha_fin = CURRENT_TIMESTAMP WHERE id_sesion = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idSesion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error finalizando sesión: " + e.getMessage());
        }
    }

    // ============================================
    // OPERACIONES LECTURA ECG
    // ============================================

    public void insertarLecturaECG(int idSesion, double valorSenal) {
        String sql = "INSERT INTO Lectura_ECG (id_sesion, valor_senal) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idSesion);
            pstmt.setDouble(2, valorSenal);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error insertando lectura ECG: " + e.getMessage());
        }
    }

    public List<DatoHistorico> obtenerHistorialPaciente(int idPaciente) {
        List<DatoHistorico> historial = new ArrayList<>();
        String sql = """
            SELECT l.id_lectura, l.id_sesion, l.valor_senal, l.timestamp,
                   e.id_tipo_evento, te.nombre as tipo_evento
            FROM Lectura_ECG l
            INNER JOIN Sesion_Monitoreo s ON l.id_sesion = s.id_sesion
            LEFT JOIN Evento_Detectado e ON l.id_lectura = e.id_lectura
            LEFT JOIN Tipo_Evento te ON e.id_tipo_evento = te.id_tipo_evento
            WHERE s.id_paciente = ?
            ORDER BY l.timestamp DESC
            LIMIT 1000
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idPaciente);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DatoHistorico d = new DatoHistorico(idPaciente, rs.getDouble("valor_senal"));
                d.setIdDato(rs.getInt("id_lectura"));
                
                String tipoEvento = rs.getString("tipo_evento");
                if (tipoEvento != null) {
                    d.setEventoDetectado(true);
                    d.setTipoEvento(tipoEvento);
                }
                
                historial.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo historial: " + e.getMessage());
        }
        return historial;
    }

    // ============================================
    // OPERACIONES BPM
    // ============================================

    public void insertarCalculoBPM(int idSesion, int bpm, int numLatidos) {
        String sql = "INSERT INTO Calculo_BPM (id_sesion, bpm, num_latidos) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idSesion);
            pstmt.setInt(2, bpm);
            pstmt.setInt(3, numLatidos);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error insertando cálculo BPM: " + e.getMessage());
        }
    }

    // ============================================
    // OPERACIONES EVENTOS
    // ============================================

    public void insertarEvento(int idLectura, int idTipoEvento, String descripcion) {
        String sql = "INSERT INTO Evento_Detectado (id_lectura, id_tipo_evento, descripcion) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idLectura);
            pstmt.setInt(2, idTipoEvento);
            pstmt.setString(3, descripcion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error insertando evento: " + e.getMessage());
        }
    }

    public int obtenerIdTipoEvento(String nombreEvento) {
        String sql = "SELECT id_tipo_evento FROM Tipo_Evento WHERE nombre = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombreEvento);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_tipo_evento");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo tipo de evento: " + e.getMessage());
        }
        return -1;
    }

    // ============================================
    // OPERACIONES MOTOR
    // ============================================

    public void registrarComandoMotor(int idSesion, String comando) {
        String sql = "INSERT INTO Historial_Motor (id_sesion, comando) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idSesion);
            pstmt.setString(2, comando);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error registrando comando motor: " + e.getMessage());
        }
    }

    // ============================================
    // OPERACIONES CONFIGURACIÓN MQTT
    // ============================================

    public void guardarConfiguracionMQTT(String brokerUrl, int puerto, String topicoDatos, String topicoMotor) {
        // Desactivar configuraciones anteriores
        String sqlDesactivar = "UPDATE Configuracion_MQTT SET activo = 0";
        
        // Insertar nueva configuración
        String sqlInsertar = "INSERT INTO Configuracion_MQTT (broker_url, puerto, topico_datos, topico_motor) VALUES (?, ?, ?, ?)";
        
        try (Statement stmt = connection.createStatement();
             PreparedStatement pstmt = connection.prepareStatement(sqlInsertar)) {
            
            stmt.execute(sqlDesactivar);
            
            pstmt.setString(1, brokerUrl);
            pstmt.setInt(2, puerto);
            pstmt.setString(3, topicoDatos);
            pstmt.setString(4, topicoMotor);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error guardando configuración MQTT: " + e.getMessage());
        }
    }

    // ============================================
    // UTILIDADES
    // ============================================

    public void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Conexión a base de datos cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
