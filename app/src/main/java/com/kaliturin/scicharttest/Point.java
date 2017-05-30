package com.kaliturin.scicharttest;

/**
 * Rate data point
 */
public class Point {
    public final long date;
    public final double rate;

    public Point(long date, double rate) {
        this.date = date;
        this.rate = rate;
    }
}
