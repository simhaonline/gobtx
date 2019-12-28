package com.gobtx.common.web;

import javax.servlet.http.HttpServletRequest;

public class NetworkUtil {

    //proxy_set_header X-Real-IP $remote_addr;
    //proxy_set_header Host $host;
    //proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;


    public static final String XForwardedForHeader = "X-Forwarded-For";
    public static final String ProxyClientIP = "Proxy-Client-IP";
    public static final String WLProxyClientIP = "WL-Proxy-Client-IP";
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    public static final String UN = "unknown".toUpperCase();
    public static final String X_Real_IP = "X-Real-IP";

    //https://gitee.com/roncoocom/roncoo-pay/raw/master/roncoo-pay-web-gateway/src/main/java/com/roncoo/pay/utils/NetworkUtil.java
    //https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-web-servers.html

    public final static String getIpAddress(final HttpServletRequest request) {

        String ip = request.getHeader(XForwardedForHeader);

        if (ip == null ||
                ip.isEmpty() ||
                UN.equalsIgnoreCase(ip)) {

            ip = request.getHeader(ProxyClientIP);

            if (ip == null ||
                    ip.isEmpty() ||
                    UN.equalsIgnoreCase(ip)) {

                ip = request.getHeader(WLProxyClientIP);
                if (ip == null ||
                        ip.isEmpty() ||
                        UN.equalsIgnoreCase(ip)) {

                    ip = request.getHeader(HTTP_CLIENT_IP);

                    if (ip == null ||
                            ip.isEmpty() ||
                            UN.equalsIgnoreCase(ip)) {

                        ip = request.getHeader(HTTP_X_FORWARDED_FOR);

                        if (ip == null ||
                                ip.isEmpty() ||
                                UN.equalsIgnoreCase(ip)) {

                            ip = request.getHeader(X_Real_IP);


                            if (ip == null ||
                                    ip.isEmpty() ||
                                    UN.equalsIgnoreCase(ip)) {
                                //TODO this may wrong as only work when no proxy Ngix etc exist
                                //TODO stop do this in production this may force kill make the system dead possible?
                                ip = request.getRemoteAddr();
                            }
                        }
                    }
                }
            }
        } else if (ip.length() > 15) {
            //How to handle IPV6
            final String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = ips[index];
                if (!(UN.equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }

        return ip == null ? UN : ip;

    }
}
