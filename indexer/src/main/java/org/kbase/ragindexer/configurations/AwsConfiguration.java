package org.kbase.ragindexer.configurations;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Getter
@Setter
@ConfigurationProperties("aws-configuration")
public class AwsConfiguration {

    private String region;

    private String endpoint;

    @Bean
    public S3Client s3Client(){
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build());
        if(StringUtils.isNotBlank(endpoint)){
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }
        return builder.build();
    }

}
