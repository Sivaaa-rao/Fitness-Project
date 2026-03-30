package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.respository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityDeleteListener {

    private final RecommendationRepository recommendationRepository;

    @KafkaListener(
            topics = "${kafka.topic.delete-name}",
            groupId = "activity-delete-processor-group"
    )
    public void handleActivityDelete(Activity activity) {
        String activityId = activity.getId();
        log.info("Received delete event for activityId: {}", activityId);

        recommendationRepository.deleteByActivityId(activityId);
        log.info("Deleted recommendation(s) for activityId: {}", activityId);
    }
}
