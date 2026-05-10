# LatentSpace Explorer

> מערכת לחקירה ויזואלית של ייצוגי מילים (Word Embeddings) — JavaFX + Python

---

## תוכן עניינים

1. [מה המערכת עושה](#מה-המערכת-עושה)
2. [הרצה מהירה](#הרצה-מהירה)
3. [ארכיטקטורה — תרשים מחלקות](#ארכיטקטורה--תרשים-מחלקות)
4. [פירוט השכבות](#פירוט-השכבות)
5. [עקרונות OOP ותבניות עיצוב](#עקרונות-oop-ותבניות-עיצוב)
6. [תכונות המערכת](#תכונות-המערכת)
7. [שאלות הגנה — שאלות ותשובות](#שאלות-הגנה)

---

## מה המערכת עושה

המערכת טוענת מודל GloVe (100 ממדים, עד 5,000 מילים), מפחיתה אותו ל-50 ממדים בעזרת PCA, ומאפשרת לחקור ויזואלית את המרחב הלטנטי:

- **תצוגה 2D ו-3D** של ענן המילים
- **ציר סמנטי** — הקרנת כל המילים על ציר בין שתי מילים שנבחרו
- **שכנים קרובים** — לחיצה על נקודה מציגה את K השכנים הקרובים ביותר
- **מרחק סמנטי** — חישוב מרחק (אוקלידי / קוסינוס) בין שתי מילים
- **משוואה וקטורית** — king − man + woman ≈ queen
- **ניתוח תת-מרחב** — בחירת קבוצת מילים ומציאת הסנטרואיד + שכניו

---

## הרצה מהירה

### דרישות מקדימות

```bash
pip install scikit-learn gensim numpy
```

### הרצה

1. פתח את הפרויקט ב-IDE (IntelliJ / VS Code עם Java Extension)
2. הרץ את `Main.java`
3. לחץ על **"Run Python & Load Data"** בסרגל הצד
4. המתן לסיום טעינת הנתונים (בפעם הראשונה יורד מודל GloVe ~66MB)

> **שים לב:** הסקריפט `embedder.py` חייב להימצא באותה תיקייה שממנה מריצים את הג'אווה.

---

## ארכיטקטורה — תרשים מחלקות

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ENTRY POINT                                        │
│                                                                                 │
│   LatentSpaceExplorer (JavaFX Application)                                      │
│   └─ start(Stage) — מרכיב את כל השכבות ומעביר תלויות                          │
└────────────────────────────────┬────────────────────────────────────────────────┘
                                 │ creates & wires
          ┌──────────────────────┼──────────────────────┐
          ▼                      ▼                      ▼
┌─────────────────┐   ┌──────────────────┐   ┌──────────────────────┐
│  SpaceManager   │   │  WorkspaceState  │   │    AppController     │
│  (engine layer) │   │  (state / model) │   │    (coordinator)     │
└────────┬────────┘   └──────────────────┘   └──────────┬───────────┘
         │                                              │
         │ uses                                         │ reads/writes
         ▼                                              │
┌─────────────────────────────────────────────┐         │
│              ENGINE LAYER                   │         │
│                                             │         │
│  ┌──────────────┐   ┌────────────────────┐  │         │
│  │  DataLoader  │   │ SemanticSearcher   │  │         │
│  │              │   │                    │  │         │
│  │ loadFromFiles│   │ findNearest(vec,k) │  │         │
│  │ (JSON→Map)   │   │ setMetric()        │  │         │
│  └──────────────┘   └────────────────────┘  │         │
└─────────────────────────────────────────────┘         │
                                                        │
          ┌─────────────────────────────────────────────┘
          │ controls
          ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              VIEW LAYER                                         │
│                                                                                 │
│  ┌──────────────────┐         ┌───────────────────────────────────────────────┐ │
│  │   SidebarView    │         │              CanvasView                       │ │
│  │                  │         │                                               │ │
│  │ - Semantic Axis  │         │  ┌─────────────────┐  ┌─────────────────────┐│ │
│  │ - Distance panel │         │  │  GraphRenderer  │  │   GraphRenderer3D   ││ │
│  │ - Vector Arith.  │         │  │  (extends Abst) │  │   (extends Abst)    ││ │
│  │ - Subspace/K     │         │  │                 │  │                     ││ │
│  │ - PCA axis sel.  │         │  │ drawElements()  │  │  drawElements()     ││ │
│  │ - Metric toggle  │         │  └─────────────────┘  └─────────────────────┘│ │
│  └──────────────────┘         │                                               │ │
│                               │  ┌──────────────────┐ ┌─────────────────────┐│ │
│                               │  │ NavigationHandler│ │ NavigationHandler3D ││ │
│                               │  │ (extends Abstr.) │ │ (extends Abstr.)    ││ │
│                               │  │ handleDrag/Scroll│ │ handleDrag/Scroll   ││ │
│                               │  └──────────────────┘ └─────────────────────┘│ │
│                               │                                               │ │
│                               │  ┌──────────────────┐                        │ │
│                               │  │  SearchOverlay   │                        │ │
│                               │  └──────────────────┘                        │ │
│                               └───────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            MATH / STRATEGY LAYER                                │
│                                                                                 │
│  «interface»                    «interface»                                     │
│  ProjectionStrategy             DistanceMetric                                  │
│  + project(wv, axes, dims,      + calculate(v1, v2): double                    │
│            min, max): double[]  + getName(): String                             │
│       ▲                              ▲                                          │
│       │ implements                   │ implements                               │
│  ┌────┴──────────────────────┐  ┌────┴──────────────────┐                      │
│  │  Linear2DProjection       │  │  EuclideanDistance    │                      │
│  │  Perspective3DProjection  │  │  AngleDistance        │                      │
│  │  SemanticAxisProjection   │  └───────────────────────┘                      │
│  └───────────────────────────┘                                                  │
│                                                                                 │
│  VectorArithmetic    VectorMath    ExplorerHelper    ExplorerHelper3D           │
│  (static utilities)  (dot/norm)   (findWordAt 2D)   (findWordAt3D)             │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              MODEL LAYER                                        │
│                                                                                 │
│  WordVector                   Match                   EquationResult            │
│  - word: String               - word: String          - pathWords: List<String> │
│  - fullVector: double[100]    - distance: double      - operations: List<Bool>  │
│  - pcaVector: double[50]      + compareTo(Match)      - resultWord: String      │
│  + getPcaCoordinate(int)      implements Comparable                             │
│  + getFullVector()                                                              │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                         DATA PIPELINE (Python)                                  │
│                                                                                 │
│  embedder.py                                                                    │
│  ├── GloVe model (gensim)  →  full_vectors.json  (word + 100D vector)          │
│  └── PCA (sklearn, 50D)    →  pca_vectors.json   (word + 50D vector)           │
│                                                                                 │
│  DataLoader.java reads both files → Map<String, WordVector>                     │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                      RENDER PIPELINE (data flow)                                │
│                                                                                 │
│  AppController.refreshView()                                                    │
│       │                                                                         │
│       ▼                                                                         │
│  WorkspaceState  ──────────────────────────────────────────────────┐           │
│  (axes, mode, proj strategy)                                        │           │
│       │                                                             │           │
│       ▼                                                             ▼           │
│  RenderContext { words, ProjectionStrategy, Viewport, angle, scale }           │
│       │                                                                         │
│       ▼                                                                         │
│  CanvasView.render(ctx, is3D)                                                   │
│       │                                                                         │
│       ├── is3D=true  → GraphRenderer3D.render(ctx)                             │
│       │                    AbstractRenderer.render() [cache projection]         │
│       │                    drawElements(ctx)          [3D rotate + depth]       │
│       │                                                                         │
│       └── is3D=false → GraphRenderer.render(ctx)                               │
│                            AbstractRenderer.render() [cache projection]         │
│                            drawElements(ctx)          [2D draw + labels]        │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## פירוט השכבות

### 1. Model Layer — `package model`

| מחלקה | תפקיד |
|---|---|
| `WordVector` | מחזיקה מילה + וקטור מלא (100D) + וקטור PCA (50D). **אין לוגיקה** — רק נתונים. |
| `Match` | DTO — מילה + מרחק. מממשת `Comparable` כדי לאפשר מיון ישיר. |
| `EquationResult` | מחזיקה תוצאת משוואה וקטורית: רשימת מילים, פעולות (+/−), ומילת התוצאה. |

### 2. Math Layer — `package math`

| ממשק / מחלקה | תבנית | תפקיד |
|---|---|---|
| `ProjectionStrategy` | **Strategy** | ממשק להמרת `WordVector` לנקודת מסך |
| `Linear2DProjection` | Strategy | נרמול לינארי לפי 2 צירי PCA |
| `Perspective3DProjection` | Strategy | נרמול לפי 3 צירי PCA למרחב תלת-ממדי |
| `SemanticAxisProjection` | Strategy | הטלה על ציר A→B עם ריווח אורתוגונלי ב-Y |
| `DistanceMetric` | **Strategy** | ממשק לחישוב מרחק בין שני וקטורים |
| `EuclideanDistance` | Strategy | שורש סכום ריבועי הפרשים |
| `AngleDistance` | Strategy | 1 − cosine\_similarity |
| `VectorArithmetic` | Utility | פתרון משוואות וקטוריות + חישוב סנטרואיד |
| `VectorMath` | Utility | פעולות בסיסיות: add, subtract, dot, norm |

### 3. Engine Layer — `package engine`

| מחלקה | תפקיד |
|---|---|
| `DataLoader` | קורא `full_vectors.json` ו-`pca_vectors.json` ובונה `Map<String, WordVector>` |
| `SemanticSearcher` | מחפש K שכנים קרובים ביותר לווקטור נתון, בעזרת `DistanceMetric` |
| `SpaceManager` | **Facade** — ממשק מרכזי לכל הלוגיקה: טעינה, חיפוש, מרחק, אנלוגיות |

### 4. App Layer — `package app`

| מחלקה | תבנית | תפקיד |
|---|---|---|
| `WorkspaceState` | **State Object** | מחזיקה את כל המצב הנוכחי (מצב תצוגה, צירים, אסטרטגיות, בחירות) |
| `AppController` | **Controller (MVC)** | מחבר בין ה-Views לבין SpaceManager. מחזיקה את כל ה-event handlers |
| `SidebarView` | **View (MVC)** | בונה את סרגל הצד. **אין לוגיקה** — רק UI |
| `CanvasView` | **View (MVC)** | מנהלת את הקנבס, הציירים, הניווט וה-SearchOverlay |
| `AbstractRenderer` | **Template Method** | מגדיר את שלד הציור: ניקוי → cache projection → `drawElements()` |
| `GraphRenderer` | Template Method | ממש `drawElements()` לציור 2D עם צבעים, תוויות, חיצים |
| `GraphRenderer3D` | Template Method | ממש `drawElements()` לציור 3D עם סיבוב ועומק |
| `AbstractNavigationHandler` | **Template Method** | מטפל בגרירה וגלילה — האבא תופס אירועים, הבן עושה מתמטיקה |
| `NavigationHandler` | Template Method | Pan/Zoom דו-ממדי |
| `NavigationHandler3D` | Template Method | סיבוב תלת-ממדי (angleX, angleY) + zoom |
| `RenderContext` | **Value Object** | "חבילת עבודה" שעוברת מהבקר לציירים |
| `Viewport` | Value Object | צירים + minVals + maxVals |
| `SearchOverlay` | View | חלון חיפוש צף לחיפוש מילה וריכוז המפה סביבה |
| `ExplorerHelper` | Utility | איתור מילה בלחיצת עכבר ב-2D |
| `ExplorerHelper3D` | Utility | איתור מילה בלחיצת עכבר ב-3D (כולל סיבוב ועומק) |
| `BackupVec` | Fallback | מייצר נתוני דמה אם הפייתון נכשל |

---

## עקרונות OOP ותבניות עיצוב

### תבניות עיצוב (Design Patterns)

#### 1. Strategy Pattern — מטריקות מרחק והטלה

```
«interface» DistanceMetric          «interface» ProjectionStrategy
     ▲                                     ▲
     │                                     │
     ├── EuclideanDistance           ├── Linear2DProjection
     └── AngleDistance               ├── Perspective3DProjection
                                     └── SemanticAxisProjection
```

**למה?** אם מחר רוצים להוסיף מטריקת מרחק חדשה (למשל Manhattan Distance), יוצרים מחלקה חדשה שמממשת `DistanceMetric`. **אפס שינויים** בקוד הקיים.

**מיקום שינוי מטריקה:** `AppController.setupMetricToggle()` → `SpaceManager.changeMetric()`.

#### 2. Template Method Pattern — ציירים וניווט

```
AbstractRenderer                 AbstractNavigationHandler
+ render(ctx)  ← לא לשנות!      + attachTo(canvas) ← לא לשנות!
# drawElements() ← abstract      # handleDrag()     ← abstract
                                  # handleScroll()   ← abstract
      ▲                                  ▲
      │                                  │
GraphRenderer   GraphRenderer3D   NavigationHandler   NavigationHandler3D
```

**למה?** `AbstractRenderer.render()` תמיד: מנקה מסך → מעדכן cache → קורא ל-`drawElements()`. הבן **לא יכול לשבור** את הסדר הזה. כשמוסיפים מצב תצוגה חדש (למשל VR), יוצרים `GraphRendererVR extends AbstractRenderer` ומממשים רק את `drawElements()`.

#### 3. MVC (Model-View-Controller)

```
Model               View               Controller
──────────          ──────             ──────────────
WordVector          SidebarView        AppController
Match               CanvasView         (מחבר ביניהם)
EquationResult      GraphRenderer
WorkspaceState      GraphRenderer3D
```

**למה?** `SidebarView` לא יודעת על `SpaceManager`. `AppController` מקשיב לאירועי ה-View וקורא לשירותי ה-Engine. **הפרדת אחריות** — ניתן להחליף כל שכבה בנפרד.

#### 4. Facade Pattern — SpaceManager

`SpaceManager` חושף ממשק פשוט לכל הלוגיקה המורכבת:

```java
// האפליקציה לא צריכה לדעת על DataLoader / SemanticSearcher בנפרד
spaceManager.ensureDataReady();
spaceManager.getNeighbors("king", 5);
spaceManager.getSemanticDistance("dog", "cat");
spaceManager.solveAnalogy("king", "man", "woman");
```

#### 5. Value Object Pattern — RenderContext, Viewport

`RenderContext` הוא אובייקט בלתי-משתנה (כמעט) שמועבר מהבקר לציירים. הוא אורז את כל מה שדרוש לציור פריים אחד — כך שהצייר **לא מחזיק מצב** ויכול לקבל context שונה בכל קריאה.

---

### עקרונות OOP שממומשים

| עיקרון | איפה בקוד |
|---|---|
| **Encapsulation** | `WordVector` — הוקטורים private, נגישים דרך getters בלבד |
| **Inheritance** | `GraphRenderer extends AbstractRenderer`, `NavigationHandler extends AbstractNavigationHandler` |
| **Polymorphism** | `ProjectionStrategy proj` יכול להיות כל אחד מ-3 מימושים — הבקר לא מבחין |
| **Abstraction** | `DistanceMetric` ו-`ProjectionStrategy` מגדירים "מה" בלי "איך" |
| **Single Responsibility** | `DataLoader` — רק קריאה. `SemanticSearcher` — רק חיפוש. `SidebarView` — רק UI |
| **Open/Closed** | הוספת מטריקה חדשה: פתוחים להרחבה, סגורים לשינוי |

---

## תכונות המערכת

### שלב א — ליבה

| תכונה | מצב | מחלקה אחראית |
|---|---|---|
| טעינת וקטורים מ-JSON | ✅ | `DataLoader`, `SpaceManager` |
| תצוגה 2D עם 2 צירי PCA | ✅ | `GraphRenderer`, `Linear2DProjection` |
| בחירת צירי PCA | ✅ | `SidebarView` + `AppController` |
| חיפוש מילה וריכוז המפה | ✅ | `SearchOverlay` |
| K שכנים קרובים בלחיצה | ✅ | `SemanticSearcher`, `GraphRenderer` |
| מרחק סמנטי בין 2 מילים | ✅ | `SpaceManager.getSemanticDistance()` |
| Cosine + Euclidean metric | ✅ | `AngleDistance`, `EuclideanDistance` |
| Custom Semantic Axis | ✅ | `SemanticAxisProjection` |

### שלב ב — מחקר עמוק

| תכונה | מצב | מחלקה אחראית |
|---|---|---|
| Vector Arithmetic Lab | ✅ | `VectorArithmetic`, `GraphRenderer` |
| Subspace / Centroid | ✅ | `AppController`, `VectorArithmetic` |
| K neighbors של סנטרואיד | ✅ | `SpaceManager.getNeighbors(double[])` |

### שלב ג — תצוגה תלת-ממדית

| תכונה | מצב | מחלקה אחראית |
|---|---|---|
| ויזואליזציה 3D | ✅ | `GraphRenderer3D`, `Perspective3DProjection` |
| סיבוב חופשי (drag) | ✅ | `NavigationHandler3D` |
| Zoom ב-3D | ✅ | `NavigationHandler3D` |
| לחיצה על נקודה ב-3D | ✅ | `ExplorerHelper3D` |

---

## שאלות הגנה

### "למה עיצבת את התכנית כמו שעיצבת?"

**על הפרדת ה-Strategy Pattern:**
> "בחרתי ב-Strategy כי המטלה דרשה לפחות שתי מטריקות מרחק. אם הייתי כותב `if (metric.equals("cosine"))` בתוך `SemanticSearcher`, הייתי נאלץ לשנות את הקוד בכל פעם שמוסיפים מטריקה חדשה. עם ממשק `DistanceMetric`, הוספת Manhattan Distance היא יצירת מחלקה אחת ושורת קוד אחת ב-Controller."

**על AbstractRenderer:**
> "הציור תמיד מתבצע באותו סדר: ניקוי מסך → עדכון cache הטלות → ציור. אם הייתי משכפל את הקוד הזה ב-GraphRenderer וב-GraphRenderer3D, כל באג בחישוב ה-cache היה מתוקן פעמיים. Template Method מבטיח שהסדר לא ישבר ושה-cache מנוהל פעם אחת."

**על MVC:**
> "SidebarView לא מחזיקה רשימת WordVector. AppController לא יודע כיצד לצייר. הפרדה זו מאפשרת להחליף את כל ממשק המשתמש (למשל Swing במקום JavaFX) בלי לגעת בלוגיקת החישובים."

---

### "לאן תלך אם יהיו לך באגים?"

| סוג הבאג | איפה לחפש |
|---|---|
| מילים לא נטענות | `DataLoader` → בדוק שמות קבצים + מבנה JSON |
| נקודות לא במיקום הנכון ב-2D | `Linear2DProjection.project()` → בדוק נרמול min/max |
| לחיצה בוחרת מילה שגויה ב-3D | `ExplorerHelper3D.projectToScreen()` → סנכרון עם `GraphRenderer3D` |
| Semantic Axis כולם בקו אחד | `SemanticAxisProjection.computeRanges()` → בדוק worldY |
| Python לא מורץ | `SpaceManager.generateDataWithPython()` → בדוק PATH + חבילות |
| שכנים שגויים | `SemanticSearcher.findNearest()` → בדוק `getFullVector()` vs `getPcaCoordinate()` |

---

### "כיצד העיצוב שלך מאפשר הרחבה עתידית?"

**הוספת מטריקת מרחק חדשה:**
```java
// 1. צור מחלקה חדשה:
public class ManhattanDistance implements DistanceMetric {
    public double calculate(double[] v1, double[] v2) { ... }
    public String getName() { return "Manhattan"; }
}
// 2. הוסף RadioButton ב-SidebarView
// 3. הוסף שורה ב-AppController.setupMetricToggle()
// אפס שינויים ב-SpaceManager / SemanticSearcher
```

**הוספת סוג נתונים חדש (למשל DNA sequences):**
```java
// WordVector כבר גנרי — מחזיק double[] ולא מניח כלום על התוכן
// DataLoader — רק כותבים loadFromDnaFiles() נוסף
// SpaceManager — מוסיפים שיטת טעינה נוספת
// כל שאר הקוד (ציור, חיפוש, מרחק) ממשיך לעבוד
```

**מעבר מ-2D ל-3D:**
> "הדגמה חיה בפרויקט זה — הוספנו `Perspective3DProjection` ו-`GraphRenderer3D` בלי לשנות שורה ב-`SpaceManager` או ב-`WorkspaceState`. רק נוסף `is3DMode` ב-state ו-switch ב-`CanvasView.render()`."

---

*נבנה כפרויקט מסכם בקורס תכנות מונחה עצמים — חורף תשפ"ו*
