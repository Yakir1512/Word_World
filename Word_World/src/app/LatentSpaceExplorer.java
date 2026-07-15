package app;

import engine.SpaceManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LatentSpaceExplorer extends Application {

    @Override
    public void start(Stage primaryStage) {

        double width  = 1280;
        double height = 800;

        // 1. Model Initialization
        SpaceManager spaceManager = new SpaceManager();
        WorkspaceState state      = new WorkspaceState();

        // 2. View Initialization
        SidebarView sidebar = new SidebarView();
        GamePanel gamePanel = new GamePanel(spaceManager);

        AppController[] controllerRef = new AppController[1];
        CanvasView canvasView = new CanvasView(width - 275, height, spaceManager, () -> {
            if (controllerRef[0] != null) controllerRef[0].refreshView();
        });

        // 3. Controller Initialization
        AppController controller = new AppController(spaceManager, state, sidebar, canvasView, gamePanel);
        controllerRef[0] = controller;
        controller.initializeBindings();

        // 4. Main Layout Assembly
        BorderPane root = new BorderPane();
        
        // Use ScrollPane from sidebar to allow scrolling if content overflows
        root.setLeft(sidebar.getScrollView());
        root.setCenter(canvasView.getView());

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add("data:text/css," +
            ".titled-pane > .title { -fx-background-color: #2d2d2d; -fx-text-fill: white; }" +
            ".titled-pane > .title > .arrow-button .arrow { -fx-background-color: #4ec9b0; }" +
            ".scroll-bar { -fx-background-color: #1e1e1e; }" +
            ".combo-box { -fx-background-color: #3c3c3c; -fx-text-fill: white; }" +
            ".combo-box .list-cell { -fx-background-color: #3c3c3c; -fx-text-fill: white; }"
        );

        // 5. Window (Stage) Configuration
        primaryStage.setTitle("LatentSpace Explorer — Word Embedding Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // Trigger initial view refresh
        controller.refreshView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
