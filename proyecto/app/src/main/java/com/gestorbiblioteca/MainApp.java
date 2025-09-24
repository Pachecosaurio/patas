package com.gestorbiblioteca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aplicación principal del Sistema de Gestión de Biblioteca
 * BiblioTech - Tu Biblioteca Digital
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BiblioTech - Sistema de Gestión de Biblioteca");
        
        initRootLayout();
    }

    /**
     * Inicializa el layout raíz de la aplicación
     */
    public void initRootLayout() {
        try {
            // Cargar el layout raíz desde el archivo FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/com/gestorbiblioteca/vista/MainView.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Mostrar la escena con el layout raíz
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(700);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna el stage principal
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}