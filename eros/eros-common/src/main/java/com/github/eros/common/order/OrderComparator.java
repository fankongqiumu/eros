package com.github.eros.common.order;

import java.util.Comparator;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 14:40
 */
public class OrderComparator implements Comparator<Object> {
    private OrderComparator(){
    }

    public static final OrderComparator INSTANCE = new OrderComparator();

    @Override
    public int compare(Object o1, Object o2) {
        return doCompare(o1, o2);
    }

    private int doCompare(Object o1,Object o2) {
        boolean p1 = (o1 instanceof Ordered);
        boolean p2 = (o2 instanceof Ordered);
        if (p1 && !p2) {
            return -1;
        }
        else if (p2 && !p1) {
            return 1;
        }

        int i1 = getOrder(o1);
        int i2 = getOrder(o2);
        return Integer.compare(i1, i2);
    }

    protected int getOrder(Object obj) {
        if (obj != null) {
            Integer order = findOrder(obj);
            if (order != null) {
                return order;
            }
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    protected Integer findOrder(Object obj) {
        if (obj instanceof Ordered){
            return ((Ordered) obj).getOrder();
        }
        Order orderAnnotation = obj.getClass().getAnnotation(Order.class);
        if (null != orderAnnotation) {
            return orderAnnotation.value();
        }
        return null;
    }
}
