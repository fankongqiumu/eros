package com.github.eros.dal.sequence;

import com.github.eros.dal.constant.SequenceContants;
import com.github.eros.dal.mapper.SequenceMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicLong;

/**
 * todo 目前只是简单实现 需要添加分布式锁来控制并发
 */
public abstract class AbstractSequence implements InitializingBean {
    protected AtomicLong sequence;

    protected AtomicLong stepMax;

    @Autowired
    private SequenceMapper sequenceMapper;

    protected abstract String getName();

    protected Long getSequence(){
        if (sequence.get() == stepMax.get()){
            stepIncrement();
        }
        return sequence.getAndIncrement();
    }

    private void stepIncrement(){
        Long currentVal = sequenceMapper.getCurrentVal(getName());
        if (null == currentVal){
            sequenceMapper.insert(getName());
            currentVal = SequenceContants.SEQUENCE_INIT;
        }
        long max = currentVal + SequenceContants.SEQUENCE_STEP;
        sequenceMapper.updateByName(getName(), currentVal, max);
        stepMax = new AtomicLong(max);
        sequence = new AtomicLong(currentVal);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        stepIncrement();
    }
}
