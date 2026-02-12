package app;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent; // <--- ייבוא חשוב

public class NavigationHandler3D {
    
    // זוויות הסיבוב
    private double angleX = 0;
    private double angleY = 0;
    
    // --- תוספת לזום ---
    private double scale = 1.0; // 1.0 = גודל מקורי
    
    private boolean enabled = true;
    
    // משתנים למעקב אחרי גרירת העכבר
    private double lastMouseX;
    private double lastMouseY;
    
    private final Runnable onUpdate; 

    public NavigationHandler3D(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void attachTo(Canvas canvas) {
        // --- סיבוב (קיים) ---
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (!enabled) return;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!enabled) return;
            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;

            double sensitivity = 0.005;
            angleY += dx * sensitivity; 
            angleX -= dy * sensitivity; 

            lastMouseX = event.getX();
            lastMouseY = event.getY();
            onUpdate.run();
        });

        // --- זום (חדש!) ---
        canvas.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (!enabled) return;

            double delta = event.getDeltaY();
            // אם גוללים למעלה - מגדילים, למטה - מקטינים
            if (delta > 0) {
                scale *= 1.1; 
            } else {
                scale /= 1.1;
            }
            
            // הגבלת הזום (שלא יהיה ענק מדי או יעלם)
            scale = Math.max(0.1, Math.min(scale, 20.0));

            onUpdate.run();
            event.consume();
        });
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