package org.kbase.ragindexer.configurations;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("activity-configuration")
public class ActivityConfiguration {
    Map<String, ActivityOptionConfiguration> activities;

    @Getter
    @Setter
    public static class ActivityOptionConfiguration {
        private String taskQueue;
        private Integer startToCloseTimeoutInMinutes;
        private Integer scheduledToCloseTimeoutInMinutes;
        private ActivityRetryOptionConfiguration retryConfig;

        @Getter
        @Setter
        public static class ActivityRetryOptionConfiguration {
            private Integer initialIntervalInSeconds;
            private Double backoffCoefficient;
            private Integer maximumIntervalInSeconds;
            private Integer maximumAttempts;
            private List<String> doNotRetry;
        }
    }

    @Bean
    @Qualifier("chunking")
    ActivityOptions chunkingActivities(){
        return buildActivityOptions("chunking");
    }

    @Bean
    @Qualifier("embedding")
    ActivityOptions embeddingActivityOption(){
        return buildActivityOptions("embedding");
    }

    private ActivityOptions buildActivityOptions(String activityName) {
        ActivityOptionConfiguration config = activities.get(activityName);
        if (config == null) {
            throw new IllegalStateException(
                    "Missing activity-configuration.activities." + activityName + " configuration");
        }

        ActivityOptionConfiguration.ActivityRetryOptionConfiguration retryConfig = config.getRetryConfig();
        if (retryConfig == null) {
            throw new IllegalStateException(
                    "Missing activity-configuration.activities." + activityName + ".retryConfig configuration");
        }

        RetryOptions.Builder retryBuilder = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(retryConfig.getInitialIntervalInSeconds()))
                .setBackoffCoefficient(retryConfig.getBackoffCoefficient())
                .setMaximumInterval(Duration.ofSeconds(retryConfig.getMaximumIntervalInSeconds()))
                .setMaximumAttempts(retryConfig.getMaximumAttempts());
        if (retryConfig.getDoNotRetry() != null && !retryConfig.getDoNotRetry().isEmpty()) {
            retryBuilder.setDoNotRetry(retryConfig.getDoNotRetry().toArray(new String[0]));
        }

        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(config.getStartToCloseTimeoutInMinutes()))
                .setScheduleToCloseTimeout(Duration.ofMinutes(config.getScheduledToCloseTimeoutInMinutes()))
                .setTaskQueue(config.getTaskQueue())
                .setRetryOptions(retryBuilder.build())
                .build();
    }
}
