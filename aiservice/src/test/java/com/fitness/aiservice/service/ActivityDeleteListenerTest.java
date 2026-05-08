package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.respository.RecommendationRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ActivityDeleteListenerTest {

    @Test
    void handleActivityDeleteDeletesRecommendationByActivityId() {
        RecommendationRepository repository = mock(RecommendationRepository.class);
        ActivityDeleteListener listener = new ActivityDeleteListener(repository);
        Activity activity = new Activity();
        activity.setId("activity-1");

        listener.handleActivityDelete(activity);

        verify(repository).deleteByActivityId("activity-1");
    }
}
