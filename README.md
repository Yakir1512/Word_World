# 🌌 Latent Space Explorer

**מערכת אינטראקטיבית לחקר ויזואלי של מרחבים סמנטיים ווקטורי מילים.**

הפרויקט **Latent Space Explorer** הוא כלי JavaFX מתקדם המאפשר למשתמשים לחקור, לנתח ולבצע מניפולציות על מרחבי מילים (Word Embeddings). המערכת משלבת מנוע חישוב וקטורי עם יכולות רינדור ב-2D וב-3D, ומאפשרת ביצוע פעולות אריתמטיות על שפה, ניתוח סמנטי והטלות מותאמות אישית.

---

## ✨ תכונות מרכזיות (Features)

* **🌍 ויזואליזציה רב-ממדית:** מעבר חלק בין תצוגת דו-ממד (2D Projection) לתצוגת תלת-ממד (3D Perspective) עם שליטה מלאה (Zoom, Pan, Rotate).
* **🧮 אריתמטיקה וקטורית:** פתרון משוואות מילים (כגון: `King - Man + Woman = ?`) והצגת מסלול החישוב ויזואלית על המסך.
* **📏 צירים סמנטיים (Semantic Axis):** הגדרת ציר הקרנה מותאם אישית על בסיס שתי מילים מנוגדות (למשל: "Poor" <-> "Rich") והצגת כל המרחב ביחס לציר זה.
* **🎯 ניתוח תת-מרחב (Subspace Analysis):** בחירה מרובה של מילים (באמצעות `CTRL`), חישוב ה-Centroid (מרכז הכובד) של הקבוצה ומציאת השכנים הקרובים אליו ביותר.
* **🔍 חיפוש ושכנים:** איתור מילים וחישוב השכנים הסמנטיים הקרובים ביותר בזמן אמת באמצעות מטריקות שונות (מרחק אוקלידי / Cosine Similarity).

---

## 🏗️ ארכיטקטורת המערכת (Architecture)

המערכת בנויה בגישה מודולרית המפרידה בין לוגיקה, נתונים ותצוגה, תוך שימוש בתבניות עיצוב כגון **Strategy Pattern** (עבור שיטות ההטלה) ו-**Delegation** (עבור חישובים מתמטיים).

### 📊 Class Diagram

```mermaid
classDiagram
    %% ==========================================================
    %% 1. Core Interfaces & Abstracts (The Foundation)
    %% ==========================================================
    
    class ProjectionStrategy {
        <<Interface>>
        +project(WordVector wv, int[] axes...) double[]
    }

    class AbstractRenderer {
        <<Abstract>>
        -Canvas canvas
        +render(RenderContext ctx)
        #drawElements(RenderContext ctx)*
    }

    class Application {
        <<JavaFX>>
    }

    %% ==========================================================
    %% 2. Implementations (Polymorphism)
    %% ==========================================================

    %% Projections
    ProjectionStrategy <|.. Linear2DProjection : Implements
    ProjectionStrategy <|.. Perspective3DProjection : Implements
    ProjectionStrategy <|.. SemanticAxisProjection : Implements

    %% Renderers
    AbstractRenderer <|-- GraphRenderer : Extends
    AbstractRenderer <|-- GraphRenderer3D : Extends

    %% Entry Point
    Application <|-- LatentSpaceExplorer : Extends

    %% ==========================================================
    %% 3. The MVC Structure (New Architecture)
    %% ==========================================================

    class LatentSpaceExplorer {
        +start(Stage primaryStage)
    }

    class AppController {
        -SpaceManager spaceManager
        -WorkspaceState state
        -SidebarView sidebar
        -CanvasView canvasView
        +initializeBindings()
        +refreshView()
    }

    class WorkspaceState {
        +boolean is3DMode
        +int[] axisIndices
        +ProjectionStrategy currentStrategy
        +resetToStandardMode()
    }

    class SidebarView {
        -VBox mainLayout
        +getView() VBox
        +getSolveEqBtn() Button
        +getKNeighborsSpinner() Spinner
    }

    class CanvasView {
        -StackPane mainLayout
        -GraphRenderer renderer2D
        -GraphRenderer3D renderer3D
        +render(RenderContext ctx, boolean is3D)
        +getCanvas() Canvas
    }

    %% ==========================================================
    %% 4. Engine & Logic
    %% ==========================================================

    class SpaceManager {
        -Map~String, WordVector~ vocabulary
        +ensureDataReady()
        +getNeighbors(double[] vec, int k)
        +solveEquation(String eq)
    }

    class VectorArithmetic {
        <<Static>>
        +calculateCentroid(List vectors)
        +solve(String eq)
    }

    class RenderContext {
        +List~WordVector~ words
        +ProjectionStrategy projStrat
        +Viewport viewport
    }

    %% ==========================================================
    %% 5. Relationships & Wiring
    %% ==========================================================

    %% Main Setup
    LatentSpaceExplorer ..> AppController : Creates & Initializes
    LatentSpaceExplorer ..> WorkspaceState : Creates
    LatentSpaceExplorer ..> SidebarView : Creates
    LatentSpaceExplorer ..> CanvasView : Creates

    %% Controller wiring
    AppController o-- WorkspaceState : Manages
    AppController o-- SidebarView : Listens/Updates
    AppController o-- CanvasView : Controls
    AppController --> SpaceManager : Uses Logic

    %% View Composition
    CanvasView *-- GraphRenderer : Composed of
    CanvasView *-- GraphRenderer3D : Composed of

    %% Logic Dependencies
    SpaceManager ..> VectorArithmetic : Delegates Math
    GraphRenderer ..> RenderContext : Consumes
    
    %% Strategy Pattern Usage
    WorkspaceState o-- ProjectionStrategy : Holds Strategy
