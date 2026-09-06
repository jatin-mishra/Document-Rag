package org.kbase.ragindexer.configurations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.kbase.ragindexer.dao.IVectorStoreDao;
import org.kbase.ragindexer.dao.PGvectorArgumentFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Wires the PostgreSQL/pgvector layer: a HikariCP-pooled {@link HikariDataSource},
 * a {@link Jdbi} instance (SqlObject + Postgres plugins + pgvector binding), and
 * the {@link IVectorStoreDao} SqlObject proxy.
 *
 * <p>Follows the same self-wiring convention as {@link AwsConfiguration} /
 * {@link ElasticSearchConfiguration}: a {@code @ConfigurationProperties} bean
 * (picked up by {@code @ConfigurationPropertiesScan}) that also declares its
 * client {@code @Bean}s. Declaring our own {@code DataSource} bean makes Spring's
 * {@code DataSourceAutoConfiguration} back off, so no {@code spring.datasource.*}
 * is required.
 */
@Getter
@Setter
@ConfigurationProperties("vector-store")
public class VectorStoreConfiguration {

    private String jdbcUrl;
    private String username;
    private String password;
    private int maxPoolSize;
    private long connectionTimeoutMs;
    private int embeddingDimension;

    @Bean(destroyMethod = "close")
    public HikariDataSource vectorDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setConnectionTimeout(connectionTimeoutMs);
        config.setPoolName("vector-store-pool");
        // Fail fast: HikariCP validates a connection at startup (default
        // initializationFailTimeout), so the application will not come up if
        // Postgres is unavailable.
        return new HikariDataSource(config);
    }

    @Bean
    public Jdbi jdbi(HikariDataSource vectorDataSource) {
        return Jdbi.create(vectorDataSource)
                .installPlugin(new SqlObjectPlugin())
                .installPlugin(new PostgresPlugin())
                .registerArgument(new PGvectorArgumentFactory());
    }

    @Bean
    public IVectorStoreDao vectorStoreDao(Jdbi jdbi) {
        return jdbi.onDemand(IVectorStoreDao.class);
    }
}
