package math;

import model.WordVector;

public class Perspective3DProjection implements ProjectionStrategy {
    //פה מיושמת תבנית האסטרטגיה שביקש לממש ProjectionStrategy
    @Override
    public double[] project(WordVector wv, int[] axisIndices, double[] dimensions, double[] minVals, double[] maxVals) {
        // 1. שליפת שלושה צירים (נדרש מערך axisIndices באורך 3)
        double rawX = wv.getPcaCoordinate(axisIndices[0]);
        double rawY = wv.getPcaCoordinate(axisIndices[1]);
        double rawZ = wv.getPcaCoordinate(axisIndices[2]); 

        // 2. מידות המרחב הויזואלי
        double width = dimensions[0];
        double height = dimensions[1];
        double depth = dimensions[2]; 

        // 3. הגדרת Padding (מרווח בטיחות מהקצוות)
        double padding = 40.0; 
        double usableWidth = width - (2 * padding);
        double usableHeight = height - (2 * padding);
        double usableDepth = depth - (2 * padding);

        // 4. נרמול שלושת הצירים - כל אחד לפי הטווח הספציפי שלו
        // נוסחה: (ערך - מינימום) / (מקסימום - מינימום)
        double normX = (rawX - minVals[0]) / (maxVals[0] - minVals[0]);
        double normY = (rawY - minVals[1]) / (maxVals[1] - minVals[1]);
        double normZ = (rawZ - minVals[2]) / (maxVals[2] - minVals[2]);

        // 5. המרה לקואורדינטות "עולם" סופיות כולל ה-Padding
        double worldX = padding + (normX * usableWidth);
        double worldY = padding + (normY * usableHeight);
        double worldZ = padding + (normZ * usableDepth);

        return new double[] { worldX, worldY, worldZ };
    }
}