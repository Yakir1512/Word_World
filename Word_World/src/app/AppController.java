package app;

import engine.SpaceManager;
import math.*;
import model.EquationResult;
import model.WordVector;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller — מחבר בין ה-Views לבין SpaceManager ו-WorkspaceState.
 */
public class AppController {

    private final SpaceManager spaceManager;
    private final WorkspaceState state;
    private final SidebarView sidebar;
    private final CanvasView canvasView;

    public AppController(SpaceManager spaceManager, WorkspaceState state,
                         SidebarView sidebar, CanvasView canvasView) {
        this.spaceManager = spaceManager;
        this.state = state;
        this.sidebar = sidebar;
        this.canvasView = canvasView;
    }

    public void initializeBindings() {
        setupDensitySlider();
        setupKNeighborsSpinner();
        setupEquationSolver();
        setupSemanticAxis();
        setupSemanticDistance();      // חדש!
        setupViewModeToggle();
        setupAxisSelectors();
        setupMetricToggle();
        setupDataLoading();
        setupCanvasClick();
        System.out.println("All bindings initialized.");
    }

    // ============================================================
    // חיבור מאזינים
    // ============================================================

    private void setupDensitySlider() {
        sidebar.getTextDensitySlider().valueProperty().addListener((obs, oldVal, newVal) -> {
            state.setTextDensity(newVal.doubleValue());
            if (state.is3DMode()) updateView();
        });
    }

