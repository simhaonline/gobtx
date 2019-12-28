package com.gobtx.common;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class MathUtils {

    //Leverage's

    public static final BigDecimal LEVERAGE_ZERO_THROUGH_300[] = new BigDecimal[301];

    static {
        LEVERAGE_ZERO_THROUGH_300[0] = BigDecimal.ZERO;
        for (int i = 1; i <= 300; i++) {
            LEVERAGE_ZERO_THROUGH_300[i] = new BigDecimal(BigInteger.valueOf(i), 0);
        }
    }

    //Speed up to get the intCompact  + scale


    public static BigDecimal leverage(final int leverage) {
        assert leverage > 0 && leverage < 300 : "WTF do not exceed the 300 leverage";
        return LEVERAGE_ZERO_THROUGH_300[leverage];
    }


}
