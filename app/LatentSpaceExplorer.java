
package app;
import java.util.List;

import engine.SpaceManager;
// בשביל החלפת המטריקה
// בשביל החלפת המטריקה
import math.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LatentSpaceExplorer extends Application {

    // רכיבי הליבה
    private boolean is3DMode = false;
    private SpaceManager spaceManager;
    //רכיבי  דו-מימד
    private ProjectionStrategy projStrt;
    private GraphRenderer renderer;
    private NavigationHandler navHandler;
    private Canvas canvas;
    // מצב המערכת
    private int[] axisIndices = {0, 1};
    private double[] minVals = {-1.0, -1.0};
    private double[] maxVals = {1.0, 1.0};
    private boolean axisChanged = true;

    // גודל החלון
    private double width = 1200; // הרחבנו קצת למראה טוב יותר
    private double height = 800;
    //רכיבי תלת מימד
    private GraphRenderer3D renderer3D;
    private NavigationHandler3D navHandler3D;
    private ProjectionStrategy projStrt3D; // נחזיק את האסטרטגיה של ה-3D בנפרד

    private ComboBox<Integer> zSelect;
    private Label zLabel;

   
@Override
public void start(Stage primaryStage) {
    // ============================================================
    // 1. אתחול משתנים ולוגיקה (Initialization)
    // ============================================================
    
    // א. מנהל הנתונים והטלה דו-ממדית
    this.spaceManager = new SpaceManager();
    this.projStrt = new Linear2DProjection();

    // ב. יצירת הקנבס
    this.canvas = new Canvas(width - 260, height);
    
    // ג. אתחול מנועי הרינדור (2D ו-3D)
    this.renderer = new GraphRenderer(canvas);
    this.renderer3D = new GraphRenderer3D(canvas);
    this.projStrt3D = new math.Perspective3DProjection();

    // ד. אתחול מנועי הניווט (חשוב: ליצור אותם לפני החיבור לקנבס!)
    // מנוע 2D (זום וגרירה)
    this.navHandler = new NavigationHandler(minVals, maxVals, () -> {
    // 1. אומרים לצייר הדו-ממדי שהגבולות השתנו וחובה לחשב מחדש
    if (renderer != null) {
        renderer.setNeedsReprojection(true);
    }
    
    refreshView();
});
    this.navHandler.attachTo(canvas);
    
    // מנוע 3D (סיבוב)
    this.navHandler3D = new NavigationHandler3D(() -> refreshView());
    this.navHandler3D.attachTo(canvas);

    // ה. שכבת החיפוש הצפה
    SearchOverlay searchOverlay = new SearchOverlay(spaceManager);

    // ============================================================
    // 2. אינטראקציה: לחיצה על מילה (Click Handling)
    // ============================================================
//     canvas.setOnMouseClicked(event -> {
//     // 1. קביעת הגבולות והאסטרטגיה לפי המצב הנוכחי (2D/3D)
//     double[] currentMin;
//     double[] currentMax;
//     math.ProjectionStrategy currentProj;

//     if (is3DMode) {
//         // ב-3D משתמשים בגבולות העולם המקוריים (אין עדיין זום ב-3D)
//         currentMin = minVals;
//         currentMax = maxVals;
//         currentProj = projStrt3D;
//     } else {
//         // ב-2D משתמשים בגבולות הזום הנוכחיים מה-NavigationHandler
//         // זה התיקון שמאפשר ללחוץ על מילה גם כשהיא בזום!
//         currentMin = navHandler.getMin();
//         currentMax = navHandler.getMax();
//         currentProj = projStrt;
//     }

//     // 2. חיפוש המילה עם הגבולות המדויקים
//     String clickedWord = ExplorerHelper.findWordAt(
//         event.getX(), event.getY(),
//         spaceManager.getWordList(),
//         currentProj,              // האסטרטגיה הרלוונטית
//         axisIndices,
//         canvas.getWidth(), canvas.getHeight(),
//         currentMin, currentMax    // הגבולות הרלוונטיים
//     );

//     // 3. טיפול בתוצאה (כמו שהיה קודם)
//     if (clickedWord != null) {
//         System.out.println("User selected: " + clickedWord);
//         List<model.Match> neighbors = spaceManager.getNeighbors(clickedWord, 10);

//         // עדכון רכיבים
//         searchOverlay.setExternalResults(clickedWord, neighbors);
//         renderer.setHighlight(clickedWord, neighbors);
//         renderer3D.setHighlight(clickedWord, neighbors); 
//     } else {
//         System.out.println("Clicked on empty space");
//         renderer.setHighlight(null, null);
//         renderer3D.setHighlight(null, null);
//     }
//     refreshView();
// });
    canvas.setOnMouseClicked(event -> {
    String clickedWord;

    if (is3DMode) {
        // חיפוש מותאם לתלת-ממד (לוקח בחשבון סיבוב וזום)
        clickedWord = ExplorerHelper3D.findWordAt3D(
            event.getX(), event.getY(),
            spaceManager.getWordList(),
            projStrt3D,
            axisIndices,
            canvas.getWidth(), canvas.getHeight(),
            minVals, maxVals,
            navHandler3D.getAngleX(),
            navHandler3D.getAngleY(),
            navHandler3D.getScale()
        );
    } else {
        // חיפוש רגיל לדו-ממד (לוקח בחשבון את גבולות ה-NavHandler)
        clickedWord = ExplorerHelper.findWordAt(
            event.getX(), event.getY(),
            spaceManager.getWordList(),
            projStrt,
            axisIndices,
            canvas.getWidth(), canvas.getHeight(),
            navHandler.getMin(), navHandler.getMax()
        );
    }

    // לוגיקה של סימון המילה (ללא שינוי)
    if (clickedWord != null) {
        System.out.println("Selected: " + clickedWord);
        List<model.Match> neighbors = spaceManager.getNeighbors(clickedWord, 10);
        searchOverlay.setExternalResults(clickedWord, neighbors);
        renderer.setHighlight(clickedWord, neighbors);
        renderer3D.setHighlight(clickedWord, neighbors); 
    } else {
        renderer.setHighlight(null, null);
        renderer3D.setHighlight(null, null);
    }
    refreshView();
    });

    // ============================================================
    // 3. בניית ממשק המשתמש (UI Construction)
    // ============================================================

    // --- כותרת ---
    Label titleLabel = new Label("Word-World");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    titleLabel.setTextFill(Color.WHITE);

    // --- אזור 1: View Mode (2D/3D) ---
    Label viewHeader = createHeader("View Mode");
    RadioButton rb2D = new RadioButton("2D Projection");
    RadioButton rb3D = new RadioButton("3D Perspective");
    
    rb2D.setTextFill(Color.WHITE);
    rb3D.setTextFill(Color.WHITE);
    rb2D.setSelected(true); // ברירת מחדל

    ToggleGroup viewGroup = new ToggleGroup();
    rb2D.setToggleGroup(viewGroup);
    rb3D.setToggleGroup(viewGroup);
    
    VBox viewBox = new VBox(5, rb2D, rb3D);

    // --- אזור 2: טעינת נתונים ---
    Label dataHeader = createHeader("Data Source");
    Button loadBtn = new Button("Run Python & Load");
    styleButton(loadBtn);
    Label statusLabel = new Label("Status: Waiting");
    statusLabel.setTextFill(Color.LIGHTGRAY);
    statusLabel.setWrapText(true);

    // --- אזור 3: שליטה בצירים ---
    Label axisHeader = createHeader("Projection Axes");
    ComboBox<Integer> xSelect = createAxisCombo();
    ComboBox<Integer> ySelect = createAxisCombo();
    zSelect = createAxisCombo(); // יצירת הקומבו-בוקס לציר Z

    // ערכי ברירת מחדל
    xSelect.setValue(0); 
    ySelect.setValue(1);
    zSelect.setValue(2);

    // הגדרת ציר Z (מוסתר בהתחלה כי אנחנו ב-2D)
    zLabel = new Label("Z-Axis:");
    zLabel.setTextFill(Color.WHITE);
    zSelect.setVisible(false);
    zSelect.setManaged(false);
    zLabel.setVisible(false);
    zLabel.setManaged(false);

    // בניית הגריד של הצירים
    GridPane axisGrid = new GridPane();
    axisGrid.setHgap(10); axisGrid.setVgap(5);
    axisGrid.add(new Label("X-Axis:"), 0, 0); axisGrid.add(xSelect, 1, 0);
    axisGrid.add(new Label("Y-Axis:"), 0, 1); axisGrid.add(ySelect, 1, 1);
    axisGrid.add(zLabel, 0, 2); 
    axisGrid.add(zSelect, 1, 2);
    styleGridLabels(axisGrid);

    // --- אזור 4: מטריקת מרחק ---
    Label metricHeader = createHeader("Distance Metric");
    RadioButton rbEuclidean = new RadioButton("Euclidean Distance");
    RadioButton rbCosine = new RadioButton("Cosine Similarity");
    rbEuclidean.setTextFill(Color.WHITE);
    rbCosine.setTextFill(Color.WHITE);
    rbEuclidean.setSelected(true);

    ToggleGroup metricGroup = new ToggleGroup();
    rbEuclidean.setToggleGroup(metricGroup);
    rbCosine.setToggleGroup(metricGroup);
    VBox metricBox = new VBox(10, rbEuclidean, rbCosine);

    // --- הרכבת הפאנל הצדי (Sidebar) ---
    VBox sidebar = new VBox(20);
    sidebar.setPadding(new Insets(20));
    sidebar.setPrefWidth(250);
    sidebar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #1a1a1a; -fx-border-width: 0 2 0 0;");
    
    sidebar.getChildren().addAll(
        titleLabel, 
        new Separator(), 
        viewHeader, viewBox,
        new Separator(),
        dataHeader, loadBtn, statusLabel,
        new Separator(),
        axisHeader, axisGrid,
        new Separator(),
        metricHeader, metricBox
    );

    // ============================================================
    // 4. לוגיקה ואירועים (Event Handlers)
    // ============================================================

    // א. לוגיקה לשינוי בין 2D ל-3D
    // א. לוגיקה לשינוי בין 2D ל-3D
viewGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal == rb3D) {
        is3DMode = true;
        System.out.println("Switching to 3D Mode...");
        
        // --- תיקון: כיבוי 2D והפעלת 3D ---
        navHandler.setEnabled(false);   
        navHandler3D.setEnabled(true);

        // הצגת ציר Z
        zSelect.setVisible(true);
        zSelect.setManaged(true);
        zLabel.setVisible(true);
        zLabel.setManaged(true);
        
        if (axisIndices.length < 3) {
                axisIndices = new int[] { xSelect.getValue(), ySelect.getValue(), zSelect.getValue() };
        }
    } else {
        is3DMode = false;
        System.out.println("Switching to 2D Mode...");
        
        // --- תיקון: כיבוי 3D והפעלת 2D ---
        navHandler.setEnabled(true);
        navHandler3D.setEnabled(false);

        // הסתרת ציר Z
        zSelect.setVisible(false);
        zSelect.setManaged(false);
        zLabel.setVisible(false);
        zLabel.setManaged(false);
        
        navHandler3D.reset();
    }
    updateView(); 
});

    // ב. אירועי שינוי צירים
    xSelect.setOnAction(e -> { axisIndices[0] = xSelect.getValue(); updateView(); });
    ySelect.setOnAction(e -> { axisIndices[1] = ySelect.getValue(); updateView(); });
    zSelect.setOnAction(e -> { 
        if(axisIndices.length >= 3) axisIndices[2] = zSelect.getValue(); 
        updateView(); 
    });

    // ג. לוגיקה לשינוי מטריקה
    metricGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal == rbEuclidean) {
            spaceManager.changeMetric(new EuclideanDistance());
        } else {
            spaceManager.changeMetric(new AngleDistance());
        }
    });

    // ד. כפתור טעינת נתונים
    loadBtn.setOnAction(e -> {
    statusLabel.setText("Processing...");
    loadBtn.setDisable(true);
    new Thread(() -> { 
        try {
            spaceManager.ensureDataReady();
            Platform.runLater(() -> {
                statusLabel.setText("Ready: " + spaceManager.getWordList().size() + " words");
                loadBtn.setDisable(false);
                
                // --- התיקון הקריטי ---
                // חייבים לחשב את הגבולות מחדש כי הנתונים השתנו!
                updateBoundaries(); 
                
                updateView(); 
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                statusLabel.setText("Error: " + ex.getMessage());
                loadBtn.setDisable(false);
            });
        }
    }).start();
});
    // ============================================================
    // 5. הרכבת המסך הראשי (Final Layout)
    // ============================================================
    StackPane centerStack = new StackPane();
    centerStack.setStyle("-fx-background-color: black;");
    StackPane.setAlignment(searchOverlay, Pos.TOP_RIGHT);
    StackPane.setMargin(searchOverlay, new Insets(20));
    centerStack.getChildren().addAll(canvas, searchOverlay);

    BorderPane root = new BorderPane();
    root.setLeft(sidebar);
    root.setCenter(centerStack);

    Scene scene = new Scene(root, width, height);
    primaryStage.setTitle("LatentSpace Explorer Pro");
    primaryStage.setScene(scene);
    primaryStage.show();

    refreshView();
}
    // --- פונקציות עזר לניקיון הקוד ---

    private void updateView() {
        axisChanged = true;
        refreshView();
    }


    private void refreshView() {
        // 1. בדיקה אם יש נתונים
        if (spaceManager.getWordList().isEmpty()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }

        if (axisChanged) {
            updateBoundaries();
            axisChanged = false;
        }

        if (is3DMode) {
            // --- כאן התיקון: שליחת ה-Scale ל-Renderer ---
            renderer3D.render(
                spaceManager.getWordList(),
                projStrt3D,
                axisIndices,
                minVals, maxVals,
                navHandler3D.getAngleX(),
                navHandler3D.getAngleY(),
                navHandler3D.getScale() // <--- שליפת הזום מה-Handler
            );
        } else {
            // לוגיקה ל-2D (ללא שינוי)
            double[] currentMin = navHandler.getMin();
            double[] currentMax = navHandler.getMax();
            
            renderer.render(
                spaceManager.getWordList(), 
                projStrt, 
                axisIndices, 
                currentMin, 
                currentMax
            );
        }
    }

