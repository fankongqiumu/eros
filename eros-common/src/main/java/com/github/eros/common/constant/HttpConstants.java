package com.github.eros.common.constant;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/24 23:09
 */
public class HttpConstants {

    public static final int CONNECT_TIMEOUT = 3000;

    public static final long LONG_PULL_TIMEOUT_LONG_VALUE = 60000L;

    public static final int LONG_PULL_TIMEOUT_INT_VALUE = 60000;

    public enum HttpStatus {
        OK("200", "OK"),
        SERVER_BUSY("250", "Server Busy"),
        NOT_MODIFIED("304", "Not Modified"),
        CONTENT_MODIFIED("399", "Content Modified"),
        ;
        private final String code;
        private final String reasonPhrase;

        private HttpStatus(String code, String reasonPhrase) {
            this.code = code;
            this.reasonPhrase = reasonPhrase;
        }

        public String getCode() {
            return code;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }
    }
}
