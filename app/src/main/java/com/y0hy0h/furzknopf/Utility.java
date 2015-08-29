package com.y0hy0h.furzknopf;

import java.util.Random;

public class Utility {
    private static final Random mRandom = new Random();

    /**
     * Returns a random int mapped between 1 and max
     * with falloff in probability respecting the slope.
     *
     * @param max The maximal value of the result.
     * @param slope The slope of the falloff in probability.
     * @return An integer between 1 and max with falloff in probability respecting slope.
     */
    public static int getMappedRandomInt(int max, int slope) {
        // Square the random number and map it between 1 and max for falloff in probability.
        float randomNumber = mRandom.nextFloat();
        randomNumber = (float) Math.pow(randomNumber, slope);
        return (int) (randomNumber * max);
    }

    /**
     * Returns random float between min and max.
     */
    public static float getFloatBetween(float min, float max) {
        return mRandom.nextFloat() * (max - min) + min;
    }

    /**
     * Returns random int between min and max.
     */
    public static int getIntBetween(int min, int max) {
        return mRandom.nextInt(max - min) + min;
    }
}
