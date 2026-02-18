package app;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.EquationResult;
import model.WordVector;

public class GraphRenderer extends AbstractRenderer {
    
    // משתנים למשוואות
    private EquationResult currentEquation = null;
    
    // משתנים ל-Subspace (CTRL selection)
    private List<String> subspaceSelection = new ArrayList<>(); // המילים שנבחרו (ירוק)
    private List<String> centroidNeighbors = new ArrayList<>(); // השכנים (זהב)
    private double[] currentCentroid = null; // המיקום המדויק של הממוצע (ציאן)

    public GraphRenderer(Canvas canvas) {
        super(canvas);
    }

    // =======================================================
    // Setters - עדכון נתונים מבחוץ
    // =======================================================

    public void setEquationResult(EquationResult result) {
        this.currentEquation = result;
    }

    /**
     * עדכון נתוני ה-Subspace
     * @param selected - מילים שנבחרו עם CTRL (ירוק)
     * @param neighbors - שכנים של הממוצע (זהב)
     * @param centroidVec - הוקטור הממוצע עצמו (לציור סימון מיוחד), או null אם אין חישוב
     */
    public void setSubspaceData(List<String> selected, List<String> neighbors, double[] centroidVec) {
        this.subspaceSelection = (selected != null) ? selected : new ArrayList<>();
        this.centroidNeighbors = (neighbors != null) ? neighbors : new ArrayList<>();
        this.currentCentroid = centroidVec;
    }

    // =======================================================
    // לוגיקת הציור
    // =======================================================
    
    @Override
    protected void drawElements(RenderContext ctx) {
        this.currentTextDensity = ctx.textDensity; 
        
        // 1. ציור כל המילים (לפי סדר עדיפויות צבעים המתוקן)
        for (WordVector wv : ctx.words) {
            double[] pos = projectionCache.get(wv.getWord());
            
            if (pos == null) continue;

            double x = pos[0];
            double y = pos[1];
            String word = wv.getWord();

            // --- סדר העדיפויות לציור ---
            
            if (subspaceSelection.contains(word)) {
                // 1. המילים שנבחרו ע"י המשתמש (CTRL) -> ירוק בוהק
                gc.setFill(Color.LIMEGREEN);
                gc.fillOval(x - 5, y - 5, 10, 10);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(null, FontWeight.BOLD, 12));
                gc.fillText(word, x + 8, y);
                gc.setFont(Font.getDefault());
                
            } else if (centroidNeighbors.contains(word)) {
                // 2. השכנים של הממוצע -> זהב/כתום
                gc.setFill(Color.GOLD); 
                gc.fillOval(x - 4, y - 4, 8, 8);
                gc.setFill(Color.LIGHTGRAY);
                gc.fillText(word, x + 6, y);
                
            } else if (selectedWord != null && word.equals(selectedWord)) {
                // 3. בחירה רגילה (קליק יחיד) -> אדום
                gc.setFill(Color.RED);
                gc.fillOval(x - 5, y - 5, 10, 10);
                gc.setFill(Color.WHITE);
                gc.fillText(word, x + 8, y);
                
            } else if (neighborWords.contains(word)) {
                // 4. שכנים של בחירה רגילה -> כתום
                gc.setFill(Color.ORANGE);
                gc.fillOval(x - 3, y - 3, 6, 6);
                gc.setFill(Color.LIGHTGRAY);
                gc.fillText(word, x + 6, y);
                
            } else {
                // 5. מילה רגילה -> אפור
                gc.setFill(Color.GRAY);
                gc.fillOval(x - 2, y - 2, 4, 4);
            }
        }

        // 2. ציור ה-Centroid עצמו (הנקודה הממוצעת) - רק אם קיים
        if (currentCentroid != null) {
            drawCentroidPoint(ctx);
        }

        // 3. ציור מסלול המשוואה (אם יש)
        if (currentEquation != null) {
            drawEquationPath();
        }
    }

    /**
     * פונקציה שמציירת את נקודת הממוצע המתמטית (ה-X הכחול)
     */
    private void drawCentroidPoint(RenderContext ctx) {
        // יצירת WordVector דמה כדי להשתמש באותה לוגיקת הטלה (Projection) כמו המילים הרגילות
        WordVector dummyCentroid = new WordVector("CENTROID", currentCentroid, currentCentroid);
        
        double[] screenPos = ctx.proj.project(
            dummyCentroid, 
            ctx.viewport.axes, 
            new double[]{canvas.getWidth(), canvas.getHeight()}, 
            ctx.viewport.minVals, 
            ctx.viewport.maxVals
        );

        double cx = screenPos[0];
        double cy = screenPos[1];

        // ציור X בולט בצבע ציאן (תכלת)
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        double size = 8;
        gc.strokeLine(cx - size, cy - size, cx + size, cy + size);
        gc.strokeLine(cx + size, cy - size, cx - size, cy + size);
        
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font(null, FontWeight.BOLD, 12));
        gc.fillText("AVERAGE", cx + 10, cy);
        gc.setFont(Font.getDefault());
    }

    private void drawEquationPath() {
        if (currentEquation == null || currentEquation.pathWords.isEmpty()) return;

        List<String> words = currentEquation.pathWords;
        gc.setLineWidth(2.0);
        
        for (int i = 0; i < words.size() - 1; i++) {
            String w1 = words.get(i);
            String w2 = words.get(i+1);
            
            if (i >= currentEquation.operations.size()) break;
            
            boolean isPlus = currentEquation.operations.get(i);
            
            double[] p1 = projectionCache.get(w1);
            double[] p2 = projectionCache.get(w2);
            
            if (p1 != null && p2 != null) {
                gc.setStroke(isPlus ? Color.LIMEGREEN : Color.RED);
                gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
                
                gc.setFill(Color.WHITE);
                gc.fillText(w1, p1[0], p1[1] - 12);
                gc.fillText(w2, p2[0], p2[1] - 12);
            }
        }
        
        if (currentEquation.resultWord != null) {
            double[] pRes = projectionCache.get(currentEquation.resultWord);
            if (pRes != null) {
                gc.setFill(Color.MAGENTA);
                gc.fillOval(pRes[0] - 7, pRes[1] - 7, 14, 14);
                
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(null, FontWeight.BOLD, 14));
                gc.fillText(currentEquation.resultWord + " (Result)", pRes[0], pRes[1] - 15);
                gc.setFont(Font.getDefault());
            }
        }
    }
}