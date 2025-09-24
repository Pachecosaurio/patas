package com.gestorbiblioteca.controlador;

import com.gestorbiblioteca.modelo.Libro;
import com.gestorbiblioteca.modelo.Prestamo;
import com.gestorbiblioteca.modelo.ReporteService;
import com.gestorbiblioteca.modelo.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Controlador para la generación y visualización de reportes del sistema de biblioteca
 */
public class ReportesController {
    
    @FXML private TabPane tabReportes;
    
    // Pestaña de Estadísticas Generales
    @FXML private Label lblTotalLibros;
    @FXML private Label lblLibrosDisponibles;
    @FXML private Label lblLibrosPrestados;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblUsuariosActivos;
    @FXML private Label lblPrestamosActivos;
    @FXML private Label lblPrestamosVencidos;
    @FXML private ProgressBar pbOcupacion;
    @FXML private Label lblPorcentajeOcupacion;
    
    // Pestaña de Gráficos
    @FXML private PieChart pieChartEstadoPrestamos;
    @FXML private BarChart<String, Number> barChartLibrosPrestados;
    @FXML private BarChart<String, Number> barChartUsuariosActivos;
    @FXML private PieChart pieChartCategorias;
    
    // Pestaña de Préstamos Vencidos
    @FXML private TableView<Prestamo> tablaPrestamosVencidos;
    @FXML private TableColumn<Prestamo, String> colUsuarioVencido;
    @FXML private TableColumn<Prestamo, String> colLibroVencido;
    @FXML private TableColumn<Prestamo, String> colFechaVencimiento;
    @FXML private TableColumn<Prestamo, Long> colDiasVencido;
    
    // Pestaña de Reportes Detallados
    @FXML private ListView<String> listReporteDetallado;
    @FXML private ComboBox<TipoReporte> cmbTipoReporte;
    @FXML private Button btnGenerarReporte;
    @FXML private TextArea txtAreaReporte;
    
    private ReporteService reporteService;
    private ObservableList<Libro> libros;
    private ObservableList<Usuario> usuarios;
    private ObservableList<Prestamo> prestamos;
    
    public enum TipoReporte {
        LIBROS_MAS_PRESTADOS("Libros Más Prestados"),
        USUARIOS_MAS_ACTIVOS("Usuarios Más Activos"),
        PRESTAMOS_POR_CATEGORIA("Préstamos por Categoría"),
        RESUMEN_MENSUAL("Resumen Mensual");
        
        private final String descripcion;
        
        TipoReporte(String descripcion) {
            this.descripcion = descripcion;
        }
        
        @Override
        public String toString() {
            return descripcion;
        }
    }

    @FXML
    private void initialize() {
        inicializarComponentes();
        configurarEventos();
        
        // Inicializar servicio de reportes
        reporteService = new ReporteService();
        
        // Crear datos de ejemplo si no se han proporcionado
        if (libros == null || usuarios == null || prestamos == null) {
            crearDatosEjemplo();
        }
        
        actualizarReportes();
    }

    private void inicializarComponentes() {
        // Configurar ComboBox de tipos de reporte
        cmbTipoReporte.setItems(FXCollections.observableArrayList(TipoReporte.values()));
        cmbTipoReporte.setValue(TipoReporte.LIBROS_MAS_PRESTADOS);
        
        // Configurar tabla de préstamos vencidos
        configurarTablaPrestamosVencidos();
        
        // Configurar gráficos
        configurarGraficos();
    }

