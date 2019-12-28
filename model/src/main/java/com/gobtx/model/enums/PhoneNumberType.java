package com.gobtx.model.enums;

public enum PhoneNumberType {
    CHINESE("+86"),
    ;


    private String areaCode;

    PhoneNumberType(String areaCode) {
        this.areaCode = areaCode;
    }

    /**
     * Enumerated values.
     */
    public static final PhoneNumberType[] VALS = values();

    public static PhoneNumberType fromOrdinal(int ord) {
        return ord >= 0 && ord < VALS.length ? VALS[ord] : null;
    }

}
