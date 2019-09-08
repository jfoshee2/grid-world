package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by James on 7/17/2017.
 */
public class MyTask extends Task<Void> {

    private ObservableList<Node> gridChildren;

    private final Image upArrow = new Image("up arrow.png", 15, 15, false, false);
    private final Image downArrow = new Image("down arrow.png", 15, 15, false, false);
    private final Image leftArrow = new Image("left arrow.png", 15, 15, false, false);
    private final Image rightArrow = new Image("right arrow.png", 15, 15, false, false);

    public MyTask(ObservableList<Node> gridChildren) {
        this.gridChildren = gridChildren;
    }

    @Override
    protected Void call() throws Exception {

        sarsa();

        return null;
    }

    private void sarsa() throws InterruptedException {

        double alpha = 1.8;     // Learning Rate // 1.8
        double lambda = 0.1;    // Decay Rate
        double gamma = 0.5;     // Discount Factor // 0.5
        double epsilon = 0.8;   // Exploration Rate (Decreases over time)

        Random random = new Random(System.currentTimeMillis()); // Used to get random s, a

        ArrayList<Q> qArrayList = new ArrayList<>(); // Could change to different data structure
        HashMap<Q, Double> hashMap = new HashMap<>(); // HashMap to hold

        // Initialize Q(s, a) arbitrarily and e(s, a) to 0 for all s, a
        for (int i = 1; i < 21; i++) {
            for (int j = 1; j < 21; j++) {
                qArrayList.add(new Q(new sample.State(i, j), Direction.UP));
                qArrayList.add(new Q(new sample.State(i, j), Direction.DOWN));
                qArrayList.add(new Q(new sample.State(i, j), Direction.LEFT));
                qArrayList.add(new Q(new sample.State(i, j), Direction.RIGHT));
                hashMap.put(qArrayList.get(qArrayList.size() - 4), 0.0);
                hashMap.put(qArrayList.get(qArrayList.size() - 3), 0.0);
                hashMap.put(qArrayList.get(qArrayList.size() - 2), 0.0);
                hashMap.put(qArrayList.get(qArrayList.size() - 1), 0.0);
            }
        }

        int numOfEpisodes = 200; // number of episodes
        boolean terminal;

        // Repeat for each episode
        for (int i = 0; i < numOfEpisodes; i++) {
            terminal = false;

            // Initialize s, a

            Q init = qArrayList.get(random.nextInt(qArrayList.size()));
            Q current = init; // Keep track of Q(s, a)
            while (!isValidAction(current)) {
                current = qArrayList.get(random.nextInt(qArrayList.size()));
            }
            sample.State initialState = current.getState();
            Direction action = current.getAction();

            Label location = (Label) getNodeAtLocation(initialState.getRowIndex(), initialState.getColumnIndex());

            // Used to keep track of where the agent is at on the grid
            int currentRowIndex = initialState.getRowIndex();
            int currentColumnIndex = initialState.getColumnIndex();

            int stepCount = 0;
            while (!terminal) {

                //System.out.println("Still going: " + stepCount);

                // Take action, observe r (reward), s' (future state)
                Label currentLocation = location;
                Direction finalAction = action;
                Platform.runLater(() -> {
                    if (currentLocation != null) {
                        switch (finalAction) {
                            case UP:
                                currentLocation.setGraphic(new ImageView(upArrow));
                                break;
                            case DOWN:
                                currentLocation.setGraphic(new ImageView(downArrow));
                                break;
                            case LEFT:
                                currentLocation.setGraphic(new ImageView(leftArrow));
                                break;
                            case RIGHT:
                                currentLocation.setGraphic(new ImageView(rightArrow));
                                break;
                        }
                    }
                });

                switch (action) {
                    case UP:
                        currentRowIndex--;
                        break;
                    case DOWN:
                        currentRowIndex++;
                        break;
                    case LEFT:
                        currentColumnIndex--;
                        break;
                    case RIGHT:
                        currentColumnIndex++;
                        break;
                }

                location = (Label) getNodeAtLocation(currentRowIndex, currentColumnIndex);

                Thread.sleep(10);

                // Choose a' (future action) from s' (future state) using policy derived from Q(e.g. greedy)

                // ((rowLength * 4 * rowIndex) - 1) - ((rowLength - columnIndex) * 4) = index of Q(s, a) ->
                int currentStateRightIndex = ((20 * 4 * currentRowIndex) - 1) - ((20 - currentColumnIndex) * 4);
                int currentStateLeftIndex = currentStateRightIndex - 1;
                int currentStateDownIndex = currentStateLeftIndex - 1;
                int currentStateUpIndex = currentStateDownIndex - 1;

                boolean valid = false;
                double exploreOrExploit = random.nextDouble();
                // Find valid futureQ
                Q futureQ = qArrayList.get(currentStateRightIndex);

                if (!isValidAction(futureQ)) {
                    futureQ = qArrayList.get(currentStateLeftIndex);
                }

                if (!isValidAction(futureQ)) {
                    futureQ = qArrayList.get(currentStateDownIndex);
                }

                if (!isValidAction(futureQ)) {
                    futureQ = qArrayList.get(currentStateUpIndex);
                }

                //System.out.println("Before while not valid loop");
                while (!valid) {

                    if (exploreOrExploit < epsilon) {
                        // Explore
                        int randomDirection = random.nextInt(4);

                        switch (randomDirection) {
                            case 0: randomDirection = currentStateUpIndex; break;
                            case 1: randomDirection = currentStateDownIndex; break;
                            case 2: randomDirection = currentStateLeftIndex; break;
                            case 3: randomDirection = currentStateRightIndex; break;
                        }

                        futureQ = qArrayList.get(randomDirection);
                        //System.out.println("last move was from exploring");
                    } else { // Exploit
                        // Check to see which of the 4 actions are better

                        // Compare with Left
                        if (futureQ.getValue() < qArrayList.get(currentStateLeftIndex).getValue() &&
                                isValidAction(qArrayList.get(currentStateLeftIndex))) {
                            futureQ = qArrayList.get(currentStateLeftIndex);
                        }

                        // Compare with Down
                        if (futureQ.getValue() < qArrayList.get(currentStateDownIndex).getValue() &&
                                isValidAction(qArrayList.get(currentStateDownIndex))) {
                            futureQ = qArrayList.get(currentStateDownIndex);
                        }

                        // Compare with Up
                        if (futureQ.getValue() < qArrayList.get(currentStateUpIndex).getValue() &&
                                isValidAction(qArrayList.get(currentStateUpIndex))) {
                            futureQ = qArrayList.get(currentStateUpIndex);
                        }

                        //System.out.println("last move was from exploiting");
                    }
                    valid = isValidAction(futureQ);
//                    if (valid) {
//                        System.out.println(futureQ.getAction() + "\t\t\t" + futureQ.getState().getRowIndex() + "\t\t\t" + futureQ.getState().getColumnIndex());
//                        System.out.println(qArrayList.indexOf(futureQ));
//                    }
//                    System.out.println("I'm stuck!!!!!!!!!");
                }

                //System.out.println("After not valid loop");


                // delta <- r + gamma(Q(s', a')) - Q(s, a)
                assert location != null;
                double reward = location.getText().equals("  *") ? 1.0 : 0.0;

                double delta = reward + gamma * (futureQ.getValue() - current.getValue());

                // e(s, a) <- e(s, a) + 1
                hashMap.put(current, hashMap.get(current) + 1);

                // for all s, a:
                for (Q q : qArrayList) {

                    // Q(s, a) <- Q(s, a) + alpha * delta * e(s, a)
                    q.setValue(q.getValue() + alpha * delta * hashMap.get(q));

                    // e(s, a) <- gamma * lambda * e(s, a)
                    hashMap.put(q, gamma * lambda * hashMap.get(q));
                }

                // s <- s' ; a <- a'
                current = futureQ;
                action = current.getAction();

                ++stepCount; // Keep track of the number of steps taken

                // until s is terminal
                terminal = location.getText().equals("  *");
            }

            System.out.println("Episode: " + i + "\t\t\tSteps taken: " + stepCount);
            if (epsilon > 0.23) { // don't want epsilon value to be too low
                epsilon = epsilon - (i * 0.001); // Decrease epsilon
            }

            //Thread.sleep(10000);
            if (i < numOfEpisodes - 1)
                clearGrid();
            else
                displayLearned(qArrayList);

        }

    }

