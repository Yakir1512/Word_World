package engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.BackupVec;
import math.DistanceMetric;
import math.EuclideanDistance;
import math.VectorArithmetic;
import math.VectorMath;
import model.EquationResult;
import model.Match;
import model.WordVector;

public class SpaceManager {
    
    private Map<String, WordVector> vocabulary = new HashMap<>();
    private List<WordVector> wordList = new ArrayList<>();
    private SemanticSearcher searcher;
    private DistanceMetric metric;

    public SpaceManager() {
        this.metric = new EuclideanDistance();
    }

    // ============================================================
    // 1. טעינת נתונים ואתחול
    // ============================================================

    public void ensureDataReady() throws IOException, InterruptedException {
        // תיקון: בודק את הקבצים הנכונים שהפייתון יוצר
        File fullFile = new File("full_vectors.json");
        File pcaFile  = new File("pca_vectors.json");

        try {
            if (fullFile.exists() && fullFile.length() > 0
             && pcaFile.exists()  && pcaFile.length()  > 0) {
                System.out.println("JSON files found! Loading directly...");
                loadData();
            } else {
                System.out.println("JSON files missing. Running Python embedder...");
                initializeSpace();
            }
        } catch (Exception e) {
            System.err.println("Real data loading failed: " + e.getMessage());
            System.err.println("Activating BackupVec protocol...");
            List<WordVector> mockList = BackupVec.generate(150, 50);
            updateVocabulary(mockList);
        }
    }

    public void initializeSpace() throws IOException, InterruptedException {
        generateDataWithPython();
        loadData();
        System.out.println("Space is ready with " + vocabulary.size() + " words!");
    }

    private void loadData() throws IOException {
        Map<String, WordVector> loadedData = DataLoader.loadFromFiles("full_vectors.json", "pca_vectors.json");
        updateVocabulary(new ArrayList<>(loadedData.values()));
    }

    private void updateVocabulary(List<WordVector> newList) {
        this.vocabulary.clear();
        this.wordList.clear();
        for (WordVector wv : newList) {
            this.vocabulary.put(wv.getWord(), wv);
            this.wordList.add(wv);
        }
        this.searcher = new SemanticSearcher(this.wordList, this.metric);
    }

    public void generateDataWithPython() throws IOException, InterruptedException {
        System.out.println("Starting Python embedder script...");
        String workingDir = System.getProperty("user.dir");
        File scriptFile = new File(workingDir, "embedder.py");

        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Python script not found at: " + scriptFile.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder("python", scriptFile.getAbsolutePath());
        pb.inheritIO();
        pb.directory(new File(workingDir));

        try {
            Process p = pb.start();
            if (p.waitFor() != 0) throw new RuntimeException("Python script failed.");
        } catch (IOException e) {
            // fallback to python3
            ProcessBuilder pb3 = new ProcessBuilder("python3", scriptFile.getAbsolutePath());
            pb3.inheritIO();
            pb3.directory(new File(workingDir));
            if (pb3.start().waitFor() != 0) throw new RuntimeException("Python script failed (python3 fallback).");
        }
        System.out.println("Python embedder finished successfully.");
    }

    // ============================================================
    // 2. גישה לנתונים
    // ============================================================

    public List<WordVector> getWordList() { return wordList; }

    public WordVector getWordVector(String word) {
        if (word == null) return null;
        WordVector wv = vocabulary.get(word);
        if (wv != null) return wv;
        return vocabulary.get(word.toLowerCase());
    }

    public double[] getAxisRange(int axisIndex) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (WordVector wv : wordList) {
            double val = wv.getPcaCoordinate(axisIndex);
            if (val < min) min = val;
            if (val > max) max = val;
        }
        if (min == Double.MAX_VALUE) return new double[]{-1.0, 1.0};
        double padding = (max - min) * 0.05;
        return new double[]{min - padding, max + padding};
    }

    // ============================================================
    // 3. לוגיקה וחיפוש
    // ============================================================

    public void changeMetric(DistanceMetric newMetric) {
        this.metric = newMetric;
        if (searcher != null) {
            searcher.setMetric(newMetric);
        }
    }

    /** מציאת שכנים למילה קיימת */
    public List<Match> getNeighbors(String queryWord, int k) {
        WordVector vector = getWordVector(queryWord);
        if (vector == null) return new ArrayList<>();
        return searcher.findNearest(vector.getFullVector(), k + 1).stream()
               .filter(m -> !m.getWord().equalsIgnoreCase(queryWord))
               .limit(k)
               .collect(java.util.stream.Collectors.toList());
    }

    /** מציאת שכנים לווקטור גולמי (Centroid) */
    public List<Match> getNeighbors(double[] targetVec, int k) {
        List<Match> matches = new ArrayList<>();
        for (WordVector wv : wordList) {
            double dist = metric.calculate(targetVec, wv.getVector());
            matches.add(new Match(wv.getWord(), dist));
        }
        matches.sort((m1, m2) -> Double.compare(m1.distance, m2.distance));
        if (k > matches.size()) k = matches.size();
        return matches.subList(0, k);
    }

    /** חישוב מרחק בין שתי מילים במרחב המלא */
    public double getSemanticDistance(String word1, String word2) {
        WordVector v1 = getWordVector(word1);
        WordVector v2 = getWordVector(word2);
        if (v1 == null || v2 == null) return -1.0;
        return metric.calculate(v1.getFullVector(), v2.getFullVector());
    }

    /** פתרון משוואה וקטורית */
    public EquationResult solveEquation(String equation) {
        return VectorArithmetic.solve(equation, this.wordList);
    }

    /** פתרון אנלוגיה קלאסית (King - Man + Woman = Queen) */
    public String solveAnalogy(String w1, String w2, String w3) {
        WordVector v1 = getWordVector(w1);
        WordVector v2 = getWordVector(w2);
        WordVector v3 = getWordVector(w3);
        if (v1 == null || v2 == null || v3 == null) return "Word not found";

        double[] step1 = VectorMath.subtract(v1.getFullVector(), v2.getFullVector());
        double[] target = VectorMath.add(step1, v3.getFullVector());

        List<Match> results = searcher.findNearest(target, 5);
        for (Match m : results) {
            String candidate = m.getWord();
            if (!candidate.equalsIgnoreCase(w1) &&
                !candidate.equalsIgnoreCase(w2) &&
                !candidate.equalsIgnoreCase(w3)) {
                return candidate;
            }
        }
        return "None";
    }
}
