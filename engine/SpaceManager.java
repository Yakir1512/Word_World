package engine;
import model.WordVector;
import math.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.BackupVec;
import model.Match;

public class SpaceManager {
    // מילון שמחזיק את כל המילים לגישה מהירה
    private Map<String, WordVector> vocabulary = new HashMap<>();
    // רשימה מסודרת לציור
    private List<WordVector> wordList = new ArrayList<>(); 
    // מחזיק את המנוע כרכיב עזר
    private SemanticSearcher searcher; 
    //שמירת הווקטורים שכבר עשינו עליהם הטלה בשלושה מימדים
    private Map<String, double[]> projectedCache = new HashMap<>();

////////////////////////////////////////////////////////////////


    public void initializeSpace() throws IOException, InterruptedException {
        // 1. קודם כל מפעילים את הפייתון שייצר את הקבצים
        generateDataWithPython(); 
        
        // 2. רק אחרי שהפייתון סיים (waitFor), אנחנו טוענים את התוצאה
        // שים לב: כאן אנחנו שולחים את שמות קבצי ה-JSON, לא את נתיב הפייתון!
        this.vocabulary = DataLoader.loadFromFiles("full_vectors.json", "pca_vectors.json");
        
        this.wordList = new ArrayList<>(vocabulary.values());
        // 3. מאתחלים את מנוע החיפוש עם הנתונים הטריים
        this.searcher = new SemanticSearcher(vocabulary.values(), new EuclideanDistance());
        
        System.out.println("Space is ready with " + vocabulary.size() + " words!");
    }
    ////////////////////////////////////////////////////////////////////


    //  הרצת סקריפט הפייתון
    public void generateDataWithPython() throws IOException, InterruptedException {
        System.out.println("Starting Python script...");
        //
        String pythonExe = "C:\\Users\\IMOE001\\AppData\\Local\\Programs\\Python\\Python314\\python.exe";
        String scriptPath = "C:\\Users\\IMOE001\\Documents\\VsCode\\embedder.py";
        //direct excecute of the script
        ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath);

        pb.inheritIO(); // כדי לראות את ההדפסות של הפייתון בקונסולה של הג'אווה
        Process p = pb.start();
        
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python script failed with error code: " + exitCode);
        }
        System.out.println("Python script finished successfully.");
    }
///////////////////////////////////////////////////////////
 


    public List<WordVector> getWordList() { return wordList; }
///////////////////////////////////////////////////////////
    public List<WordVector> getBackupWordList() { return new ArrayList<>(vocabulary.values()); }
    // פונקציה לחיפוש (לפי דרישות הליבה)
    public WordVector getWord(String w) { return vocabulary.get(w); }
///////////////////////////////////////////////////////////



    //set (again)the serch type
    public void setMetric(DistanceMetric metric) {
        if (searcher != null){
            searcher.setMetric(metric);
        }
    }
///////////////////////////////////////////////////////////


   public List<Match> getNeighbors(String queryWord, int k) {
    // 1. קבלה ותרגום ראשוני (מילה -> וקטור)
    WordVector vector = vocabulary.get(queryWord); // נניח שיש גישה למילון
    
    if (vector == null) {
        return new ArrayList<>(); // רשימה ריקה אם לא נמצא
    }

    // 2. האצלת סמכויות למנוע החיפוש
    // המנהל פשוט מעביר את הבקשה הלאה ומחזיר את התשובה כמו שהיא
    return searcher.findNearest(vector.getFullVector(), k);
}
///////////////////////////////////////////////////////////



    public String solveAnalogy(String w1, String w2, String w3) {
        WordVector v1 = vocabulary.get(w1); // מילה 1
        WordVector v2 = vocabulary.get(w2); // מילה2 
        WordVector v3 = vocabulary.get(w3); // מילה 3

        if (v1 == null || v2 == null || v3 == null) return "Word not found";

        // 1. חישוב מתמטי נקי (VectorMath)
        // תוצאה = מילה1 - מילה 2
        double[] step1 = VectorMath.subtract(v1.getFullVector(), v2.getFullVector());
        // תוצאה2 = תוצאה + מילה 3
        double[] target = VectorMath.add(step1, v3.getFullVector());

        List<Match> results = searcher.findNearest(target, 5);
        // 2. חיפוש המילים המקושרות לווקטור(Searcher)
        for (Match m : results) {
        String candidate = m.getWord(); // כאן אנחנו מחלצים את ה-String!
        
        // מוודאים שזו לא אחת המילים מהשאלה
        if (!candidate.equalsIgnoreCase(w1) && 
            !candidate.equalsIgnoreCase(w2) && 
            !candidate.equalsIgnoreCase(w3)) {
            return candidate; // מחזירים String
        }
    }

    return "None";
    }
