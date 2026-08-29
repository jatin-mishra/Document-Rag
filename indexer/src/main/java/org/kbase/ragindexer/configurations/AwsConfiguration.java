package org.kbase.ragindexer.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConfigurationProperties("awsConfiguration")
public class AwsConfiguration {

    private String region;

    @Bean
    public S3Client s3Client(){
        return S3Client.builder().region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }

}
