package com.github.eros.common.step;

import java.util.List;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 15:07
 */
public interface BootStrap {

     void addCustomSteps(List<StartupStep> customSteps);

     void start();

}
