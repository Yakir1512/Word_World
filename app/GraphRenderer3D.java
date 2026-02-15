package app;

import model.*;
import math.ProjectionStrategy;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GraphRenderer3D {

    private Canvas canvas;
    private GraphicsContext gc;
    // --- משתני ייעול (Cache) ---
    private java.util.Map<String, double[]> projectionCache = new java.util.HashMap<>();
    private boolean needsReprojection = true; // דלוק כברירת מחדל כדי שיחשב בפעם הראשונה

    // משתנים לשמירת המצב (מי מודגש כרגע)
    private String selectedWord;
    private List<String> neighborWords = new ArrayList<>();

    public GraphRenderer3D(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    /**
     * עדכון נתוני ההדגשה (בדיוק כמו ב-2D)
     */
    public void setHighlight(String selectedWord, List<Match> neighbors) {
        this.selectedWord = selectedWord;
        if (neighbors != null) {
            this.neighborWords = neighbors.stream()
                                          .map(Match::getWord)
                                          .collect(Collectors.toList());
        } else {
            this.neighborWords.clear();
        }
    }

    /**
     * פונקציית הציור הראשית לתלת-ממד
     */



    public void render(List<WordVector> words, 
                       ProjectionStrategy proj, 
                       int[] axisIndices, 
                       double[] minVals, double[] maxVals,
                       double angleX, double angleY, 
                       double scale) { 
        
        // 1. ניקוי המסך
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

        // =======================================================
        // שלב א': עדכון המטמון (Batch Update) - מחוץ ללולאת הציור!
        // =======================================================
        if (needsReprojection) {
            projectionCache.clear(); // מנקים את הישן
            for (WordVector wv : words) {
                // מחשבים ושומרים
                double[] worldPos = proj.project(wv, axisIndices, new double[]{width, height, width}, minVals, maxVals);
                projectionCache.put(wv.getWord(), worldPos);
            }
            // מכבים את הדגל מיד אחרי העדכון
            setNeedsReprojection(false);  
            System.out.println("NeedsReprojection = false");
        }
        // =======================================================

        List<RenderPoint> pointsToDraw = new ArrayList<>();

        // =======================================================
        // שלב ב': לולאת הציור (Fast Path בלבד, ללא תנאים מיותרים)
        // =======================================================
        for (WordVector wv : words) {
            // שליפה ישירה מהמטמון (אנחנו בטוחים שהוא מעודכן)
            double[] worldPos = projectionCache.get(wv.getWord());
            
            // חישוב קליל של המצלמה (סיבוב וזום)
            double[] screenPos = ExplorerHelper3D.projectToScreen(worldPos, centerX, centerY, angleX, angleY, scale);
            
            Color color = Color.GRAY;
            boolean isHighlighted = false;

            if (selectedWord != null && wv.getWord().equals(selectedWord)) {
                color = Color.RED;
                isHighlighted = true;
            } else if (neighborWords.contains(wv.getWord())) {
                color = Color.ORANGE;
                isHighlighted = true;
            }

            pointsToDraw.add(new RenderPoint(screenPos[0], screenPos[1], screenPos[2], wv.getWord(), color, isHighlighted));
        }

        // שלב ג': מיון וציור
        Collections.sort(pointsToDraw);

        for (RenderPoint p : pointsToDraw) {
            drawPoint(p);
        }
    }


    private void drawPoint(RenderPoint p) {
        // שינוי גודל העיגול לפי המרחק (Scale)
        double baseSize = 4.0;
        double radius = baseSize * p.scale;

        // הדגשה נוספת לנקודות נבחרות
        if (p.isHighlighted) {
            radius *= 1.5; 
            // אפקט זוהר קטן מסביב (אופציונלי)
            gc.setGlobalAlpha(0.3);
            gc.setFill(p.color);
            gc.fillOval(p.x - radius * 1.5, p.y - radius * 1.5, radius * 3, radius * 3);
            gc.setGlobalAlpha(1.0);
        }

        gc.setFill(p.color);
        gc.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

        // טקסט מצויר רק אם הנקודה מודגשת או קרובה מאוד למצלמה
        if (p.isHighlighted || p.scale > 0.8) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(10 * p.scale)); // גם הטקסט משנה גודל!
            gc.fillText(p.word, p.x + radius + 2, p.y);
        }
    }

    // --- מחלקה פנימית למיון (Helper Class) ---
    private static class RenderPoint implements Comparable<RenderPoint> {
        double x, y, scale;
        String word;
        Color color;
        boolean isHighlighted;

        public RenderPoint(double x, double y, double scale, String word, Color color, boolean isHighlighted) {
            this.x = x;
            this.y = y;
            this.scale = scale; // scale קטן = רחוק, scale גדול = קרוב
            this.word = word;
            this.color = color;
            this.isHighlighted = isHighlighted;
        }

        @Override
        public int compareTo(RenderPoint other) {
            // מיון עולה: מהקטן לגדול.
            // אנחנו רוצים לצייר את הקטנים (הרחוקים) קודם, כדי שהגדולים יסתירו אותם.
            return Double.compare(this.scale, other.scale);
        }
    }

        public void setNeedsReprojection(boolean changeTo){
            this.needsReprojection=changeTo;
        }
}