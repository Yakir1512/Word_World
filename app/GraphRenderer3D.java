package app;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import model.WordVector;

import java.util.ArrayList;
import java.util.List;

public class GraphRenderer3D extends AbstractRenderer {

    public GraphRenderer3D(Canvas canvas) {
        super(canvas);
    }

    // =======================================================
    // אובייקט עזר: שומר את מצב הנקודה אחרי המתמטיקה ולפני הציור
    // =======================================================
    private static class RenderElement {
        WordVector wv;
        double x, y, z;
        double size;
        double opacity;

        RenderElement(WordVector wv, double x, double y, double z, double size, double opacity) {
            this.wv = wv;
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.opacity = opacity;
        }
    }

    // =======================================================
    // הפונקציה הראשית: פייפליין הרינדור
    // =======================================================
    @Override
    protected void drawElements(RenderContext ctx) {
        List<RenderElement> elements = new ArrayList<>();
        double cx = canvas.getWidth() / 2.0;
        double cy = canvas.getHeight() / 2.0;

        // 1. שלב ההתמרה (Transformation): חישוב זוויות לכל המילים
        for (WordVector wv : ctx.words) {
            double[] pos = projectionCache.get(wv.getWord());
            if (pos == null || pos.length < 3) continue;

            RenderElement el = transformPoint(wv, pos, ctx, cx, cy);
            elements.add(el);
        }

        // 2. שלב המיון (Z-Sorting / Painter's Algorithm): 
        // ממיינים מהעמוק ביותר (רחוק) לקרוב ביותר (Z גדול), כדי שהקרובים יסתירו את הרחוקים
        elements.sort((e1, e2) -> Double.compare(e1.z, e2.z));

        // 3. שלב הציור (Drawing)
        for (RenderElement el : elements) {
            drawSingleElement(el);
        }
    }

    // =======================================================
    // אחריות 1: מתמטיקה טהורה (Rotation & Projection)
    // =======================================================
    private RenderElement transformPoint(WordVector wv, double[] pos, RenderContext ctx, double cx, double cy) {
        // מזיזים את הנקודות למרכז כדי לסובב את המרחב סביב האמצע
        double x = pos[0] - cx;
        double y = pos[1] - cy;
        double z = pos[2]; 

        // סיבוב סביב ציר Y
        double cosY = Math.cos(ctx.angleY);
        double sinY = Math.sin(ctx.angleY);
        double x1 = x * cosY - z * sinY;
        double z1 = x * sinY + z * cosY;

        // סיבוב סביב ציר X
        double cosX = Math.cos(ctx.angleX);
        double sinX = Math.sin(ctx.angleX);
        double y1 = y * cosX - z1 * sinX;
        double z2 = y * sinX + z1 * cosX;

        // החלת זום (Scale)
        x1 *= ctx.scale;
        y1 *= ctx.scale;
        z2 *= ctx.scale;

        // פרספקטיבה עדינה: נקודות קרובות ייראו מעט גדולות יותר
        double depthFactor = 1000.0 / (1000.0 - z2); 
        if (depthFactor <= 0) depthFactor = 0.01;

        // מחזירים את הנקודה למקומה על המסך + הפרספקטיבה
        double finalX = (x1 * depthFactor) + cx;
        double finalY = (y1 * depthFactor) + cy;

        // קביעת גודל ושקיפות בסיסיים לפי העומק
        double baseSize = 5.0;
        double size = Math.max(1.0, baseSize * depthFactor);
        double opacity = Math.max(0.1, Math.min(1.0, depthFactor));

        return new RenderElement(wv, finalX, finalY, z2, size, opacity);
    }

    // =======================================================
    // אחריות 2: גרפיקה וצבע (UI Rendering)
    // =======================================================
    private void drawSingleElement(RenderElement el) {
        boolean isSelected = (selectedWord != null && el.wv.getWord().equals(selectedWord));
        boolean isNeighbor = neighborWords.contains(el.wv.getWord());

        Color color;
        double drawSize = el.size;

        if (isSelected) {
            color = Color.RED;
            drawSize *= 2.5; // הגדלה משמעותית למילה הנבחרת
        } else if (isNeighbor) {
            color = Color.ORANGE;
            drawSize *= 1.8;
        } else {
            // נקודה רגילה - הצבע אפור עם שקיפות המבוססת על המרחק (Z)
            color = Color.color(0.5, 0.5, 0.5, el.opacity); 
        }

        gc.setFill(color);
        gc.fillOval(el.x - drawSize / 2, el.y - drawSize / 2, drawSize, drawSize);

        // ציור טקסט: רק אם המילה נבחרת/שכנה, או אם היא קרובה מאוד אלינו (מונע בלגן במסך)
        if (isSelected || isNeighbor || el.size > 6.0) {
            if (isSelected) {
                gc.setFill(Color.WHITE);
            } else if (isNeighbor) {
                gc.setFill(Color.LIGHTGRAY);
            } else {
                gc.setFill(Color.color(0.8, 0.8, 0.8, el.opacity));
            }
            gc.fillText(el.wv.getWord(), el.x + drawSize, el.y);
        }
    }
}