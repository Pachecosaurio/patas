package com.gestorbiblioteca.controlador;

import com.gestorbiblioteca.modelo.Libro;
import com.gestorbiblioteca.modelo.Prestamo;
import com.gestorbiblioteca.modelo.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la gestión de préstamos en el sistema de biblioteca
 */
public class PrestamosController {
    
    @FXML private TableView<Prestamo> tablaPrestamos;
    @FXML private TableColumn<Prestamo, Integer> colId;
    @FXML private TableColumn<Prestamo, String> colUsuario;
    @FXML private TableColumn<Prestamo, String> colLibro;
    @FXML private TableColumn<Prestamo, LocalDate> colFechaPrestamo;
    @FXML private TableColumn<Prestamo, LocalDate> colFechaDevolucion;
    @FXML private TableColumn<Prestamo, Prestamo.EstadoPrestamo> colEstado;
    @FXML private TableColumn<Prestamo, Long> colDiasRestantes;
    
    @FXML private ComboBox<Usuario> cmbUsuario;
    @FXML private ComboBox<Libro> cmbLibro;
    @FXML private DatePicker dpFechaPrestamo;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private ComboBox<Prestamo.EstadoPrestamo> cmbEstado;
    @FXML private TextArea txtObservaciones;
    
    @FXML private Button btnCrearPrestamo;
    @FXML private Button btnModificar;
    @FXML private Button btnDevolver;
    @FXML private Button btnRenovar;
    @FXML private Button btnBuscar;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Prestamo.EstadoPrestamo> cmbFiltroEstado;
    
    private ObservableList<Prestamo> listaPrestamos;
    private ObservableList<Usuario> listaUsuarios;
    private ObservableList<Libro> listaLibros;
    private Prestamo prestamoSeleccionado;

