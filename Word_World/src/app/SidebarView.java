package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * SidebarView — אחראית אך ורק על בניית ה-UI של סרגל הצד.
 * ללא לוגיקה. כל הרכיבים נחשפים ל-AppController.
 */
public class SidebarView {

    private VBox mainLayout;

    private Button toggleCollapseBtn;
    private boolean isCollapsed = false;
    private VBox contentContainer; // יחזיק את כל התוכן מתחת לכפתור הצמצום

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

    // מרחק סמנטי — חדש!
    private ComboBox<String> distWordA;
    private ComboBox<String> distWordB;
    private Button distanceBtn;
    private Label distanceResultLabel;

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

    private void buildUI() {
        // --- Title & Toggle Button ---
        Label titleLabel = new Label("LatentSpace Explorer");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#4ec9b0"));

        toggleCollapseBtn = new Button("◀");
        styleButton(toggleCollapseBtn, "#333333");
        toggleCollapseBtn.setPrefWidth(40);

        HBox headerBar = new HBox(10, titleLabel, toggleCollapseBtn);
        headerBar.setAlignment(Pos.CENTER_LEFT);

        // --- Main Content Container (Collapsible) ---
        contentContainer = new VBox(12);

        Label subtitleLabel = new Label("Word Embedding Visualizer");
        subtitleLabel.setFont(Font.font("Arial", 11));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);

        // --- View Mode ---
        Label viewHeader = createHeader("📐 View Mode");
        rb2D = new RadioButton("2D Projection");
        rb3D = new RadioButton("3D Perspective");
        styleRadio(rb2D); styleRadio(rb3D);
        rb2D.setSelected(true);
        viewGroup = new ToggleGroup();
        rb2D.setToggleGroup(viewGroup);
        rb3D.setToggleGroup(viewGroup);
        VBox viewBox = new VBox(5, rb2D, rb3D);

        // --- Text Density (3D) ---
        Label densityHeader = createHeader("🔤 Text Density (3D)");
        textDensitySlider = new Slider(9.0, 12.0, 9.0);
        textDensitySlider.setShowTickMarks(true);
        textDensitySlider.setShowTickLabels(true);
        textDensitySlider.setStyle("-fx-control-inner-background: #3c3c3c;");
        VBox densityBox = new VBox(5, densityHeader, textDensitySlider);

        // --- Tool 1: Subspace Analysis (Centroid) ---
        Label kHeader = new Label("Neighbors (K):");
        kHeader.setTextFill(Color.WHITE);
        kNeighborsSpinner = new Spinner<>(1, 50, 5);
        kNeighborsSpinner.setEditable(true);
        kNeighborsSpinner.setPrefWidth(80);

        Label subspaceHint = new Label("Hold CTRL + Click to select multiple words.\nSystem shows centroid + K nearest neighbors.");
        subspaceHint.setTextFill(Color.LIGHTGRAY);
        subspaceHint.setWrapText(true);
        subspaceHint.setFont(Font.font("Arial", 10));

        VBox subspaceContent = new VBox(8,
            subspaceHint,
            new HBox(10, kHeader, kNeighborsSpinner)
        );
        subspaceContent.setPadding(new Insets(10));
        TitledPane subspacePane = createStyledPane("🔵 Subspace / Centroid", subspaceContent);

        // --- Tool 2: Semantic Axis ---
        wordComboA = new ComboBox<>();
        wordComboB = new ComboBox<>();
        wordComboA.setPromptText("Start Word (e.g. poor)");
        wordComboB.setPromptText("End Word (e.g. rich)");
        wordComboA.setPrefWidth(230);
        wordComboB.setPrefWidth(230);
        wordComboA.setEditable(true);
        wordComboB.setEditable(true);
        applyAxisBtn = new Button("⚡ Apply Semantic Axis");
        styleButton(applyAxisBtn, "#0e639c");

        Label axisHint = new Label("Projects all words onto the A→B semantic axis.\nY-axis shows orthogonal PCA spread.");
        axisHint.setTextFill(Color.LIGHTGRAY);
        axisHint.setWrapText(true);
        axisHint.setFont(Font.font("Arial", 10));

        VBox semanticContent = new VBox(8, axisHint,
            new Label("Start:") {{ setTextFill(Color.WHITE); }},
            wordComboA,
            new Label("End:") {{ setTextFill(Color.WHITE); }},
            wordComboB,
            applyAxisBtn
        );
        semanticContent.setPadding(new Insets(10));
        TitledPane semanticPane = createStyledPane("🧭 Semantic Axis", semanticContent);

