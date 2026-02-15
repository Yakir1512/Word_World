package app;
import math.ProjectionStrategy;
import model.WordVector;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Match;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphRenderer {
    private Canvas canvas;
    private GraphicsContext gc;

    private java.util.Map<String, double[]> projectionCache = new java.util.HashMap<>();
    private boolean needsReprojection = true;
    // נוסיף שדות לשמירת המצב הנבחר
    private String selectedWord = null;
    private Set<String> neighborWords = new HashSet<>();

    public GraphRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }
/////////////////////////////////////////////////
/// 
/// 
// בתוך GraphRenderer.java

public void render(List<WordVector> words, ProjectionStrategy proj, int[] axes, double[] min, double[] max) {
    // 1. ניקוי הקנבס וציור רקע
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

    double w = canvas.getWidth();
    double h = canvas.getHeight();

    // =======================================================
    // שלב א': עדכון המחסן (Cache) - רלוונטי רק אם הצירים השתנו
    // =======================================================
    if (needsReprojection) {
        projectionCache.clear();
        for (WordVector wv : words) {
            // החישוב היקר של ההטלה
            double[] pos = proj.project(wv, axes, new double[]{w, h}, min, max);
            projectionCache.put(wv.getWord(), pos);
        }
        // כיבוי הדגל לאחר הסיום
        needsReprojection = false;
    }
    // =======================================================

    // =======================================================
    // שלב ב': לולאת הציור המהירה - שימוש בערכים מוכנים
    // =======================================================
    for (WordVector wv : words) {
        // שליפה מהירה מהמחסן (אין חישובי מתמטיקה בתוך הלולאה!)
        double[] pos = projectionCache.get(wv.getWord());
        
        if (pos == null) continue; // הגנה למקרה של מילה חדשה

        double x = pos[0];
        double y = pos[1];

        // לוגיקת הצביעה והציור (מבוססת על המיקומים מהמחסן)
        if (selectedWord != null && wv.getWord().equals(selectedWord)) {
            gc.setFill(Color.RED);
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.setFill(Color.WHITE);
            gc.fillText(wv.getWord(), x + 8, y);
        } else if (neighborWords.contains(wv.getWord())) {
            gc.setFill(Color.ORANGE);
            gc.fillOval(x - 3, y - 3, 6, 6);
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText(wv.getWord(), x + 6, y);
        } else {
            gc.setFill(Color.GRAY);
            gc.fillOval(x - 2, y - 2, 4, 4);
        }
    }
    // =======================================================
}

/////////////////////////////////////////////
    // מתודה לעדכון המצב כשצריך לסמן מילים על הגרף
public void setHighlight(String selected, List<Match> neighbors) {
    this.selectedWord = selected;
    this.neighborWords.clear();
    if (neighbors != null) {
        for (Match m : neighbors) this.neighborWords.add(m.getWord());
    }
}

public void setNeedsReprojection(boolean changeTo){
            this.needsReprojection=changeTo;
        }
}