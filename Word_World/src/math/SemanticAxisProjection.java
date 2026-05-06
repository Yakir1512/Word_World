package math;

import model.WordVector;

/**
 * הטלה על ציר סמנטי מותאם אישית.
 *
 * כשמשתמש בוחר מילה A ומילה B, כל מילה אחרת מוקרנת על הציר שמחבר אותן.
 * ציר X = מיקום על הציר הסמנטי (t ∈ [0,1] כאשר A=0, B=1)
 * ציר Y = ריווח אנכי ע"ב ה-PCA (כדי שלא הכל יהיה בקו אחד)
 *
 * הבעיה הקודמת:
 *   worldY חושב ע"ב hash → ערכים קטנים מאוד ← כל הנקודות בפס צר
 *   minVals/maxVals לא מכסים את טווח הציר הסמנטי ← נקודות יוצאות מהמסך
 *
 * הפתרון:
 *   1. X = הטלה נרמלת לטווח [0,1] של הציר הסמנטי
 *   2. Y = קואורדינטת PCA שנייה (orthogonal לציר) לפיזור ויזואלי אמיתי
 *   3. minVals/maxVals מחושבים מ-all words בעת יצירת האובייקט
 */
public class SemanticAxisProjection implements ProjectionStrategy {

    private final double[] startVec;   // וקטור מילה A
    private final double[] axisVec;    // B - A (כיוון הציר)
    private final double axisLenSq;    // |B-A|²

    // טווחי worldX ו-worldY — מחושבים מראש על כל המילים
    private double worldXMin, worldXMax;
    private double worldYMin, worldYMax;

    // ציר PCA לשימוש כ-Y (ריווח אורתוגונלי)
    private final int yPcaAxis;

    /**
     * @param wordA     מילת ההתחלה
     * @param wordB     מילת הסוף
     * @param allWords  כל המילים (לחישוב טווחים)
     * @param yPcaAxis  אינדקס ציר PCA לשימוש כ-Y (בד"כ 2 או 3)
     */
    public SemanticAxisProjection(WordVector wordA, WordVector wordB,
                                  java.util.List<WordVector> allWords, int yPcaAxis) {
        this.startVec = wordA.getFullVector();
        double[] endVec = wordB.getFullVector();
        this.yPcaAxis = yPcaAxis;

        // חישוב וקטור הציר
        this.axisVec = new double[startVec.length];
        double sumSq = 0;
        for (int i = 0; i < startVec.length; i++) {
            this.axisVec[i] = endVec[i] - startVec[i];
            sumSq += axisVec[i] * axisVec[i];
        }
        this.axisLenSq = (sumSq < 1e-9) ? 1.0 : sumSq;

        // מעבר על כל המילים לחישוב הטווחים
        computeRanges(allWords);
    }

    /** Constructor נוח ללא ציר Y מפורש (משתמש בציר PCA 2) */
    public SemanticAxisProjection(WordVector wordA, WordVector wordB,
                                  java.util.List<WordVector> allWords) {
        this(wordA, wordB, allWords, 2);
    }

    /** Constructor לאחורה-תואם (ללא allWords — פחות מדויק) */
    public SemanticAxisProjection(WordVector wordA, WordVector wordB) {
        this.startVec = wordA.getFullVector();
        double[] endVec = wordB.getFullVector();
        this.yPcaAxis = 2;

        this.axisVec = new double[startVec.length];
        double sumSq = 0;
        for (int i = 0; i < startVec.length; i++) {
            this.axisVec[i] = endVec[i] - startVec[i];
            sumSq += axisVec[i] * axisVec[i];
        }
        this.axisLenSq = (sumSq < 1e-9) ? 1.0 : sumSq;

        // ברירת מחדל — טווחים רחבים
        this.worldXMin = -0.5; this.worldXMax = 1.5;
        this.worldYMin = -3.0; this.worldYMax = 3.0;
    }

    private void computeRanges(java.util.List<WordVector> allWords) {
        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;

        for (WordVector wv : allWords) {
            double t = projectOnAxis(wv.getFullVector());
            double y = wv.getPcaCoordinate(yPcaAxis);
            if (t < xMin) xMin = t;
            if (t > xMax) xMax = t;
            if (y < yMin) yMin = y;
            if (y > yMax) yMax = y;
        }

        // padding 10%
        double xPad = (xMax - xMin) * 0.10;
        double yPad = (yMax - yMin) * 0.10;
        this.worldXMin = xMin - xPad;
        this.worldXMax = xMax + xPad;
        this.worldYMin = yMin - yPad;
        this.worldYMax = yMax + yPad;
    }

    /** מחשב את t — מיקום הווקטור על ציר A→B */
    private double projectOnAxis(double[] vec) {
        double dot = 0;
        for (int i = 0; i < vec.length; i++) {
            dot += (vec[i] - startVec[i]) * axisVec[i];
        }
        return dot / axisLenSq;
    }

    @Override
    public double[] project(WordVector wv, int[] axisIndices,
                            double[] dims, double[] minVals, double[] maxVals) {
        double screenW = dims[0];
        double screenH = dims[1];
        double padding = 50.0;

        // X = הטלה על הציר הסמנטי, נרמל לטווח [worldXMin, worldXMax]
        double worldX = projectOnAxis(wv.getFullVector());

        // Y = קואורדינטת PCA אורתוגונלית (ריווח ויזואלי אמיתי)
        double worldY = wv.getPcaCoordinate(yPcaAxis);

        double rangeX = worldXMax - worldXMin;
        double rangeY = worldYMax - worldYMin;
        if (Math.abs(rangeX) < 1e-9) rangeX = 1.0;
        if (Math.abs(rangeY) < 1e-9) rangeY = 1.0;

        double usableW = screenW - 2 * padding;
        double usableH = screenH - 2 * padding;

        double screenX = padding + ((worldX - worldXMin) / rangeX) * usableW;
        // Y הפוך — גדול = למעלה
        double screenY = padding + ((worldYMax - worldY) / rangeY) * usableH;

        return new double[]{screenX, screenY, 0};
    }

    // Getters לשרטוט תוויות על הציר
    public double getStartProjection() { return projectOnAxis(startVec); }
    public double getEndProjection()   {
        double[] endVec = new double[startVec.length];
        for (int i = 0; i < endVec.length; i++) endVec[i] = startVec[i] + axisVec[i];
        return projectOnAxis(endVec);
    }
}
