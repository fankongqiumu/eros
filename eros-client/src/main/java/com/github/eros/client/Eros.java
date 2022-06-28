package com.github.eros.client;

import com.github.eros.client.forest.Address;
import com.github.eros.common.constant.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Eros {

    /**
     * 获取client实例
     * @return
     */
    public static Client getInstance(List<Address> nameserverAddress){
        return Client.newInstance(nameserverAddress);
    }

    public static Client getInstance(){
        return Client.newInstance(getNameServerDomains());
    }

    /**
     * 添加 ErosClientListener
     * @param listener
     */
    public static void addListener(ErosClientListener listener){
        if (isStarted()){
            // 允许client启动后添加, 会自动拉取一次配置并长轮训监听
            synchronized (Eros.class){
                listener.fetchAndWatch();
            }
        }
    }

    /**
     * 获取当前所有的ErosClientListener
     * @return
     */
    public static Collection<ErosClientListener> getListeners(){
        return Client.getListeners();
    }

    /**
     *
     * @return client 是否已经启动
     */
    public static boolean isStarted(){
        return Client.isStarted();
    }

    public static List<Address> getNameServerDomains() {
        // 优先系统参数
        List<Address> nameServerDomainsFromSystemProperty = getNameServerDomainsFromSystemProperty();
        if (!nameServerDomainsFromSystemProperty.isEmpty()){
            return nameServerDomainsFromSystemProperty;
        }
        // 配置文件
        List<Address> nameServerDomainsFromPropertyFile = getNameServerDomainsFromPropertyFile();
        if (!nameServerDomainsFromPropertyFile.isEmpty()){
            return nameServerDomainsFromPropertyFile;
        }
        // 默认
        return getSingleAddressList();
    }

    private static List<Address> getSingleAddressList(){
        String nameServerDomain = System.getProperty(Constants.PropertyConstants.NAME_SERVER_DOMAIN);
        String portStr = System.getProperty(Constants.PropertyConstants.NAME_SERVER_PORT);
        if (StringUtils.isBlank(nameServerDomain)){
            nameServerDomain = Constants.ErosConstants.DEFAULT_NAME_SERVER_DOMAIN;
        }
        Integer port;
        if (StringUtils.isNumeric(portStr)){
            port = Integer.getInteger(portStr);
        } else {
            port = Constants.ErosConstants.DEFAULT_NAME_SERVER_PORT;
        }
        return Collections.singletonList(new Address(nameServerDomain, port));
    }

    private static List<Address> getNameServerDomainsFromPropertyFile() {
        // TODO 从配置文件中读取
        return Collections.emptyList();
    }

    private static List<Address> getNameServerDomainsFromSystemProperty(){
        String nameserverDomains = System.getProperty(Constants.PropertyConstants.NAME_SERVER_DOMAINS);
        if (null == nameserverDomains
                || (nameserverDomains = nameserverDomains.trim()).length() <= Constants.INTEGER_ZERO){
            return Collections.emptyList();
        }
        String[] nameserverDomainArray = nameserverDomains.split(Constants.PunctuationConstants.COMMA);
        List<Address> addressList = new ArrayList<>(nameserverDomainArray.length);
        parseAddress(addressList, Arrays.asList(nameserverDomainArray));
        return addressList;
    }

    public static void parseAddress(List<Address> addressList, List<String> nameserverDomains){
        for (String nameserverDomain : nameserverDomains) {
            String[] nameserverDomainSplit = nameserverDomain.split(Constants.PunctuationConstants.COLON);
            String domain = nameserverDomainSplit[0];
            int port;
            if (nameserverDomainSplit.length < Constants.INTEGER_TWO){
                port = 80;
            } else {
                port = Integer.getInteger(nameserverDomainSplit[Constants.INTEGER_ONE]);
            }
            addressList.add(new Address(domain, port));
        }
    }
}
