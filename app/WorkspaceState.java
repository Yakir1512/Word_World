package app;

import java.util.ArrayList;
import java.util.List;
import math.Linear2DProjection;
import math.Perspective3DProjection;
import math.ProjectionStrategy;
import model.EquationResult;

/**
 * מחלקה זו משמשת כ"זיכרון" (State) של האפליקציה.
 * היא מאגדת את כל הנתונים המשתנים שמגדירים את מה שמוצג למשתמש,
 * כגון מצב תצוגה, צירים פעילים, ובחירות של המשתמש.
 */
public class WorkspaceState {

    // =======================================================
    // 1. הגדרות תצוגה כלליות (View Settings)
    // =======================================================
    private boolean is3DMode = false;
    private int[] axisIndices = {0, 1}; // ב-3D זה יהיה מערך של 3 איברים
    private double[] minVals = {-1.0, -1.0};
    private double[] maxVals = {1.0, 1.0};
    private boolean axisChanged = true;
    private double textDensity = 9.0; // ערך ה-Slider לצפיפות טקסט

    // =======================================================
    // 2. אסטרטגיות הטלה (Projection Strategies)
    // =======================================================
    // אנחנו שומרים את האסטרטגיות כאן כדי שהבקר יידע במה להשתמש
    private ProjectionStrategy projStrt2D = new Linear2DProjection();
    private ProjectionStrategy projStrt3D = new Perspective3DProjection();

    // =======================================================
    // 3. מצב בחירה (Selection State)
    // =======================================================
    
    // מצב 1: בחירה רגילה (קליק בודד)
    private String selectedWord = null;
    private List<model.Match> currentNeighbors = new ArrayList<>();

    // מצב 2: תת-מרחב (Subspace - בחירה מרובה עם CTRL)
    private List<String> multiSelectedWords = new ArrayList<>();
    
    // מצב 3: משוואה וקטורית (Vector Arithmetic)
    private EquationResult currentEquation = null;


    // =======================================================
    // Getters & Setters
    // =======================================================

    // --- הגדרות תצוגה ---
    
    public boolean is3DMode() { return is3DMode; }
    public void set3DMode(boolean is3DMode) { this.is3DMode = is3DMode; }

    public int[] getAxisIndices() { return axisIndices; }
    public void setAxisIndices(int[] axisIndices) { this.axisIndices = axisIndices; }

    public double[] getMinVals() { return minVals; }
    public double[] getMaxVals() { return maxVals; }
    public void setBoundaries(double[] minVals, double[] maxVals) {
        this.minVals = minVals;
        this.maxVals = maxVals;
    }

    public boolean isAxisChanged() { return axisChanged; }
    public void setAxisChanged(boolean axisChanged) { this.axisChanged = axisChanged; }

    public double getTextDensity() { return textDensity; }
    public void setTextDensity(double textDensity) { this.textDensity = textDensity; }

    // --- אסטרטגיות הטלה ---

    public ProjectionStrategy getProjStrt2D() { return projStrt2D; }
    public void setProjStrt2D(ProjectionStrategy projStrt2D) { this.projStrt2D = projStrt2D; }

    public ProjectionStrategy getProjStrt3D() { return projStrt3D; }
    // לרוב לא נשנה את ה-3D, אבל נשמור Setter לאחידות
    public void setProjStrt3D(ProjectionStrategy projStrt3D) { this.projStrt3D = projStrt3D; }

    // --- מצב בחירה ---

    public String getSelectedWord() { return selectedWord; }
    public void setSelectedWord(String selectedWord) { this.selectedWord = selectedWord; }

    public List<model.Match> getCurrentNeighbors() { return currentNeighbors; }
    public void setCurrentNeighbors(List<model.Match> currentNeighbors) { 
        this.currentNeighbors = (currentNeighbors != null) ? currentNeighbors : new ArrayList<>(); 
    }

    public List<String> getMultiSelectedWords() { return multiSelectedWords; }
    public void setMultiSelectedWords(List<String> multiSelectedWords) { 
        this.multiSelectedWords = (multiSelectedWords != null) ? multiSelectedWords : new ArrayList<>(); 
    }
    
    public void addMultiSelectedWord(String word) {
        if (!this.multiSelectedWords.contains(word)) {
            this.multiSelectedWords.add(word);
        }
    }
    
    public void removeMultiSelectedWord(String word) {
        this.multiSelectedWords.remove(word);
    }

    public EquationResult getCurrentEquation() { return currentEquation; }
    public void setCurrentEquation(EquationResult currentEquation) { this.currentEquation = currentEquation; }

    // =======================================================
    // פעולות עזר (Helpers)
    // =======================================================

    /**
     * מאפס את המערכת למצב הדו-ממדי הסטנדרטי.
     * מנקה את כל הבחירות ומחזיר את אסטרטגיית ה-PCA הרגילה.
     */
    public void resetToStandardMode() {
        if (!(projStrt2D instanceof Linear2DProjection)) {
            this.projStrt2D = new Linear2DProjection();
        }
        clearAllSelections();
    }

    /**
     * מנקה את כל המידע הוויזואלי הזמני (בחירות, שכנים, משוואות).
     */
    public void clearAllSelections() {
        this.selectedWord = null;
        this.currentNeighbors.clear();
        this.multiSelectedWords.clear();
        this.currentEquation = null;
    }
}