    private void configurarTablaPrestamosVencidos() {
        colUsuarioVencido.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue().getUsuario();
            String nombreCompleto = usuario != null ? usuario.getNombreCompleto() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(nombreCompleto);
        });
        
        colLibroVencido.setCellValueFactory(cellData -> {
            Libro libro = cellData.getValue().getLibro();
            String titulo = libro != null ? libro.getTitulo() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(titulo);
        });
        
        colFechaVencimiento.setCellValueFactory(cellData -> {
            String fecha = cellData.getValue().getFechaDevolucionEsperada().toString();
            return new javafx.beans.property.SimpleStringProperty(fecha);
        });
        
        colDiasVencido.setCellValueFactory(cellData -> {
            long diasVencido = Math.abs(cellData.getValue().getDiasRestantes());
            return new javafx.beans.property.SimpleLongProperty(diasVencido).asObject();
        });
        
        // Personalizar columna de días vencidos
        colDiasVencido.setCellFactory(column -> new TableCell<Prestamo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + " días");
                    if (item > 30) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item > 7) {
                        setStyle("-fx-background-color: #ffe6cc; -fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #fff2cc; -fx-text-fill: #b8860b;");
                    }
                }
            }
        });
    }

    private void configurarGraficos() {
        // Configurar ejes del gráfico de barras
        CategoryAxis xAxis = (CategoryAxis) barChartLibrosPrestados.getXAxis();
        NumberAxis yAxis = (NumberAxis) barChartLibrosPrestados.getYAxis();
        
        xAxis.setLabel("Libros");
        yAxis.setLabel("Número de Préstamos");
        
        CategoryAxis xAxis2 = (CategoryAxis) barChartUsuariosActivos.getXAxis();
        NumberAxis yAxis2 = (NumberAxis) barChartUsuariosActivos.getYAxis();
        
        xAxis2.setLabel("Usuarios");
        yAxis2.setLabel("Préstamos Realizados");
    }

    private void configurarEventos() {
        btnGenerarReporte.setOnAction(e -> generarReporteDetallado());
        cmbTipoReporte.setOnAction(e -> generarReporteDetallado());
    }

    private void crearDatosEjemplo() {
        // Crear usuarios de ejemplo
        usuarios = FXCollections.observableArrayList();
        usuarios.add(new Usuario(1, "Ana", "García", "ana@email.com", "555-0101", "Calle 1", Usuario.TipoUsuario.ESTUDIANTE));
        usuarios.add(new Usuario(2, "Carlos", "Rodríguez", "carlos@email.com", "555-0102", "Calle 2", Usuario.TipoUsuario.PROFESOR));
        usuarios.add(new Usuario(3, "María", "López", "maria@email.com", "555-0103", "Calle 3", Usuario.TipoUsuario.ADMINISTRATIVO));
        
        // Crear libros de ejemplo
        libros = FXCollections.observableArrayList();
        libros.add(new Libro(1, "Cien años de soledad", "Gabriel García Márquez", "978-123", "Ficción", "1967", "Desc", "/libros/libro1.jpeg"));
        libros.add(new Libro(2, "El Quijote", "Miguel de Cervantes", "978-456", "Clásicos", "1605", "Desc", "/libros/libro2.jpeg"));
        libros.add(new Libro(3, "1984", "George Orwell", "978-789", "Distopía", "1949", "Desc", "/libros/libro3.jpeg"));
        libros.add(new Libro(4, "Cálculo", "James Stewart", "978-101", "Matemáticas", "2015", "Desc", "/libros/libro4.jpeg"));
        libros.add(new Libro(5, "Historia del Arte", "Ernst Gombrich", "978-202", "Arte", "1950", "Desc", "/libros/libro5.jpeg"));
        
        // Crear préstamos de ejemplo
        prestamos = FXCollections.observableArrayList();
        // Algunos préstamos activos
        Prestamo p1 = new Prestamo(1, usuarios.get(0), libros.get(0), java.time.LocalDate.now().plusDays(10));
        Prestamo p2 = new Prestamo(2, usuarios.get(1), libros.get(1), java.time.LocalDate.now().plusDays(5));
        // Algunos préstamos vencidos
        Prestamo p3 = new Prestamo(3, usuarios.get(2), libros.get(2), java.time.LocalDate.now().minusDays(5));
        p3.setEstado(Prestamo.EstadoPrestamo.VENCIDO);
        // Préstamos devueltos
        Prestamo p4 = new Prestamo(4, usuarios.get(0), libros.get(3), java.time.LocalDate.now());
        p4.marcarComoDevuelto();
        
        prestamos.addAll(p1, p2, p3, p4);
    }

    public void actualizarReportes() {
        if (libros != null && usuarios != null && prestamos != null) {
            // Configurar el servicio de reportes
            reporteService.setLibros(new ArrayList<>(libros));
            reporteService.setUsuarios(new ArrayList<>(usuarios));
            reporteService.setPrestamos(new ArrayList<>(prestamos));
            
            // Actualizar estadísticas generales
            actualizarEstadisticasGenerales();
            
            // Actualizar gráficos
            actualizarGraficos();
            
            // Actualizar tabla de préstamos vencidos
            actualizarPrestamosVencidos();
        }
    }

    private void actualizarEstadisticasGenerales() {
        ReporteService.EstadisticasGenerales stats = reporteService.getEstadisticasGenerales();
        
        lblTotalLibros.setText(String.valueOf(stats.getTotalLibros()));
        lblLibrosDisponibles.setText(String.valueOf(stats.getLibrosDisponibles()));
        lblLibrosPrestados.setText(String.valueOf(stats.getLibrosPrestados()));
        lblTotalUsuarios.setText(String.valueOf(stats.getTotalUsuarios()));
        lblUsuariosActivos.setText(String.valueOf(stats.getUsuariosActivos()));
        lblPrestamosActivos.setText(String.valueOf(stats.getPrestamosActivos()));
        lblPrestamosVencidos.setText(String.valueOf(stats.getPrestamosVencidos()));
        
        // Calcular y mostrar porcentaje de ocupación
        double porcentajeOcupacion = stats.getTotalLibros() > 0 ? 
            (double) stats.getLibrosPrestados() / stats.getTotalLibros() : 0;
        pbOcupacion.setProgress(porcentajeOcupacion);
        lblPorcentajeOcupacion.setText(String.format("%.1f%%", porcentajeOcupacion * 100));
    }

    private void actualizarGraficos() {
        // Gráfico de estado de préstamos
        actualizarGraficoEstadoPrestamos();
        
        // Gráfico de libros más prestados
        actualizarGraficoLibrosPrestados();
        
        // Gráfico de usuarios más activos
        actualizarGraficoUsuariosActivos();
        
        // Gráfico de categorías
        actualizarGraficoCategorias();
    }

    private void actualizarGraficoEstadoPrestamos() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Map<Prestamo.EstadoPrestamo, Long> estadosPrestamos = reporteService.getPrestamosPorEstado();
        
        for (Map.Entry<Prestamo.EstadoPrestamo, Long> entry : estadosPrestamos.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey().toString(), entry.getValue()));
            }
        }
        
        pieChartEstadoPrestamos.setData(pieChartData);
        pieChartEstadoPrestamos.setTitle("Estado de Préstamos");
    }

    private void actualizarGraficoLibrosPrestados() {
        barChartLibrosPrestados.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Préstamos");
        
        Map<String, Integer> librosMasPrestados = reporteService.getLibrosMasPrestados();
        
        // Tomar solo los 5 más prestados
        librosMasPrestados.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                String titulo = entry.getKey().length() > 20 ? 
                    entry.getKey().substring(0, 17) + "..." : entry.getKey();
                series.getData().add(new XYChart.Data<>(titulo, entry.getValue()));
            });
        
        barChartLibrosPrestados.getData().add(series);
        barChartLibrosPrestados.setTitle("Libros Más Prestados");
    }

    private void actualizarGraficoUsuariosActivos() {
        barChartUsuariosActivos.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Préstamos");
        
        Map<String, Integer> usuariosMasActivos = reporteService.getUsuariosMasActivos();
        
        // Tomar solo los 5 más activos
        usuariosMasActivos.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        barChartUsuariosActivos.getData().add(series);
        barChartUsuariosActivos.setTitle("Usuarios Más Activos");
    }

    private void actualizarGraficoCategorias() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Map<String, Long> librosPorCategoria = reporteService.getLibrosPorCategoria();
        
        for (Map.Entry<String, Long> entry : librosPorCategoria.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        
        pieChartCategorias.setData(pieChartData);
        pieChartCategorias.setTitle("Distribución por Categorías");
    }

    private void actualizarPrestamosVencidos() {
        List<Prestamo> prestamosVencidos = reporteService.getPrestamosVencidos();
        ObservableList<Prestamo> prestamosVencidosObservable = 
            FXCollections.observableArrayList(prestamosVencidos);
        
        tablaPrestamosVencidos.setItems(prestamosVencidosObservable);
    }

    @FXML
    private void generarReporteDetallado() {
        TipoReporte tipoSeleccionado = cmbTipoReporte.getValue();
        if (tipoSeleccionado != null) {
            String reporte = generarTextoReporte(tipoSeleccionado);
            txtAreaReporte.setText(reporte);
        }
    }

    private String generarTextoReporte(TipoReporte tipo) {
        StringBuilder sb = new StringBuilder();
        java.time.LocalDate fecha = java.time.LocalDate.now();
        
        sb.append("=".repeat(50)).append("\n");
        sb.append("BIBLIOTECA DIGITAL - ").append(tipo.toString().toUpperCase()).append("\n");
        sb.append("Fecha de generación: ").append(fecha).append("\n");
        sb.append("=".repeat(50)).append("\n\n");
        
        switch (tipo) {
            case LIBROS_MAS_PRESTADOS:
                generarReporteLibrosMasPrestados(sb);
                break;
            case USUARIOS_MAS_ACTIVOS:
                generarReporteUsuariosMasActivos(sb);
                break;
            case PRESTAMOS_POR_CATEGORIA:
                generarReportePrestamosPorCategoria(sb);
                break;
            case RESUMEN_MENSUAL:
                generarReporteResumenMensual(sb);
                break;
        }
        
        return sb.toString();
    }

    private void generarReporteLibrosMasPrestados(StringBuilder sb) {
        Map<String, Integer> libros = reporteService.getLibrosMasPrestados();
        
        sb.append("RANKING DE LIBROS MÁS PRESTADOS:\n");
        sb.append("-".repeat(40)).append("\n");
        
        libros.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                sb.append(String.format("%-30s: %d préstamos\n", 
                    entry.getKey(), entry.getValue()));
            });
    }

    private void generarReporteUsuariosMasActivos(StringBuilder sb) {
        Map<String, Integer> usuarios = reporteService.getUsuariosMasActivos();
        
        sb.append("RANKING DE USUARIOS MÁS ACTIVOS:\n");
        sb.append("-".repeat(40)).append("\n");
        
        usuarios.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                sb.append(String.format("%-25s: %d préstamos\n", 
                    entry.getKey(), entry.getValue()));
            });
    }

    private void generarReportePrestamosPorCategoria(StringBuilder sb) {
        Map<String, Long> categorias = reporteService.getLibrosPorCategoria();
        
        sb.append("DISTRIBUCIÓN DE LIBROS POR CATEGORÍA:\n");
        sb.append("-".repeat(40)).append("\n");
        
        categorias.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(entry -> {
                sb.append(String.format("%-20s: %d libros\n", 
                    entry.getKey(), entry.getValue()));
            });
    }

    private void generarReporteResumenMensual(StringBuilder sb) {
        ReporteService.EstadisticasGenerales stats = reporteService.getEstadisticasGenerales();
        
        sb.append("RESUMEN ESTADÍSTICO GENERAL:\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("Total de libros: %d\n", stats.getTotalLibros()));
        sb.append(String.format("Libros disponibles: %d\n", stats.getLibrosDisponibles()));
        sb.append(String.format("Libros prestados: %d\n", stats.getLibrosPrestados()));
        sb.append(String.format("Total de usuarios: %d\n", stats.getTotalUsuarios()));
        sb.append(String.format("Usuarios activos: %d\n", stats.getUsuariosActivos()));
        sb.append(String.format("Préstamos activos: %d\n", stats.getPrestamosActivos()));
        sb.append(String.format("Préstamos vencidos: %d\n", stats.getPrestamosVencidos()));
        
        double porcentaje = stats.getTotalLibros() > 0 ? 
            (double) stats.getLibrosPrestados() / stats.getTotalLibros() * 100 : 0;
        sb.append(String.format("Porcentaje de ocupación: %.1f%%\n", porcentaje));
    }

    // Métodos públicos para integración
    public void setDatos(ObservableList<Libro> libros, ObservableList<Usuario> usuarios, 
                        ObservableList<Prestamo> prestamos) {
        this.libros = libros;
        this.usuarios = usuarios;
        this.prestamos = prestamos;
        actualizarReportes();
    }
}