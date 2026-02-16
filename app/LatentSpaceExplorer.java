package app;

import java.util.List;
import engine.SpaceManager;
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
    
    // רכיבי דו-מימד
    private ProjectionStrategy projStrt;
    private GraphRenderer renderer;
    private NavigationHandler navHandler; // נשאר כטיפוס ספציפי כי צריך את getMin()
    private Canvas canvas;
    
    // מצב המערכת
    private int[] axisIndices = {0, 1};
    private double[] minVals = {-1.0, -1.0};
    private double[] maxVals = {1.0, 1.0};
    private boolean axisChanged = true;

    // גודל החלון
    private double width = 1200; 
    private double height = 800;
    
    // רכיבי תלת מימד
    private GraphRenderer3D renderer3D;
    private NavigationHandler3D navHandler3D; // נשאר כטיפוס ספציפי כי צריך את getAngleX()
    private ProjectionStrategy projStrt3D; 

    private ComboBox<Integer> zSelect;
    private Label zLabel;

    @Override
    public void start(Stage primaryStage) {
        // ============================================================
        // 1. אתחול משתנים ולוגיקה (Initialization)
        // ============================================================
        
        this.spaceManager = new SpaceManager();
        this.projStrt = new Linear2DProjection();

        this.canvas = new Canvas(width - 260, height);
        
        this.renderer = new GraphRenderer(canvas);
        this.renderer3D = new GraphRenderer3D(canvas);
        this.projStrt3D = new Perspective3DProjection();

        // אתחול מנועי הניווט - מחוברים לקנבס דרך פונקציית האבא האבסטרקטי!
        this.navHandler = new NavigationHandler(minVals, maxVals, () -> {
            if (renderer != null) {
                renderer.setNeedsReprojection(true); // חובה בדו-ממד כי הגבולות זזים
            }
            refreshView();
        });
        this.navHandler.attachTo(canvas);
        
        this.navHandler3D = new NavigationHandler3D(() -> refreshView());
        this.navHandler3D.attachTo(canvas);

        SearchOverlay searchOverlay = new SearchOverlay(spaceManager);

        // ============================================================
        // 2. אינטראקציה: לחיצה על מילה (Click Handling)
        // ============================================================
        canvas.setOnMouseClicked(event -> {
            String clickedWord;

            if (is3DMode) {
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
                clickedWord = ExplorerHelper.findWordAt(
                    event.getX(), event.getY(),
                    spaceManager.getWordList(),
                    projStrt,
                    axisIndices,
                    canvas.getWidth(), canvas.getHeight(),
                    navHandler.getMin(), navHandler.getMax()
                );
            }

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
        Label titleLabel = new Label("Word-World");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label viewHeader = createHeader("View Mode");
        RadioButton rb2D = new RadioButton("2D Projection");
        RadioButton rb3D = new RadioButton("3D Perspective");
        
        rb2D.setTextFill(Color.WHITE);
        rb3D.setTextFill(Color.WHITE);
        rb2D.setSelected(true);

        ToggleGroup viewGroup = new ToggleGroup();
        rb2D.setToggleGroup(viewGroup);
        rb3D.setToggleGroup(viewGroup);
        
        VBox viewBox = new VBox(5, rb2D, rb3D);

        Label dataHeader = createHeader("Data Source");
        Button loadBtn = new Button("Run Python & Load");
        styleButton(loadBtn);
        Label statusLabel = new Label("Status: Waiting");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setWrapText(true);

        Label axisHeader = createHeader("Projection Axes");
        ComboBox<Integer> xSelect = createAxisCombo();
        ComboBox<Integer> ySelect = createAxisCombo();
        zSelect = createAxisCombo(); 

        xSelect.setValue(0); 
        ySelect.setValue(1);
        zSelect.setValue(2);

        zLabel = new Label("Z-Axis:");
        zLabel.setTextFill(Color.WHITE);
        zSelect.setVisible(false);
        zSelect.setManaged(false);
        zLabel.setVisible(false);
        zLabel.setManaged(false);

        GridPane axisGrid = new GridPane();
        axisGrid.setHgap(10); axisGrid.setVgap(5);
        axisGrid.add(new Label("X-Axis:"), 0, 0); axisGrid.add(xSelect, 1, 0);
        axisGrid.add(new Label("Y-Axis:"), 0, 1); axisGrid.add(ySelect, 1, 1);
        axisGrid.add(zLabel, 0, 2); 
        axisGrid.add(zSelect, 1, 2);
        styleGridLabels(axisGrid);

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

        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #1a1a1a; -fx-border-width: 0 2 0 0;");
        
        sidebar.getChildren().addAll(
            titleLabel, new Separator(), 
            viewHeader, viewBox, new Separator(),
            dataHeader, loadBtn, statusLabel, new Separator(),
            axisHeader, axisGrid, new Separator(),
            metricHeader, metricBox
        );

        // ============================================================
        // 4. לוגיקה ואירועים (Event Handlers)
        // ============================================================

        // מעבר בין המצבים (הפעלת הפולימורפיזם!)
        viewGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean switchingTo3D = (newVal == rb3D);
            is3DMode = switchingTo3D;
            
            // כיבוי והדלקה גנריים דרך האבא
            navHandler.setEnabled(!switchingTo3D);
            navHandler3D.setEnabled(switchingTo3D);

            if (switchingTo3D) {
                navHandler3D.reset(); 
                
                zSelect.setVisible(true);
                zSelect.setManaged(true);
                zLabel.setVisible(true);
                zLabel.setManaged(true);
                
                if (axisIndices.length < 3) {
                    axisIndices = new int[] { xSelect.getValue(), ySelect.getValue(), zSelect.getValue() };
                }
            } else {
                navHandler.reset(); 
                
                zSelect.setVisible(false);
                zSelect.setManaged(false);
                zLabel.setVisible(false);
                zLabel.setManaged(false);
            }
            
            updateView(); 
        });

        xSelect.setOnAction(e -> { axisIndices[0] = xSelect.getValue(); updateView(); });
        ySelect.setOnAction(e -> { axisIndices[1] = ySelect.getValue(); updateView(); });
        zSelect.setOnAction(e -> { 
            if(axisIndices.length >= 3) axisIndices[2] = zSelect.getValue(); 
            updateView(); 
        });

        metricGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbEuclidean) {
                spaceManager.changeMetric(new EuclideanDistance());
            } else {
                spaceManager.changeMetric(new AngleDistance());
            }
        });

        loadBtn.setOnAction(e -> {
            statusLabel.setText("Processing...");
            loadBtn.setDisable(true);
            new Thread(() -> { 
                try {
                    spaceManager.ensureDataReady();
                    Platform.runLater(() -> {
                        statusLabel.setText("Ready: " + spaceManager.getWordList().size() + " words");
                        loadBtn.setDisable(false);
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

    // ============================================================
    // פונקציות עזר (Helpers & Rendering Logic)
    // ============================================================

    private void updateView() {
        axisChanged = true;
        refreshView();
    }

    private void refreshView() {
        // 1. הגנה: אם אין נתונים, פשוט מנקים את המסך
        if (spaceManager.getWordList().isEmpty()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }

        // 2. עדכון גבולות במקרה של שינוי צירים
        if (axisChanged) {
            updateBoundaries();
            axisChanged = false;
        }

        // 3. הכנת המשתנים הפולימורפיים (מכנה משותף)
        AbstractRenderer activeRenderer;
        RenderContext ctx;

        if (is3DMode) {
            // א. הגדרת השטח (Viewport) ל-3D - משתמש בגבולות העולם המקוריים
            Viewport vp = new Viewport(axisIndices, minVals, maxVals);
            
            // ב. אריזת התיק (Context) המלא כולל זוויות וזום
            ctx = new RenderContext(
                spaceManager.getWordList(),
                projStrt3D,
                vp,
                navHandler3D.getAngleX(),
                navHandler3D.getAngleY(),
                navHandler3D.getScale()
            );
            
            // ג. בחירת הצייר
            activeRenderer = renderer3D;
            
        } else {
            // א. הגדרת השטח (Viewport) ל-2D - משתמש בגבולות הזום של העכבר
            Viewport vp = new Viewport(axisIndices, navHandler.getMin(), navHandler.getMax());
            
            // ב. אריזת התיק (Context) החלקי - מפעיל את הבנאי השני שלא דורש זוויות
            ctx = new RenderContext(
                spaceManager.getWordList(), 
                projStrt, 
                vp
            );
            
            // ג. בחירת הצייר
            activeRenderer = renderer;
        }

        // 4. קסם הפולימורפיזם: שורת קוד אחת שמפעילה את שני העולמות!
        activeRenderer.render(ctx);
    }

    private void updateBoundaries() {
        double[] xRange = spaceManager.getAxisRange(axisIndices[0]);
        double[] yRange = spaceManager.getAxisRange(axisIndices[1]);
        double[] zRange = (axisIndices.length > 2) ? spaceManager.getAxisRange(axisIndices[2]) : new double[]{0, 0};

        this.minVals = new double[] { xRange[0], yRange[0], zRange[0] }; 
        this.maxVals = new double[] { xRange[1], yRange[1], zRange[1] };

        if (navHandler != null) {
            double[] min2D = { xRange[0], yRange[0] };
            double[] max2D = { xRange[1], yRange[1] };
            navHandler.resetTo(min2D, max2D);
        }
        
        // התיקון הקריטי בוצע כאן: פניה נכונה לשני הציירים
        if (renderer3D != null) renderer3D.setNeedsReprojection(true);
        if (renderer != null) renderer.setNeedsReprojection(true);
    }
    
    private Label createHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        l.setTextFill(Color.web("#4ec9b0")); 
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