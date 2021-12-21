package com.github.eros;

import com.github.eros.client.init.ErosClientBootStrap;
import com.github.eros.common.step.BootStrap;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/19 21:24
 */
public class Test {
    public static void main(String[] args) {
        BootStrap strap = ErosClientBootStrap.newInstance();
        strap.start();
        for (;;){

        }
    }
}