    private void setupKNeighborsSpinner() {
        sidebar.getkNeighborsSpinner().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!state.getMultiSelectedWords().isEmpty()) {
                updateCentroidAnalysis();
                refreshView();
            }
        });
    }

    private void setupEquationSolver() {
        sidebar.getSolveEqBtn().setOnAction(e -> {
            String eq = sidebar.getEquationField().getText();
            if (eq != null && !eq.isEmpty()) {
                EquationResult result = spaceManager.solveEquation(eq);
                if (result != null) {
                    resetToStandardMode();
                    if (state.is3DMode()) force2DMode();
                    state.setCurrentEquation(result);
                    canvasView.getRenderer2D().setEquationResult(result);
                    sidebar.setStatusText("Result: " + result.resultWord);
                    refreshView();
                } else {
                    sidebar.setStatusText("Error: Equation invalid or word not found");
                }
            }
        });
    }

    private void setupSemanticAxis() {
        sidebar.getApplyAxisBtn().setOnAction(e -> {
            String w1 = sidebar.getWordComboA().getValue();
            String w2 = sidebar.getWordComboB().getValue();

            if (w1 != null && w2 != null && !w1.equals(w2)) {
                WordVector vecA = spaceManager.getWordVector(w1);
                WordVector vecB = spaceManager.getWordVector(w2);

                if (vecA != null && vecB != null) {
                    // תיקון: מעביר את כל המילים לחישוב טווח נכון
                    SemanticAxisProjection proj = new SemanticAxisProjection(
                        vecA, vecB, spaceManager.getWordList()
                    );
                    state.setProjStrt2D(proj);

                    if (state.is3DMode()) force2DMode();
                    canvasView.getRenderer2D().setEquationResult(null);
                    canvasView.getRenderer2D().setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);
                    state.getMultiSelectedWords().clear();

                    sidebar.setStatusText("Axis: \"" + w1 + "\" → \"" + w2 + "\"");
                    canvasView.getNavHandler2D().reset();
                    canvasView.getRenderer2D().setNeedsReprojection(true);

                    // עדכון גבולות ל-Semantic Axis
                    updateBoundariesForSemanticAxis();
                    refreshView();
                } else {
                    sidebar.setStatusText("Error: One or more words not found in vocabulary");
                }
            } else {
                sidebar.setStatusText("Error: Select 2 different words");
            }
        });
    }

    /** חדש: מחשב ומציג מרחק בין 2 מילים */
    private void setupSemanticDistance() {
        sidebar.getDistanceBtn().setOnAction(e -> {
            String w1 = sidebar.getDistWordA().getValue();
            String w2 = sidebar.getDistWordB().getValue();

            if (w1 != null && w2 != null && !w1.equals(w2)) {
                double dist = spaceManager.getSemanticDistance(w1, w2);
                if (dist < 0) {
                    sidebar.setStatusText("Distance: word not found");
                    sidebar.setDistanceResult("N/A");
                } else {
                    String metricName = sidebar.getRbCosine().isSelected() ? "Cosine" : "Euclidean";
                    sidebar.setStatusText(String.format("Distance(%s, %s) = %.4f [%s]", w1, w2, dist, metricName));
                    sidebar.setDistanceResult(String.format("%.4f", dist));
                }
            } else {
                sidebar.setStatusText("Error: Select 2 different words for distance");
            }
        });
    }

    private void setupViewModeToggle() {
        sidebar.getViewGroup().selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean switchingTo3D = (newVal == sidebar.getRb3D());
            state.set3DMode(switchingTo3D);
            canvasView.set3DNavigationEnabled(switchingTo3D);

            if (switchingTo3D) {
                canvasView.getNavHandler3D().reset();
                sidebar.getZSelect().setVisible(true);
                sidebar.getZSelect().setManaged(true);
                sidebar.getZLabel().setVisible(true);
                sidebar.getZLabel().setManaged(true);

                int[] currentAxes = state.getAxisIndices();
                if (currentAxes.length < 3) {
                    state.setAxisIndices(new int[]{
                        sidebar.getXSelect().getValue(),
                        sidebar.getYSelect().getValue(),
                        sidebar.getZSelect().getValue()
                    });
                }
            } else {
                canvasView.getNavHandler2D().reset();
                sidebar.getZSelect().setVisible(false);
                sidebar.getZSelect().setManaged(false);
                sidebar.getZLabel().setVisible(false);
                sidebar.getZLabel().setManaged(false);
            }
            updateView();
        });
    }

    private void setupAxisSelectors() {
        sidebar.getXSelect().setOnAction(e -> {
            resetToStandardMode();
            state.getAxisIndices()[0] = sidebar.getXSelect().getValue();
            updateView();
        });
        sidebar.getYSelect().setOnAction(e -> {
            resetToStandardMode();
            state.getAxisIndices()[1] = sidebar.getYSelect().getValue();
            updateView();
        });
        sidebar.getZSelect().setOnAction(e -> {
            if (state.getAxisIndices().length >= 3) {
                state.getAxisIndices()[2] = sidebar.getZSelect().getValue();
                updateView();
            }
        });
    }

    private void setupMetricToggle() {
        sidebar.getMetricGroup().selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == sidebar.getRbEuclidean()) {
                spaceManager.changeMetric(new EuclideanDistance());
            } else {
                spaceManager.changeMetric(new AngleDistance());
            }
            if (!state.getMultiSelectedWords().isEmpty()) {
                updateCentroidAnalysis();
                refreshView();
            }
        });
    }

    private void setupDataLoading() {
        sidebar.getLoadBtn().setOnAction(e -> {
            sidebar.setStatusText("Processing... (may take a few minutes)");
            sidebar.getLoadBtn().setDisable(true);

            new Thread(() -> {
                try {
                    spaceManager.ensureDataReady();
                    Platform.runLater(() -> {
                        int wordCount = spaceManager.getWordList().size();
                        sidebar.setStatusText("Ready: " + wordCount + " words loaded");

                        List<String> words = spaceManager.getWordList().stream()
                            .map(WordVector::getWord)
                            .sorted()
                            .collect(Collectors.toList());

                        // מילוי כל הקומבו-בוקסים
                        sidebar.getWordComboA().getItems().setAll(words);
                        sidebar.getWordComboB().getItems().setAll(words);
                        sidebar.getDistWordA().getItems().setAll(words);
                        sidebar.getDistWordB().getItems().setAll(words);

                        sidebar.getLoadBtn().setDisable(false);
                        updateBoundaries();
                        updateView();
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        sidebar.setStatusText("Error: " + ex.getMessage());
                        sidebar.getLoadBtn().setDisable(false);
                    });
                }
            }).start();
        });
    }

    private void setupCanvasClick() {
        canvasView.getCanvas().setOnMouseClicked(event -> handleCanvasClick(event));
    }

    // ============================================================
    // לוגיקה מרכזית
    // ============================================================

    private void handleCanvasClick(javafx.scene.input.MouseEvent event) {
        String clickedWord = null;
        double x = event.getX();
        double y = event.getY();

        if (state.is3DMode()) {
            NavigationHandler3D nav3D = canvasView.getNavHandler3D();
            clickedWord = ExplorerHelper3D.findWordAt3D(x, y,
                spaceManager.getWordList(), state.getProjStrt3D(),
                state.getAxisIndices(),
                canvasView.getCanvas().getWidth(), canvasView.getCanvas().getHeight(),
                state.getMinVals(), state.getMaxVals(),
                nav3D.getAngleX(), nav3D.getAngleY(), nav3D.getScale());
        } else {
            NavigationHandler nav2D = canvasView.getNavHandler2D();
            clickedWord = ExplorerHelper.findWordAt(x, y,
                spaceManager.getWordList(), state.getProjStrt2D(),
                state.getAxisIndices(),
                canvasView.getCanvas().getWidth(), canvasView.getCanvas().getHeight(),
                nav2D.getMin(), nav2D.getMax());
        }

        if (event.isControlDown()) {
            if (clickedWord != null) {
                if (state.getMultiSelectedWords().contains(clickedWord)) {
                    state.removeMultiSelectedWord(clickedWord);
                } else {
                    state.addMultiSelectedWord(clickedWord);
                }
                updateCentroidAnalysis();
                sidebar.setStatusText("Subspace: " + state.getMultiSelectedWords().size() + " words selected");
            }
        } else {
            state.getMultiSelectedWords().clear();
            canvasView.getRenderer2D().setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);

            if (clickedWord != null) {
                List<model.Match> neighbors = spaceManager.getNeighbors(clickedWord, 10);
                canvasView.getSearchOverlay().setExternalResults(clickedWord, neighbors);
                canvasView.getRenderer2D().setHighlight(clickedWord, neighbors);
                canvasView.getRenderer3D().setHighlight(clickedWord, neighbors);
                sidebar.setStatusText("Selected: \"" + clickedWord + "\" | " + neighbors.size() + " neighbors shown");
            } else {
                canvasView.getRenderer2D().setHighlight(null, null);
                canvasView.getRenderer3D().setHighlight(null, null);
            }
        }
        refreshView();
    }

    private void updateCentroidAnalysis() {
        List<String> selection = state.getMultiSelectedWords();

        if (selection.size() < 2) {
            canvasView.getRenderer2D().setSubspaceData(selection, new ArrayList<>(), null);
            return;
        }

        List<WordVector> selectedVectors = new ArrayList<>();
        for (String w : selection) {
            WordVector wv = spaceManager.getWordVector(w);
            if (wv != null) selectedVectors.add(wv);
        }

        double[] centroid = VectorArithmetic.calculateCentroid(selectedVectors);
        int k = sidebar.getkNeighborsSpinner().getValue();

        List<model.Match> centroidMatches = spaceManager.getNeighbors(centroid, k);
        List<String> neighborNames = centroidMatches.stream()
            .map(m -> m.word)
            .collect(Collectors.toList());

        canvasView.getRenderer2D().setSubspaceData(selection, neighborNames, centroid);
    }

    private void force2DMode() {
        state.set3DMode(false);
        sidebar.getRb2D().setSelected(true);
        canvasView.set3DNavigationEnabled(false);
        sidebar.getZSelect().setVisible(false);
        sidebar.getZSelect().setManaged(false);
        sidebar.getZLabel().setVisible(false);
        sidebar.getZLabel().setManaged(false);
    }

    private void resetToStandardMode() {
        state.resetToStandardMode();
        sidebar.setStatusText("Standard 2D Mode");
        canvasView.getNavHandler2D().reset();
        canvasView.getRenderer2D().setNeedsReprojection(true);
        canvasView.getRenderer2D().setEquationResult(null);
        canvasView.getRenderer2D().setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);
        refreshView();
    }

    public void updateView() {
        state.setAxisChanged(true);
        refreshView();
    }

    public void refreshView() {
        if (spaceManager.getWordList().isEmpty()) {
            GraphicsContext gc = canvasView.getCanvas().getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvasView.getCanvas().getWidth(), canvasView.getCanvas().getHeight());
            return;
        }

        if (state.isAxisChanged()) {
            updateBoundaries();
            state.setAxisChanged(false);
        }

        RenderContext ctx;
        if (state.is3DMode()) {
            NavigationHandler3D nav3D = canvasView.getNavHandler3D();
            Viewport vp = new Viewport(state.getAxisIndices(), state.getMinVals(), state.getMaxVals());
            ctx = new RenderContext(spaceManager.getWordList(), state.getProjStrt3D(), vp,
                nav3D.getAngleX(), nav3D.getAngleY(), nav3D.getScale(), state.getTextDensity());
        } else {
            NavigationHandler nav2D = canvasView.getNavHandler2D();
            Viewport vp = new Viewport(state.getAxisIndices(), nav2D.getMin(), nav2D.getMax());
            ctx = new RenderContext(spaceManager.getWordList(), state.getProjStrt2D(), vp);
            ctx.textDensity = state.getTextDensity();
        }

        canvasView.render(ctx, state.is3DMode());
    }

    private void updateBoundaries() {
        // אם Semantic Axis פעיל — SemanticAxisProjection מחזיק את הטווחים בעצמו
        if (state.getProjStrt2D() instanceof SemanticAxisProjection) {
            updateBoundariesForSemanticAxis();
            return;
        }

        int[] axes = state.getAxisIndices();
        double[] xRange = spaceManager.getAxisRange(axes[0]);
        double[] yRange = spaceManager.getAxisRange(axes[1]);
        double[] zRange = (axes.length > 2) ? spaceManager.getAxisRange(axes[2]) : new double[]{0, 0};

        state.setBoundaries(
            new double[]{xRange[0], yRange[0], zRange[0]},
            new double[]{xRange[1], yRange[1], zRange[1]}
        );

        double[] min2D = {xRange[0], yRange[0]};
        double[] max2D = {xRange[1], yRange[1]};
        canvasView.getNavHandler2D().resetTo(min2D, max2D);
        canvasView.getRenderer2D().setNeedsReprojection(true);
        canvasView.getRenderer3D().setNeedsReprojection(true);
    }

    /** עדכון גבולות עבור Semantic Axis — minVals/maxVals לא רלוונטיים (ה-proj מחזיק טווחים) */
    private void updateBoundariesForSemanticAxis() {
        // ל-SemanticAxisProjection, ה-project() מחשב פנימית את הטווחים
        // נגדיר min/max גנריים כדי שה-findWordAt יעבוד
        state.setBoundaries(new double[]{0, 0}, new double[]{1, 1});
        canvasView.getNavHandler2D().reset();
        canvasView.getRenderer2D().setNeedsReprojection(true);
    }
}
