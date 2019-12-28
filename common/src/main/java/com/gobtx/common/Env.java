package com.gobtx.common;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Aaron Kuai on 2019/11/7.
 */
public enum Env {

    DEV("dev"),
    QA("qa"),
    UAT("uat"),
    PROD("prod");

    final String name;

    private static Env CURRENT = DEV;

    private static AtomicBoolean inited = new AtomicBoolean(false);

    Env(String env) {
        name = env;
    }

    public String getName() {
        return name;
    }

    public static void init() {
        if (inited.compareAndSet(false, true)) {
            update(Utils.sysProperty("app.env", "dev").trim());
        }
    }

    public static boolean isQA() {
        return CURRENT == QA;
    }

    public static boolean isProd() {
        return CURRENT == PROD;
    }

    public static boolean isUAT() {
        return CURRENT == UAT;
    }

    public static boolean isDev() {
        return CURRENT == DEV;
    }

    public static Env current() {
        return fromOrdinal(CURRENT.ordinal());
    }

    private static Env update(final String env) {

        Env got;
        switch (env) {
            case "prod":
                got = PROD;
                break;
            case "qa":
                got = QA;
                break;
            case "uat":
                got = UAT;
                break;
            case "dev":
            default:
                got = DEV;
                break;
        }
        CURRENT = got;
        return CURRENT;

    }

    /**
     * Enumerated values.
     */
    public static final Env[] VALS = values();

    public static Env fromOrdinal(int ord) {
        return ord >= 0 && ord < VALS.length ? VALS[ord] : null;
    }

}
