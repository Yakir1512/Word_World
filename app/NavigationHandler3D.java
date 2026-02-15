package app;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent; // <--- ייבוא חשוב

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

    // public void attachTo(Canvas canvas) {
    //     // --- סיבוב (קיים) ---
    //     canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
    //         if (!enabled) return;
    //         lastMouseX = event.getX();
    //         lastMouseY = event.getY();
    //     });

    //     canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
    //         if (!enabled) return;
    //         double dx = event.getX() - lastMouseX;
    //         double dy = event.getY() - lastMouseY;

    //         double sensitivity = 0.005;
    //         angleY += dx * sensitivity; 
    //         angleX -= dy * sensitivity; 

    //         lastMouseX = event.getX();
    //         lastMouseY = event.getY();
    //         onUpdate.run();
    //     });

    //     // --- זום (חדש!) ---
    //     canvas.addEventHandler(ScrollEvent.SCROLL, event -> {
    //         if (!enabled) return;

    //         double delta = event.getDeltaY();
    //         // אם גוללים למעלה - מגדילים, למטה - מקטינים
    //         if (delta > 0) {
    //             scale *= 1.1; 
    //         } else {
    //             scale /= 1.1;
    //         }
            
    //         // הגבלת הזום (שלא יהיה ענק מדי או יעלם)
    //         scale = Math.max(0.1, Math.min(scale, 20.0));

    //         onUpdate.run();
    //         event.consume();
    //     });
    // }

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