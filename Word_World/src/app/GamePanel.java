package app;


import javafx.stage.Stage;
import engine.SpaceManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;

/**
 * GamePanel handles the creation and management of the word list window.
 * It provides a user interface with a text input field and a list display.
 */
public class GamePanel {

    // UI Components
    private TextField inputField;
    private ListView<String> listView;
    private VBox rootLayout;
    private Stage window;
    private SpaceManager spaceManager;
    
    /**
     * Constructor - Initializes the core UI components in memory.
     */
    public GamePanel(SpaceManager manager) {
        spaceManager = manager;
        inputField = new TextField();
        listView = new ListView<>();
        rootLayout = new VBox(10); // 10px vertical spacing between components
        window = new Stage();
        buildUI();
    }

    /**
     * Builds and configures the layout, event handling, and window properties.
     */
    public void buildUI() {
        
        // --- 1. Content & Layout Layer ---
        inputField.setPromptText("Type a word and press Enter...");
        
        // Add padding around the layout to prevent components from touching the window edges
        rootLayout.setPadding(new Insets(15));
        
        // Arrange components vertically: input field on top, list view below
        rootLayout.getChildren().addAll(inputField, listView);

        // --- 2. Logic & Event Handling Layer ---
        // Define action when the user presses Enter in the text field
        inputField.setOnAction(event -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                listView.getItems().add(text); // Dynamically append word to the list model
                inputField.clear();            // Reset input field for better user experience
            }
        });

        // --- 3. Scene Layer ---
        // Create the scene canvas and set its fixed dimensions (width, height) in pixels
        Scene scene = new Scene(rootLayout, 350, 450);

        // --- 4. Window (Stage) Layer ---
        window.setTitle("Word List Manager");
        window.setScene(scene);
        window.setResizable(false); // Keeps the window size fixed and prevents resizing

        window.show();
    }

    /**
     * Displays the window on the screen after the UI structure has been built.
     */
    public void showWindow() {
        if (window != null) {
            window.show();
        }
    }
}
