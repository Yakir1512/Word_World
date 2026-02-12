package engine;
import model.WordVector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataLoader {

    // מחלקות עזר פנימיות שמתאימות בדיוק למבנה ה-JSON מהפייתון
    private static class JsonWordData {
        String word;
        double[] vector;
    }

    public static Map<String, WordVector> loadFromFiles(String fullPath, String pcaPath) throws IOException {
        Gson gson = new Gson();
        Map<String, WordVector> vocabulary = new HashMap<>();

        // 1. קריאת הווקטורים המלאים (100 ממדים)
        try (FileReader fullReader = new FileReader(fullPath)) {
            List<JsonWordData> fullData = gson.fromJson(fullReader, 
                new TypeToken<List<JsonWordData>>(){}.getType());
            
            // 2. קריאת וקטורי ה-PCA (50 ממדים)
            try (FileReader pcaReader = new FileReader(pcaPath)) {
                List<JsonWordData> pcaData = gson.fromJson(pcaReader, 
                    new TypeToken<List<JsonWordData>>(){}.getType());

                // 3. איחוד הנתונים לאובייקט WordVector אחד
                for (int i = 0; i < fullData.size(); i++) {
                    JsonWordData fullEntry = fullData.get(i);
                    JsonWordData pcaEntry = pcaData.get(i);
                    
                    WordVector wv = new WordVector(
                        fullEntry.word, 
                        fullEntry.vector, 
                        pcaEntry.vector
                    );
                    vocabulary.put(wv.getWord(), wv);
                }
            }
        }
        return vocabulary;
    }
}
//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV