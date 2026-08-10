package com.cleanroommc.modularui.value;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public class ProgressValue extends DoubleValue {

    private final int duration;

    public ProgressValue(int duration) {
        super(0);
        this.duration = duration;
    }

    public ProgressValue(long duration, TimeUnit unit) {
        this((int) unit.toMillis(duration));
    }

    @Override
    public double getDoubleValue() {
        setDoubleValue(System.currentTimeMillis() % this.duration / (double) this.duration);
        return super.getDoubleValue();
    }
}
