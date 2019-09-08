package sample;

import java.util.Random;

/**
 * Created by James on 7/14/2017.
 */
public class Q {

    private State state;
    private Direction action;
    private double value;

    public Q(State state, Direction action) {
        this.state = state;
        this.action = action;

        Random random = new Random();
        this.value = random.nextDouble(); // TODO: this will need to be changed to something else
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Direction getAction() {
        return action;
    }

    public void setAction(Direction action) {
        this.action = action;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
