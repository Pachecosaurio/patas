import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// ============================================
// MODELO - Gestión de datos
// ============================================

class Paciente {
    private int idPaciente;
    private String nombre;
    private int edad;
    private double estatura;
    private LocalDateTime fechaRegistro;

    public Paciente(String nombre, int edad, double estatura) {
        this.nombre = nombre;
        this.edad = edad;
        this.estatura = estatura;
        this.fechaRegistro = LocalDateTime.now();
    }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int id) { this.idPaciente = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public double getEstatura() { return estatura; }
    public void setEstatura(double estatura) { this.estatura = estatura; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}

class DatoHistorico {
    private int idDato;
    private int idPaciente;
    private double valorSenal;
    private boolean eventoDetectado;
    private String tipoEvento;
    private LocalDateTime timestamp;

    public DatoHistorico(int idPaciente, double valorSenal) {
        this.idPaciente = idPaciente;
        this.valorSenal = valorSenal;
        this.timestamp = LocalDateTime.now();
        this.eventoDetectado = false;
    }

    public int getIdDato() { return idDato; }
    public void setIdDato(int id) { this.idDato = id; }
    public int getIdPaciente() { return idPaciente; }
    public double getValorSenal() { return valorSenal; }
    public boolean isEventoDetectado() { return eventoDetectado; }
    public void setEventoDetectado(boolean evento) { this.eventoDetectado = evento; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipo) { this.tipoEvento = tipo; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class ModeloECG {
    private DatabaseManager db;
    private Paciente pacienteActual;
    private int sesionActual = -1;

    public ModeloECG() {
        this.db = new DatabaseManager();
    }

    public void crearPaciente(String nombre, int edad, double estatura) {
        int id = db.insertarPaciente(nombre, edad, estatura);
        if (id > 0) {
            System.out.println("✓ Paciente creado con ID: " + id);
        }
    }

    public Paciente obtenerPaciente(int id) {
        return db.obtenerPaciente(id);
    }

    public List<Paciente> obtenerTodosPacientes() {
        return db.obtenerTodosPacientes();
    }

    public void actualizarPaciente(int id, String nombre, int edad, double estatura) {
        db.actualizarPaciente(id, nombre, edad, estatura);
    }

    public void eliminarPaciente(int id) {
        db.eliminarPaciente(id);
    }

    public void agregarDatoHistorico(int idPaciente, double valorSenal) {
        // Solo agregar si hay una sesión activa
        if (sesionActual > 0) {
            db.insertarLecturaECG(sesionActual, valorSenal);
        }
    }

    public List<DatoHistorico> obtenerHistorialPaciente(int idPaciente) {
        return db.obtenerHistorialPaciente(idPaciente);
    }

    public int calcularBPM(List<Double> senalECG) {
        int complejos = detectarComplejos(senalECG);
        int bpm = complejos * 6;
        bpm = Math.min(Math.max(bpm, 40), 200);
        
        // Guardar cálculo en BD si hay sesión activa
        if (sesionActual > 0) {
            db.insertarCalculoBPM(sesionActual, bpm, complejos);
        }
        
        return bpm;
    }

    private int detectarComplejos(List<Double> senal) {
        int complejos = 0;
        double umbral = 150;
        for (double valor : senal) {
            if (valor > umbral) complejos++;
        }
        return complejos;
    }

    public void iniciarSesion(int idPaciente, String notas) {
        sesionActual = db.crearSesionMonitoreo(idPaciente, notas);
        System.out.println("✓ Sesión iniciada: " + sesionActual);
    }

    public void finalizarSesion() {
        if (sesionActual > 0) {
            db.finalizarSesion(sesionActual);
            System.out.println("✓ Sesión finalizada: " + sesionActual);
            sesionActual = -1;
        }
    }

    public void registrarComandoMotor(String comando) {
        if (sesionActual > 0) {
            db.registrarComandoMotor(sesionActual, comando);
        }
    }

    public void setPacienteActual(Paciente p) { 
        this.pacienteActual = p;
        // Iniciar nueva sesión cuando se selecciona un paciente
        if (p != null && sesionActual == -1) {
            iniciarSesion(p.getIdPaciente(), "Sesión de monitoreo");
        }
    }
    
    public Paciente getPacienteActual() { return pacienteActual; }
    
    public DatabaseManager getDatabase() { return db; }
    
    public void cerrarConexion() {
        finalizarSesion();
        db.cerrarConexion();
    }
}

// ============================================
// LECTOR MQTT (Mosquitto)
// ============================================

class MQTTDataReader {
    private String brokerURL;
    private String topico;
    private double ultimoDato;
    private boolean activo = false;
    private Thread hiloConexion;
    
    // Interface para callbacks
    interface MQTTCallback {
        void onDatoRecibido(double valor);
        void onConectado();
        void onDesconectado();
        void onError(String mensaje);
    }
    
    private MQTTCallback callback;

    public MQTTDataReader(String brokerURL, String topico) {
        this.brokerURL = brokerURL;
        this.topico = topico;
        this.ultimoDato = -1;
    }

    public void setCallback(MQTTCallback callback) {
        this.callback = callback;
    }

    public boolean conectar() {
        try {
            // Validar formato de URL
            if (!brokerURL.startsWith("tcp://") && !brokerURL.startsWith("mqtt://")) {
                brokerURL = "tcp://" + brokerURL;
            }
            
            System.out.println("Conectando a MQTT Broker: " + brokerURL);
            System.out.println("Topic: " + topico);
            
            // Simular conexión MQTT con HttpURLConnection (alternativa sin librerías externas)
            hiloConexion = new Thread(() -> {
                try {
                    // Conectar y mantener conexión simulada
                    activo = true;
                    if (callback != null) callback.onConectado();
                    
                    // Mantener conexión abierta
                    while (activo) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            hiloConexion.setDaemon(true);
            hiloConexion.start();
            
            return true;
        } catch (Exception e) {
            System.err.println("Error conectando a MQTT: " + e.getMessage());
            if (callback != null) callback.onError("Error de conexión: " + e.getMessage());
            return false;
        }
    }

    public void recibirDato(double valor) {
        this.ultimoDato = valor;
        if (callback != null) {
            callback.onDatoRecibido(valor);
        }
    }

    public void publicarMensaje(String topico, String mensaje) {
        try {
            System.out.println("[MQTT PUBLISH] Tema: " + topico + " | Mensaje: " + mensaje);
            // En producción, aquí iría el código real de publicación MQTT
        } catch (Exception e) {
            System.err.println("Error publicando mensaje: " + e.getMessage());
        }
    }

    public double leerValor() {
        if (ultimoDato >= 0) {
            double temp = ultimoDato;
            ultimoDato = -1;
            return temp;
        }
        return -1;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void desconectar() {
        activo = false;
        try {
            if (hiloConexion != null && hiloConexion.isAlive()) {
                hiloConexion.join(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (callback != null) callback.onDesconectado();
    }
}

// ============================================
// CONTROLADOR - Gestión de eventos
// ============================================

class ControladorECG {
    private final ModeloECG modelo;
    private final VistaECG vista;
    private final List<Double> bufferSenal;
    private boolean conectado = false;
    private MQTTDataReader mqttReader;
    private Thread hiloLectura;

    public ControladorECG(ModeloECG modelo, VistaECG vista) {
        this.modelo = modelo;
        this.vista = vista;
        this.bufferSenal = new ArrayList<>();
        inicializarDatos();
    }

    private void inicializarDatos() {
        modelo.crearPaciente("Juan Pérez", 45, 175.5);
        modelo.crearPaciente("María García", 38, 162.0);
        vista.actualizarListaPacientes(modelo.obtenerTodosPacientes());
    }

    public void conectarMQTT(String brokerURL, String topico) {
        try {
            mqttReader = new MQTTDataReader(brokerURL, topico);
            
            // Configurar callback MQTT
            mqttReader.setCallback(new MQTTDataReader.MQTTCallback() {
                @Override
                public void onConectado() {
                    vista.mostrarMensaje("Conectado a MQTT Broker: " + brokerURL);
                    vista.actualizarEstado("CONECTADO");
                }
                
                @Override
                public void onDatoRecibido(double valor) {
                    // Los datos se procesan en iniciarLecturaDatos()
                }
                
                @Override
                public void onDesconectado() {
                    vista.actualizarEstado("DESCONECTADO");
                    conectado = false;
                }
                
                @Override
                public void onError(String mensaje) {
                    vista.mostrarMensaje("Error MQTT: " + mensaje);
                    conectado = false;
                }
            });
            
            conectado = mqttReader.conectar();
            
            if (conectado) {
                iniciarLecturaDatos();
            } else {
                vista.mostrarMensaje("Error al conectar a MQTT");
            }
        } catch (Exception e) {
            vista.mostrarMensaje("Error: " + e.getMessage());
        }
    }
    
    // Mantener compatibilidad con llamadas anteriores
    public void conectarPuertoSerial(String puerto, int baudRate) {
        conectarMQTT(puerto, "ecg/datos");
    }

    private void iniciarLecturaDatos() {
        hiloLectura = new Thread(() -> {
            while (conectado && mqttReader.estaActivo()) {
                try {
                    double valor = mqttReader.leerValor();
                    if (valor >= 0) {
                        bufferSenal.add(valor);
                        if (bufferSenal.size() > 600) bufferSenal.remove(0);

                        if (modelo.getPacienteActual() != null) {
                            modelo.agregarDatoHistorico(modelo.getPacienteActual().getIdPaciente(), valor);
                        }

                        vista.actualizarGrafico(new ArrayList<>(bufferSenal));

                        if (bufferSenal.size() >= 100) {
                            int bpm = modelo.calcularBPM(bufferSenal);
                            vista.actualizarBPM(bpm);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error leyendo datos: " + e.getMessage());
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            conectado = false;
            vista.actualizarEstado("DESCONECTADO");
        });
        hiloLectura.setDaemon(true);
        hiloLectura.start();
    }

    public void crearPaciente(String nombre, int edad, double estatura) {
        modelo.crearPaciente(nombre, edad, estatura);
        vista.actualizarListaPacientes(modelo.obtenerTodosPacientes());
    }

    public void seleccionarPaciente(int idPaciente) {
        Paciente p = modelo.obtenerPaciente(idPaciente);
        modelo.setPacienteActual(p);
        vista.mostrarHistorialPaciente(modelo.obtenerHistorialPaciente(idPaciente));
    }

    public void actualizarPaciente(int id, String nombre, int edad, double estatura) {
        modelo.actualizarPaciente(id, nombre, edad, estatura);
        vista.actualizarListaPacientes(modelo.obtenerTodosPacientes());
    }

    public void eliminarPaciente(int idPaciente) {
        modelo.eliminarPaciente(idPaciente);
        vista.actualizarListaPacientes(modelo.obtenerTodosPacientes());
    }

    public void desconectar() {
        conectado = false;
        if (mqttReader != null) {
            mqttReader.desconectar();
        }
    }

    public void controlarMotor(String comando) {
        if (mqttReader == null || !conectado) {
            vista.mostrarMensaje("Error: No hay conexión MQTT activa");
            return;
        }
        
        // Registrar comando en la base de datos
        modelo.registrarComandoMotor(comando);
        
        // Publicar comando al motor
        String topico = "esp8266/motor";
        try {
            mqttReader.publicarMensaje(topico, comando);
            vista.mostrarMensaje("Comando enviado: MOTOR " + comando.toUpperCase());
        } catch (Exception e) {
            vista.mostrarMensaje("Error al controlar motor: " + e.getMessage());
        }
    }
}

// ============================================
// VISTA - Interfaz gráfica (Estilo Osciloscopio)
// ============================================

class VistaECG extends JFrame {
    private ControladorECG controlador;
    private JPanel panelGrafico;
    private JLabel labelBPM;
    private JLabel labelEstado;
    private JLabel labelTiempo;
    private JTable tablaPacientes;
    private JTable tablaHistorial;
    private List<Double> datosGrafico;
    
    // Colores estilo osciloscopio profesional
    private static final Color COLOR_FONDO = new Color(20, 25, 35);
    private static final Color COLOR_PANEL = new Color(30, 35, 45);
    private static final Color COLOR_GRAFICO = new Color(15, 18, 25);
    private static final Color COLOR_LINEA = new Color(0, 200, 100);
    private static final Color COLOR_LINEA_SECUNDARIA = new Color(100, 150, 200);
    private static final Color COLOR_TEXTO = new Color(200, 200, 200);
    private static final Color COLOR_BOTON = new Color(40, 45, 55);
    private static final Color COLOR_BOTON_HOVER = new Color(60, 70, 85);
    private static final Color COLOR_GRID = new Color(40, 45, 55);

    public VistaECG() {
        setTitle("Monitor ECG - Osciloscopio Digital");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 950);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(COLOR_FONDO);
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(COLOR_FONDO);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelPrincipal.add(crearPanelSuperior(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelGrafico(), BorderLayout.CENTER);
        panelPrincipal.add(crearPanelControles(), BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL);
        panel.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 2));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("█ OSCILOSCOPIO DIGITAL ECG");
        titulo.setFont(new Font("Courier New", Font.BOLD, 18));
        titulo.setForeground(COLOR_LINEA);

        JPanel panelInfo = new JPanel(new GridLayout(1, 3, 20, 0));
        panelInfo.setBackground(COLOR_PANEL);

        labelBPM = new JLabel("❤ BPM: --");
        labelBPM.setFont(new Font("Courier New", Font.BOLD, 24));
        labelBPM.setForeground(COLOR_LINEA);
        panelInfo.add(labelBPM);

        labelEstado = new JLabel("⚡ Estado: DESCONECTADO");
        labelEstado.setFont(new Font("Courier New", Font.PLAIN, 14));
        labelEstado.setForeground(new Color(255, 100, 100));
        panelInfo.add(labelEstado);

        labelTiempo = new JLabel("⏱ T: 0ms");
        labelTiempo.setFont(new Font("Courier New", Font.PLAIN, 14));
        labelTiempo.setForeground(COLOR_LINEA_SECUNDARIA);
        panelInfo.add(labelTiempo);

        panel.add(titulo, BorderLayout.WEST);
        panel.add(panelInfo, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelGrafico() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_FONDO);

        panelGrafico = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(COLOR_GRAFICO);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(COLOR_GRID);
                g2d.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < getWidth(); i += 40) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 40) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
                
                g2d.setColor(new Color(35, 40, 50));
                g2d.setStroke(new BasicStroke(0.5f));
                for (int i = 0; i < getWidth(); i += 8) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 8) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
                
                g2d.setColor(new Color(60, 70, 85));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
                
                dibujarECG(g2d);
            }
        };
        panelGrafico.setBackground(COLOR_GRAFICO);
        panelGrafico.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 3));

        JPanel panelMedidas = crearPanelMedidas();

        panel.add(panelGrafico, BorderLayout.CENTER);
        panel.add(panelMedidas, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelMedidas() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
        panel.setBackground(COLOR_PANEL);
        panel.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(150, 0));

        String[] medidas = {"SENSIBILIDAD\n1.0 mV/div", 
                           "BASE TIEMPO\n10 ms/div", 
                           "ACOPLAMIENTO\nDC", 
                           "MODO\nAuto", 
                           "DISPARO\nSubida"};
        
        for (String medida : medidas) {
            JLabel lbl = new JLabel("<html>" + medida.replace("\n", "<br>") + "</html>");
            lbl.setFont(new Font("Courier New", Font.BOLD, 10));
            lbl.setForeground(COLOR_LINEA_SECUNDARIA);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 1));
            panel.add(lbl);
        }

        return panel;
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_PANEL);
        panel.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBackground(COLOR_PANEL);

        JButton btnConectar = crearBoton("▶ INICIAR");
        btnConectar.addActionListener(e -> {
            String brokerURL = JOptionPane.showInputDialog(this, 
                "URL del broker MQTT (ej: localhost:1883):", "localhost:1883");
            if (brokerURL != null && !brokerURL.isEmpty() && controlador != null) {
                String topico = JOptionPane.showInputDialog(this,
                    "Topic MQTT (ej: ecg/datos):", "ecg/datos");
                if (topico != null && !topico.isEmpty()) {
                    controlador.conectarMQTT(brokerURL, topico);
                    labelEstado.setText("⚡ Estado: CONECTADO");
                    labelEstado.setForeground(new Color(0, 200, 100));
                }
            }
        });
        panelBotones.add(btnConectar);

        JButton btnDetener = crearBoton("⏹ DETENER");
        btnDetener.addActionListener(e -> {
            if (controlador != null) {
                controlador.desconectar();
                labelBPM.setText("❤ BPM: --");
                labelEstado.setText("⚡ Estado: DESCONECTADO");
                labelEstado.setForeground(new Color(255, 100, 100));
            }
        });
        panelBotones.add(btnDetener);

        JButton btnBorrar = crearBoton("🔄 REINICIAR");
        btnBorrar.addActionListener(e -> {
            datosGrafico = new ArrayList<>();
            panelGrafico.repaint();
        });
        panelBotones.add(btnBorrar);

        JButton btnPacientes = crearBoton("📋 PACIENTES");
        btnPacientes.addActionListener(e -> {
            mostrarVentanaPacientes();
        });
        panelBotones.add(btnPacientes);

        JButton btnMotorOn = crearBoton("🔋 MOTOR ENCENDER");
        btnMotorOn.addActionListener(e -> {
            if (controlador != null) {
                controlador.controlarMotor("on");
            }
        });
        panelBotones.add(btnMotorOn);

        JButton btnMotorOff = crearBoton("🛑 MOTOR APAGAR");
        btnMotorOff.addActionListener(e -> {
            if (controlador != null) {
                controlador.controlarMotor("off");
            }
        });
        panelBotones.add(btnMotorOff);

        panel.add(panelBotones, BorderLayout.CENTER);

        return panel;
    }

    private void mostrarVentanaPacientes() {
        JFrame ventana = new JFrame("Gestión de Pacientes");
        ventana.setSize(1000, 600);
        ventana.setLocationRelativeTo(this);
        ventana.getContentPane().setBackground(COLOR_FONDO);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelForm = new JPanel(new GridLayout(3, 2, 10, 10));
        panelForm.setBackground(COLOR_PANEL);
        panelForm.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 85), 1),
            "Nuevo Paciente", 0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_TEXTO));

        JTextField txtNombre = crearTextField();
        JTextField txtEdad = crearTextField();
        JTextField txtEstatura = crearTextField();

        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setForeground(COLOR_TEXTO);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelForm.add(lblNombre);
        panelForm.add(txtNombre);
        
        JLabel lblEdad = new JLabel("Edad:");
        lblEdad.setForeground(COLOR_TEXTO);
        lblEdad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelForm.add(lblEdad);
        panelForm.add(txtEdad);
        
        JLabel lblEstatura = new JLabel("Altura (cm):");
        lblEstatura.setForeground(COLOR_TEXTO);
        lblEstatura.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelForm.add(lblEstatura);
        panelForm.add(txtEstatura);

        JButton btnAgregar = crearBoton("Agregar");
        btnAgregar.addActionListener(e -> {
            try {
                if (controlador == null) {
                    JOptionPane.showMessageDialog(ventana, "Error: Controlador no inicializado", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Obtener y limpiar los valores de entrada
                String nombre = txtNombre.getText().trim().replaceAll("\\s+", " ");
                String edadStr = txtEdad.getText().trim();
                String estaturaStr = txtEstatura.getText().trim();
                
                // Validar campos vacíos
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(ventana, "Por favor ingresa el nombre del paciente", "Nombre requerido", JOptionPane.WARNING_MESSAGE);
                    txtNombre.requestFocus();
                    return;
                }
                
                if (edadStr.isEmpty()) {
                    JOptionPane.showMessageDialog(ventana, "Por favor ingresa la edad", "Edad requerida", JOptionPane.WARNING_MESSAGE);
                    txtEdad.requestFocus();
                    return;
                }
                
                if (estaturaStr.isEmpty()) {
                    JOptionPane.showMessageDialog(ventana, "Por favor ingresa la estatura", "Estatura requerida", JOptionPane.WARNING_MESSAGE);
                    txtEstatura.requestFocus();
                    return;
                }
                
                // Validar y convertir edad
                int edad;
                try {
                    edad = Integer.parseInt(edadStr);
                    if (edad <= 0 || edad >= 150) {
                        JOptionPane.showMessageDialog(ventana, 
                            "La edad debe estar entre 1 y 150 años\nValor ingresado: " + edad, 
                            "Edad fuera de rango", 
                            JOptionPane.ERROR_MESSAGE);
                        txtEdad.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ventana, 
                        "La edad debe ser un número entero\nTexto ingresado: '" + edadStr + "'\nEjemplo válido: 21", 
                        "Edad inválida", 
                        JOptionPane.ERROR_MESSAGE);
                    txtEdad.requestFocus();
                    txtEdad.selectAll();
                    return;
                }
                
                // Validar y convertir estatura
                double estatura;
                try {
                    estatura = Double.parseDouble(estaturaStr);
                    if (estatura <= 0 || estatura >= 300) {
                        JOptionPane.showMessageDialog(ventana, 
                            "La estatura debe estar entre 1 y 300 cm\nValor ingresado: " + estatura, 
                            "Estatura fuera de rango", 
                            JOptionPane.ERROR_MESSAGE);
                        txtEstatura.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ventana, 
                        "La estatura debe ser un número válido\nTexto ingresado: '" + estaturaStr + "'\nEjemplo válido: 171", 
                        "Estatura inválida", 
                        JOptionPane.ERROR_MESSAGE);
                    txtEstatura.requestFocus();
                    txtEstatura.selectAll();
                    return;
                }
                
                controlador.crearPaciente(nombre, edad, estatura);
                txtNombre.setText("");
                txtEdad.setText("");
                txtEstatura.setText("");
                JOptionPane.showMessageDialog(ventana, "Paciente agregado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ventana, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        JPanel panelBtnAgregar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBtnAgregar.setBackground(COLOR_FONDO);
        panelBtnAgregar.add(btnAgregar);

        tablaPacientes = new JTable(new DefaultTableModel(
                new String[]{"ID", "Nombre", "Edad", "Estatura"}, 0));
        estilizarTabla(tablaPacientes);
        tablaPacientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tablaPacientes.getSelectedRow();
                if (row >= 0) {
                    int id = (int) tablaPacientes.getValueAt(row, 0);
                    controlador.seleccionarPaciente(id);
                }
            }
        });
        JScrollPane scrollPacientes = new JScrollPane(tablaPacientes);

        tablaHistorial = new JTable(new DefaultTableModel(
                new String[]{"Timestamp", "Valor", "Evento"}, 0));
        estilizarTabla(tablaHistorial);
        JScrollPane scrollHistorial = new JScrollPane(tablaHistorial);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPacientes, scrollHistorial);
        split.setDividerLocation(0.5);

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(COLOR_FONDO);
        panelNorte.add(panelForm, BorderLayout.NORTH);
        panelNorte.add(panelBtnAgregar, BorderLayout.SOUTH);

        panel.add(panelNorte, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        ventana.add(panel);
        ventana.setVisible(true);
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Courier New", Font.BOLD, 11));
        btn.setBackground(COLOR_BOTON);
        btn.setForeground(COLOR_LINEA);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 2));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(10, 20, 10, 20));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_BOTON_HOVER);
                btn.setBorder(BorderFactory.createLineBorder(COLOR_LINEA, 2));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_BOTON);
                btn.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 2));
            }
        });
        return btn;
    }

    private JTextField crearTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Courier New", Font.PLAIN, 11));
        txt.setBackground(COLOR_GRAFICO);
        txt.setForeground(COLOR_TEXTO);
        txt.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 85), 1));
        txt.setCaretColor(COLOR_LINEA);
        return txt;
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setBackground(COLOR_PANEL);
        tabla.setForeground(COLOR_TEXTO);
        tabla.setGridColor(new Color(40, 50, 65));
        tabla.getTableHeader().setBackground(COLOR_GRAFICO);
        tabla.getTableHeader().setForeground(COLOR_LINEA);
        tabla.getTableHeader().setFont(new Font("Courier New", Font.BOLD, 10));
        tabla.setFont(new Font("Courier New", Font.PLAIN, 10));
        tabla.setRowHeight(22);
    }

    private void dibujarECG(Graphics2D g) {
        if (datosGrafico == null || datosGrafico.isEmpty()) return;

        int ancho = panelGrafico.getWidth();
        int alto = panelGrafico.getHeight();
        int centroY = alto / 2;

        g.setColor(COLOR_LINEA);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 1; i < datosGrafico.size(); i++) {
            int x1 = (i - 1) * ancho / datosGrafico.size();
            int x2 = i * ancho / datosGrafico.size();
            int y1 = (int) (centroY - datosGrafico.get(i - 1) * 0.3);
            int y2 = (int) (centroY - datosGrafico.get(i) * 0.3);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public void actualizarGrafico(List<Double> datos) {
        this.datosGrafico = datos;
        panelGrafico.repaint();
        labelTiempo.setText("⏱ T: " + (datos.size() * 10) + "ms");
    }

    public void actualizarBPM(int bpm) {
        labelBPM.setText("❤ BPM: " + bpm);
    }

    public void actualizarEstado(String estado) {
        labelEstado.setText("⚡ Estado: " + estado);
    }

    public void actualizarListaPacientes(List<Paciente> pacientes) {
        if (tablaPacientes == null) return;
        DefaultTableModel model = (DefaultTableModel) tablaPacientes.getModel();
        model.setRowCount(0);
        for (Paciente p : pacientes) {
            model.addRow(new Object[]{p.getIdPaciente(), p.getNombre(),
                    p.getEdad(), p.getEstatura()});
        }
    }

    public void mostrarHistorialPaciente(List<DatoHistorico> historial) {
        if (tablaHistorial == null) return;
        DefaultTableModel model = (DefaultTableModel) tablaHistorial.getModel();
        model.setRowCount(0);
        for (DatoHistorico d : historial) {
            model.addRow(new Object[]{d.getTimestamp(), String.format("%.2f", d.getValorSenal()),
                    d.isEventoDetectado() ? d.getTipoEvento() : "Normal"});
        }
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Sistema ECG", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setControlador(ControladorECG controlador) {
        this.controlador = controlador;
    }
}

// ============================================
// MAIN - Inicialización
// ============================================

public class SistemaECG {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ModeloECG modelo = new ModeloECG();
            VistaECG vista = new VistaECG();
            ControladorECG controlador = new ControladorECG(modelo, vista);
            vista.setControlador(controlador);
            vista.setVisible(true);
            
            // Agregar hook para cerrar la base de datos al salir
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n✓ Cerrando sistema ECG...");
                modelo.cerrarConexion();
            }));
        });
    }
}
