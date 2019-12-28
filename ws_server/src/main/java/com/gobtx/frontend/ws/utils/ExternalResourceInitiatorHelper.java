package com.gobtx.frontend.ws.utils;

import com.gobtx.common.Env;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static com.gobtx.common.Utils.IS_OS_LINUX;

public class ExternalResourceInitiatorHelper {

    public static final String DEFAULT_PASSWORD_LOCATION = "/etc/db-prod.properties";

    static final Logger logger = LoggerFactory.getLogger(ExternalResourceInitiatorHelper.class);


    public static final HikariDataSource dataSource(
            final ResourceLoader configLoader,
            final String configPath) throws Exception {

        return dataSource(configLoader, configPath, System.getProperty("user.name"));
    }


    public static final HikariDataSource dataSource(
            final ResourceLoader configLoader,
            final String configPath,
            final String appId) throws Exception {

        return dataSource(configLoader, configPath, appId, DEFAULT_PASSWORD_LOCATION);

    }

    public static final HikariDataSource dataSource(
            final ResourceLoader configLoader,
            final String configPath,
            final String appId,
            final String passwordFileLocation) throws Exception {


        return dataSource(
                configLoader.getResource(configPath).getInputStream(),
                appId,
                passwordFileLocation);
    }

    public static final HikariDataSource dataSource(
            final InputStream inputStream) throws Exception {

        return dataSource(inputStream, System.getProperty("user.name"), DEFAULT_PASSWORD_LOCATION);
    }

    public static final HikariDataSource dataSource(
            final InputStream inputStream,
            final String appId,
            final String passwordFileLocation) throws Exception {


        //TODO Why deco this:  com.mysql.jdbc.jdbc2.optional.MysqlDataSource
        //https://github.com/brettwooldridge/HikariCP#popular-datasource-class-names
        //https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        //http://assets.en.oreilly.com/1/event/21/Connector_J%20Performance%20Gems%20Presentation.pdf

        final Properties properties = new Properties();

        properties.load(inputStream);

        if (Env.isProd()) {

            properties.put("username", appId);

            final Properties properties2 = new Properties();
            // example: /etc/db-prod.properties
            FileInputStream stream = new FileInputStream(passwordFileLocation);
            properties2.load(stream);

            final String password = properties2.getProperty(appId);
            if (password == null || password.isEmpty()) {
                logger.error("NO_PASSWORD_FOR_PROD_CONFIG {}", appId);
                throw new Exception("no password set for the :" + appId + " contact your system admin!");
            }
            properties.put("password", password);
        }
        HikariConfig config = new HikariConfig(properties);

        config.validate();
        return new HikariDataSource(config);
    }


    public static final RedissonClient redissonClient(final ResourceLoader resourceLoader,
                                                      final String configPath) throws Exception {
        return redissonClient(resourceLoader.getResource(configPath).getInputStream());
    }


    public static final RedissonClient redissonClient(final InputStream inputStream) throws Exception {

        Config config = Config.fromYAML(inputStream);

        if (IS_OS_LINUX) {
            //require netty-transport-native-epoll
            config.setTransportMode(TransportMode.EPOLL);
        }

        // TODO: 2019/4/26  think about this in producer or consumer module?
        //config.setNettyThreads();
        //config.setThreads()

        return Redisson.create(config);
    }


}
