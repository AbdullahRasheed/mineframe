package me.abdullahrasheed.mineframe.materials.drops;

import java.util.concurrent.ThreadLocalRandom;

public record DropRange(int minimum, int maximum) {

    public DropRange {
        if (minimum < 1) {
            throw new IllegalArgumentException("minimum must be at least 1");
        }
        if (maximum < minimum) {
            throw new IllegalArgumentException("maximum must be greater than or equal to minimum");
        }
    }

    public int randomAmount() {
        if (minimum == maximum) {
            return minimum;
        }
        return (int) ThreadLocalRandom.current().nextLong(minimum, (long) maximum + 1L);
    }
}
