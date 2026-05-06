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

        // 1. אתחול המודלים
        SpaceManager spaceManager = new SpaceManager();
        WorkspaceState state      = new WorkspaceState();

        // 2. אתחול התצוגות
        SidebarView sidebar = new SidebarView();

        AppController[] controllerRef = new AppController[1];
        CanvasView canvasView = new CanvasView(width - 275, height, spaceManager, () -> {
            if (controllerRef[0] != null) controllerRef[0].refreshView();
        });

        // 3. אתחול הבקר
        AppController controller = new AppController(spaceManager, state, sidebar, canvasView);
        controllerRef[0] = controller;
        controller.initializeBindings();

        // 4. הרכבת המסך הראשי
        BorderPane root = new BorderPane();
        // שימוש ב-ScrollPane כדי שה-Sidebar יהיה גלילה אם צריך
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

        primaryStage.setTitle("LatentSpace Explorer — Word Embedding Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        controller.refreshView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
