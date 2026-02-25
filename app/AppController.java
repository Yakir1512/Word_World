package app;

import engine.SpaceManager;
import math.*;
import model.EquationResult;
import model.WordVector;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * מחלקה זו מנהלת את הלוגיקה של האפליקציה (Controller).
 * היא מחברת בין ממשק המשתמש (Views) לבין המודל (SpaceManager) והמצב (WorkspaceState).
 */
public class AppController {

    private final SpaceManager spaceManager;
    private final WorkspaceState state;
    private final SidebarView sidebar;
    private final CanvasView canvasView;

    public AppController(SpaceManager spaceManager, WorkspaceState state, SidebarView sidebar, CanvasView canvasView) {
        this.spaceManager = spaceManager;
        this.state = state;
        this.sidebar = sidebar;
        this.canvasView = canvasView;
    }

    /**
     * פונקציה זו נקראת פעם אחת בלבד בעת עליית האפליקציה.
     * היא מחברת את כל אירועי העכבר והכפתורים לפעולות המתאימות.
     */
    public void initializeBindings() {
        setupDensitySlider();
        setupKNeighborsSpinner();
        setupEquationSolver();
        setupSemanticAxis();
        setupViewModeToggle();
        setupAxisSelectors();
        setupMetricToggle();
        setupDataLoading();
        setupCanvasClick();
        System.out.println("canvas and bottons craeated");
    }

    // ============================================================
    // חיבור מאזינים (Bindings)
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
                System.out.println("view refreshed");
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
                    sidebar.setStatusText("Error: Equation invalid");
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
                    state.setProjStrt2D(new SemanticAxisProjection(vecA, vecB));
                    if (state.is3DMode()) force2DMode();
                    
                    // ניקוי ויזואלי
                    canvasView.getRenderer2D().setEquationResult(null);
                    canvasView.getRenderer2D().setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);
                    state.getMultiSelectedWords().clear();
                    
                    sidebar.setStatusText("Axis: " + w1 + " <-> " + w2);
                    canvasView.getNavHandler2D().reset(); 
                    canvasView.getRenderer2D().setNeedsReprojection(true);
                    
                    refreshView();
                }
            } else { 
                sidebar.setStatusText("Error: Select 2 diff words"); 
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
                sidebar.getZSelect().setVisible(true); sidebar.getZSelect().setManaged(true);
                sidebar.getZLabel().setVisible(true); sidebar.getZLabel().setManaged(true);
                
                int[] currentAxes = state.getAxisIndices();
                if (currentAxes.length < 3) {
                    state.setAxisIndices(new int[] { sidebar.getXSelect().getValue(), sidebar.getYSelect().getValue(), sidebar.getZSelect().getValue() });
                }
            } else {
                canvasView.getNavHandler2D().reset(); 
                sidebar.getZSelect().setVisible(false); sidebar.getZSelect().setManaged(false);
                sidebar.getZLabel().setVisible(false); sidebar.getZLabel().setManaged(false);
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
            if(state.getAxisIndices().length >= 3) {
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
            
            // עדכון בזמן אמת אם יש חישוב פעיל
            if (!state.getMultiSelectedWords().isEmpty()) {
                updateCentroidAnalysis();
                refreshView();
            }
        });
    }

    private void setupDataLoading() {
        sidebar.getLoadBtn().setOnAction(e -> {
            sidebar.setStatusText("Processing...");
            sidebar.getLoadBtn().setDisable(true);
            
            new Thread(() -> { 
                try {
                    spaceManager.ensureDataReady();
                    Platform.runLater(() -> {
                        sidebar.setStatusText("Ready: " + spaceManager.getWordList().size() + " words");
                        List<String> words = spaceManager.getWordList().stream()
                                                         .map(WordVector::getWord)
                                                         .sorted().toList();
                        sidebar.getWordComboA().getItems().setAll(words);
                        sidebar.getWordComboB().getItems().setAll(words);
                        
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
        System.out.println("Mouse Clicked");
        
    }

    // ============================================================
    // לוגיקה מרכזית ופעולות תצוגה
    // ============================================================

    private void handleCanvasClick(MouseEvent event) {
        String clickedWord = null;
        double x = event.getX();
        double y = event.getY();
        
        // 1. זיהוי מילה בהתאם למצב התצוגה
        if (state.is3DMode()) {
            NavigationHandler3D nav3D = canvasView.getNavHandler3D();
            clickedWord = ExplorerHelper3D.findWordAt3D(x, y, spaceManager.getWordList(), state.getProjStrt3D(), state.getAxisIndices(), 
                    canvasView.getCanvas().getWidth(), canvasView.getCanvas().getHeight(), state.getMinVals(), state.getMaxVals(), 
                    nav3D.getAngleX(), nav3D.getAngleY(), nav3D.getScale());
                    System.out.println("3D");
        } else {
            NavigationHandler nav2D = canvasView.getNavHandler2D();
            clickedWord = ExplorerHelper.findWordAt(x, y, spaceManager.getWordList(), state.getProjStrt2D(), state.getAxisIndices(), 
                    canvasView.getCanvas().getWidth(), canvasView.getCanvas().getHeight(), nav2D.getMin(), nav2D.getMax());
                    System.out.println("2D");
        }

        // 2. פיצול לוגיקה: CTRL לעומת לחיצה רגילה
        if (event.isControlDown()) {
            System.out.println("Ctrl cliked");
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
            // התנהגות רגילה
            state.getMultiSelectedWords().clear();
            canvasView.getRenderer2D().setSubspaceData(new ArrayList<>(), new ArrayList<>(), null);
            
            if (clickedWord != null) {
                List<model.Match> neighbors = spaceManager.getNeighbors(clickedWord, 10);
                canvasView.getSearchOverlay().setExternalResults(clickedWord, neighbors);
                
                canvasView.getRenderer2D().setHighlight(clickedWord, neighbors);
                canvasView.getRenderer3D().setHighlight(clickedWord, neighbors);
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
        sidebar.getZSelect().setVisible(false); sidebar.getZSelect().setManaged(false);
        sidebar.getZLabel().setVisible(false); sidebar.getZLabel().setManaged(false);
        System.out.println("2D forced");
    }

    private void resetToStandardMode() {
        state.resetToStandardMode();
        
        sidebar.getWordComboA().setValue(null);
        sidebar.getWordComboB().setValue(null);
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
            System.out.println("view refreshed");
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
            // הגדרת הצפיפות גם ל-2D במידה והצייר משתמש בה
            ctx.textDensity = state.getTextDensity(); 
        }
        
        canvasView.render(ctx, state.is3DMode());
    }

    private void updateBoundaries() {
        int[] axes = state.getAxisIndices();
        double[] xRange = spaceManager.getAxisRange(axes[0]);
        double[] yRange = spaceManager.getAxisRange(axes[1]);
        double[] zRange = (axes.length > 2) ? spaceManager.getAxisRange(axes[2]) : new double[]{0, 0};
        
        state.setBoundaries(
            new double[] { xRange[0], yRange[0], zRange[0] },
            new double[] { xRange[1], yRange[1], zRange[1] }
        );
        
        double[] min2D = { xRange[0], yRange[0] };
        double[] max2D = { xRange[1], yRange[1] };
        canvasView.getNavHandler2D().resetTo(min2D, max2D);
        
        canvasView.getRenderer2D().setNeedsReprojection(true);
        canvasView.getRenderer3D().setNeedsReprojection(true);
        System.out.println("Boundaries updated");
    }
}