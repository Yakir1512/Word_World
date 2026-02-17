package app;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import model.WordVector;

public class GraphRenderer extends AbstractRenderer {

    // הבנאי פשוט מעביר את הקנבס לאבא שינהל אותו
    public GraphRenderer(Canvas canvas) {
        super(canvas);
    }

    // =======================================================
    // שלב ב': לולאת הציור המהירה (ממומשת על ידי הבן)
    // =======================================================
    @Override
    protected void drawElements(RenderContext ctx) {
        this.currentTextDensity = ctx.textDensity; // שואבים את הערך מהתיק
        for (WordVector wv : ctx.words) {
            // שליפה מהירה מהמחסן (projectionCache) שמנוהל על ידי מחלקת האב
            double[] pos = projectionCache.get(wv.getWord());
            
            if (pos == null) continue; // הגנה למקרה של מילה חדשה

            double x = pos[0];
            double y = pos[1];

            // לוגיקת הצביעה והציור (משתמשת במשתני ההדגשה selectedWord ו-neighborWords של האבא)
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
}