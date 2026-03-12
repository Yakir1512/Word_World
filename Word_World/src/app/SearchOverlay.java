package app;

import java.util.List;

import engine.SpaceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Match;

public class SearchOverlay extends VBox {
    
    private TextField searchField;
    private ListView<String> resultsList;
    private SpaceManager spaceManager;

    public SearchOverlay(SpaceManager manager) {
        this.spaceManager = manager;
        
        // --- עיצוב החלון הצף ---
        this.setMaxSize(300, 400); // גודל קבוע לחלון הצף
        this.setPadding(new Insets(15));
        this.setSpacing(10);
        this.setAlignment(Pos.TOP_CENTER);
        
        // רקע שחור חצי שקוף עם פינות עגולות
        this.setStyle("-fx-background-color: rgba(30, 30, 30, 0.85); " +
                      "-fx-background-radius: 15; " +
                      "-fx-border-color: #4ec9b0; " +
                      "-fx-border-radius: 15; " +
                      "-fx-border-width: 1;");

        // --- רכיבים פנימיים ---
        Label title = new Label("Semantic Search");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        searchField = new TextField();
        searchField.setPromptText("Type a word (e.g., 'King')...");
        searchField.setStyle("-fx-background-color: #444; -fx-text-fill: white;");

        resultsList = new ListView<>();
        resultsList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #444;");
        
        // --- לוגיקה ---
        // חיפוש בלחיצה על ENTER
        searchField.setOnAction(e -> performSearch());

        this.getChildren().addAll(title, searchField, resultsList);
    }


//הפיכת הנתונים לטקסט יפה למשתמש
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        // 1. קבלת הנתונים הגולמיים מהמנהל
        List<Match> rawMatches = spaceManager.getNeighbors(query, 10);
        
        resultsList.getItems().clear();

        if (rawMatches.isEmpty()) {
            resultsList.getItems().add("Word not found.");
            return;
        }

        // 2. עיבוד לתצוגה (Presentation Logic)
        for (Match m : rawMatches) {
            // דילוג על המילה עצמה אם צריך
            if (m.getWord().equalsIgnoreCase(query)) continue;

            // כאן אנחנו מחליטים איך זה ייראה!
            // למשל: "King      [Dist: 0.15]"
            String displayText = String.format("%-15s [Dist: %.4f]", m.getWord(), m.getDistance());
            
            resultsList.getItems().add(displayText);
        }
    }


    //שינוי תוך החלון הצף כשלוחצים על מילה
    public void setExternalResults(String centerWord, List<Match> matches) {
        // 1. איפוס ועדכון שדה הטקסט (כדי שישקף את מה שנבחר)
        this.searchField.setText(centerWord);
        this.resultsList.getItems().clear();

        // 2. מילוי הרשימה
        for (Match m : matches) {
            if (m.getWord().equalsIgnoreCase(centerWord)) continue;
            String displayText = String.format("%-15s [Dist: %.4f]", m.getWord(), m.getDistance());
            resultsList.getItems().add(displayText);
        }
    }
}