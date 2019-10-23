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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * uts数据库
 * @author: mintc
 * @date: 2019/6/18 10:31
 */
@Configuration
@MapperScan(basePackages = "com.wave.carRecord.dao.uts", sqlSessionTemplateRef = "utsSqlSessionTemplate")
public class UtsDataSourceConfig {

    /**
     * 创建数据源
     *
     * @return
     */
    @Bean(name = "utsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.uts")
    public DataSource utsDataSource() {
        return DataSourceBuilder.create().build();
    }


    /**
     * 创建工厂
     *
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "utsSqlSessionFactory")
    public SqlSessionFactory utsSqlSessionFactory(@Qualifier("utsDataSource") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mybatis/uts/*.xml"));
        return bean.getObject();
    }

    /**
     * 创建事务
     *
     * @param dataSource
     * @return
     */
    @Bean(name = "utsTransactionManager")
    public DataSourceTransactionManager utsDataSourceTransactionManager(@Qualifier("utsDataSource")
                                                                                    DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建模板
     *
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name = "utsSqlSessionTemplate")
    public SqlSessionTemplate utsSqlSessionTemplate(@Qualifier("utsSqlSessionFactory")
                                                                SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
