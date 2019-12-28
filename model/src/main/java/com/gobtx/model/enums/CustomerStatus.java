package com.gobtx.model.enums;

public enum CustomerStatus {
    NORMAL,
    FORBID_LOGON,
    DELETED,
    UNSYNC;


    /**
     * Enumerated values.
     */
    public static final CustomerStatus[] VALS = values();

    public static CustomerStatus fromOrdinal(int ord) {
        return ord >= 0 && ord < VALS.length ? VALS[ord] : null;
    }

}

