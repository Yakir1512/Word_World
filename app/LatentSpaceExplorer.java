package app;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import engine.SpaceManager;
import math.*;
import model.WordVector;
import model.EquationResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LatentSpaceExplorer extends Application {

    // ============================================================
    // משתני המערכת
    // ============================================================
    
    // רכיבי הליבה
    private boolean is3DMode = false;
    private SpaceManager spaceManager;
    private Slider textDensitySlider;
    
    // רכיבי דו-מימד
    private ProjectionStrategy projStrt;
    private GraphRenderer renderer;
    private NavigationHandler navHandler; 
    private Canvas canvas;
    
    // מצב המערכת
    private int[] axisIndices = {0, 1};
    private double[] minVals = {-1.0, -1.0};
    private double[] maxVals = {1.0, 1.0};
    private boolean axisChanged = true;

    // ניהול בחירה מרובה (Subspace / Centroid)
    private List<String> multiSelectedWords = new ArrayList<>();

    // גודל החלון
    private double width = 1200; 
    private double height = 800;
    
    // רכיבי תלת מימד
    private GraphRenderer3D renderer3D;
    private NavigationHandler3D navHandler3D; 
    private ProjectionStrategy projStrt3D;

    // רכיבי UI כלליים
    private ComboBox<Integer> zSelect;
    private Label zLabel;
    private Label statusLabel; 

    // רכיבי כלי הציר הסמנטי
    private ComboBox<String> wordComboA;
    private ComboBox<String> wordComboB;
    private Button applyAxisBtn;

    // רכיבי כלי המשוואות
    private TextField equationField;
    private Button solveEqBtn;
    
    // שליטה ב-K שכנים ל-Centroid
    private Spinner<Integer> kNeighborsSpinner;

    @Override
    public void start(Stage primaryStage) {
        
        // 1. אתחול המנועים
        this.spaceManager = new SpaceManager();
        this.projStrt = new Linear2DProjection();

        // 2. אתחול התצוגה והקנבס
        this.canvas = new Canvas(width - 260, height);
        
        this.renderer = new GraphRenderer(canvas);
        this.renderer3D = new GraphRenderer3D(canvas);
        this.projStrt3D = new Perspective3DProjection();

        // 3. אתחול מנועי הניווט (עכבר)
        this.navHandler = new NavigationHandler(minVals, maxVals, () -> {
            if (renderer != null) renderer.setNeedsReprojection(true); 
            refreshView();
        });
        this.navHandler.attachTo(canvas);
        
        this.navHandler3D = new NavigationHandler3D(() -> refreshView());
        this.navHandler3D.attachTo(canvas);

        SearchOverlay searchOverlay = new SearchOverlay(spaceManager);

        // ============================================================
        // חיבור אירועי העכבר (הלוגיקה המרכזית)
        // ============================================================
        canvas.setOnMouseClicked(event -> handleCanvasClick(event, searchOverlay));

        // ============================================================
        // בניית ממשק המשתמש (UI Construction)
        // ============================================================
        Label titleLabel = new Label("Word-World");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // --- בחירת מצב תצוגה ---
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

        // --- צפיפות טקסט ---
        Label densityHeader = createHeader("Text Density (3D)");
        textDensitySlider = new Slider(9.0, 12.0, 9.0);
        textDensitySlider.setShowTickMarks(true);
        textDensitySlider.setShowTickLabels(true);
        VBox densityBox = new VBox(5, densityHeader, textDensitySlider);

        // --- כלי 1: Subspace Analysis (Centroid) ---
        Label kHeader = new Label("Neighbors (K):");
        kHeader.setTextFill(Color.WHITE);
        kNeighborsSpinner = new Spinner<>(1, 50, 5);
        kNeighborsSpinner.setEditable(true);
        kNeighborsSpinner.setPrefWidth(80);
        
        VBox subspaceContent = new VBox(10, 
            new Label("Hold CTRL + Click to select multiple words."),
            new HBox(10, kHeader, kNeighborsSpinner)
        );
        subspaceContent.setPadding(new Insets(10));
        TitledPane subspacePane = new TitledPane("Subspace / Centroid", subspaceContent);
        subspacePane.setExpanded(false); 

        // --- כלי 2: ציר סמנטי ---
        wordComboA = new ComboBox<>();
        wordComboB = new ComboBox<>();
        wordComboA.setPromptText("Start Word");
        wordComboB.setPromptText("End Word");
        wordComboA.setPrefWidth(220); 
        wordComboB.setPrefWidth(220);
        applyAxisBtn = new Button("Apply Semantic Axis");
        styleButton(applyAxisBtn);
        VBox semanticContent = new VBox(10, new Label("Select 2 words:"), wordComboA, wordComboB, applyAxisBtn);
        semanticContent.setPadding(new Insets(10));
        TitledPane semanticPane = new TitledPane("Semantic Axis", semanticContent);
        semanticPane.setExpanded(false);

        // --- כלי 3: משוואה וקטורית ---
        equationField = new TextField();
        equationField.setPromptText("e.g. king - man + woman");
        solveEqBtn = new Button("Solve & Visualize");
        styleButton(solveEqBtn);
        VBox mathContent = new VBox(10, new Label("Enter Equation:"), equationField, solveEqBtn);
        mathContent.setPadding(new Insets(10));
        TitledPane mathPane = new TitledPane("Vector Arithmetic", mathContent);
        mathPane.setExpanded(false);
        
        // --- ניהול דאטה ---
        Label dataHeader = createHeader("Data Source");
        Button loadBtn = new Button("Run Python & Load");
        styleButton(loadBtn);
        statusLabel = new Label("Status: Waiting");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setWrapText(true);

        // --- בחירת צירים ---
        Label axisHeader = createHeader("Projection Axes");
        ComboBox<Integer> xSelect = createAxisCombo();
        ComboBox<Integer> ySelect = createAxisCombo();
        zSelect = createAxisCombo(); 
        xSelect.setValue(0); ySelect.setValue(1); zSelect.setValue(2);
        zLabel = new Label("Z-Axis:");
        zLabel.setTextFill(Color.WHITE);
        zSelect.setVisible(false); zSelect.setManaged(false);
        zLabel.setVisible(false); zLabel.setManaged(false);
        GridPane axisGrid = new GridPane();
        axisGrid.setHgap(10); axisGrid.setVgap(5);
        axisGrid.add(new Label("X-Axis:"), 0, 0); axisGrid.add(xSelect, 1, 0);
        axisGrid.add(new Label("Y-Axis:"), 0, 1); axisGrid.add(ySelect, 1, 1);
        axisGrid.add(zLabel, 0, 2); axisGrid.add(zSelect, 1, 2);
        styleGridLabels(axisGrid);

        // --- בחירת מטריקה ---
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

        // --- הרכבת ה-Sidebar ---
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #1a1a1a; -fx-border-width: 0 2 0 0;");
        
        sidebar.getChildren().addAll(
            titleLabel, new Separator(), 
            viewHeader, viewBox, new Separator(),
            densityBox, new Separator(), 
            subspacePane, semanticPane, mathPane, new Separator(), 
            dataHeader, loadBtn, statusLabel, new Separator(),
            axisHeader, axisGrid, new Separator(),
            metricHeader, metricBox
        );

        // ============================================================
        // לוגיקה ואירועים (Event Handlers)
        // ============================================================

        textDensitySlider.valueProperty().addListener((obs, oldVal, newVal) -> { if (is3DMode) updateView(); });
        
        // עדכון K שכנים בזמן אמת
        kNeighborsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!multiSelectedWords.isEmpty()) {
                updateCentroidAnalysis();
                refreshView();
            }
        });

        // פתרון משוואה
        solveEqBtn.setOnAction(e -> {
            String eq = equationField.getText();
            if (eq != null && !eq.isEmpty()) {
                EquationResult result = spaceManager.solveEquation(eq);
                if (result != null) {
                    resetToStandardMode();
                    if (is3DMode) force2DMode(); 
                    renderer.setEquationResult(result);
                    statusLabel.setText("Result: " + result.resultWord);
                    refreshView();
                } else {
                    statusLabel.setText("Error: Equation invalid");
                }
            }
        });

        // ציר סמנטי
        applyAxisBtn.setOnAction(e -> {
            String w1 = wordComboA.getValue();
            String w2 = wordComboB.getValue();
            if (w1 != null && w2 != null && !w1.equals(w2)) {
                WordVector vecA = spaceManager.getWordVector(w1);
                WordVector vecB = spaceManager.getWordVector(w2);
                if (vecA != null && vecB != null) {
                    this.projStrt = new SemanticAxisProjection(vecA, vecB);
                    if (is3DMode) force2DMode();
                    
                    if (renderer != null) {
                        renderer.setEquationResult(null);
                        renderer.setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);
                    }
                    multiSelectedWords.clear();
                    
                    statusLabel.setText("Axis: " + w1 + " <-> " + w2);
                    navHandler.reset(); 
                    if (renderer != null) renderer.setNeedsReprojection(true);
                    refreshView();
                }
            } else { statusLabel.setText("Error: Select 2 diff words"); }
        });

        // החלפת מצב תצוגה
        viewGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean switchingTo3D = (newVal == rb3D);
            is3DMode = switchingTo3D;
            navHandler.setEnabled(!switchingTo3D);
            navHandler3D.setEnabled(switchingTo3D);
            if (switchingTo3D) {
                navHandler3D.reset(); 
                zSelect.setVisible(true); zSelect.setManaged(true);
                zLabel.setVisible(true); zLabel.setManaged(true);
                if (axisIndices.length < 3) axisIndices = new int[] { xSelect.getValue(), ySelect.getValue(), zSelect.getValue() };
            } else {
                navHandler.reset(); 
                zSelect.setVisible(false); zSelect.setManaged(false);
                zLabel.setVisible(false); zLabel.setManaged(false);
            }
            updateView(); 
        });

        // החלפת צירים
        xSelect.setOnAction(e -> { resetToStandardMode(); axisIndices[0] = xSelect.getValue(); updateView(); });
        ySelect.setOnAction(e -> { resetToStandardMode(); axisIndices[1] = ySelect.getValue(); updateView(); });
        zSelect.setOnAction(e -> { if(axisIndices.length >= 3) axisIndices[2] = zSelect.getValue(); updateView(); });

        // החלפת מטריקה
        metricGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbEuclidean) spaceManager.changeMetric(new EuclideanDistance());
            else spaceManager.changeMetric(new AngleDistance());
            
            if (!multiSelectedWords.isEmpty()) {
                updateCentroidAnalysis();
                refreshView();
            }
        });

        // טעינת נתונים
        loadBtn.setOnAction(e -> {
            statusLabel.setText("Processing...");
            loadBtn.setDisable(true);
            new Thread(() -> { 
                try {
                    spaceManager.ensureDataReady();
                    Platform.runLater(() -> {
                        statusLabel.setText("Ready: " + spaceManager.getWordList().size() + " words");
                        List<String> words = spaceManager.getWordList().stream().map(WordVector::getWord).sorted().toList();
                        wordComboA.getItems().setAll(words);
                        wordComboB.getItems().setAll(words);
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
        // הרכבת המסך הראשי
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
    // טיפול בלחיצות עכבר (הלוגיקה המרכזית)
    // ============================================================
    private void handleCanvasClick(MouseEvent event, SearchOverlay searchOverlay) {
        String clickedWord = null;
        
        if (is3DMode) {
            clickedWord = ExplorerHelper3D.findWordAt3D(event.getX(), event.getY(), spaceManager.getWordList(), projStrt3D, axisIndices, canvas.getWidth(), canvas.getHeight(), minVals, maxVals, navHandler3D.getAngleX(), navHandler3D.getAngleY(), navHandler3D.getScale());
        } else {
            clickedWord = ExplorerHelper.findWordAt(event.getX(), event.getY(), spaceManager.getWordList(), projStrt, axisIndices, canvas.getWidth(), canvas.getHeight(), navHandler.getMin(), navHandler.getMax());
        }

        // --- פיצול לוגיקה לפי מקש CTRL ---
        if (event.isControlDown()) {
            if (clickedWord != null) {
                if (multiSelectedWords.contains(clickedWord)) {
                    multiSelectedWords.remove(clickedWord);
                } else {
                    multiSelectedWords.add(clickedWord);
                }
                
                // עדכון חישוב Centroid
                updateCentroidAnalysis();
                statusLabel.setText("Subspace: " + multiSelectedWords.size() + " words selected");
            }
        } else {
            // התנהגות רגילה: איפוס בחירה מרובה
            multiSelectedWords.clear();
            // איפוס מלא של Subspace ב-Renderer
            renderer.setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);
            
            if (clickedWord != null) {
                System.out.println("Selected: " + clickedWord);
                List<model.Match> neighbors = spaceManager.getNeighbors(clickedWord, 10);
                searchOverlay.setExternalResults(clickedWord, neighbors);
                renderer.setHighlight(clickedWord, neighbors);
                if (renderer3D != null) renderer3D.setHighlight(clickedWord, neighbors);
            } else {
                renderer.setHighlight(null, null);
                if (renderer3D != null) renderer3D.setHighlight(null, null);
            }
        }
        refreshView();
    }

    /**
     * פונקציית עזר לחישוב Centroid ושכנים.
     * כעת מסונכרנת עם ה-Signature החדש של GraphRenderer.
     */
    private void updateCentroidAnalysis() {
        // תנאי סף: חייבים לפחות 2 מילים
        if (multiSelectedWords.size() < 2) {
            // מעבירים רשימה של המילים (ירוק), אבל ריק לשכנים (צהוב) ו-null ל-Centroid (ציאן)
            renderer.setSubspaceData(multiSelectedWords, new ArrayList<>(), null);
            return;
        }

        // 1. איסוף וקטורים
        List<WordVector> selectedVectors = new ArrayList<>();
        for (String w : multiSelectedWords) {
            WordVector wv = spaceManager.getWordVector(w);
            if (wv != null) selectedVectors.add(wv);
        }

        // 2. חישוב הממוצע
        double[] centroid = VectorArithmetic.calculateCentroid(selectedVectors);

        // 3. מציאת השכנים
        int k = kNeighborsSpinner.getValue();
        List<model.Match> centroidMatches = spaceManager.getNeighbors(centroid, k);
        List<String> neighborNames = centroidMatches.stream()
                                                    .map(m -> m.word)
                                                    .collect(Collectors.toList());

        // 4. שליחת הנתונים המלאים לצייר
        renderer.setSubspaceData(multiSelectedWords, neighborNames, centroid);
    }

    private void force2DMode() {
        is3DMode = false;
        navHandler.setEnabled(true);
        navHandler3D.setEnabled(false);
        zSelect.setVisible(false); zSelect.setManaged(false);
        zLabel.setVisible(false); zLabel.setManaged(false);
    }

    private void resetToStandardMode() {
        if (!(projStrt instanceof Linear2DProjection)) {
            this.projStrt = new Linear2DProjection();
            wordComboA.setValue(null);
            wordComboB.setValue(null);
            statusLabel.setText("Standard 2D Mode");
            if (navHandler != null) navHandler.reset();
            if (renderer != null) renderer.setNeedsReprojection(true);
        }
        
        if (renderer != null) {
            renderer.setEquationResult(null);
            renderer.setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);
        }
        multiSelectedWords.clear();
        refreshView();
    }

    private void updateView() { axisChanged = true; refreshView(); }

    private void refreshView() {
        if (spaceManager.getWordList().isEmpty()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }
        if (axisChanged) { updateBoundaries(); axisChanged = false; }

        AbstractRenderer activeRenderer;
        RenderContext ctx;

        if (is3DMode) {
            Viewport vp = new Viewport(axisIndices, minVals, maxVals);
            ctx = new RenderContext(spaceManager.getWordList(), projStrt3D, vp, navHandler3D.getAngleX(), navHandler3D.getAngleY(), navHandler3D.getScale(), textDensitySlider.getValue());
            activeRenderer = renderer3D;
        } else {
            Viewport vp = new Viewport(axisIndices, navHandler.getMin(), navHandler.getMax());
            ctx = new RenderContext(spaceManager.getWordList(), projStrt, vp);
            activeRenderer = renderer;
        }
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
        if (renderer3D != null) renderer3D.setNeedsReprojection(true);
        if (renderer != null) renderer.setNeedsReprojection(true);
    }
    
    // UI Helpers
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
            if (node instanceof Label) ((Label) node).setTextFill(Color.WHITE);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}