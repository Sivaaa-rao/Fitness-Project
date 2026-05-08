package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.ActivityType;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.respository.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityMessageListenerTest {

    @Mock
    private ActivityAIService activityAIService;

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private ActivityMessageListener listener;

    @Test
    void processActivityCreatesRecommendationWhenNoneExists() {
        Activity activity = activity();
        Recommendation generated = recommendation("Generated analysis");

        when(activityAIService.generateRecommendation(activity)).thenReturn(generated);
        when(recommendationRepository.findByActivityId("activity-1")).thenReturn(Optional.empty());

        listener.processActivity(activity);

        verify(recommendationRepository).save(generated);
    }

    @Test
    void processActivityUpdatesExistingRecommendation() {
        Activity activity = activity();
        Recommendation generated = recommendation("New analysis");
        Recommendation existing = recommendation("Old analysis");

        when(activityAIService.generateRecommendation(activity)).thenReturn(generated);
        when(recommendationRepository.findByActivityId("activity-1")).thenReturn(Optional.of(existing));

        listener.processActivity(activity);

        assertThat(existing.getRecommendation()).isEqualTo("New analysis");
        verify(recommendationRepository).save(existing);
    }

    private static Activity activity() {
        Activity activity = new Activity();
        activity.setId("activity-1");
        activity.setUserId("user-1");
        activity.setType(ActivityType.RUNNING);
        return activity;
    }

    private static Recommendation recommendation(String text) {
        return Recommendation.builder()
                .activityId("activity-1")
                .userId("user-1")
                .type("RUNNING")
                .recommendation(text)
                .improvements(List.of("Improve"))
                .suggestions(List.of("Suggest"))
                .safety(List.of("Safe"))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
