package math;

import java.util.ArrayList;
import java.util.List;
import model.EquationResult;
import model.WordVector;

public class VectorArithmetic {

    /**
     * פונקציה סטטית לחישוב וקטור ממוצע (Centroid) מתוך רשימת מילים.
     * מבצעת את כל העבודה המתמטית ה"שחורה".
     */
    public static double[] calculateCentroid(List<WordVector> vectors) {
        if (vectors == null || vectors.isEmpty()) return null;

        int dims = vectors.get(0).getVector().length;
        double[] centroid = new double[dims];

        // 1. סכימה של כל הווקטורים
        for (WordVector wv : vectors) {
            double[] v = wv.getVector();
            for (int i = 0; i < dims; i++) {
                centroid[i] += v[i];
            }
        }

        // 2. חלוקה במספר הווקטורים לקבלת הממוצע
        for (int i = 0; i < dims; i++) {
            centroid[i] /= vectors.size();
        }

        return centroid;
    }

    /**
     * פותרת משוואה וקטורית (מילה + מילה - מילה).
     */
    public static EquationResult solve(String equation, List<WordVector> wordList) {
        // ... (אותו קוד שכתבנו קודם לפרסור המשוואה) ...
        // כדי לחסוך מקום בתשובה, שמרתי על הלוגיקה הקודמת של solve כאן.
        // הקוד הזהה מופיע בתשובה הקודמת, רק תוודא שהוא נמצא כאן.
        
        // כאן אני אדגים רק את המבנה כדי לוודא שאתה מעתיק נכון:
        String[] parts = equation.split(" ");
        if (parts.length == 0) return null;
        
        WordVector first = findWord(parts[0], wordList);
        if (first == null) return null;
        
        double[] currentVec = first.getVector().clone();
        List<String> pathWords = new ArrayList<>();
        pathWords.add(first.getWord());
        List<Boolean> operations = new ArrayList<>();

        for (int i = 1; i < parts.length; i += 2) {
             // ... לוגיקת החיבור/חיסור (ראה תשובה קודמת) ...
             if (i + 1 >= parts.length) break;
             String op = parts[i];
             WordVector next = findWord(parts[i+1], wordList);
             if (next == null) continue;
             
             pathWords.add(next.getWord());
             if (op.equals("+")) {
                 operations.add(true);
                 for(int j=0; j<currentVec.length; j++) currentVec[j] += next.getVector()[j];
             } else if (op.equals("-")) {
                 operations.add(false);
                 for(int j=0; j<currentVec.length; j++) currentVec[j] -= next.getVector()[j];
             }
        }
        
        // מציאת השכן הקרוב ביותר לתוצאה
        String bestMatch = findNearestNeighbor(currentVec, wordList, pathWords);
        return new EquationResult(pathWords, operations, bestMatch);
    }

    // פונקציית עזר פנימית למציאת שכנים (מונעת שכפול קוד)
    private static String findNearestNeighbor(double[] targetVec, List<WordVector> wordList, List<String> exclude) {
        String bestMatch = null;
        double minDist = Double.MAX_VALUE;
        
        for (WordVector wv : wordList) {
            if (exclude != null && exclude.contains(wv.getWord())) continue;
            
            double dist = 0;
            for(int k=0; k<targetVec.length; k++) {
                double d = targetVec[k] - wv.getVector()[k];
                dist += d*d;
            }
            if (dist < minDist) {
                minDist = dist;
                bestMatch = wv.getWord();
            }
        }
        return bestMatch;
    }

    private static WordVector findWord(String word, List<WordVector> list) {
        for (WordVector wv : list) {
            if (wv.getWord().equalsIgnoreCase(word)) return wv;
        }
        return null;
    }
}