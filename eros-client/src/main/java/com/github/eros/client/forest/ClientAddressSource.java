package com.github.eros.client.forest;

import com.dtflys.forest.callback.AddressSource;
import com.dtflys.forest.http.ForestAddress;
import com.dtflys.forest.http.ForestRequest;
import com.github.eros.common.constant.Contants;

import java.util.ArrayList;
import java.util.List;

public class ClientAddressSource implements AddressSource {

    private static final List<ForestAddress> WATCH_ADDRESS_HOLDER = new ArrayList<>(16);

    private static final Object lock = new Object();

    private static volatile boolean NOT_INIT = true;

    @Override
    public ForestAddress getAddress(ForestRequest forestRequest) {
        if (WATCH_ADDRESS_HOLDER.isEmpty()){
            loadAddress();
        }
        return WATCH_ADDRESS_HOLDER.get(Contants.INTEGER_ZERO);
    }

    public void  loadAddress(){
        if (NOT_INIT) {
            synchronized (lock) {
                if (NOT_INIT) {
                    WATCH_ADDRESS_HOLDER.add(new ForestAddress("eros.test.b2c.srv", 80));
                }
                NOT_INIT = false;
            }
        }
    }

    public void addressChange(){

    }


}
