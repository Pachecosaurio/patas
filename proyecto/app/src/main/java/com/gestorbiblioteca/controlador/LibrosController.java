package com.gestorbiblioteca.controlador;

import com.gestorbiblioteca.modelo.Libro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la gestión de libros en el sistema de biblioteca
 */
public class LibrosController {
    
    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, Integer> colId;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colCategoria;
    @FXML private TableColumn<Libro, Boolean> colDisponible;
    
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtFechaPublicacion;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtImagenPath;
    @FXML private ImageView imgPortada;
    
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnBuscar;
    @FXML private TextField txtBuscar;
    
    private ObservableList<Libro> listaLibros;
    private Libro libroSeleccionado;

    @FXML
    private void initialize() {
        configurarTabla();
        inicializarDatos();
        configurarEventos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDisponible.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        
        // Configurar columna de disponibilidad con íconos
        colDisponible.setCellFactory(column -> new TableCell<Libro, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item ? "✓ Disponible" : "✗ Prestado");
                    label.setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                    setGraphic(label);
                }
            }
        });
        
        listaLibros = FXCollections.observableArrayList();
        tablaLibros.setItems(listaLibros);
        
        // Listener para selección de filas
        tablaLibros.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> mostrarDetallesLibro(newValue)
        );
    }

    private void inicializarDatos() {
        // Datos de ejemplo
        List<Libro> librosEjemplo = crearLibrosEjemplo();
        listaLibros.addAll(librosEjemplo);
    }

    private List<Libro> crearLibrosEjemplo() {
        List<Libro> libros = new ArrayList<>();
        
        libros.add(new Libro(1, "Cien años de soledad", "Gabriel García Márquez", 
                "978-0-06-088328-7", "Ficción", "1967", 
                "Una obra maestra del realismo mágico", "/libros/libro1.jpeg"));
        
        libros.add(new Libro(2, "El Quijote", "Miguel de Cervantes", 
                "978-84-376-0494-7", "Clásicos", "1605",
                "La obra cumbre de la literatura española", "/libros/libro2.jpeg"));
        
        libros.add(new Libro(3, "1984", "George Orwell", 
                "978-0-452-28423-4", "Distopía", "1949",
                "Una visión profética del totalitarismo", "/libros/libro3.jpeg"));
        
        libros.add(new Libro(4, "Matar un ruiseñor", "Harper Lee", 
                "978-0-06-112008-4", "Drama", "1960",
                "Una poderosa historia sobre justicia y moralidad", "/libros/libro4.jpeg"));
        
        libros.add(new Libro(5, "El nombre del viento", "Patrick Rothfuss", 
                "978-0-7564-0474-1", "Fantasía", "2007",
                "Primera parte de la Crónica del Asesino de Reyes", "/libros/libro5.jpeg"));
        
        return libros;
    }

    private void configurarEventos() {
        btnAgregar.setOnAction(e -> agregarLibro());
        btnModificar.setOnAction(e -> modificarLibro());
        btnEliminar.setOnAction(e -> eliminarLibro());
        btnBuscar.setOnAction(e -> buscarLibros());
        
        txtBuscar.setOnKeyReleased(e -> buscarLibros());
        txtImagenPath.textProperty().addListener((obs, oldText, newText) -> cargarImagen(newText));
    }

    private void mostrarDetallesLibro(Libro libro) {
        this.libroSeleccionado = libro;
        
        if (libro != null) {
            txtTitulo.setText(libro.getTitulo());
            txtAutor.setText(libro.getAutor());
            txtIsbn.setText(libro.getIsbn());
            txtCategoria.setText(libro.getCategoria());
            txtFechaPublicacion.setText(libro.getFechaPublicacion());
            txtDescripcion.setText(libro.getDescripcion());
            txtImagenPath.setText(libro.getImagenPath());
            
            cargarImagen(libro.getImagenPath());
            
            btnModificar.setDisable(false);
            btnEliminar.setDisable(false);
        } else {
            limpiarCampos();
            btnModificar.setDisable(true);
            btnEliminar.setDisable(true);
        }
    }

    @FXML
    private void agregarLibro() {
        if (validarCampos()) {
            int nuevoId = listaLibros.size() + 1;
            Libro nuevoLibro = new Libro(
                nuevoId,
                txtTitulo.getText(),
                txtAutor.getText(),
                txtIsbn.getText(),
                txtCategoria.getText(),
                txtFechaPublicacion.getText(),
                txtDescripcion.getText(),
                txtImagenPath.getText()
            );
            
            listaLibros.add(nuevoLibro);
            limpiarCampos();
            mostrarMensaje("Libro agregado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void modificarLibro() {
        if (libroSeleccionado != null && validarCampos()) {
            libroSeleccionado.setTitulo(txtTitulo.getText());
            libroSeleccionado.setAutor(txtAutor.getText());
            libroSeleccionado.setIsbn(txtIsbn.getText());
            libroSeleccionado.setCategoria(txtCategoria.getText());
            libroSeleccionado.setFechaPublicacion(txtFechaPublicacion.getText());
            libroSeleccionado.setDescripcion(txtDescripcion.getText());
            libroSeleccionado.setImagenPath(txtImagenPath.getText());
            
            tablaLibros.refresh();
            mostrarMensaje("Libro modificado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void eliminarLibro() {
        if (libroSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("¿Está seguro de eliminar este libro?");
            confirmacion.setContentText(libroSeleccionado.getTitulo());
            
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    listaLibros.remove(libroSeleccionado);
                    limpiarCampos();
                    mostrarMensaje("Libro eliminado correctamente", Alert.AlertType.INFORMATION);
                }
            });
        }
    }

    @FXML
    private void buscarLibros() {
        String termino = txtBuscar.getText().toLowerCase().trim();
        
        if (termino.isEmpty()) {
            tablaLibros.setItems(listaLibros);
        } else {
            ObservableList<Libro> librosFiltrados = FXCollections.observableArrayList();
            
            for (Libro libro : listaLibros) {
                if (libro.getTitulo().toLowerCase().contains(termino) ||
                    libro.getAutor().toLowerCase().contains(termino) ||
                    libro.getCategoria().toLowerCase().contains(termino) ||
                    libro.getIsbn().toLowerCase().contains(termino)) {
                    librosFiltrados.add(libro);
                }
            }
            
            tablaLibros.setItems(librosFiltrados);
        }
    }

    private void cargarImagen(String path) {
        try {
            if (path != null && !path.trim().isEmpty()) {
                Image imagen = new Image(getClass().getResourceAsStream(path));
                imgPortada.setImage(imagen);
            } else {
                imgPortada.setImage(null);
            }
        } catch (Exception e) {
            imgPortada.setImage(null);
        }
    }

    private boolean validarCampos() {
        if (txtTitulo.getText().trim().isEmpty()) {
            mostrarMensaje("El título es obligatorio", Alert.AlertType.WARNING);
            return false;
        }
        if (txtAutor.getText().trim().isEmpty()) {
            mostrarMensaje("El autor es obligatorio", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtTitulo.clear();
        txtAutor.clear();
        txtIsbn.clear();
        txtCategoria.clear();
        txtFechaPublicacion.clear();
        txtDescripcion.clear();
        txtImagenPath.clear();
        imgPortada.setImage(null);
        libroSeleccionado = null;
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Métodos públicos para integración
    public ObservableList<Libro> getLibros() {
        return listaLibros;
    }

    public void agregarLibro(Libro libro) {
        listaLibros.add(libro);
    }
}