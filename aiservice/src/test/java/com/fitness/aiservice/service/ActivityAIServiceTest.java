package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.ActivityType;
import com.fitness.aiservice.model.Recommendation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActivityAIServiceTest {

    private final GeminiService geminiService = mock(GeminiService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ActivityAIService activityAIService = new ActivityAIService(geminiService, objectMapper);

    @Test
    void generateRecommendationParsesGeminiJsonPayload() throws Exception {
        Activity activity = activity();
        when(geminiService.getRecommendations(contains("Activity Type: RUNNING")))
                .thenReturn(geminiResponse("""
                        {
                          "analysis": {
                            "overall": "Strong workout",
                            "pace": "Steady pace",
                            "heartRate": "Moderate intensity",
                            "caloriesBurned": "Good calorie burn"
                          },
                          "improvements": [
                            {
                              "area": "Hydration",
                              "recommendation": "Drink water before running"
                            }
                          ],
                          "suggestions": [
                            {
                              "workout": "Intervals",
                              "description": "Add short fast efforts"
                            }
                          ],
                          "safety": [
                            "Warm up first"
                          ]
                        }
                        """));

        Recommendation recommendation = activityAIService.generateRecommendation(activity);

        assertThat(recommendation.getActivityId()).isEqualTo("activity-1");
        assertThat(recommendation.getUserId()).isEqualTo("user-1");
        assertThat(recommendation.getRecommendation()).contains("Overall:Strong workout");
        assertThat(recommendation.getImprovements()).containsExactly("Hydration: Drink water before running");
        assertThat(recommendation.getSuggestions()).containsExactly("Intervals: Add short fast efforts");
        assertThat(recommendation.getSafety()).containsExactly("Warm up first");
    }

    @Test
    void generateRecommendationFallsBackWhenAiResponseIsInvalid() {
        Activity activity = activity();
        when(geminiService.getRecommendations(contains("Activity Type: RUNNING")))
                .thenReturn("not-json");

        Recommendation recommendation = activityAIService.generateRecommendation(activity);

        assertThat(recommendation.getRecommendation()).isEqualTo("Unable to generate detailed analysis");
        assertThat(recommendation.getImprovements()).containsExactly("Continue with your current routine");
        assertThat(recommendation.getSuggestions()).containsExactly("Consider consulting a fitness consultant");
    }

    @Test
    void generateUserCombinedRecommendationBuildsUserSummary() throws Exception {
        Recommendation existing = Recommendation.builder()
                .activityId("activity-1")
                .userId("user-1")
                .type("RUNNING")
                .recommendation("Existing activity analysis")
                .improvements(List.of("Pace: Keep it steady"))
                .suggestions(List.of("Tempo: Try tempo run"))
                .safety(List.of("Warm up"))
                .createdAt(LocalDateTime.now())
                .build();

        when(geminiService.getRecommendations(contains("USER ID: user-1")))
                .thenReturn(geminiResponse("""
                        {
                          "analysis": {
                            "overall": "Consistent user",
                            "pace": "Pace is improving",
                            "heartRate": "Intensity is stable",
                            "caloriesBurned": "Burn pattern is balanced"
                          },
                          "improvements": [],
                          "suggestions": [],
                          "safety": []
                        }
                        """));

        Recommendation recommendation = activityAIService.generateUserCombinedRecommendation("user-1", List.of(existing));

        assertThat(recommendation.getUserId()).isEqualTo("user-1");
        assertThat(recommendation.getType()).isEqualTo("USER_SUMMARY");
        assertThat(recommendation.getRecommendation()).contains("Overall:Consistent user");
    }

    private String geminiResponse(String modelText) throws Exception {
        String escapedModelText = objectMapper.writeValueAsString("```json\n" + modelText.trim() + "\n```");
        return """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": %s
                          }
                        ]
                      }
                    }
                  ]
                }
                """.formatted(escapedModelText);
    }

    private static Activity activity() {
        Activity activity = new Activity();
        activity.setId("activity-1");
        activity.setUserId("user-1");
        activity.setType(ActivityType.RUNNING);
        activity.setDuration(30);
        activity.setCaloriesBurned(300);
        activity.setStartTime(LocalDateTime.of(2026, 5, 7, 8, 0));
        activity.setAdditionalMetrics(Map.of("waterIntakeMl", 500));
        return activity;
    }
}