//     private void refreshView() {
//     // 1. בדיקה אם יש נתונים
//     if (spaceManager.getWordList().isEmpty()) {
//         GraphicsContext gc = canvas.getGraphicsContext2D();
//         gc.setFill(Color.BLACK);
//         gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//         return;
//     }

//     if (axisChanged) {
//         updateBoundaries();
//         axisChanged = false;
//     }

//     if (is3DMode) {
//         renderer3D.render(
//             spaceManager.getWordList(),
//             projStrt3D,
//             axisIndices,
//             minVals, maxVals,
//             navHandler3D.getAngleX(),
//             navHandler3D.getAngleY()
//         );
//     } else {
//         // --- דיבאג: בדיקת גבולות ---
//         double[] currentMin = navHandler.getMin();
//         double[] currentMax = navHandler.getMax();
        
//         // הדפסה לקונסולה כדי לראות מה הצייר מקבל
//         System.out.println("2D Render Bounds: " + 
//              "X[" + currentMin[0] + " to " + currentMax[0] + "], " + 
//              "Y[" + currentMin[1] + " to " + currentMax[1] + "]");

//         renderer.render(
//             spaceManager.getWordList(), 
//             projStrt, 
//             axisIndices, 
//             currentMin, // שימוש בגבולות מה-Handler
//             currentMax
//         );
//     }
// }

    private void updateBoundaries() {
    // 1. שליפת הטווחים האמיתיים מה-SpaceManager
    double[] xRange = spaceManager.getAxisRange(axisIndices[0]);
    double[] yRange = spaceManager.getAxisRange(axisIndices[1]);
    
    // בדיקה אם קיים ציר שלישי (Z) - אם כן שולפים אותו, אחרת מאפסים
    double[] zRange = (axisIndices.length > 2) ? spaceManager.getAxisRange(axisIndices[2]) : new double[]{0, 0};

    // 2. עדכון השדות המקומיים של המחלקה (תמיד מחזיקים 3 ערכים ליתר ביטחון)
    this.minVals = new double[] { xRange[0], yRange[0], zRange[0] }; 
    this.maxVals = new double[] { xRange[1], yRange[1], zRange[1] };

    // 3. איפוס מנוע הניווט הדו-ממדי (Reset Zoom)
    // אנחנו שולחים לו רק את X ו-Y כי הוא לא יודע לטפל ב-Z
    if (navHandler != null) {
        double[] min2D = { xRange[0], yRange[0] };
        double[] max2D = { xRange[1], yRange[1] };
        navHandler.resetTo(min2D, max2D);
    }
    if (renderer3D != null) renderer3D.setNeedsReprojection(true);
    if (renderer != null) renderer3D.setNeedsReprojection(true);
}
    // --- Helpers לעיצוב ---
    
    private Label createHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        l.setTextFill(Color.web("#4ec9b0")); // צבע טורקיז יפה
        return l;
    }

    private void styleButton(Button b) {
        b.setStyle("-fx-background-color: #0e639c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        b.setPrefWidth(200);
    }

    private ComboBox<Integer> createAxisCombo() {
        ComboBox<Integer> cb = new ComboBox<>();
        for(int i=0; i<50; i++) cb.getItems().add(i);
        cb.setStyle("-fx-pref-width: 80;");
        return cb;
    }
    
    private void styleGridLabels(GridPane grid) {
        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.WHITE);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}