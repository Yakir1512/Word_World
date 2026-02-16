package app;

import math.ProjectionStrategy;
import model.WordVector;
import java.util.List;

public class RenderContext {
    
    // ==========================================
    // נתונים משותפים (בשימוש האבא והבנים)
    // ==========================================
    public final List<WordVector> words;       // מידע: המילים עצמן (האבא מעביר למטמון, הבנים מציירים)
    public final ProjectionStrategy proj;      // כלי: אסטרטגיית ההטלה (האבא והבנים צריכים)
    public final Viewport viewport;            // מידע: גבולות וצירים (האבא למטמון, הבנים לציור)

    // ==========================================
    // נתוני תלת-ממד בלבד (בשימוש GraphRenderer3D)
    // ==========================================
    public final double angleX;
    public final double angleY;
    public final double scale;

    /**
     * בנאי מלא - מיועד לשימוש על ידי מנוע התלת-ממד (3D)
     */
    public RenderContext(List<WordVector> words, ProjectionStrategy proj, Viewport viewport, 
                         double angleX, double angleY, double scale) {
        this.words = words;
        this.proj = proj;
        this.viewport = viewport;
        this.angleX = angleX;
        this.angleY = angleY;
        this.scale = scale;
    }

    /**
     * בנאי חלקי (נוחות) - מיועד לשימוש על ידי מנוע הדו-ממד (2D).
     * הוא מאתחל את נתוני התלת-ממד לערכי ברירת מחדל, כי הדו-ממד פשוט יתעלם מהם.
     */
    public RenderContext(List<WordVector> words, ProjectionStrategy proj, Viewport viewport) {
        this.words = words;
        this.proj = proj;
        this.viewport = viewport;
        
        // ערכי "זבל" שהבן הדו-ממדי בכלל לא יסתכל עליהם
        this.angleX = 0;
        this.angleY = 0;
        this.scale = 1.0;
    }
}