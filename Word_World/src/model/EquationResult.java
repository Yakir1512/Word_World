package model;

import java.util.List;

/**
 * מחלקה המייצגת תוצאה של חישוב משוואה וקטורית.
 * היא מחזיקה את המסלול לציור ואת התוצאה הסופית.
 */
public class EquationResult {
    // רשימת המילים שמרכיבות את המשוואה (כולל התוצאה או שלבי הביניים)
    public List<String> pathWords; 
    
    // רשימת הפעולות: true = חיבור (ירוק), false = חיסור (אדום)
    public List<Boolean> operations; 
    
    // המילה שנמצאה כקרובה ביותר לתוצאה הווקטורית הסופית
    public String resultWord; 
    
    public EquationResult(List<String> pathWords, List<Boolean> operations, String resultWord) {
        this.pathWords = pathWords;
        this.operations = operations;
        this.resultWord = resultWord;
    }
}