package app;

/*אחרי כל פעולה מתמתית שקורית כאן
    ONUPDATE מעדכנת את הציור שוב ושוב בעזרת refreshView
*/

public class NavigationHandler3D extends AbstractNavigationHandler{
    // זוויות הסיבוב
    private double angleX = 0;
    private double angleY = 0;
    private double scale = 1.0; // 1.0 = גודל מקורי
    
    public NavigationHandler3D(Runnable onUpdate) {
        super(onUpdate); // מעביר את פונקציית העדכון למחלקת האב
    }
    @Override
    protected void handleDrag(double dx, double dy, double width, double height) {
        // הבן התלת-ממדי מתרגם גרירה לזוויות סיבוב (מתעלם מרוחב/גובה כי הוא לא צריך)
        double sensitivity = 0.005;
        angleY += dx * sensitivity; 
        angleX -= dy * sensitivity; 
        //הגבלת סיבוב ל90
        double maxAngle = Math.PI / 2.0 - 0.05; 
        if (angleX > maxAngle) angleX = maxAngle;
        if (angleX < -maxAngle) angleX = -maxAngle;
    }
    @Override
    protected void handleScroll(double deltaY) {
        // הבן התלת-ממדי מתרגם גלילה לשינוי של משתנה ה-Scale שלו
        if (deltaY > 0) {
            scale *= 1.1; 
        } else {
            scale /= 1.1;
        }
        scale = Math.max(0.1, Math.min(scale, 20.0));
    }

    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // getters לשימוש ה-Renderer
    public double getAngleX() { return angleX; }
    public double getAngleY() { return angleY; }
    public double getScale() { return scale; } // <--- Getter חדש
    
    // איפוס המבט
    public void reset() {
        this.angleX = 0;
        this.angleY = 0;
        this.scale = 1.0; // איפוס גם לזום
    }
}