        // --- Tool 3: Semantic Distance ---
        distWordA = new ComboBox<>();
        distWordB = new ComboBox<>();
        distWordA.setPromptText("Word A");
        distWordB.setPromptText("Word B");
        distWordA.setPrefWidth(230);
        distWordB.setPrefWidth(230);
        distWordA.setEditable(true);
        distWordB.setEditable(true);
        distanceBtn = new Button("📏 Calculate Distance");
        styleButton(distanceBtn, "#6a3d9a");
        distanceResultLabel = new Label("Distance: —");
        distanceResultLabel.setTextFill(Color.web("#c792ea"));
        distanceResultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        VBox distContent = new VBox(8,
            new Label("Word A:") {{ setTextFill(Color.WHITE); }},
            distWordA,
            new Label("Word B:") {{ setTextFill(Color.WHITE); }},
            distWordB,
            distanceBtn,
            distanceResultLabel
        );
        distContent.setPadding(new Insets(10));
        TitledPane distPane = createStyledPane("📐 Semantic Distance", distContent);

        // --- Tool 4: Vector Arithmetic ---
        equationField = new TextField();
        equationField.setPromptText("e.g. king - man + woman");
        equationField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;");
        solveEqBtn = new Button("🔢 Solve & Visualize");
        styleButton(solveEqBtn, "#1f6b35");

        Label mathHint = new Label("Use: word + word - word\nResult shown on canvas.");
        mathHint.setTextFill(Color.LIGHTGRAY);
        mathHint.setWrapText(true);
        mathHint.setFont(Font.font("Arial", 10));

        VBox mathContent = new VBox(8, mathHint, equationField, solveEqBtn);
        mathContent.setPadding(new Insets(10));
        TitledPane mathPane = createStyledPane("🧮 Vector Arithmetic", mathContent);

        // --- Data Management ---
        Label dataHeader = createHeader("💾 Data Source");
        loadBtn = new Button("🐍 Run Python & Load Data");
        styleButton(loadBtn, "#8b4513");
        loadBtn.setPrefWidth(240);
        statusLabel = new Label("Status: Click button to load");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setWrapText(true);
        statusLabel.setFont(Font.font("Arial", 11));

        // --- PCA Axis Selection ---
        Label axisHeader = createHeader("📊 Projection Axes (PCA)");
        xSelect = createAxisCombo(); ySelect = createAxisCombo(); zSelect = createAxisCombo();
        xSelect.setValue(0); ySelect.setValue(1); zSelect.setValue(2);

        zLabel = new Label("Z-Axis (PC):");
        zLabel.setTextFill(Color.WHITE);
        zSelect.setVisible(false); zSelect.setManaged(false);
        zLabel.setVisible(false); zLabel.setManaged(false);

        GridPane axisGrid = new GridPane();
        axisGrid.setHgap(10); axisGrid.setVgap(5);
        Label xLbl = new Label("X-Axis (PC):"); xLbl.setTextFill(Color.WHITE);
        Label yLbl = new Label("Y-Axis (PC):"); yLbl.setTextFill(Color.WHITE);
        axisGrid.add(xLbl, 0, 0); axisGrid.add(xSelect, 1, 0);
        axisGrid.add(yLbl, 0, 1); axisGrid.add(ySelect, 1, 1);
        axisGrid.add(zLabel, 0, 2); axisGrid.add(zSelect, 1, 2);

        // --- Distance Metric ---
        Label metricHeader = createHeader("📏 Distance Metric");
        rbEuclidean = new RadioButton("Euclidean Distance");
        rbCosine = new RadioButton("Cosine Similarity");
        styleRadio(rbEuclidean); styleRadio(rbCosine);
        rbEuclidean.setSelected(true);
        metricGroup = new ToggleGroup();
        rbEuclidean.setToggleGroup(metricGroup);
        rbCosine.setToggleGroup(metricGroup);
        VBox metricBox = new VBox(8, rbEuclidean, rbCosine);

        // --- Legend ---
        Label legendHeader = createHeader("🎨 Legend");
        VBox legendBox = new VBox(4,
            legendItem("●", Color.RED, "Selected word"),
            legendItem("●", Color.ORANGE, "Nearest neighbors"),
            legendItem("●", Color.LIMEGREEN, "CTRL-selected (Subspace)"),
            legendItem("●", Color.GOLD, "Centroid neighbors"),
            legendItem("✕", Color.CYAN, "Centroid point"),
            legendItem("●", Color.MAGENTA, "Equation result")
        );

        // --- Assemble Content Container ---
        contentContainer.getChildren().addAll(
            subtitleLabel, new Separator(),
            viewHeader, viewBox, new Separator(),
            densityBox, new Separator(),
            subspacePane, semanticPane, distPane, mathPane, new Separator(),
            dataHeader, loadBtn, statusLabel, new Separator(),
            axisHeader, axisGrid, new Separator(),
            metricHeader, metricBox, new Separator(),
            legendHeader, legendBox
        );

