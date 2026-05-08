package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.respository.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private ActivityAIService activityAIService;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void getActivityRecommendationReturnsExistingRecommendation() {
        Recommendation recommendation = recommendation("activity-1", "user-1");
        when(recommendationRepository.findByActivityId("activity-1"))
                .thenReturn(Optional.of(recommendation));

        assertThat(recommendationService.getActivityRecommendation("activity-1"))
                .isSameAs(recommendation);
    }

    @Test
    void getActivityRecommendationThrowsWhenMissing() {
        when(recommendationRepository.findByActivityId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendationService.getActivityRecommendation("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No recommendation found for this activity: missing");
    }

    @Test
    void getUserRecommendationAggregatesExistingRecommendations() {
        Recommendation existing = recommendation("activity-1", "user-1");
        Recommendation summary = Recommendation.builder()
                .userId("user-1")
                .type("USER_SUMMARY")
                .recommendation("Summary")
                .build();

        when(recommendationRepository.findByUserId("user-1")).thenReturn(List.of(existing));
        when(activityAIService.generateUserCombinedRecommendation("user-1", List.of(existing)))
                .thenReturn(summary);

        assertThat(recommendationService.getUserRecommendation("user-1")).isSameAs(summary);
        verify(activityAIService).generateUserCombinedRecommendation("user-1", List.of(existing));
    }

    @Test
    void getUserRecommendationThrowsWhenUserHasNoRecommendations() {
        when(recommendationRepository.findByUserId("user-1")).thenReturn(List.of());

        assertThatThrownBy(() -> recommendationService.getUserRecommendation("user-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No recommendations found for user: user-1");
    }

    private static Recommendation recommendation(String activityId, String userId) {
        return Recommendation.builder()
                .activityId(activityId)
                .userId(userId)
                .type("RUNNING")
                .recommendation("Analysis")
                .improvements(List.of("Improve pace"))
                .suggestions(List.of("Try intervals"))
                .safety(List.of("Warm up"))
                .build();
    }
}
