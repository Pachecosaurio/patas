package com.gestorbiblioteca.controlador;

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
 * Controlador para la gestión de usuarios en el sistema de biblioteca
 */
public class UsuariosController {
    
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colTelefono;
    @FXML private TableColumn<Usuario, Usuario.TipoUsuario> colTipo;
    @FXML private TableColumn<Usuario, Boolean> colActivo;
    
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<Usuario.TipoUsuario> cmbTipoUsuario;
    @FXML private CheckBox chkActivo;
    @FXML private DatePicker dpFechaRegistro;
    
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnBuscar;
    @FXML private TextField txtBuscar;
    
    private ObservableList<Usuario> listaUsuarios;
    private Usuario usuarioSeleccionado;

    @FXML
    private void initialize() {
        configurarTabla();
        inicializarComponentes();
        inicializarDatos();
        configurarEventos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoUsuario"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        
        // Configurar columna de estado activo con íconos
        colActivo.setCellFactory(column -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item ? "✓ Activo" : "✗ Inactivo");
                    label.setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                    setGraphic(label);
                }
            }
        });
        
        listaUsuarios = FXCollections.observableArrayList();
        tablaUsuarios.setItems(listaUsuarios);
        
        // Listener para selección de filas
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> mostrarDetallesUsuario(newValue)
        );
    }

    private void inicializarComponentes() {
        // Configurar ComboBox de tipo de usuario
        cmbTipoUsuario.setItems(FXCollections.observableArrayList(Usuario.TipoUsuario.values()));
        cmbTipoUsuario.setValue(Usuario.TipoUsuario.ESTUDIANTE);
        
        // Configurar DatePicker
        dpFechaRegistro.setValue(LocalDate.now());
        
        // Estado inicial de controles
        chkActivo.setSelected(true);
    }

    private void inicializarDatos() {
        // Datos de ejemplo
        List<Usuario> usuariosEjemplo = crearUsuariosEjemplo();
        listaUsuarios.addAll(usuariosEjemplo);
    }

    private List<Usuario> crearUsuariosEjemplo() {
        List<Usuario> usuarios = new ArrayList<>();
        
        Usuario usuario1 = new Usuario(1, "Ana", "García", "ana.garcia@email.com", 
                "555-0101", "Calle Principal 123", Usuario.TipoUsuario.ESTUDIANTE);
        
        Usuario usuario2 = new Usuario(2, "Carlos", "Rodríguez", "carlos.rodriguez@email.com",
                "555-0102", "Avenida Central 456", Usuario.TipoUsuario.PROFESOR);
        
        Usuario usuario3 = new Usuario(3, "María", "López", "maria.lopez@email.com",
                "555-0103", "Plaza Mayor 789", Usuario.TipoUsuario.ADMINISTRATIVO);
        
        Usuario usuario4 = new Usuario(4, "Pedro", "Martínez", "pedro.martinez@email.com",
                "555-0104", "Calle Segunda 321", Usuario.TipoUsuario.EXTERNO);
        
        Usuario usuario5 = new Usuario(5, "Laura", "Sánchez", "laura.sanchez@email.com",
                "555-0105", "Boulevard Norte 654", Usuario.TipoUsuario.ESTUDIANTE);
        
        usuarios.add(usuario1);
        usuarios.add(usuario2);
        usuarios.add(usuario3);
        usuarios.add(usuario4);
        usuarios.add(usuario5);
        
        return usuarios;
    }

    private void configurarEventos() {
        btnAgregar.setOnAction(e -> agregarUsuario());
        btnModificar.setOnAction(e -> modificarUsuario());
        btnEliminar.setOnAction(e -> eliminarUsuario());
        btnBuscar.setOnAction(e -> buscarUsuarios());
        
        txtBuscar.setOnKeyReleased(e -> buscarUsuarios());
    }

    private void mostrarDetallesUsuario(Usuario usuario) {
        this.usuarioSeleccionado = usuario;
        
        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            txtEmail.setText(usuario.getEmail());
            txtTelefono.setText(usuario.getTelefono());
            txtDireccion.setText(usuario.getDireccion());
            cmbTipoUsuario.setValue(usuario.getTipoUsuario());
            chkActivo.setSelected(usuario.isActivo());
            dpFechaRegistro.setValue(usuario.getFechaRegistro());
            
            btnModificar.setDisable(false);
            btnEliminar.setDisable(false);
        } else {
            limpiarCampos();
            btnModificar.setDisable(true);
            btnEliminar.setDisable(true);
        }
    }

    @FXML
    private void agregarUsuario() {
        if (validarCampos()) {
            int nuevoId = listaUsuarios.size() + 1;
            Usuario nuevoUsuario = new Usuario(
                nuevoId,
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                txtEmail.getText().trim(),
                txtTelefono.getText().trim(),
                txtDireccion.getText().trim(),
                cmbTipoUsuario.getValue()
            );
            
            nuevoUsuario.setActivo(chkActivo.isSelected());
            nuevoUsuario.setFechaRegistro(dpFechaRegistro.getValue());
            
            listaUsuarios.add(nuevoUsuario);
            limpiarCampos();
            mostrarMensaje("Usuario agregado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void modificarUsuario() {
        if (usuarioSeleccionado != null && validarCampos()) {
            usuarioSeleccionado.setNombre(txtNombre.getText().trim());
            usuarioSeleccionado.setApellido(txtApellido.getText().trim());
            usuarioSeleccionado.setEmail(txtEmail.getText().trim());
            usuarioSeleccionado.setTelefono(txtTelefono.getText().trim());
            usuarioSeleccionado.setDireccion(txtDireccion.getText().trim());
            usuarioSeleccionado.setTipoUsuario(cmbTipoUsuario.getValue());
            usuarioSeleccionado.setActivo(chkActivo.isSelected());
            usuarioSeleccionado.setFechaRegistro(dpFechaRegistro.getValue());
            
            tablaUsuarios.refresh();
            mostrarMensaje("Usuario modificado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void eliminarUsuario() {
        if (usuarioSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("¿Está seguro de eliminar este usuario?");
            confirmacion.setContentText(usuarioSeleccionado.getNombreCompleto());
            
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    listaUsuarios.remove(usuarioSeleccionado);
                    limpiarCampos();
                    mostrarMensaje("Usuario eliminado correctamente", Alert.AlertType.INFORMATION);
                }
            });
        }
    }

    @FXML
    private void buscarUsuarios() {
        String termino = txtBuscar.getText().toLowerCase().trim();
        
        if (termino.isEmpty()) {
            tablaUsuarios.setItems(listaUsuarios);
        } else {
            ObservableList<Usuario> usuariosFiltrados = FXCollections.observableArrayList();
            
            for (Usuario usuario : listaUsuarios) {
                if (usuario.getNombre().toLowerCase().contains(termino) ||
                    usuario.getApellido().toLowerCase().contains(termino) ||
                    usuario.getEmail().toLowerCase().contains(termino) ||
                    usuario.getTelefono().toLowerCase().contains(termino) ||
                    usuario.getTipoUsuario().toString().toLowerCase().contains(termino)) {
                    usuariosFiltrados.add(usuario);
                }
            }
            
            tablaUsuarios.setItems(usuariosFiltrados);
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarMensaje("El nombre es obligatorio", Alert.AlertType.WARNING);
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtApellido.getText().trim().isEmpty()) {
            mostrarMensaje("El apellido es obligatorio", Alert.AlertType.WARNING);
            txtApellido.requestFocus();
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            mostrarMensaje("El email es obligatorio", Alert.AlertType.WARNING);
            txtEmail.requestFocus();
            return false;
        }
        
        if (!validarEmail(txtEmail.getText().trim())) {
            mostrarMensaje("El formato del email no es válido", Alert.AlertType.WARNING);
            txtEmail.requestFocus();
            return false;
        }
        
        if (cmbTipoUsuario.getValue() == null) {
            mostrarMensaje("Debe seleccionar un tipo de usuario", Alert.AlertType.WARNING);
            cmbTipoUsuario.requestFocus();
            return false;
        }
        
        return true;
    }

    private boolean validarEmail(String email) {
        String patron = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(patron);
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        cmbTipoUsuario.setValue(Usuario.TipoUsuario.ESTUDIANTE);
        chkActivo.setSelected(true);
        dpFechaRegistro.setValue(LocalDate.now());
        usuarioSeleccionado = null;
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Métodos públicos para integración
    public ObservableList<Usuario> getUsuarios() {
        return listaUsuarios;
    }

    public void agregarUsuario(Usuario usuario) {
        listaUsuarios.add(usuario);
    }
}