////////////////////////////////////////////////////////////





    /**
     * פונקציית עזר למציאת הגבולות של ציר מסוים.
     * @param axisIndex - איזה רכיב PCA אנחנו בודקים (0, 1, 2...)
     * @return מערך של שני מספרים: [מינימום, מקסימום]
     */
    public double[] getAxisRange(int axisIndex) {
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;

    for (WordVector wv : wordList) { // עוברים על כל המילים
        double val = wv.getPcaCoordinate(axisIndex);
        
        if (val < min) min = val; // מצאנו שיא נמוך חדש
        if (val > max) max = val; // מצאנו שיא גבוה חדש
    }

    // מקרה קצה: אם הרשימה ריקה, נחזיר ברירת מחדל כדי לא לקרוס
    if (min == Double.MAX_VALUE) return new double[]{-1.0, 1.0};
    
    // מוסיפים קצת "אוויר" (Padding) כדי שהנקודות לא יידבקו לקצה המסך
    double padding = (max - min) * 0.05; 
    return new double[]{min - padding, max + padding};
    }
////////////////////////////////////////////////////////////



public void changeMetric(DistanceMetric newMetric) {
    if (searcher != null) {
        searcher.setMetric(newMetric);
    }
}
/////////////////////////////////////////////////////////////


// בתוך SpaceManager
// בתוך SpaceManager.java

public void ensureDataReady() throws IOException, InterruptedException {
    File f = new File("pca_vectors.json");
    boolean loadSuccess = false; // דגל למעקב

    try {
        if (f.exists() && f.length() > 0) {
            System.out.println("Files found! Loading directly...");
            this.vocabulary = DataLoader.loadFromFiles("full_vectors.json", "pca_vectors.json");
            loadSuccess = true;
        } else {
            System.out.println("Files missing. Running Python...");
            initializeSpace(); // מנסה להריץ פייתון ולטעון
            loadSuccess = true;
        }
    } catch (Exception e) {
        // --- תרחיש כישלון: הפעלת נוהל חירום ---
        System.err.println("Real data loading failed: " + e.getMessage());
        System.err.println("Activating BackupVec protocol...");

        // יצירת נתוני דמה (משתמש בבנאי השני של WordVector)
        List<model.WordVector> mockList = BackupVec.generate(150, 50);
        
        this.vocabulary = new java.util.HashMap<>();
        for (model.WordVector wv : mockList) {
            this.vocabulary.put(wv.getWord(), wv);
        }
        loadSuccess = true;
    } finally {
        // --- הנקודה הקריטית: סנכרון הציור ---
        // הבלוק הזה ירוץ תמיד, ויוודא שה-Renderer מקבל את מה שיש ב-vocabulary
        // (בין אם זה אמיתי ובין אם זה דמה)
        if (this.vocabulary != null && !this.vocabulary.isEmpty()) {
            this.wordList = new ArrayList<>(this.vocabulary.values());
            
            // אתחול מנוע החיפוש
            this.searcher = new SemanticSearcher(
                this.vocabulary.values(), 
                new math.EuclideanDistance()
            );
            
            System.out.println("Data ready. Total words: " + wordList.size());
        }
    }
}
/////////////////////////////////////////////////////////////////


// בתוך SpaceManager.java
public List<String> searchForWord(String queryWord) {
    // 1. בדיקה האם המילה קיימת במילון
    // נניח שיש לך מפה (Map) או דרך לשלוף WordVector לפי שם
    WordVector target = vocabulary.get(queryWord.toLowerCase().trim());
    
    if (target == null) {
        List<String> err = new ArrayList<>();
        err.add("Word not found: " + queryWord);
        return err;
    }

    // 2. הפעלת המנוע (Searcher)
    // שים לב: אנחנו שולחים את הוקטור של המילה שמצאנו
    List<Match> matches = searcher.findNearest(target.getFullVector(), 10);

    // 3. פירמוט התוצאה לתצוגה (כאן המנהל מחליט איך הטקסט ייראה)
    List<String> resultStrings = new ArrayList<>();
    for (Match m : matches) {
        // פורמט יפה: "Word      (0.123)"
        resultStrings.add(String.format("%-15s (%.3f)", m.word, m.distance));
    }
    return resultStrings;
}
}