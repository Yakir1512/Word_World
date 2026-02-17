package math;

import model.WordVector;
//פה מיושמת תבנית האסטרטגיה שביקש לממש ProjectionStrategy
public class Linear2DProjection implements ProjectionStrategy {

    @Override
    public double[] project(WordVector wv, int[] axisIndices, double[] dimensions, double[] minVal, double[] maxVal) {
        // שולפים את הערכים הגולמיים מהווקטור (למשל PC1 ו-PC2)
        double rawX = wv.getPcaCoordinate(axisIndices[0]);
        double rawY = wv.getPcaCoordinate(axisIndices[1]);
        
        double width = dimensions[0];
        double height = dimensions[1];

        //Padding
        double padding = 40.0; 
        double usableWidth = width - (2 * padding);
        double usableHeight = height - (2 * padding);

        // --- נוסחת הנרמול (Normalization Logic) ---
        // הופך את הערך למספר בין 0 ל-1
        double normalizedX = (rawX - minVal[0]) / (maxVal[0] - minVal[0]);
        double normalizedY = (rawY - minVal[1]) / (maxVal[1] - minVal[1]);

        // מותח את זה לגודל המסך
        double screenX = padding + (normalizedX * usableWidth);
        double screenY = padding + (normalizedY * usableHeight);

        return new double[] { screenX, screenY };
    }
}