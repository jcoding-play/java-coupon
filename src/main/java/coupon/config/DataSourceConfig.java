package coupon.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    protected static final String WRITE = "write";
    protected static final String READ = "read";

    @Bean
    @ConfigurationProperties(prefix = "coupon.datasource.writer")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "coupon.datasource.reader")
    public DataSource readDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @DependsOn({"writeDataSource", "readDataSource"})
    public DataSource routeDataSource() {
        DataSource writeDataSource = writeDataSource();
        DataSource readDataSource = readDataSource();
        Map<Object, Object> dataSourceMap = getDataSourceMap(writeDataSource, readDataSource);

        ReadOnlyDataSourceRouter dataSourceRouter = new ReadOnlyDataSourceRouter();
        dataSourceRouter.setTargetDataSources(dataSourceMap);
        dataSourceRouter.setDefaultTargetDataSource(writeDataSource);
        return dataSourceRouter;
    }

    private Map<Object, Object> getDataSourceMap(DataSource writeDataSource, DataSource readDataSource) {
        return Map.of(WRITE, writeDataSource, READ, readDataSource);
    }

    @Bean
    @Primary
    @DependsOn("routeDataSource")
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routeDataSource());
    }
}