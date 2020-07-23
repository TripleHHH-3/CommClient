package com.ut.commclient.util;

import java.util.List;

public abstract class ListUtil {
    public static Boolean GtZero(List list) {
        return list != null && list.size() > 0;
    }
}
