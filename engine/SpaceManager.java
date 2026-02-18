package engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.BackupVec;
import math.DistanceMetric;
import math.EuclideanDistance;
import engine.SemanticSearcher;
import math.VectorArithmetic; // השימוש במחלקה החיצונית
import math.VectorMath;
import model.EquationResult; // שימוש באובייקט התוצאה
import model.Match;
import model.WordVector;

public class SpaceManager {
    
    // מילון שמחזיק את כל המילים לגישה מהירה (O(1))
    private Map<String, WordVector> vocabulary = new HashMap<>();
    
    // רשימה מסודרת לציור ולמעבר סדרתי
    private List<WordVector> wordList = new ArrayList<>(); 
    
    // מחזיק את המנוע כרכיב עזר לחיפושים מהירים
    private SemanticSearcher searcher; 
    
    // המטריקה הנוכחית לחישוב מרחקים (Euclidean / Cosine)
    private DistanceMetric metric; 

    public SpaceManager() {
        // ברירת מחדל: מרחק אוקלידי
        this.metric = new EuclideanDistance();
    }

    // ============================================================
    // 1. טעינת נתונים ואתחול (Initialization)
    // ============================================================

    public void ensureDataReady() throws IOException, InterruptedException {
        File f = new File("pca_vectors.json");
        
        try {
            if (f.exists() && f.length() > 0) {
                System.out.println("Files found! Loading directly...");
                loadData();
            } else {
                System.out.println("Files missing. Running Python...");
                initializeSpace(); // מריץ פייתון וטוען
            }
        } catch (Exception e) {
            // --- נוהל חירום: טעינת נתוני דמה ---
            System.err.println("Real data loading failed: " + e.getMessage());
            System.err.println("Activating BackupVec protocol...");
            
            List<model.WordVector> mockList = BackupVec.generate(150, 50);
            updateVocabulary(mockList);
        }
    }

    public void initializeSpace() throws IOException, InterruptedException {
        generateDataWithPython(); 
        loadData();
        System.out.println("Space is ready with " + vocabulary.size() + " words!");
    }

    private void loadData() throws IOException {
        Map<String, WordVector> loadedData = DataLoader.loadFromFiles("full_vectors.json", "pca_vectors.json");
        updateVocabulary(new ArrayList<>(loadedData.values()));
    }

    private void updateVocabulary(List<WordVector> newList) {
        this.vocabulary.clear();
        this.wordList.clear();
        
        for (WordVector wv : newList) {
            this.vocabulary.put(wv.getWord(), wv); // חשוב: מיפוי לפי שם המילה
            this.wordList.add(wv);
        }
        
        // אתחול מחדש של המנוע עם הנתונים והמטריקה הנוכחית
        this.searcher = new SemanticSearcher(this.wordList, this.metric);
    }

    public void generateDataWithPython() throws IOException, InterruptedException {
        System.out.println("Starting Python script...");
        String workingDir = System.getProperty("user.dir");
        File scriptFile = new File(workingDir, "embedder.py");
        
        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Python script not found at: " + scriptFile.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder("python", scriptFile.getAbsolutePath());
        pb.inheritIO();
        
        try {
            if (pb.start().waitFor() != 0) throw new RuntimeException("Python script failed.");
        } catch (IOException e) {
            // ניסיון גיבוי עם python3
            pb.command("python3", scriptFile.getAbsolutePath());
            if (pb.start().waitFor() != 0) throw new RuntimeException("Python script failed (fallback).");
        }
        System.out.println("Python script finished successfully.");
    }

    // ============================================================
    // 2. גישה לנתונים (Getters & Lookups)
    // ============================================================

    public List<WordVector> getWordList() { return wordList; }

    /**
     * שליפת וקטור לפי מילה בצורה יעילה (O(1)).
     * מטפל גם באותיות גדולות/קטנות.
     */
    public WordVector getWordVector(String word) {
        if (word == null) return null;
        // מנסים בדיוק
        WordVector wv = vocabulary.get(word);
        if (wv != null) return wv;
        
        // מנסים באותיות קטנות (למקרה שהמפתח נשמר אחרת)
        return vocabulary.get(word.toLowerCase()); 
    }

    public double[] getAxisRange(int axisIndex) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (WordVector wv : wordList) {
            double val = wv.getPcaCoordinate(axisIndex);
            if (val < min) min = val;
            if (val > max) max = val;
        }

        if (min == Double.MAX_VALUE) return new double[]{-1.0, 1.0};
        double padding = (max - min) * 0.05; 
        return new double[]{min - padding, max + padding};
    }

    // ============================================================
    // 3. לוגיקה וחיפוש (Search & Logic)
    // ============================================================

    /**
     * החלפת שיטת חישוב המרחק (Strategy Pattern).
     */
    public void changeMetric(DistanceMetric newMetric) {
        this.metric = newMetric;
        if (searcher != null) {
            searcher.setMetric(newMetric);
        }
    }

    /**
     * גרסה 1: מציאת שכנים למילה קיימת.
     */
    public List<Match> getNeighbors(String queryWord, int k) {
        WordVector vector = getWordVector(queryWord);
        if (vector == null) return new ArrayList<>();
        
        // שימוש במנוע החיפוש היעיל
        return searcher.findNearest(vector.getFullVector(), k);
    }

    /**
     * גרסה 2: מציאת שכנים לווקטור גולמי (למשל Centroid).
     * מממש את הלוגיקה ישירות מול ה-Metric כדי לתמוך בנקודות שרירותיות בחלל.
     */
    public List<Match> getNeighbors(double[] targetVec, int k) {
        List<Match> matches = new ArrayList<>();
        
        for (WordVector wv : wordList) {
            // שימוש במטריקה הנוכחית לחישוב המרחק
            double dist = metric.calculate(targetVec, wv.getVector());
            matches.add(new Match(wv.getWord(), dist));
        }
        
        // מיון לפי מרחק (מהקטן לגדול)
        matches.sort((m1, m2) -> Double.compare(m1.distance, m2.distance));
        
        // החזרת K הראשונים
        if (k > matches.size()) k = matches.size();
        return matches.subList(0, k);
    }

    /**
     * פתרון משוואה וקטורית.
     * שימוש ב-Delegation למחלקה VectorArithmetic.
     */
    public EquationResult solveEquation(String equation) {
        // SpaceManager רק מספק את רשימת המילים, הלוגיקה בחוץ!
        return VectorArithmetic.solve(equation, this.wordList);
    }

    /**
     * פתרון אנלוגיה קלאסית (King - Man + Woman).
     * נשמר לאחור, משתמש במנוע החיפוש.
     */
    public String solveAnalogy(String w1, String w2, String w3) {
        WordVector v1 = getWordVector(w1);
        WordVector v2 = getWordVector(w2);
        WordVector v3 = getWordVector(w3);

        if (v1 == null || v2 == null || v3 == null) return "Word not found";

        double[] step1 = VectorMath.subtract(v1.getFullVector(), v2.getFullVector());
        double[] target = VectorMath.add(step1, v3.getFullVector());

        List<Match> results = searcher.findNearest(target, 5);
        
        for (Match m : results) {
            String candidate = m.getWord();
            if (!candidate.equalsIgnoreCase(w1) && 
                !candidate.equalsIgnoreCase(w2) && 
                !candidate.equalsIgnoreCase(w3)) {
                return candidate;
            }
        }
        return "None";
    }
}