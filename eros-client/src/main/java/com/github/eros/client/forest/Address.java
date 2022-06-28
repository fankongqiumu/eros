package com.github.eros.client.forest;

public class Address {

    /**
     * 主机地址(主机名/ip地址)
     */
    private final String host;

    /**
     * 主机端口号
     */
    private final int port;

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
