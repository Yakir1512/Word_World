package app;

import engine.SpaceManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LatentSpaceExplorer extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        double width = 1200; 
        double height = 800;

        // 1. אתחול המודלים (Data & State)
        SpaceManager spaceManager = new SpaceManager();
        WorkspaceState state = new WorkspaceState();

        // 2. אתחול התצוגות (Views)
        SidebarView sidebar = new SidebarView();
        
        // שמנו לב שצריך להעביר פונקציית רענון לקנבס. נעשה זאת דרך משתנה זמני.
        AppController[] controllerRef = new AppController[1];
        CanvasView canvasView = new CanvasView(width - 260, height, spaceManager, () -> {
            if (controllerRef[0] != null) controllerRef[0].refreshView();
        });

        // 3. אתחול הבקר המתווך (Controller)
        AppController controller = new AppController(spaceManager, state, sidebar, canvasView);
        controllerRef[0] = controller; // סגירת המעגל עבור ה-Callback
        
        // הפעלת כל החיבורים והמאזינים
        controller.initializeBindings();

        // 4. הרכבת המסך הראשי
        BorderPane root = new BorderPane();
        root.setLeft(sidebar.getView());
        root.setCenter(canvasView.getView());

        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle("LatentSpace Explorer Pro (MVC Architecture)");
        primaryStage.setScene(scene);
        primaryStage.show();

        // ציור ראשוני
        controller.refreshView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}