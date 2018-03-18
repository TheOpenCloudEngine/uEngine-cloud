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
        if(ramp == null){
            ramp = new Ramp();
        }
        return ramp;
    }

    public void setRamp(Ramp ramp) {
        if(ramp == null){
            ramp = new Ramp();
        }
        this.ramp = ramp;
    }

    public Canary getCanary() {
        if(canary == null){
            canary = new Canary();
        }
        return canary;
    }

    public void setCanary(Canary canary) {
        if(canary == null){
            canary = new Canary();
        }
        this.canary = canary;
    }

    public AbTest getAbtest() {
        if(abtest == null){
            abtest = new AbTest();
        }
        return abtest;
    }

    public void setAbtest(AbTest abtest) {
        if(abtest == null){
            abtest = new AbTest();
        }
        this.abtest = abtest;
    }
}
