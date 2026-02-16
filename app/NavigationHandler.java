package app;


public class NavigationHandler extends AbstractNavigationHandler {

    // גבולות הזום הנוכחיים
    private double[] currentMin;
    private double[] currentMax;
    

    public NavigationHandler(double[] initialMin, double[] initialMax, Runnable onUpdate) {
        super(onUpdate); // מעביר את פונקציית העדכון למחלקת האב
        this.currentMin = initialMin.clone();
        this.currentMax = initialMax.clone();
    }

    /**
     * מאפשר לכבות או להדליק את המנוע (עבור מעבר בין 2D ל-3D)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void handleDrag(double dx, double dy, double width, double height) {
        // הבן הדו-ממדי יודע שגרירה אצלו מפעילה את פונקציית ה-Pan
        applyPan(dx, dy, width, height);
    }

    @Override
    protected void handleScroll(double deltaY) {
        // הבן הדו-ממדי מתרגם את הגלילה לפקטור זום ומפעיל את applyZoom
        double zoomFactor = (deltaY > 0) ? 0.9 : 1.1;
        applyZoom(zoomFactor);
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


    @Override
    public void reset() {
        this.currentMin = new double[]{-1.0, -1.0};
        this.currentMax = new double[]{1.0, 1.0};
    }


    // --- איפוס גבולות (למשל כשמחליפים צירים) ---
    public void resetTo(double[] newMin, double[] newMax) {
        this.currentMin = newMin.clone();
        this.currentMax = newMax.clone();
     //   if (onUpdate != null) onUpdate.run();
    }
}