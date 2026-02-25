package app;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * מחלקה זו אחראית אך ורק על בניית ממשק המשתמש (UI) של סרגל הצד השמאלי.
 * היא אינה מכילה לוגיקה, אלא רק מציגה רכיבים וחושפת אותם עבור ה-Controller.
 */
public class SidebarView {

    // ============================================================
    // רכיבי ה-UI שנחשוף החוצה (כדי שנוכל להאזין להם מהבקר)
    // ============================================================
    private VBox mainLayout; // המכל הראשי של הסרגל

    // מצב תצוגה
    private ToggleGroup viewGroup;
    private RadioButton rb2D;
    private RadioButton rb3D;

    // צפיפות טקסט
    private Slider textDensitySlider;

    // Subspace / Centroid
    private Spinner<Integer> kNeighborsSpinner;

    // ציר סמנטי
    private ComboBox<String> wordComboA;
    private ComboBox<String> wordComboB;
    private Button applyAxisBtn;

    // משוואה וקטורית
    private TextField equationField;
    private Button solveEqBtn;

    // ניהול דאטה
    private Button loadBtn;
    private Label statusLabel;

    // צירי הקרנה
    private ComboBox<Integer> xSelect;
    private ComboBox<Integer> ySelect;
    private ComboBox<Integer> zSelect;
    private Label zLabel;

    // מטריקת מרחק
    private ToggleGroup metricGroup;
    private RadioButton rbEuclidean;
    private RadioButton rbCosine;

    public SidebarView() {
        buildUI();
    }

    /**
     * הפונקציה המרכזית שבונה את כל האלמנטים של ה-Sidebar.
     * נלקחה כמעט במלואה מתוך מתודת start() הישנה.
     */
    private void buildUI() {
        Label titleLabel = new Label("Word-World");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // --- בחירת מצב תצוגה ---
        Label viewHeader = createHeader("View Mode");
        rb2D = new RadioButton("2D Projection");
        rb3D = new RadioButton("3D Perspective");
        rb2D.setTextFill(Color.WHITE);
        rb3D.setTextFill(Color.WHITE);
        rb2D.setSelected(true);
        viewGroup = new ToggleGroup();
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
        loadBtn = new Button("Run Python & Load");
        styleButton(loadBtn);
        statusLabel = new Label("Status: Waiting");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setWrapText(true);

        // --- בחירת צירים ---
        Label axisHeader = createHeader("Projection Axes");
        xSelect = createAxisCombo();
        ySelect = createAxisCombo();
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
        rbEuclidean = new RadioButton("Euclidean Distance");
        rbCosine = new RadioButton("Cosine Similarity");
        rbEuclidean.setTextFill(Color.WHITE);
        rbCosine.setTextFill(Color.WHITE);
        rbEuclidean.setSelected(true);
        metricGroup = new ToggleGroup();
        rbEuclidean.setToggleGroup(metricGroup);
        rbCosine.setToggleGroup(metricGroup);
        VBox metricBox = new VBox(10, rbEuclidean, rbCosine);

        // --- הרכבת ה-Sidebar ---
        mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setPrefWidth(280);
        mainLayout.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #1a1a1a; -fx-border-width: 0 2 0 0;");
        
        mainLayout.getChildren().addAll(
            titleLabel, new Separator(), 
            viewHeader, viewBox, new Separator(),
            densityBox, new Separator(), 
            subspacePane, semanticPane, mathPane, new Separator(), 
            dataHeader, loadBtn, statusLabel, new Separator(),
            axisHeader, axisGrid, new Separator(),
            metricHeader, metricBox
        );
    }

    // ============================================================
    // פונקציות עיצוב (UI Helpers מתוך המחלקה הישנה)
    // ============================================================
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

    // ============================================================
    // Getters - חשיפת הרכיבים עבור ה-AppController
    // ============================================================
    
    public VBox getView() { return mainLayout; } // קריטי כדי להוסיף לחלון הראשי

    public ToggleGroup getViewGroup() { return viewGroup; }
    public RadioButton getRb2D() { return rb2D; }
    public RadioButton getRb3D() { return rb3D; }

    public Slider getTextDensitySlider() { return textDensitySlider; }

    public Spinner<Integer> getkNeighborsSpinner() { return kNeighborsSpinner; }

    public ComboBox<String> getWordComboA() { return wordComboA; }
    public ComboBox<String> getWordComboB() { return wordComboB; }
    public Button getApplyAxisBtn() { return applyAxisBtn; }

    public TextField getEquationField() { return equationField; }
    public Button getSolveEqBtn() { return solveEqBtn; }

    public Button getLoadBtn() { return loadBtn; }
    public Label getStatusLabel() { return statusLabel; }
    
    // פונקציית נוחות לעדכון סטטוס ישירות
    public void setStatusText(String text) {
        if (this.statusLabel != null) {
            this.statusLabel.setText(text);
        }
    }

    public ComboBox<Integer> getXSelect() { return xSelect; }
    public ComboBox<Integer> getYSelect() { return ySelect; }
    public ComboBox<Integer> getZSelect() { return zSelect; }
    public Label getZLabel() { return zLabel; }

    public ToggleGroup getMetricGroup() { return metricGroup; }
    public RadioButton getRbEuclidean() { return rbEuclidean; }
    public RadioButton getRbCosine() { return rbCosine; }
}