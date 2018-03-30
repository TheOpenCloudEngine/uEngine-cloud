package org.uengine.cloud.strategies;

public class Canary {
    private boolean active;
    private int weight;
    private boolean auto = true;
    private int increase;
    private int test;
    private int decrease;

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean getAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public int getIncrease() {
        return increase;
    }

    public void setIncrease(int increase) {
        this.increase = increase;
    }

    public int getTest() {
        return test;
    }

    public void setTest(int test) {
        this.test = test;
    }

    public int getDecrease() {
        return decrease;
    }

    public void setDecrease(int decrease) {
        this.decrease = decrease;
    }
}
