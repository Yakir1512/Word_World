package model;
public class WordVector {
    private String word;
    private double[] fullVector; // 100 dimensions
    private double[] pcaVector;  // 50 dimensions
    

    public WordVector(String word, double[] fullVector, double[] pcaVector) {
        this.word = word;
        this.fullVector = fullVector;
        this.pcaVector = pcaVector;
    }


    public WordVector(String word, double[] simpleVector) {
        this.word = word;
        // במקרה דמה, אנחנו משתמשים באותו וקטור גם לחיפוש וגם לציור
        // זה מה שמונע את ה-NullPointerException!
        this.fullVector = simpleVector; 
        this.pcaVector = simpleVector;  
    }

    public String getWord() { return word; }
    
    public double[] getFullVector() { return fullVector; }

    // פונקציה לשליפת קואורדינטה ספציפית (עבור הויזואליזציה)
    public double getPcaCoordinate(int axis) {
        if (axis < 0 || axis >= pcaVector.length) {
            return 0.0; // Fallback
        }
        return pcaVector[axis];
    }

    public double[] getVector() {
        return fullVector;
    }

}
//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV