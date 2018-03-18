package org.uengine.cloud.strategies;

public class DeploymentStrategy {
    private InstanceStrategy instanceStrategy;
    private boolean bluegreen;
    private boolean sticky;
    private Ramp ramp;
    private Canary canary;
    private AbTest abtest;

    public DeploymentStrategy() {
        instanceStrategy = InstanceStrategy.RECREATE;
    }

    public InstanceStrategy getInstanceStrategy() {
        return instanceStrategy;
    }

    public void setInstanceStrategy(InstanceStrategy instanceStrategy) {
        this.instanceStrategy = instanceStrategy;
    }

    public boolean getBluegreen() {
        return bluegreen;
    }

    public void setBluegreen(boolean bluegreen) {
        this.bluegreen = bluegreen;
    }

    public boolean getSticky() {
        return sticky;
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    public Ramp getRamp() {
        return ramp;
    }

    public void setRamp(Ramp ramp) {
        this.ramp = ramp;
    }

    public Canary getCanary() {
        return canary;
    }

    public void setCanary(Canary canary) {
        this.canary = canary;
    }

    public AbTest getAbtest() {
        return abtest;
    }

    public void setAbtest(AbTest abtest) {
        this.abtest = abtest;
    }
}
