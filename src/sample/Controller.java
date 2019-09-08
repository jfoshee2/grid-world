package sample;

import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Button button;
    public GridPane grid;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        for (int i = 1; i < 21; i++) {
            for (int j = 1; j < 21; j++) {
                Label label = (Label) getNodeAtLocation(i, j);
                assert label != null;
                if (!label.getText().equals("  *") && !label.getText().equals("Wall")) {
                    label.setText("");
                }
            }
        }
    }

    public void buttonHandler() throws InterruptedException {
        button.setText("Changed Text");


        MyTask task = new MyTask(grid.getChildren());
        task.runningProperty().addListener((ov, wasRunning, isRunning) -> {
            if (!isRunning) {
                button.setDisable(false);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    private Node getNodeAtLocation(int rowIndex, int columnIndex) {

        ObservableList<Node> children = grid.getChildren();

        for (Node child : children) {
            if (GridPane.getRowIndex(child) == rowIndex && GridPane.getColumnIndex(child) == columnIndex) {
                return child;
            }
        }

        return null;
    }
}
