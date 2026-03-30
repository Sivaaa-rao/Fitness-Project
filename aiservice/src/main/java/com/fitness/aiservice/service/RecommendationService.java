package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.respository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository recommendationRepository;
    private final ActivityAIService activityAIService;

    public Recommendation getUserRecommendation(String userId) {
        List<Recommendation> recs = recommendationRepository.findByUserId(userId);

        if (recs == null || recs.isEmpty()) {
            throw new RuntimeException("No recommendations found for user: " + userId);
        }

        return activityAIService.generateUserCombinedRecommendation(userId, recs);
    }

    public Recommendation getActivityRecommendation(String activityId) {
        return recommendationRepository.findByActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("No recommendation found for this activity: " + activityId));
    }
}
