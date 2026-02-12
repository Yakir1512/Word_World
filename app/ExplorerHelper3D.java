package app;

import java.util.List;

public class ExplorerHelper3D {

    /**
     * הופכת קואורדינטות עולם תלת-ממדיות לנקודת מסך דו-ממדית.
     * הוספנו את הפרמטר scale כדי לתמוך בזום.
     */
    public static double[] projectToScreen(double[] worldCoords, double centerX, double centerY, double angleX, double angleY, double scale) {
        // 1. הזזה למרכז
        double x = worldCoords[0] - centerX;
        double y = worldCoords[1] - centerY;
        double z = worldCoords[2];

        // 2. סיבוב סביב ציר Y
        double cosY = Math.cos(angleY);
        double sinY = Math.sin(angleY);
        double xRotY = x * cosY + z * sinY;
        double zRotY = -x * sinY + z * cosY;

        // 3. סיבוב סביב ציר X
        double cosX = Math.cos(angleX);
        double sinX = Math.sin(angleX);
        double yRotX = y * cosX - zRotY * sinX;
        double zFinal = y * sinX + zRotY * cosX;

        // --- כאן נכנס הזום (Scale) ---
        // אנחנו מכפילים את המיקום במרחב לפני שמחשבים פרספקטיבה
        xRotY *= scale;
        yRotX *= scale;
        zFinal *= scale;

        // 4. חישוב פרספקטיבה (Perspective Divide)
        double fov = 600.0; 
        // מונע חלוקה באפס אם הנקודה קרובה מדי למצלמה
        double denominator = fov + zFinal;
        if (Math.abs(denominator) < 0.1) denominator = 0.1;

        double perspectiveScale = fov / denominator;

        double screenX = centerX + (xRotY * perspectiveScale);
        double screenY = centerY + (yRotX * perspectiveScale);

        // מחזירים: X, Y, וגודל הנקודה (המושפע גם מהמרחק וגם מהזום)
        return new double[] { screenX, screenY, perspectiveScale * scale };
    }


    public static String findWordAt3D(double mouseX, double mouseY, 
                                 List<model.WordVector> words, 
                                 math.ProjectionStrategy proj, 
                                 int[] axisIndices, 
                                 double width, double height, 
                                 double[] minVals, double[] maxVals,
                                 double angleX, double angleY, double scale) {
    
    String closestWord = null;
    double minDistance = 15.0; // רדיוס לחיצה בפיקסלים

    double centerX = width / 2;
    double centerY = height / 2;

    for (model.WordVector wv : words) {
        // 1. מקבלים מיקום תלת-ממדי גולמי
        double[] worldPos = proj.project(wv, axisIndices, new double[]{width, height, width}, minVals, maxVals);
        
        // 2. מטילים למסך לפי המבט הנוכחי (כולל Scale וסיבוב)
        double[] screenPos = projectToScreen(worldPos, centerX, centerY, angleX, angleY, scale);
        
        // 3. חישוב מרחק בין העכבר לנקודה המוטלת
        double dx = mouseX - screenPos[0];
        double dy = mouseY - screenPos[1];
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < minDistance) {
            minDistance = dist;
            closestWord = wv.getWord();
        }
    }
    return closestWord;
}
}