        // --- Assembling the Main Sidebar Layout ---
        mainLayout = new VBox(12);
        mainLayout.setPadding(new Insets(12));
        mainLayout.setPrefWidth(260);
        mainLayout.setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-border-color: #333; -fx-border-width: 0 1 0 0;"
        );

        // Add only the header and the collapsible content container
        mainLayout.getChildren().addAll(headerBar, contentContainer);

        // Wrap in ScrollPane - keep reference to mainLayout but return scroll
        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        scroll.setPrefWidth(275);

        this._scrollPane = scroll;
    }

    // ScrollPane wrapper
    private ScrollPane _scrollPane;
    public ScrollPane getScrollView() { return _scrollPane; }

    // ============================================================
    // UI Helpers
    // ============================================================

    private Label createHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.web("#4ec9b0"));
        return l;
    }

    private void styleButton(Button b, String color) {
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                   "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        b.setPrefWidth(230);
    }

    private void styleRadio(RadioButton rb) {
        rb.setTextFill(Color.WHITE);
    }

    private ComboBox<Integer> createAxisCombo() {
        ComboBox<Integer> cb = new ComboBox<>();
        for (int i = 0; i < 50; i++) cb.getItems().add(i);
        cb.setPrefWidth(80);
        return cb;
    }

    private TitledPane createStyledPane(String title, VBox content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setExpanded(false);
        tp.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        return tp;
    }

    private HBox legendItem(String symbol, Color color, String desc) {
        Label sym = new Label(symbol);
        sym.setTextFill(color);
        sym.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sym.setMinWidth(16);
        Label txt = new Label(desc);
        txt.setTextFill(Color.LIGHTGRAY);
        txt.setFont(Font.font("Arial", 11));
        HBox box = new HBox(6, sym, txt);
        return box;
    }


    public void toggleCollapse() {
    isCollapsed = !isCollapsed;
    
    if (isCollapsed) {
        // מצב מצומצם: מעלימים את התוכן ומקטינים את הרוחב
        contentContainer.setVisible(false);
        contentContainer.setManaged(false);
        
        toggleCollapseBtn.setText("▶"); // חץ ימינה לפתיחה
        
        // כיווץ ויזואלי של הסיידבר
        mainLayout.setPrefWidth(60);
        _scrollPane.setPrefWidth(75);
    } else {
        // מצב מורחב: מחזירים את התוכן והרוחב המקורי
        contentContainer.setVisible(true);
        contentContainer.setManaged(true);
        
        toggleCollapseBtn.setText("◀");
        
        mainLayout.setPrefWidth(260);
        _scrollPane.setPrefWidth(275);
    }
}



    // ============================================================
    // Getters
    // ============================================================

    public Button getToggleCollapseBtn() {
    return toggleCollapseBtn;
    }
    public VBox getView() { return mainLayout; }

    public ToggleGroup getViewGroup() { return viewGroup; }
    public RadioButton getRb2D() { return rb2D; }
    public RadioButton getRb3D() { return rb3D; }

    public Slider getTextDensitySlider() { return textDensitySlider; }

    public Spinner<Integer> getkNeighborsSpinner() { return kNeighborsSpinner; }

    public ComboBox<String> getWordComboA() { return wordComboA; }
    public ComboBox<String> getWordComboB() { return wordComboB; }
    public Button getApplyAxisBtn() { return applyAxisBtn; }

    public ComboBox<String> getDistWordA() { return distWordA; }
    public ComboBox<String> getDistWordB() { return distWordB; }
    public Button getDistanceBtn() { return distanceBtn; }
    public void setDistanceResult(String text) {
        distanceResultLabel.setText("Distance: " + text);
    }

    public TextField getEquationField() { return equationField; }
    public Button getSolveEqBtn() { return solveEqBtn; }

    public Button getLoadBtn() { return loadBtn; }
    public Label getStatusLabel() { return statusLabel; }
    public void setStatusText(String text) {
        if (this.statusLabel != null) this.statusLabel.setText("Status: " + text);
    }

    public ComboBox<Integer> getXSelect() { return xSelect; }
    public ComboBox<Integer> getYSelect() { return ySelect; }
    public ComboBox<Integer> getZSelect() { return zSelect; }
    public Label getZLabel() { return zLabel; }

    public ToggleGroup getMetricGroup() { return metricGroup; }
    public RadioButton getRbEuclidean() { return rbEuclidean; }
    public RadioButton getRbCosine() { return rbCosine; }
}
