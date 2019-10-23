package com.wave.carRecord.config;


import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;


/**
 * 车闸终端数据库
 * @author: mintc
 * @date: 2019/6/18 10:31
 */
@Configuration
@MapperScan(basePackages = "com.wave.carRecord.dao.carGate", sqlSessionTemplateRef = "carGateSqlSessionTemplate")
public class CatGateDataSourceConfig {

    /**
     * 创建数据源
     *
     * @return
     */
    @Bean(name = "carGateDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.cargate")
    @Primary
    public DataSource carGateDataSource() {
        return DataSourceBuilder.create().build();
    }


    /**
     * 创建工厂
     *
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "carGateSqlSessionFactory")
    @Primary
    public SqlSessionFactory carGateSqlSessionFactory(@Qualifier("carGateDataSource") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mybatis/carGate/**/*.xml"));
        return bean.getObject();
    }


    /**
     * 创建事务
     *
     * @param dataSource
     * @return
     */
    @Bean(name = "carGateTransactionManager")
    @Primary
    public DataSourceTransactionManager carGateDataSourceTransactionManager(@Qualifier("carGateDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建模板
     *
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name = "carGateSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate carGateSqlSessionTemplate(@Qualifier("carGateSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
