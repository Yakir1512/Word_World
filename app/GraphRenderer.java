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
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    // ציור רקע
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

    double w = canvas.getWidth();
    double h = canvas.getHeight();

    for (WordVector wv : words) {
        // שימוש בממשק שלך שמחזיר קואורדינטות מסך סופיות
        double[] pos = proj.project(wv, axes, new double[]{w, h}, min, max);
        
        double x = pos[0];
        double y = pos[1];

        // לוגיקת הצביעה (נשארת זהה)
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
}