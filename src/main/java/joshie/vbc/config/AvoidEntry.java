package joshie.vbc.config;

public class AvoidEntry {
    public AvoidEntry(String block, int radius, double penalty) {
        this.block = block;
        this.radius = radius;
        this.penalty = penalty;
    }
    public String block;
    public int radius;
    public double penalty;
}
