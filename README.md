# 🌌 Latent Space Explorer Pro

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)

**Latent Space Explorer Pro** הוא כלי ויזואליזציה אינטראקטיבי המאפשר לחקור מרחבים וקטוריים רב-ממדיים (Word Embeddings) בזמן אמת. הפרויקט משלב כוח חישובי של Python (לעיבוד שפה ו-PCA) עם ממשק משתמש מהיר ומרהיב ב-JavaFX.

---

## ✨ תכונות עיקריות (Key Features)

* **🔍 חקירה סמנטית:** חיפוש מילים ומציאת השכנים הקרובים ביותר (Nearest Neighbors) לפי מטריקות מרחק שונות.
* **📐 הדמיה בתלת-ממד (3D Perspective):** מנוע גרפי מותאם אישית הכולל סיבוב (Rotation), זום (Zoom) ופרספקטיבה עמוקה.
* **🖥️ מצב דו-ממד אינטראקטיבי:** ניווט חלק (Pan & Zoom) במרחב הלטנטי המוטל.
* **🤖 אינטגרציה עם Python:** שימוש בספריות `Gensim` ו-`Scikit-Learn` ליצירת וקטורים והפחתת ממדים (PCA).
* **💡 מנוע אנלוגיות:** פתרון משוואות סמנטיות (לדוגמה: King - Man + Woman = Queen).
* **🛡️ מנגנון BackupVec:** מערכת "נפילה בטוחה" המייצרת נתוני דמה רנדומליים במידה וסביבת הפייתון אינה זמינה.

---

## 📸 תצוגה ויזואלית

| חקירה בתלת-ממד | ניווט דו-ממדי |
| :---: | :---: |
| ![3D View](https://via.placeholder.com/400x250?text=3D+Visualization+Screenshot) | ![2D View](https://via.placeholder.com/400x250?text=2D+Navigation+Screenshot) |

---

## 🛠️ ארכיטקטורת המערכת

המערכת בנויה משלוש שכבות מרכזיות:
1.  **Data Layer (Python):** טעינת מודלים (כמו Word2Vec/GloVe), ניקוי נתונים והרצת אלגוריתם PCA.
2.  **Engine Layer (Java):** ניהול הזיכרון (`SpaceManager`), חישובי מרחקים (`SemanticSearcher`) ומתמטיקה וקטורית.
3.  **UI/Rendering Layer:** ניהול האינטראקציות והטלת הנקודות למסך בזמן אמת.

---

## 🚀 איך מריצים?

### דרישות קדם (Prerequisites)
* **Java 17** ומעלה.
* **Python 3.10+** עם הספריות הבאות:
    ```bash
    pip install gensim scikit-learn numpy
    ```

### הרצה
1.  וודא שהנתיבים ל-`python.exe` ולסקריפט ה-`embedder.py` מעודכנים בתוך מחלקת `SpaceManager`.
2.  הרץ את המחלקה `LatentSpaceExplorer`.
3.  לחץ על כפתור **"Run Python & Load"** כדי להתחיל את החגיגה.

---

## 🧪 טכנולוגיות בשימוש

* **JavaFX:** לממשק המשתמש והגרפיקה.
* **Gensim:** לעבודה עם מודלים של NLP.
* **PCA (Principal Component Analysis):** להפחתת ממדים מ-50+ ל-2 או 3 צירים.
* **Vector Math:** פיתוח עצמאי של חישובי מרחק אוקלידי ומרחק קוסינוס.

---

## 📝 סיכום הפרויקט
הפרויקט נבנה כחלק ממחקר על הדרך בה מחשבים "מבינים" משמעות של מילים וכיצד ניתן להנגיש מידע מורכב זה בצורה ויזואלית ואינטואיטיבית.
