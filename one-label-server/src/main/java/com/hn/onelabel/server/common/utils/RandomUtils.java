package com.hn.onelabel.server.common.utils;

import java.util.Random;

public class RandomUtils {

    public static long generateRandom(long offset, int bound) {
        return offset + new Random().nextInt(bound);
    }

}
