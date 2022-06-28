package com.github.eros.common.constant;

public interface Constants {
    int INTEGER_ZERO = 0;

    int INTEGER_ONE = 1;

    int INTEGER_TWO = 2;

    int COLLECTION_DEFAULT_INITIAL_CAPACITY = 16;

    long LONG_ZERO = 0L;

    long LONG_ONE = 1L;

    interface PunctuationConstants{
        String COMMA = ",";
        String SEMICOLON = ";";
        String COLON = ":";
    }


    interface ErosConstants {
        String DEFAULT_APP_NAME = "EROS_SERVER";
        String DEFAULT_NAME_SERVER_DOMAIN = "data.nameserver.com";
        int DEFAULT_NAME_SERVER_PORT = 81;
    }

    interface PropertyFileConstants {
        String FACADE_RESOURCE_LOCATION = "META-INF/eros.facade";
        String EROS_CONFIG_RESOURCE_LOCATION = "META-INF/eros.properties";
    }

    interface PropertyConstants {
        String LOCAL_SERVER_DOMAIN = "local.server.domain";
        String LOCAL_SERVER_PORT = "local.server.port";
        /**
         * localhost:8080,localhost:8081
         */
        String NAME_SERVER_DOMAINS = "nameserver.domains";

        String NAME_SERVER_DOMAIN = "nameserver.domain";
        String NAME_SERVER_PORT = "nameserver.port";
    }
}
