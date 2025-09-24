package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("BiblioTech - Tu Biblioteca Digital");
        
        // Crear el layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #faf8fc;");
        
        // Crear la barra superior
        VBox topSection = createTopSection();
        root.setTop(topSection);
        
        // Crear el contenido principal con libros
        ScrollPane scrollPane = createLibraryContent();
        root.setCenter(scrollPane);
        
        // Crear la barra de navegación inferior
        HBox bottomNavBar = createBottomNavBar();
        root.setBottom(bottomNavBar);
        
        // Configurar la escena
        Scene scene = new Scene(root, 900, 700);
        
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
    
    private VBox createTopSection() {
        VBox topSection = new VBox();
        topSection.setStyle("-fx-background-color: #f9f7fb;");
        
        // Header principal
        HBox header = createHeader();
        
        // Barra de búsqueda y filtros
        HBox searchSection = createSearchSection();
        
        // Sección de categorías
        HBox categoriesSection = createCategoriesSection();
        
        topSection.getChildren().addAll(header, searchSection, categoriesSection);
        return topSection;
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white;");
        
        // Logo de biblioteca
        ImageView logoIcon = new ImageView();
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/logo.png"));
            logoIcon.setImage(logoImage);
            logoIcon.setFitHeight(40);
            logoIcon.setFitWidth(40);
            logoIcon.setPreserveRatio(true);
        } catch (Exception e) {
            // Fallback al emoji si no se puede cargar la imagen
            Label fallbackLogo = new Label("📚");
            fallbackLogo.setFont(Font.font("System", 32));
        }
        
        VBox titleSection = new VBox(2);
        titleSection.setPadding(new Insets(0, 0, 0, 15));
        
        Label appTitle = new Label("BiblioTech");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        appTitle.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Tu biblioteca digital");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        titleSection.getChildren().addAll(appTitle, subtitle);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Botón de perfil
        Button profileBtn = new Button("👤");
        profileBtn.setStyle("-fx-background-color: #b8a9d9; " +
                           "-fx-text-fill: white; " +
                           "-fx-font-size: 18px; " +
                           "-fx-pref-width: 45; " +
                           "-fx-pref-height: 45; " +
                           "-fx-background-radius: 22.5; " +
                           "-fx-border: none;");
        
        header.getChildren().addAll(logoIcon, titleSection, spacer, profileBtn);
        return header;
    }
    
    private HBox createSearchSection() {
        HBox searchBox = new HBox(15);
        searchBox.setPadding(new Insets(0, 30, 20, 30));
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        // Campo de búsqueda
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar libros, autores, categorías...");
        searchField.setPrefHeight(45);
        searchField.setStyle("-fx-background-color: #ecf0f1; " +
                            "-fx-border-color: transparent; " +
                            "-fx-background-radius: 22.5; " +
                            "-fx-padding: 0 20; " +
                            "-fx-font-size: 14px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Botón de búsqueda
        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: #f5b7c4; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-size: 16px; " +
                          "-fx-pref-width: 45; " +
                          "-fx-pref-height: 45; " +
                          "-fx-background-radius: 22.5; " +
                          "-fx-border: none;");
        
        searchBox.getChildren().addAll(searchField, searchBtn);
        return searchBox;
    }
    
    private HBox createCategoriesSection() {
        HBox categoriesBox = new HBox(15);
        categoriesBox.setPadding(new Insets(0, 30, 20, 30));
        categoriesBox.setAlignment(Pos.CENTER_LEFT);
        
        Label categoriesLabel = new Label("Categorías:");
        categoriesLabel.setFont(Font.font("System", FontWeight.MEDIUM, 16));
        categoriesLabel.setTextFill(Color.web("#2c3e50"));
        
        // Botones de categorías
        Button allBtn = createCategoryButton("Todos", "#a8d0f0", true);
        Button novelBtn = createCategoryButton("Novelas", "#f5b7c4", false);
        Button scienceBtn = createCategoryButton("Ciencia", "#b8e6d3", false);
        Button historyBtn = createCategoryButton("Historia", "#f5d7a8", false);
        Button techBtn = createCategoryButton("Tecnología", "#d7b8f0", false);
        
        categoriesBox.getChildren().addAll(categoriesLabel, allBtn, novelBtn, scienceBtn, historyBtn, techBtn);
        return categoriesBox;
    }
    
    private Button createCategoryButton(String text, String color, boolean active) {
        Button button = new Button(text);
        
        if (active) {
            button.setStyle("-fx-background-color: " + color + "; " +
                           "-fx-text-fill: white; " +
                           "-fx-font-size: 12px; " +
                           "-fx-font-weight: bold; " +
                           "-fx-padding: 8 16; " +
                           "-fx-background-radius: 15; " +
                           "-fx-border: none;");
        } else {
            button.setStyle("-fx-background-color: transparent; " +
                           "-fx-text-fill: " + color + "; " +
                           "-fx-font-size: 12px; " +
                           "-fx-font-weight: bold; " +
                           "-fx-padding: 8 16; " +
                           "-fx-border-color: " + color + "; " +
                           "-fx-border-width: 1; " +
                           "-fx-background-radius: 15; " +
                           "-fx-border-radius: 15;");
        }
        
        // Efecto hover
        button.setOnMouseEntered(e -> {
            if (!active) {
                button.setStyle("-fx-background-color: " + color + "; " +
                               "-fx-text-fill: white; " +
                               "-fx-font-size: 12px; " +
                               "-fx-font-weight: bold; " +
                               "-fx-padding: 8 16; " +
                               "-fx-background-radius: 15; " +
                               "-fx-border: none;");
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!active) {
                button.setStyle("-fx-background-color: transparent; " +
                               "-fx-text-fill: " + color + "; " +
                               "-fx-font-size: 12px; " +
                               "-fx-font-weight: bold; " +
                               "-fx-padding: 8 16; " +
                               "-fx-border-color: " + color + "; " +
                               "-fx-border-width: 1; " +
                               "-fx-background-radius: 15; " +
                               "-fx-border-radius: 15;");
            }
        });
        
        return button;
    }
    
    private Label createNavButton(String text, boolean active) {
        Label button = new Label(text);
        button.setFont(Font.font("System", FontWeight.MEDIUM, 16));
        
        if (active) {
            button.setTextFill(Color.web("#333333"));
            button.setStyle("-fx-font-weight: bold;");
        } else {
            button.setTextFill(Color.web("#888888"));
        }
        
        button.setOnMouseEntered(e -> {
            if (!active) {
                button.setTextFill(Color.web("#333333"));
                button.setStyle("-fx-cursor: hand;");
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!active) {
                button.setTextFill(Color.web("#888888"));
            }
        });
        
        return button;
    }
    
    private ScrollPane createLibraryContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20, 30, 20, 30));
        
        // Sección "Libros Destacados"
        VBox featuredSection = createFeaturedSection();
        
        // Sección "Biblioteca"
        VBox librarySection = createLibraryGrid();
        
        mainContent.getChildren().addAll(featuredSection, librarySection);
        scrollPane.setContent(mainContent);
        return scrollPane;
    }
    
    private VBox createFeaturedSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("📖 Libros Destacados");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        // Scroll horizontal para libros destacados
        ScrollPane horizontalScroll = new ScrollPane();
        horizontalScroll.setFitToHeight(true);
        horizontalScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        horizontalScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontalScroll.setPrefHeight(200);
        
        HBox featuredBooks = new HBox(15);
        featuredBooks.setPadding(new Insets(10));
        
        // Crear algunos libros destacados
        VBox book1 = createFeaturedBook("El Quijote", "Miguel de Cervantes", "#f5b7c4");
        VBox book2 = createFeaturedBook("1984", "George Orwell", "#a8d0f0");
        VBox book3 = createFeaturedBook("Cien Años", "Gabriel García Márquez", "#b8e6d3");
        VBox book4 = createFeaturedBook("El Principito", "Antoine de Saint-Exupéry", "#f5d7a8");
        VBox book5 = createFeaturedBook("Sapiens", "Yuval Noah Harari", "#d7b8f0");
        
        featuredBooks.getChildren().addAll(book1, book2, book3, book4, book5);
        horizontalScroll.setContent(featuredBooks);
        
        section.getChildren().addAll(sectionTitle, horizontalScroll);
        return section;
    }
    
    private VBox createFeaturedBook(String title, String author, String color) {
        VBox bookCard = new VBox(8);
        bookCard.setAlignment(Pos.CENTER);
        bookCard.setPrefSize(120, 160);
        bookCard.setMaxSize(120, 160);
        bookCard.setMinSize(120, 160);
        
        // Portada del libro
        Region bookCover = new Region();
        bookCover.setPrefSize(80, 110);
        bookCover.setMaxSize(80, 110);
        bookCover.setMinSize(80, 110);
        bookCover.setStyle("-fx-background-color: " + color + "; " +
                          "-fx-background-radius: 8; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        
        // Información del libro
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setMaxWidth(120);
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        
        Label authorLabel = new Label(author);
        authorLabel.setFont(Font.font("System", 9));
        authorLabel.setTextFill(Color.web("#7f8c8d"));
        authorLabel.setMaxWidth(120);
        authorLabel.setWrapText(true);
        authorLabel.setAlignment(Pos.CENTER);
        
        bookCard.getChildren().addAll(bookCover, titleLabel, authorLabel);
        
        // Efecto hover
        bookCard.setOnMouseEntered(e -> {
            bookCard.setScaleX(1.05);
            bookCard.setScaleY(1.05);
            bookCard.setStyle("-fx-cursor: hand;");
        });
        
        bookCard.setOnMouseExited(e -> {
            bookCard.setScaleX(1.0);
            bookCard.setScaleY(1.0);
        });
        
        bookCard.setOnMouseClicked(e -> {
            showBookDetails(title, author);
        });
        
        return bookCard;
    }
    
    private VBox createLibraryGrid() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("📚 Mi Biblioteca");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        // Grid de libros
        GridPane booksGrid = new GridPane();
        booksGrid.setHgap(15);
        booksGrid.setVgap(15);
        booksGrid.setAlignment(Pos.TOP_LEFT);
        
        // Crear una colección de libros con colores pastel
        String[][] books = {
            {"Don Quijote", "Miguel de Cervantes", "#f5b7c4"},
            {"Orgullo y Prejuicio", "Jane Austen", "#f8c8dc"},
            {"El Gran Gatsby", "F. Scott Fitzgerald", "#d7b8f0"},
            {"Matar a un Ruiseñor", "Harper Lee", "#c8b6db"},
            {"1984", "George Orwell", "#a8d0f0"},
            {"Un Mundo Feliz", "Aldous Huxley", "#b5d6f5"},
            {"El Señor de los Anillos", "J.R.R. Tolkien", "#a1c9f0"},
            {"Harry Potter", "J.K. Rowling", "#b8e6e1"},
            {"Crónica de una Muerte", "Gabriel García Márquez", "#b8e6d3"},
            {"Los Miserables", "Victor Hugo", "#c4e6c4"},
            {"Guerra y Paz", "León Tolstói", "#d4edda"},
            {"El Código Da Vinci", "Dan Brown", "#f5d7a8"}
        };
        
        for (int i = 0; i < books.length; i++) {
            VBox bookCard = createLibraryBook(books[i][0], books[i][1], books[i][2]);
            int row = i / 3;
            int col = i % 3;
            booksGrid.add(bookCard, col, row);
        }
        
        // Configurar columnas para que sean responsivas
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            col.setPercentWidth(33.33);
            booksGrid.getColumnConstraints().add(col);
        }
        
        section.getChildren().addAll(sectionTitle, booksGrid);
        return section;
    }
    
    private VBox createLibraryBook(String title, String author, String color) {
        VBox bookCard = new VBox(10);
        bookCard.setAlignment(Pos.CENTER);
        bookCard.setPadding(new Insets(15));
        bookCard.setStyle("-fx-background-color: white; " +
                         "-fx-background-radius: 12; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        bookCard.setMaxWidth(Double.MAX_VALUE);
        
        // Portada del libro
        Region bookCover = new Region();
        bookCover.setPrefSize(100, 130);
        bookCover.setMaxSize(100, 130);
        bookCover.setMinSize(100, 130);
        bookCover.setStyle("-fx-background-color: " + color + "; " +
                          "-fx-background-radius: 8; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        
        // Información del libro
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(200);
        
        Label authorLabel = new Label(author);
        authorLabel.setFont(Font.font("System", 12));
        authorLabel.setTextFill(Color.web("#7f8c8d"));
        authorLabel.setWrapText(true);
        authorLabel.setAlignment(Pos.CENTER);
        authorLabel.setMaxWidth(200);
        
        // Botón de acción
        Button actionBtn = new Button("Leer ahora");
        actionBtn.setStyle("-fx-background-color: " + color + "; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-size: 12px; " +
                          "-fx-font-weight: bold; " +
                          "-fx-padding: 8 16; " +
                          "-fx-background-radius: 20; " +
                          "-fx-border: none;");
        
        actionBtn.setOnAction(e -> showBookDetails(title, author));
        
        bookCard.getChildren().addAll(bookCover, titleLabel, authorLabel, actionBtn);
        
        // Efecto hover
        bookCard.setOnMouseEntered(e -> {
            bookCard.setStyle("-fx-background-color: white; " +
                             "-fx-background-radius: 12; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5); " +
                             "-fx-cursor: hand;");
        });
        
        bookCard.setOnMouseExited(e -> {
            bookCard.setStyle("-fx-background-color: white; " +
                             "-fx-background-radius: 12; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        });
        
        return bookCard;
    }
    
    private void showBookDetails(String title, String author) {
        showAlert("Detalles del Libro", 
                 "📖 Título: " + title + "\n" +
                 "✍️ Autor: " + author + "\n\n" +
                 "¡Próximamente podrás leer este libro!");
    }
    
    private StackPane createResponsiveCard(String color) {
        StackPane card = new StackPane();
        
        // Hacer la tarjeta responsiva: ocupará TODO el espacio disponible en su celda
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        card.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        card.setMinSize(100, 100); // Tamaño mínimo reducido para permitir más expansión
        
        // Estilo inicial con sombra suave y bordes redondeados
        card.setStyle("-fx-background-color: " + color + "; " +
                     "-fx-background-radius: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3);");
        
        // CLAVE: Permitir que la tarjeta crezca para llenar TODO el espacio disponible
        GridPane.setFillWidth(card, true);
        GridPane.setFillHeight(card, true);
        GridPane.setHgrow(card, Priority.ALWAYS);
        GridPane.setVgrow(card, Priority.ALWAYS);
        
        // Forzar que la tarjeta use todo el espacio de su contenedor padre
        HBox.setHgrow(card, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);
        
        // Efecto hover mejorado
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + color + "; " +
                         "-fx-background-radius: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 5); " +
                         "-fx-cursor: hand;");
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: " + color + "; " +
                         "-fx-background-radius: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3);");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        card.setOnMouseClicked(e -> {
            showAlert("Tarjeta seleccionada", "Has clickeado en una tarjeta de color " + color);
        });
        
        return card;
    }
    
    private HBox createBottomNavBar() {
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(15, 40, 15, 40));
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: white; -fx-border-color: #ecf0f1; -fx-border-width: 2 0 0 0;");
        
        // Crear los botones de navegación para biblioteca
        VBox homeBtn = createBottomNavButton("🏠", "#a8d0f0", true, "Inicio");
        VBox searchBtn = createBottomNavButton("🔍", "#b3b3b3", false, "Buscar");
        VBox libraryBtn = createBottomNavButton("📚", "#b3b3b3", false, "Biblioteca");
        VBox favBtn = createBottomNavButton("❤️", "#b3b3b3", false, "Favoritos");
        VBox profileBtn = createBottomNavButton("👤", "#b3b3b3", false, "Perfil");
        
        // Espaciado uniforme entre botones
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        Region spacer3 = new Region();
        Region spacer4 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);
        
        bottomBar.getChildren().addAll(homeBtn, spacer1, searchBtn, spacer2, libraryBtn, spacer3, favBtn, spacer4, profileBtn);
        return bottomBar;
    }
    
    private VBox createBottomNavButton(String icon, String color, boolean active, String text) {
        VBox buttonContainer = new VBox(3);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPrefWidth(70);
        
        // Crear el ícono
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 20));
        iconLabel.setTextFill(active ? Color.web(color) : Color.web("#b3b3b3"));
        iconLabel.setAlignment(Pos.CENTER);
        
        // Crear el texto
        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("System", 10));
        textLabel.setTextFill(active ? Color.web(color) : Color.web("#b3b3b3"));
        textLabel.setAlignment(Pos.CENTER);
        
        buttonContainer.getChildren().addAll(iconLabel, textLabel);
        
        // Agregar funcionalidad de click
        buttonContainer.setOnMouseClicked(e -> {
            showAlert("Navegación", "Has clickeado en: " + text);
        });
        
        // Efecto hover
        buttonContainer.setOnMouseEntered(e -> {
            buttonContainer.setStyle("-fx-cursor: hand;");
            if (!active) {
                iconLabel.setTextFill(Color.web(color));
                textLabel.setTextFill(Color.web(color));
            }
        });
        
        buttonContainer.setOnMouseExited(e -> {
            if (!active) {
                iconLabel.setTextFill(Color.web("#b3b3b3"));
                textLabel.setTextFill(Color.web("#b3b3b3"));
            }
        });
        
        return buttonContainer;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Estilizar el diálogo
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15;");
        
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
