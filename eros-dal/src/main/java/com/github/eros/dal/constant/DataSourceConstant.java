package com.github.eros.dal.constant;

public interface DataSourceConstant {
    String MYBATIS_CONFIG_FILE_NAME = "mybatis/mybatis-config.xml";

    String DATA_SOURCE_CONFIG_PREFIX = "data.db.eros";

    String MYBATIS_MAPPER_CLASS_PKG = "com.github.eros.dal.mapper";

    String MADC_MYBATIS_SQL_SESSION_FACTORY_BEAN_NAME = "erosMybatisSqlSessionFactory";


    /***
     * 数据源类型枚举
     */
    enum DataSourceType {
        /**
         * 主库数据源
         */
        MASTER,
        /**
         * 从库数据源
         */
        SLAVE
    }

}