    private Node getNodeAtLocation(int rowIndex, int columnIndex) {

        ObservableList<Node> children = gridChildren;

        for (Node child : children) {
            if (GridPane.getRowIndex(child) == rowIndex && GridPane.getColumnIndex(child) == columnIndex) {
                return child;
            }
        }

        return null;
    }

    /**
     * Purpose of the function is to not allow the AI to run into a wall or off the grid
     * @param q - future Q(s, a)
     * @return whether or not the AI will run into a wall
     */
    private boolean isValidAction(Q q) {
        Label current = (Label) getNodeAtLocation(q.getState().getRowIndex(), q.getState().getColumnIndex());
        Label location = null;
        switch (q.getAction()) {
            case UP: location = (Label) getNodeAtLocation(q.getState().getRowIndex() - 1,
                    q.getState().getColumnIndex()); break;
            case DOWN: location = (Label) getNodeAtLocation(q.getState().getRowIndex() + 1,
                    q.getState().getColumnIndex()); break;
            case LEFT: location = (Label) getNodeAtLocation(q.getState().getRowIndex(),
                    q.getState().getColumnIndex() - 1); break;
            case RIGHT: location = (Label) getNodeAtLocation(q.getState().getRowIndex(),
                    q.getState().getColumnIndex() + 1); break;
        }
        assert location != null && current != null;
        return !location.getText().contains("Wall") && !current.getText().contains("Wall");
    }

    private void clearGrid() {
        Platform.runLater(() -> {
            for (int i = 1; i < 21; i++) {
                for (int j = 1; j < 21; j++) {
                    Label label = (Label) getNodeAtLocation(i, j);
                    assert label != null;
                    if (!label.getText().equals("  *") && !label.getText().equals("Wall")) {
                        label.setText("");
                    }
                    label.setGraphic(null);
                }
            }
        });
    }

    private void displayLearned(ArrayList<Q> qArrayList) {
        for (int i = 0; i < qArrayList.size(); i++) {
            Q max = qArrayList.get(i);
            if (max.getValue() < qArrayList.get(i + 1).getValue()) {
                max = qArrayList.get(i + 1);
            }

            if (max.getValue() < qArrayList.get(i + 2).getValue()) {
                max = qArrayList.get(i + 2);
            }

            if (max.getValue() < qArrayList.get(i + 3).getValue()) {
                max = qArrayList.get(i + 3);
            }

            // After finding max, display on grid
            Label location = (Label) getNodeAtLocation(max.getState().getRowIndex(), max.getState().getColumnIndex());

            Q finalMax = max;
            Platform.runLater(() -> {
                if (!location.getText().equals("  *") && !location.getText().equals("Wall")) {
                    switch (finalMax.getAction()) {
                        case UP: location.setGraphic(new ImageView(upArrow)); break;
                        case DOWN: location.setGraphic(new ImageView(downArrow)); break;
                        case LEFT: location.setGraphic(new ImageView(leftArrow)); break;
                        case RIGHT: location.setGraphic(new ImageView(rightArrow)); break;
                    }
                }

            });

        }


    }

}
