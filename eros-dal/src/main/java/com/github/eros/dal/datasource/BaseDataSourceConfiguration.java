package com.github.eros.dal.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.eros.dal.constant.DataSourceConstant;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fankongqiumu
 * @Description
 * @Date Created in 2022/1/2 19:40
 */
public class BaseDataSourceConfiguration {


    /**
     * 生成数据源
     *
     * @param environment
     * @param datasourcePrefix
     * @return
     * @throws SQLException
     */
    public DataSource genDatasource(Environment environment, String datasourcePrefix) throws SQLException {
        return DruidDataSourceBuilder.create().build(environment, datasourcePrefix);
    }

    /**
     * 生成 SqlSessionFactory
     *
     * @param dataSource
     * @param mybatisConfigFileName
     * @return
     * @throws Exception
     */
    protected SqlSessionFactory genSqlSessionFactory(DataSource dataSource, String mybatisConfigFileName) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setConfigLocation(new ClassPathResource(mybatisConfigFileName));
        return sessionFactory.getObject();
    }

}
