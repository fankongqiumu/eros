package com.github.eros.common.order;

/**
 * @author fankongqiumu
 * @description TODO
 * @date 2021/12/17 14:36
 */
public interface Ordered {
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


    int getOrder();

}
