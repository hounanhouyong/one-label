package com.hn.onelabel.server.common.utils;

import java.util.UUID;

public class SequenceIdUtils {

    public static String generateSequenceId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
