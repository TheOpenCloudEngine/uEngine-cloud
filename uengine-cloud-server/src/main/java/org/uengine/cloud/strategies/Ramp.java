package org.uengine.cloud.strategies;

public class Ramp {

    private Double maximumOverCapacity;

    public Double getMaximumOverCapacity() {
        if (maximumOverCapacity == null) {
            maximumOverCapacity = new Double(0);
        }
        return Double.parseDouble(String.format("%.1f", maximumOverCapacity));
    }

    public void setMaximumOverCapacity(Double maximumOverCapacity) {
        if (maximumOverCapacity == null) {
            maximumOverCapacity = new Double(0);
        }
        this.maximumOverCapacity = Double.parseDouble(String.format("%.1f", maximumOverCapacity));
    }
}