    @FXML
    private void initialize() {
        configurarTabla();
        inicializarComponentes();
        inicializarDatos();
        configurarEventos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFechaPrestamo.setCellValueFactory(new PropertyValueFactory<>("fechaPrestamo"));
        colFechaDevolucion.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucionEsperada"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Configurar columna de usuario
        colUsuario.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue().getUsuario();
            String nombreCompleto = usuario != null ? usuario.getNombreCompleto() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(nombreCompleto);
        });
        
        // Configurar columna de libro
        colLibro.setCellValueFactory(cellData -> {
            Libro libro = cellData.getValue().getLibro();
            String titulo = libro != null ? libro.getTitulo() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(titulo);
        });
        
        // Configurar columna de días restantes
        colDiasRestantes.setCellValueFactory(cellData -> {
            long diasRestantes = cellData.getValue().getDiasRestantes();
            return new javafx.beans.property.SimpleLongProperty(diasRestantes).asObject();
        });
        
        // Personalizar la columna de estado
        colEstado.setCellFactory(column -> new TableCell<Prestamo, Prestamo.EstadoPrestamo>() {
            @Override
            protected void updateItem(Prestamo.EstadoPrestamo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toString());
                    switch (item) {
                        case ACTIVO:
                            label.setStyle("-fx-text-fill: blue;");
                            break;
                        case DEVUELTO:
                            label.setStyle("-fx-text-fill: green;");
                            break;
                        case VENCIDO:
                            label.setStyle("-fx-text-fill: red;");
                            break;
                        case RENOVADO:
                            label.setStyle("-fx-text-fill: orange;");
                            break;
                    }
                    setGraphic(label);
                }
            }
        });
        
        // Personalizar la columna de días restantes
        colDiasRestantes.setCellFactory(column -> new TableCell<Prestamo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item.toString());
                    if (item < 0) {
                        label.setText(Math.abs(item) + " días vencido");
                        label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item <= 3) {
                        label.setText(item + " días");
                        label.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        label.setText(item + " días");
                        label.setStyle("-fx-text-fill: green;");
                    }
                    setGraphic(label);
                }
            }
        });
        
        listaPrestamos = FXCollections.observableArrayList();
        tablaPrestamos.setItems(listaPrestamos);
        
        // Listener para selección de filas
        tablaPrestamos.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> mostrarDetallesPrestamo(newValue)
        );
    }

    private void inicializarComponentes() {
        // Configurar ComboBoxes
        listaUsuarios = FXCollections.observableArrayList();
        listaLibros = FXCollections.observableArrayList();
        
        cmbUsuario.setItems(listaUsuarios);
        cmbLibro.setItems(listaLibros);
        cmbEstado.setItems(FXCollections.observableArrayList(Prestamo.EstadoPrestamo.values()));
        
        // ComboBox de filtro
        ObservableList<Prestamo.EstadoPrestamo> estadosConTodos = FXCollections.observableArrayList();
        estadosConTodos.addAll(Prestamo.EstadoPrestamo.values());
        cmbFiltroEstado.setItems(estadosConTodos);
        
        // Configurar DatePickers
        dpFechaPrestamo.setValue(LocalDate.now());
        dpFechaDevolucion.setValue(LocalDate.now().plusDays(14)); // 2 semanas por defecto
        
        // Estado inicial de controles
        cmbEstado.setValue(Prestamo.EstadoPrestamo.ACTIVO);
        
        // Configurar StringConverter para ComboBoxes
        cmbUsuario.setConverter(new javafx.util.StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario != null ? usuario.getNombreCompleto() + " (" + usuario.getTipoUsuario() + ")" : "";
            }
            
            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });
        
        cmbLibro.setConverter(new javafx.util.StringConverter<Libro>() {
            @Override
            public String toString(Libro libro) {
                return libro != null ? libro.getTitulo() + " - " + libro.getAutor() : "";
            }
            
            @Override
            public Libro fromString(String string) {
                return null;
            }
        });
    }

    private void inicializarDatos() {
        // Crear datos de ejemplo
        crearUsuariosEjemplo();
        crearLibrosEjemplo();
        crearPrestamosEjemplo();
    }

    private void crearUsuariosEjemplo() {
        listaUsuarios.add(new Usuario(1, "Ana", "García", "ana@email.com", "555-0101", "Calle 1", Usuario.TipoUsuario.ESTUDIANTE));
        listaUsuarios.add(new Usuario(2, "Carlos", "Rodríguez", "carlos@email.com", "555-0102", "Calle 2", Usuario.TipoUsuario.PROFESOR));
        listaUsuarios.add(new Usuario(3, "María", "López", "maria@email.com", "555-0103", "Calle 3", Usuario.TipoUsuario.ADMINISTRATIVO));
    }

    private void crearLibrosEjemplo() {
        listaLibros.add(new Libro(1, "Cien años de soledad", "Gabriel García Márquez", "978-123", "Ficción", "1967", "Desc", "/libros/libro1.jpeg"));
        listaLibros.add(new Libro(2, "El Quijote", "Miguel de Cervantes", "978-456", "Clásicos", "1605", "Desc", "/libros/libro2.jpeg"));
        listaLibros.add(new Libro(3, "1984", "George Orwell", "978-789", "Distopía", "1949", "Desc", "/libros/libro3.jpeg"));
    }

    private void crearPrestamosEjemplo() {
        Prestamo prestamo1 = new Prestamo(1, listaUsuarios.get(0), listaLibros.get(0), LocalDate.now().plusDays(10));
        prestamo1.setFechaPrestamo(LocalDate.now().minusDays(4));
        
        Prestamo prestamo2 = new Prestamo(2, listaUsuarios.get(1), listaLibros.get(1), LocalDate.now().minusDays(2));
        prestamo2.setFechaPrestamo(LocalDate.now().minusDays(16));
        prestamo2.setEstado(Prestamo.EstadoPrestamo.VENCIDO);
        
        Prestamo prestamo3 = new Prestamo(3, listaUsuarios.get(2), listaLibros.get(2), LocalDate.now().plusDays(20));
        prestamo3.setFechaPrestamo(LocalDate.now().minusDays(1));
        prestamo3.setEstado(Prestamo.EstadoPrestamo.RENOVADO);
        
        listaPrestamos.addAll(prestamo1, prestamo2, prestamo3);
        
        // Marcar libros como no disponibles
        listaLibros.get(0).setDisponible(false);
        listaLibros.get(1).setDisponible(false);
        listaLibros.get(2).setDisponible(false);
    }

    private void configurarEventos() {
        btnCrearPrestamo.setOnAction(e -> crearPrestamo());
        btnModificar.setOnAction(e -> modificarPrestamo());
        btnDevolver.setOnAction(e -> devolverLibro());
        btnRenovar.setOnAction(e -> renovarPrestamo());
        btnBuscar.setOnAction(e -> buscarPrestamos());
        
        txtBuscar.setOnKeyReleased(e -> buscarPrestamos());
        cmbFiltroEstado.setOnAction(e -> filtrarPorEstado());
        
        // Filtrar libros disponibles cuando se selecciona crear préstamo
        cmbLibro.setOnMouseClicked(e -> actualizarLibrosDisponibles());
    }

    private void mostrarDetallesPrestamo(Prestamo prestamo) {
        this.prestamoSeleccionado = prestamo;
        
        if (prestamo != null) {
            cmbUsuario.setValue(prestamo.getUsuario());
            cmbLibro.setValue(prestamo.getLibro());
            dpFechaPrestamo.setValue(prestamo.getFechaPrestamo());
            dpFechaDevolucion.setValue(prestamo.getFechaDevolucionEsperada());
            cmbEstado.setValue(prestamo.getEstado());
            txtObservaciones.setText(prestamo.getObservaciones());
            
            btnModificar.setDisable(false);
            btnDevolver.setDisable(prestamo.getEstado() == Prestamo.EstadoPrestamo.DEVUELTO);
            btnRenovar.setDisable(prestamo.getEstado() != Prestamo.EstadoPrestamo.ACTIVO);
        } else {
            limpiarCampos();
            btnModificar.setDisable(true);
            btnDevolver.setDisable(true);
            btnRenovar.setDisable(true);
        }
    }

    @FXML
    private void crearPrestamo() {
        if (validarCamposPrestamo()) {
            int nuevoId = listaPrestamos.size() + 1;
            
            Prestamo nuevoPrestamo = new Prestamo(
                nuevoId,
                cmbUsuario.getValue(),
                cmbLibro.getValue(),
                dpFechaDevolucion.getValue()
            );
            
            nuevoPrestamo.setFechaPrestamo(dpFechaPrestamo.getValue());
            nuevoPrestamo.setObservaciones(txtObservaciones.getText());
            
            // Marcar el libro como no disponible
            cmbLibro.getValue().setDisponible(false);
            
            listaPrestamos.add(nuevoPrestamo);
            limpiarCampos();
            mostrarMensaje("Préstamo creado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void modificarPrestamo() {
        if (prestamoSeleccionado != null && validarCamposPrestamo()) {
            prestamoSeleccionado.setUsuario(cmbUsuario.getValue());
            prestamoSeleccionado.setLibro(cmbLibro.getValue());
            prestamoSeleccionado.setFechaPrestamo(dpFechaPrestamo.getValue());
            prestamoSeleccionado.setFechaDevolucionEsperada(dpFechaDevolucion.getValue());
            prestamoSeleccionado.setEstado(cmbEstado.getValue());
            prestamoSeleccionado.setObservaciones(txtObservaciones.getText());
            
            tablaPrestamos.refresh();
            mostrarMensaje("Préstamo modificado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void devolverLibro() {
        if (prestamoSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar devolución");
            confirmacion.setHeaderText("¿Confirmar la devolución del libro?");
            confirmacion.setContentText(prestamoSeleccionado.getLibro().getTitulo());
            
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    prestamoSeleccionado.marcarComoDevuelto();
                    tablaPrestamos.refresh();
                    limpiarCampos();
                    mostrarMensaje("Libro devuelto correctamente", Alert.AlertType.INFORMATION);
                }
            });
        }
    }

    @FXML
    private void renovarPrestamo() {
        if (prestamoSeleccionado != null && prestamoSeleccionado.getEstado() == Prestamo.EstadoPrestamo.ACTIVO) {
            TextInputDialog dialog = new TextInputDialog("14");
            dialog.setTitle("Renovar Préstamo");
            dialog.setHeaderText("Renovación de préstamo");
            dialog.setContentText("Días adicionales:");
            
            dialog.showAndWait().ifPresent(diasStr -> {
                try {
                    int dias = Integer.parseInt(diasStr);
                    if (dias > 0) {
                        prestamoSeleccionado.renovar(dias);
                        tablaPrestamos.refresh();
                        mostrarMensaje("Préstamo renovado por " + dias + " días", Alert.AlertType.INFORMATION);
                    }
                } catch (NumberFormatException e) {
                    mostrarMensaje("Número de días inválido", Alert.AlertType.ERROR);
                }
            });
        }
    }

    @FXML
    private void buscarPrestamos() {
        String termino = txtBuscar.getText().toLowerCase().trim();
        
        if (termino.isEmpty()) {
            tablaPrestamos.setItems(listaPrestamos);
        } else {
            ObservableList<Prestamo> prestamosFiltrados = FXCollections.observableArrayList();
            
            for (Prestamo prestamo : listaPrestamos) {
                if ((prestamo.getUsuario() != null && 
                     prestamo.getUsuario().getNombreCompleto().toLowerCase().contains(termino)) ||
                    (prestamo.getLibro() != null && 
                     prestamo.getLibro().getTitulo().toLowerCase().contains(termino)) ||
                    prestamo.getEstado().toString().toLowerCase().contains(termino)) {
                    prestamosFiltrados.add(prestamo);
                }
            }
            
            tablaPrestamos.setItems(prestamosFiltrados);
        }
    }

    @FXML
    private void filtrarPorEstado() {
        if (cmbFiltroEstado.getValue() == null) {
            tablaPrestamos.setItems(listaPrestamos);
        } else {
            ObservableList<Prestamo> prestamosFiltrados = FXCollections.observableArrayList();
            
            for (Prestamo prestamo : listaPrestamos) {
                if (prestamo.getEstado() == cmbFiltroEstado.getValue()) {
                    prestamosFiltrados.add(prestamo);
                }
            }
            
            tablaPrestamos.setItems(prestamosFiltrados);
        }
    }

    private void actualizarLibrosDisponibles() {
        ObservableList<Libro> librosDisponibles = FXCollections.observableArrayList();
        
        for (Libro libro : listaLibros) {
            if (libro.isDisponible()) {
                librosDisponibles.add(libro);
            }
        }
        
        cmbLibro.setItems(librosDisponibles);
    }

    private boolean validarCamposPrestamo() {
        if (cmbUsuario.getValue() == null) {
            mostrarMensaje("Debe seleccionar un usuario", Alert.AlertType.WARNING);
            return false;
        }
        
        if (cmbLibro.getValue() == null) {
            mostrarMensaje("Debe seleccionar un libro", Alert.AlertType.WARNING);
            return false;
        }
        
        if (dpFechaPrestamo.getValue() == null) {
            mostrarMensaje("Debe seleccionar la fecha de préstamo", Alert.AlertType.WARNING);
            return false;
        }
        
        if (dpFechaDevolucion.getValue() == null) {
            mostrarMensaje("Debe seleccionar la fecha de devolución", Alert.AlertType.WARNING);
            return false;
        }
        
        if (dpFechaDevolucion.getValue().isBefore(dpFechaPrestamo.getValue())) {
            mostrarMensaje("La fecha de devolución no puede ser anterior a la fecha de préstamo", Alert.AlertType.WARNING);
            return false;
        }
        
        return true;
    }

    private void limpiarCampos() {
        cmbUsuario.setValue(null);
        cmbLibro.setValue(null);
        dpFechaPrestamo.setValue(LocalDate.now());
        dpFechaDevolucion.setValue(LocalDate.now().plusDays(14));
        cmbEstado.setValue(Prestamo.EstadoPrestamo.ACTIVO);
        txtObservaciones.clear();
        prestamoSeleccionado = null;
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Métodos públicos para integración
    public ObservableList<Prestamo> getPrestamos() {
        return listaPrestamos;
    }

    public void setUsuarios(ObservableList<Usuario> usuarios) {
        this.listaUsuarios = usuarios;
        cmbUsuario.setItems(usuarios);
    }

    public void setLibros(ObservableList<Libro> libros) {
        this.listaLibros = libros;
        actualizarLibrosDisponibles();
    }
}