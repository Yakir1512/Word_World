# 🌌 Latent Space Explorer

**מערכת אינטראקטיבית לחקר ויזואלי של מרחבים סמנטיים ווקטורי מילים.**

הפרויקט **Latent Space Explorer** הוא כלי JavaFX מתקדם המאפשר למשתמשים לחקור, לנתח ולבצע מניפולציות על מרחבי מילים (Word Embeddings). המערכת משלבת מנוע חישוב וקטורי עם יכולות רינדור ב-2D וב-3D, ומאפשרת ביצוע פעולות אריתמטיות על שפה, ניתוח סמנטי והטלות מותאמות אישית.

---

## 📸 תמונות מסך (Screenshots)

| תצוגת דו-ממד ואריתמטיקה | ניתוח צירים ותלת-ממד |
| :---: | :---: |
| ![Main View](screenshots/main_view.png) | ![3D Projection](screenshots/3d_view.png) |
| *חישוב וקטורי של משוואות מילים* | *חקר המרחב בתצוגת פרספקטיבה* |

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
    %% --- Main Application ---
    class LatentSpaceExplorer {
        -SpaceManager spaceManager
        -GraphRenderer renderer
        -NavigationHandler navHandler
        +start(Stage primaryStage)
        -handleCanvasClick()
        -updateCentroidAnalysis()
    }

    %% --- Engine & Data Management ---
    namespace Engine {
        class SpaceManager {
            -Map~String, WordVector~ vocabulary
            -List~WordVector~ wordList
            -SemanticSearcher searcher
            +ensureDataReady()
            +getNeighbors(double[] vec, int k)
            +solveEquation(String eq)
        }
    }

    %% --- Model ---
    namespace Model {
        class WordVector {
            -String word
            -double[] fullVector
            -double[] pcaVector
            +getVector()
        }
        class EquationResult {
            +List~String~ pathWords
            +List~Boolean~ operations
            +String resultWord
        }
        class Match {
            +String word
            +double distance
        }
    }

    %% --- Math & Logic ---
    namespace Math {
        class VectorArithmetic {
            <<Static>>
            +calculateCentroid(List vectors)
            +solve(String eq, List list)
        }
        class SemanticSearcher {
            +findNearest()
        }
        class ProjectionStrategy {
            <<Interface>>
            +project(WordVector wv...)
        }
        class Linear2DProjection {
            +project()
        }
        class SemanticAxisProjection {
            -double[] axisVec
            +project()
        }
        class Perspective3DProjection {
            +project()
        }
    }

    %% --- View / Rendering ---
    namespace View {
        class AbstractRenderer {
            <<Abstract>>
            -Canvas canvas
            +render(RenderContext ctx)
            #drawElements()
        }
        class GraphRenderer {
            -EquationResult currentEquation
            -List subspaceSelection
            -double[] currentCentroid
            +setEquationResult()
            +setSubspaceData()
            #drawElements()
        }
        class GraphRenderer3D {
            #drawElements()
        }
        class RenderContext {
            +List~WordVector~ words
            +ProjectionStrategy projStrat
            +Viewport viewport
        }
    }

    %% --- Relationships ---
    LatentSpaceExplorer --> SpaceManager : Uses
    LatentSpaceExplorer --> GraphRenderer : Uses
    LatentSpaceExplorer ..> ProjectionStrategy : Selects Strategy

    SpaceManager --> WordVector : Manages
    SpaceManager ..> VectorArithmetic : Delegates Math
    SpaceManager --> SemanticSearcher : Uses

    VectorArithmetic ..> EquationResult : Creates

    AbstractRenderer <|-- GraphRenderer
    AbstractRenderer <|-- GraphRenderer3D
    GraphRenderer ..> RenderContext : Reads

    ProjectionStrategy <|.. Linear2DProjection : Implements
    ProjectionStrategy <|.. SemanticAxisProjection : Implements
    ProjectionStrategy <|.. Perspective3DProjection : Implements
