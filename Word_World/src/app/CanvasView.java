package app;

import engine.SpaceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;

/**
 * מחלקה זו אחראית על ניהול אזור התצוגה המרכזי (הימני) של האפליקציה.
 * היא אורזת בתוכה את הקנבס, הציירים, מנגנוני הניווט וחלון החיפוש הצף.
 */
public class CanvasView {

    // ============================================================
    // רכיבי התצוגה והציור
    // ============================================================
    private StackPane mainLayout; // המכל הראשי שמחזיק הכל
    private Canvas canvas;
    
    private GraphRenderer renderer2D;
    private GraphRenderer3D renderer3D;
    
    private NavigationHandler navHandler2D;
    private NavigationHandler3D navHandler3D;
    
    private SearchOverlay searchOverlay;

    /**
     * בנאי המחלקה.
     * @param width - רוחב הקנבס הרצוי
     * @param height - גובה הקנבס הרצוי
     * @param spaceManager - מועבר רק לצורך אתחול ה-SearchOverlay הפנימי
     * @param onNavigate - פונקציית Callback שתופעל כשמנווטים במרחב (כדי שהבקר ירענן את המסך)
     */
    public CanvasView(double width, double height, SpaceManager spaceManager, Runnable onNavigate) {
        
        // 1. יצירת הקנבס
        this.canvas = new Canvas(width, height);
        
        // 2. יצירת הציירים (מעבירים להם את הקנבס)
        this.renderer2D = new GraphRenderer(canvas);
        this.renderer3D = new GraphRenderer3D(canvas);
        
        // 3. יצירת מנגנוני הניווט וחיבורם לקנבס
        // כאשר יש תזוזה (Zoom/Pan), נסמן לצייר שהוא צריך לחשב מחדש, ונקרא ל-Callback של ה-Controller
        this.navHandler2D = new NavigationHandler(new double[]{-1, -1}, new double[]{1, 1}, () -> {
            renderer2D.setNeedsReprojection(true);
            if (onNavigate != null) onNavigate.run();
        });
        this.navHandler2D.attachTo(canvas);
        
        this.navHandler3D = new NavigationHandler3D(() -> {
            if (onNavigate != null) onNavigate.run();
        });
        this.navHandler3D.attachTo(canvas);

        // 4. יצירת חלון החיפוש הצף (הוא שייך ויזואלית לקנבס)
        this.searchOverlay = new SearchOverlay(spaceManager);
        
        // 5. הרכבת השכבות: קנבס ברקע, חלון חיפוש למעלה בפינה
        this.mainLayout = new StackPane();
        this.mainLayout.setStyle("-fx-background-color: black;");
        StackPane.setAlignment(searchOverlay, Pos.TOP_RIGHT);
        StackPane.setMargin(searchOverlay, new Insets(20));
        this.mainLayout.getChildren().addAll(canvas, searchOverlay);
    }

    // ============================================================
    // פעולות שליטה (Controllers Methods)
    // ============================================================
    
    /**
     * הפונקציה המרכזית שמציירת את המרחב לפי ההקשר שהתקבל.
     */
    public void render(RenderContext ctx, boolean is3DMode) {
        if (is3DMode) {
            renderer3D.render(ctx);
        } else {
            renderer2D.render(ctx);
        }
    }
    
    /**
     * מעדכנת איזה מנוע ניווט פועל כרגע על הקנבס (עכבר ב-2D או ב-3D).
     */
    public void set3DNavigationEnabled(boolean is3DMode) {
        navHandler2D.setEnabled(!is3DMode);
        navHandler3D.setEnabled(is3DMode);
    }

    // ============================================================
    // Getters - חשיפת הרכיבים עבור ה-AppController
    // ============================================================
    
    // קריטי כדי להוסיף את אזור הציור לחלון הראשי (BorderPane)
    public StackPane getView() { return mainLayout; } 
    
    // קריטי כדי שהבקר יוכל להצמיד לזה event.setOnMouseClicked(...)
    public Canvas getCanvas() { return canvas; } 
    
    public GraphRenderer getRenderer2D() { return renderer2D; }
    public GraphRenderer3D getRenderer3D() { return renderer3D; }
    
    public NavigationHandler getNavHandler2D() { return navHandler2D; }
    public NavigationHandler3D getNavHandler3D() { return navHandler3D; }
    
    public SearchOverlay getSearchOverlay() { return searchOverlay; }
}