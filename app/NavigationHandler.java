package app;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class NavigationHandler {

    // גבולות הזום הנוכחיים
    private double[] currentMin;
    private double[] currentMax;
    
    // פונקציה לקריאה כשיש שינוי (לצייר מחדש)
    private Runnable onUpdate;
    
    // משתנים למעקב אחרי הגרירה
    private double lastMouseX;
    private double lastMouseY;

    // --- תוספת קריטית: דגל לשליטה על הפעלה/כיבוי ---
    // זה מונע התנגשות עם ה-3D
    private boolean enabled = true;

    public NavigationHandler(double[] initialMin, double[] initialMax, Runnable onUpdate) {
        this.currentMin = initialMin.clone();
        this.currentMax = initialMax.clone();
        this.onUpdate = onUpdate;
    }

    /**
     * מאפשר לכבות או להדליק את המנוע (עבור מעבר בין 2D ל-3D)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * רושם את המאזינים לקנבס
     */
    public void attachTo(Canvas canvas) {
        // --- טיפול בזום (גלגלת) ---
        canvas.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (!enabled) return; // אם המנוע כבוי, לא עושים כלום

            double delta = event.getDeltaY();
            // פקטור זום: 0.9 מתקרב, 1.1 מתרחק
            double zoomFactor = (delta > 0) ? 0.9 : 1.1;

            applyZoom(zoomFactor);
            onUpdate.run();
            event.consume(); // מונע אירועים כפולים
        });

        // --- טיפול בתחילת גרירה ---
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (!enabled) return;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        // --- טיפול בגרירה עצמה (Pan) ---
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!enabled) return;

            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;

            applyPan(dx, dy, canvas.getWidth(), canvas.getHeight());

            lastMouseX = event.getX();
            lastMouseY = event.getY();
            onUpdate.run();
        });
    }

    private void applyZoom(double factor) {
        for (int i = 0; i < 2; i++) {
            // חישוב המרכז הנוכחי
            double center = (currentMin[i] + currentMax[i]) / 2.0;
            // חישוב הטווח החדש
            double halfRange = ((currentMax[i] - currentMin[i]) * factor) / 2.0;
            
            // עדכון הגבולות סביב המרכז
            currentMin[i] = center - halfRange;
            currentMax[i] = center + halfRange;
        }
    }

    private void applyPan(double dx, double dy, double width, double height) {
    // חישוב גודל העולם הנוכחי
    double rangeX = currentMax[0] - currentMin[0];
    double rangeY = currentMax[1] - currentMin[1];

    // המרה מפיקסלים ליחידות עולם
    double moveX = (dx / width) * rangeX;
    double moveY = (dy / height) * rangeY;

    // עדכון ציר X (מינוס = גרירת הדף)
    currentMin[0] -= moveX;
    currentMax[0] -= moveX;

    // עדכון ציר Y (שינוי למינוס כדי שיהיה תואם ל-X וירגיש טבעי)
    currentMin[1] -= moveY; 
    currentMax[1] -= moveY;
}

    // --- Getters חשובים ל-Explorer ---
    // (משמשים כדי לדעת אילו גבולות לשלוח ל-Helper בחיפוש מילים)
    public double[] getMin() { return currentMin; }
    public double[] getMax() { return currentMax; }

    // --- איפוס גבולות (למשל כשמחליפים צירים) ---
    public void resetTo(double[] newMin, double[] newMax) {
        this.currentMin = newMin.clone();
        this.currentMax = newMax.clone();
     //   if (onUpdate != null) onUpdate.run();
    }
}