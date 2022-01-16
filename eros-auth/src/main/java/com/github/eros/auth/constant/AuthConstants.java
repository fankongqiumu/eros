package com.github.eros.auth.constant;

public interface AuthConstants {

    enum ErrorInfo{
        USER_NOT_EXIST("001", "user not exist"),
        PWD_NOT_MATHING("002", "pwd not matching"),
        ;

        private ErrorInfo(String code, String message){
            this.code = code;
            this.message = message;
        }
        private String code;
        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
