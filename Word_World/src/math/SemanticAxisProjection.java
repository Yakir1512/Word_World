package math;

import model.WordVector;

public class SemanticAxisProjection implements ProjectionStrategy {

    private final double[] startVec; 
    private final double[] axisVec;  
    private final double axisLenSq;  

    public SemanticAxisProjection(WordVector wordA, WordVector wordB) {
        this.startVec = wordA.getVector();
        double[] endVec = wordB.getVector();
        
        this.axisVec = new double[startVec.length];
        double sumSq = 0;
        for (int i = 0; i < startVec.length; i++) {
            this.axisVec[i] = endVec[i] - startVec[i];
            sumSq += axisVec[i] * axisVec[i];
        }
        
        this.axisLenSq = (sumSq < 1e-9) ? 1.0 : sumSq;
    }

    @Override
    public double[] project(WordVector wv, int[] axisIndices, double[] dims, double[] minVals, double[] maxVals) {
        // 1. חישוב המיקום האבסטרקטי בעולם (בין 0 ל-1, או פחות/יותר אם המילה קיצונית)
        double[] currentVec = wv.getVector();
        double dotProduct = 0;

        for (int i = 0; i < currentVec.length; i++) {
            dotProduct += (currentVec[i] - startVec[i]) * axisVec[i];
        }

        // t הוא קואורדינטת ה-X שלנו בעולם האמיתי
        double worldX = dotProduct / axisLenSq; 
        
        // 2. חישוב גובה דטרמיניסטי (כדי שלא הכל יהיה שטוח, אבל שיישאר קבוע)
        // אנחנו מנרמלים את זה לערכים קטנים (למשל בין -0.5 ל 0.5) כדי שיתאים לסקאלת הזום
        int hash = wv.getWord().hashCode(); 
        double worldY = (hash % 1000) / 2000.0; // ערך בין -0.5 ל-0.5
        
        // 3. המרה לפיקסלים בהתבסס על המצלמה (minVals/maxVals)
        // זה החלק שהופך את הגרירה והזום לפעילים!
        
        double screenWidth = dims[0];
        double screenHeight = dims[1];

        // נוסחת המיפוי הסטנדרטית: (ערך - מינימום) / הטווח * גודל_מסך
        double rangeX = maxVals[0] - minVals[0];
        double rangeY = maxVals[1] - minVals[1];

        // הגנה מחלוקה באפס (למקרה של זום אינסופי)
        if (Math.abs(rangeX) < 1e-9) rangeX = 1.0;
        if (Math.abs(rangeY) < 1e-9) rangeY = 1.0;

        double screenX = ((worldX - minVals[0]) / rangeX) * screenWidth;
        
        // ב-JavaFX ציר ה-Y הפוך (0 למעלה), אז לפעמים צריך להפוך, אבל לנוסחה הזו:
        // נשתמש במיפוי רגיל כדי שהגרירה תעבוד אינטואיטיבית
        double screenY = ((worldY - minVals[1]) / rangeY) * screenHeight;

        return new double[]{screenX, screenY, 0}; 
    }
}