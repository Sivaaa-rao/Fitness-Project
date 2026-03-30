package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final KafkaTemplate<String, Activity> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    @Value("${kafka.topic.delete-name}")
    private String deleteTopicName;

    public ActivityResponse trackActivity(ActivityRequest request) {

        boolean isValidUser = userValidationService.validateUser(request.getUserId());

        if (!isValidUser) {
            throw new RuntimeException("Invalid User: " + request.getUserId());
        }

        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        Activity savedActivity = activityRepository.save(activity);

        try {
            kafkaTemplate.send(topicName, savedActivity.getUserId(), savedActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return mapToResponse(savedActivity);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;

    }


    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activityList = activityRepository.findByUserId(userId);
        return activityList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteActivity(String activityId, String userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));

        // simple ownership check
        if (!activity.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this activity");
        }

        // 1) delete from activity DB
        activityRepository.delete(activity);

        // 2) send delete-event to Kafka (AI service will clean up recommendation)
        try {
            kafkaTemplate.send(deleteTopicName, activity.getId(), activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ActivityResponse updateActivity(String activityId, ActivityRequest request) {
        Activity existing = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!existing.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("You cannot modify this activity");
        }

        // Update mutable fields
        existing.setType(request.getType());
        existing.setDuration(request.getDuration());
        existing.setCaloriesBurned(request.getCaloriesBurned());
        existing.setStartTime(request.getStartTime());
        existing.setAdditionalMetrics(request.getAdditionalMetrics());

        Activity updated = activityRepository.save(existing);

        kafkaTemplate.send(topicName, updated.getUserId(), updated);

        return mapToResponse(updated);
    }

    public ActivityResponse getActivityById(String activityId, String userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));

        // Optional but recommended: ensure this activity belongs to the logged-in user
        if (!activity.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to view this activity");
        }

        return mapToResponse(activity);
    }


}
