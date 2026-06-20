package jp.andpad.api.util;

import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

public final class Ids {

    private Ids() {}

    public static String random(String prefix) {
        long nano = System.nanoTime();
        int rand = ThreadLocalRandom.current().nextInt();
        String hex = HexFormat.of().toHexDigits(nano) + HexFormat.of().toHexDigits(rand);
        return prefix + hex;
    }
}
