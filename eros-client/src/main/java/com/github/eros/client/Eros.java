package com.github.eros.client;

import com.github.eros.client.forest.Address;
import com.github.eros.common.constant.Constants;
import com.github.eros.common.lang.FacadeLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author fankongqiumu
 * @description
 * @date 2022/01/17 02:30
 */
public class Eros {

    /**
     * 获取client实例
     * @return
     */
    public static Client getInstance(List<Address> nameserverAddress){
        return Client.newInstance(nameserverAddress);
    }

    public static Client getInstance(){
        return Client.newInstance(getNameServerDomains(Eros.class.getClassLoader()));
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

    public static List<Address> getNameServerDomains(ClassLoader classLoader) {
        // 优先系统参数
        List<Address> nameServerDomainsFromSystemProperty = getNameServerDomainsFromSystemProperty();
        if (!nameServerDomainsFromSystemProperty.isEmpty()){
            return nameServerDomainsFromSystemProperty;
        }
        // 配置文件
        List<Address> nameServerDomainsFromPropertyFile = getNameServerDomainsFromPropertyFile(classLoader);
        if (!nameServerDomainsFromPropertyFile.isEmpty()){
            return nameServerDomainsFromPropertyFile;
        }
        // 默认
        return getSingleAddressList();
    }

    private static List<Address> getSingleAddressList(){
        return Collections.singletonList(new Address(
                Constants.ErosConstants.DEFAULT_NAME_SERVER_DOMAIN
                , Constants.ErosConstants.DEFAULT_NAME_SERVER_PORT));
    }

    private static List<Address> getNameServerDomainsFromPropertyFile(ClassLoader classLoader) {
        // 解析配置文件并设置为系统属性
        FacadeLoader.parseConfigPropertiesFileAndSetToSystemProperty(classLoader);
        return getNameServerDomainsFromSystemProperty();
    }

    /**
     * getNameServerDomainsFromSystemProperty
     * @return nameserver addressList
     */
    private static List<Address> getNameServerDomainsFromSystemProperty(){
        // 优先取列表属性
        String nameserverDomains = System.getProperty(Constants.PropertyConstants.NAME_SERVER_DOMAINS);
        if (null == nameserverDomains
                || (nameserverDomains = nameserverDomains.trim()).length() <= Constants.INTEGER_ZERO){
            // 取单个配置属性
            String nameServerDomain = System.getProperty(Constants.PropertyConstants.NAME_SERVER_DOMAIN);
            String portStr = System.getProperty(Constants.PropertyConstants.NAME_SERVER_PORT);
            if (StringUtils.isNotBlank(nameServerDomain) && StringUtils.isNumeric(portStr)){
                return Collections.singletonList(new Address(nameServerDomain, Integer.getInteger(portStr)));
            }
            return Collections.emptyList();
        }
        String[] nameserverDomainArray = nameserverDomains.split(Constants.PunctuationConstants.COMMA);
        List<Address> addressList = new ArrayList<>(nameserverDomainArray.length);
        parseAddress(addressList, Arrays.asList(nameserverDomainArray));
        return addressList;
    }

    /**
     * parse nameserverDomainString
     * @param addressList
     * @param nameserverDomains
     */
    public static void parseAddress(List<Address> addressList, List<String> nameserverDomains){
        if (null == nameserverDomains || nameserverDomains.isEmpty()){
            return;
        }
        for (String nameserverDomain : nameserverDomains) {
            String[] nameserverDomainSplit = nameserverDomain.split(Constants.PunctuationConstants.COLON);
            String domain = nameserverDomainSplit[0];
            if (StringUtils.isBlank(domain)){
                continue;
            }
            int port;
            if (nameserverDomainSplit.length < Constants.INTEGER_TWO){
                port = 80;
            } else if (!StringUtils.isNumeric(nameserverDomainSplit[Constants.INTEGER_ONE])){
                continue;
            } else {
                port = Integer.getInteger(nameserverDomainSplit[Constants.INTEGER_ONE]);
            }
            addressList.add(new Address(domain, port));
        }
    }
}
