package app;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public abstract class AbstractNavigationHandler {

    private Runnable onUpdate;
    // משתנים למעקב אחרי גרירת העכבר
    private double lastMouseX;
    private double lastMouseY;

    protected boolean enabled = true;

    // בנאי המקבל את פונקציית העדכון (refreshView)
    public AbstractNavigationHandler(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }


    // =======================================================
    // חתימות אבסטרקטיות - החוזה שהמחלקות הבנות חייבות לממש
    // =======================================================

    /**
     * מתודה זו מופעלת בכל פעם שהמשתמש גורר את העכבר.
     * * @param dx ההפרש בתנועת העכבר בציר X
     * @param dy ההפרש בתנועת העכבר בציר Y
     * @param width רוחב הקנבס הנוכחי (דרוש לחישובי יחס בדו-ממד)
     * @param height גובה הקנבס הנוכחי (דרוש לחישובי יחס בדו-ממד)
     */
    protected abstract void handleDrag(double dx, double dy, double width, double height);

    /**
     * מתודה זו מופעלת בכל פעם שהמשתמש גולל בגלגלת העכבר.
     * * @param deltaY הערך המייצג את כיוון ועוצמת הגלילה (חיובי או שלילי)
     */
    protected abstract void handleScroll(double deltaY);

    /**
     * מתודה זו מופעלת כאשר צריך לאפס את המבט למצב ההתחלתי (למשל בהחלפת צירים).
     */
    public abstract void reset();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }



    public void attachTo(Canvas canvas) {
    // 1. טיפול בלחיצה (זהה לחלוטין בשתיהן)
    canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
        if (!enabled) return;
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    });

    // 2. טיפול בגרירה (האבא מחשב את המרחק, הבן עושה את המתמטיקה)
    canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
        if (!enabled) return;

        double dx = event.getX() - lastMouseX;
        double dy = event.getY() - lastMouseY;

        // ---> כאן קורה הקסם! אנחנו קוראים לפונקציה שהבן יממש <---
        handleDrag(dx, dy, canvas.getWidth(), canvas.getHeight());

        lastMouseX = event.getX();
        lastMouseY = event.getY();
        if (onUpdate != null) onUpdate.run();
    });

    // 3. טיפול בגלילה (האבא תופס את האירוע, הבן מפרש אותו)
    canvas.addEventHandler(ScrollEvent.SCROLL, event -> {
        if (!enabled) return;

        // ---> העברת גודל הגלילה (deltaY) לבן <---
        handleScroll(event.getDeltaY());

        if (onUpdate != null) onUpdate.run();
        event.consume();
    });
}

}
