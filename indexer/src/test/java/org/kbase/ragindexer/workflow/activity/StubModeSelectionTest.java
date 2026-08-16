package org.kbase.ragindexer.workflow.activity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kbase.ragindexer.workflow.activity.live.DocumentActivitiesLive;
import org.kbase.ragindexer.workflow.activity.live.EmbeddingActivitiesLive;
import org.kbase.ragindexer.workflow.activity.stub.DocumentActivitiesStub;
import org.kbase.ragindexer.workflow.activity.stub.EmbeddingActivitiesStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

class StubModeSelectionTest {

    @Nested
    @SpringBootTest
    class StubModeEnabled {

        @Autowired
        ApplicationContext context;

        @Test
        void registersStubActivities() {
            assertThat(context.getBean(DocumentActivities.class)).isInstanceOf(DocumentActivitiesStub.class);
            assertThat(context.getBean(EmbeddingActivities.class)).isInstanceOf(EmbeddingActivitiesStub.class);
        }
    }

    @Nested
    @SpringBootTest(properties = "ragindexer.stub-mode=false")
    class StubModeDisabled {

        @Autowired
        ApplicationContext context;

        @Test
        void registersLiveActivities() {
            assertThat(context.getBean(DocumentActivities.class)).isInstanceOf(DocumentActivitiesLive.class);
            assertThat(context.getBean(EmbeddingActivities.class)).isInstanceOf(EmbeddingActivitiesLive.class);
        }
    }
}
