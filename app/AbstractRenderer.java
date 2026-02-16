package app;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Match;
import model.WordVector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRenderer {
    
    // ==========================================
    // 1. משתנים משותפים (Protected כדי שהבנים יראו אותם)
    // ==========================================
    protected Canvas canvas;
    protected GraphicsContext gc;
    
    // ניהול מטמון (Cache) כדי לא לחשב השלכות מחדש בכל פריים
    protected Map<String, double[]> projectionCache = new HashMap<>();
    protected boolean needsReprojection = true;
    
    // ניהול בחירות והדגשות של המשתמש
    protected String selectedWord = null;
    protected Set<String> neighborWords = new HashSet<>();

    // ==========================================
    // 2. בנאי (Constructor)
    // ==========================================
    public AbstractRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    // ==========================================
    // 3. פונקציות ניהול מצב (משותפות לחלוטין)
    // ==========================================
    public void setNeedsReprojection(boolean changeTo) {
        this.needsReprojection = changeTo;
    }

    public void setHighlight(String selected, List<Match> neighbors) {
        this.selectedWord = selected;
        this.neighborWords.clear();
        if (neighbors != null) {
            for (Match m : neighbors) {
                this.neighborWords.add(m.getWord());
            }
        }
    }

    // ==========================================
    // 4. פונקציית הציור הראשית (הבוס)
    // ==========================================
    /**
     * פונקציה זו נקראת על ידי ה-Explorer. 
     * היא מבצעת את "העבודה השחורה" שמשותפת לכולם, ואז קוראת לבן.
     */
    public void render(RenderContext ctx) {
        // א. ניקוי המסך (רקע שחור)
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // ב. עדכון המטמון (רק אם חייבים!)
        if (needsReprojection) {
            projectionCache.clear();
            for (WordVector wv : ctx.words) {
                // האבא משתמש ב-Viewport שנמצא בתוך תיק העבודה (ctx)
                double[] pos = ctx.proj.project(
                    wv, 
                    ctx.viewport.axes, 
                    new double[]{w, h, w}, // מעבירים מימדים (רוחב/גובה) גם ל-Z ליתר ביטחון
                    ctx.viewport.minVals, 
                    ctx.viewport.maxVals
                );
                projectionCache.put(wv.getWord(), pos);
            }
            needsReprojection = false;
        }

        // ג. קריאה לפונקציה האבסטרקטית - העברת השרביט למחלקת הבן!
        drawElements(ctx);
    }

    // ==========================================
    // 5. החוזה מול הבנים (Abstract Signature)
    // ==========================================
    /**
     * מחלקות הבנים (2D/3D) חייבות לממש פונקציה זו כדי לצייר את הנקודות בפועל.
     */
    protected abstract void drawElements(RenderContext ctx);
}