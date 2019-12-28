package com.gobtx.common;

public enum CPUModel {

    DEFAULT(0.5), // that is 1/2 of the core size
    CALCULATION(1), // if that is calculation better is the core size
    IO(2) //Better the size of the core *2
    ;

    public final int count;
    public final double weight;

    CPUModel(double weight) {
        this.weight = weight;
        count = (int) (Runtime.getRuntime().availableProcessors() * weight);
    }

    public static CPUModel map(final String value) {
        try {
            return CPUModel.valueOf(value);
        } catch (Throwable e) {
            System.err.println("NO_RECON_CPU_MODEL " + value);
            return DEFAULT;
        }
    }
}
