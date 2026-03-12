package app;

import model.WordVector;
import math.ProjectionStrategy;
import java.util.List;

public class ExplorerHelper {
    /**
     * מחשבת את המיקום המדויק בפיקסלים (X, Y) של וקטור נתון על גבי המסך.
     * הפונקציה מבצעת 3 שלבים: הטלה (Projection), נרמול (Normalization) והתאמה למסך (Scaling).
     *
     * @param vector       הווקטור המקורי (רב-מימדי)
     * @param projStrt     אסטרטגיית ההטלה (למשל Linear2D)
     * @param axisIndices  אינדקסים של הצירים הנבחרים (למשל {0, 1})
     * @param minVals      ערכי המינימום של הצירים (לצורך נרמול)
     * @param maxVals      ערכי המקסימום של הצירים (לצורך נרמול)
     * @param width        רוחב הקנבס בפיקסלים
     * @param height       גובה הקנבס בפיקסלים
     * @return             מערך בגודל 2: [0] הוא X במסך, [1] הוא Y במסך
     */

    /**
     * פונקציית עזר שפשוט משתמשת ב-ProjectionStrategy הקיימת שלך.
     * אנחנו צריכים אותה כדי שגם ה-Renderer וגם ה-findWordAt ישתמשו באותה לוגיקה בדיוק.
     */
    public static double[] calculateScreenPosition(
            WordVector wv,
            ProjectionStrategy projStrt,
            int[] axisIndices,
            double canvasWidth, double canvasHeight,
            double[] minVals, double[] maxVals
    ) {
        // הכנת מערך ה-dimensions כפי שהממשק שלך מצפה (רוחב וגובה)
        double[] dimensions = { canvasWidth, canvasHeight };

        // קריאה למתודה המקורית שלך
        return projStrt.project(wv, axisIndices, dimensions, minVals, maxVals);
    }


    /**
     * פונקציית מציאת מילה לפי לחיצה
     */
    public static String findWordAt(
            double mouseX, double mouseY,
            List<WordVector> words,
            ProjectionStrategy projStrt,
            int[] axisIndices,
            double canvasWidth, double canvasHeight,
            double[] minVals, double[] maxVals
    ) {
        double threshold = 10.0; // רדיוס לחיצה בפיקסלים

        for (WordVector wv : words) {
            // שימוש בחישוב המקורי שלך!
            double[] screenPos = calculateScreenPosition(wv, projStrt, axisIndices, canvasWidth, canvasHeight, minVals, maxVals);

            // חישוב מרחק מהעכבר
            double dist = Math.sqrt(Math.pow(screenPos[0] - mouseX, 2) + Math.pow(screenPos[1] - mouseY, 2));

            if (dist < threshold) {
                return wv.getWord();
            }
        }
        return null;
    }
}

