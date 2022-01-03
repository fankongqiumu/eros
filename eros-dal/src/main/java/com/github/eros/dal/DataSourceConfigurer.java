package com.github.eros.dal;

import com.github.eros.dal.constant.DataSourceConstant;
import com.github.eros.dal.datasource.BaseDataSourceConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@MapperScan(basePackages = {DataSourceConstant.MYBATIS_MAPPER_CLASS_PKG})
@Configuration
public class DataSourceConfigurer extends BaseDataSourceConfiguration {

    @Autowired
    private Environment environment;

//    @Primary
//    @Bean(DataSourceConstant.MADC_MYBATIS_SQL_SESSION_FACTORY_BEAN_NAME)
//    public SqlSessionFactory sqlSessionFactory() throws Exception {
//        SqlSessionFactory sqlSessionFactory = genSqlSessionFactory(genDatasource(environment, DataSourceConstant.MYBATIS_CONFIG_FILE_NAME)
//                , DataSourceConstant.MYBATIS_CONFIG_FILE_NAME);
//        return sqlSessionFactory;
//    }

}
