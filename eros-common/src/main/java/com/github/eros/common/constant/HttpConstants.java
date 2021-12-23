package com.github.eros.common.constant;

public class HttpConstants {

    public static final Integer LONG_PULL_TIMEOUT = 3000;

    public enum HttpStatus {
        OK("200", "OK"),
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
