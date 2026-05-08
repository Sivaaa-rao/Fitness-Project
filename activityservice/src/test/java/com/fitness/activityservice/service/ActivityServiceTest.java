package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private KafkaTemplate<String, Activity> kafkaTemplate;

    @InjectMocks
    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(activityService, "topicName", "activity-events");
        ReflectionTestUtils.setField(activityService, "deleteTopicName", "activity-delete-events");
    }

    @Test
    void trackActivityValidatesUserSavesActivityAndPublishesEvent() {
        ActivityRequest request = request("user-1");
        Activity savedActivity = activity("activity-1", "user-1");

        when(userValidationService.validateUser("user-1")).thenReturn(true);
        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

        ActivityResponse response = activityService.trackActivity(request);

        assertThat(response.getId()).isEqualTo("activity-1");
        assertThat(response.getUserId()).isEqualTo("user-1");
        verify(kafkaTemplate).send("activity-events", "user-1", savedActivity);
    }

    @Test
    void trackActivityRejectsInvalidUser() {
        ActivityRequest request = request("bad-user");
        when(userValidationService.validateUser("bad-user")).thenReturn(false);

        assertThatThrownBy(() -> activityService.trackActivity(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid User: bad-user");
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void getUserActivitiesReturnsMappedResponses() {
        when(activityRepository.findByUserId("user-1"))
                .thenReturn(List.of(activity("activity-1", "user-1")));

        List<ActivityResponse> responses = activityService.getUserActivities("user-1");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo("activity-1");
    }

    @Test
    void getActivityByIdRequiresOwnership() {
        when(activityRepository.findById("activity-1"))
                .thenReturn(Optional.of(activity("activity-1", "owner")));

        assertThatThrownBy(() -> activityService.getActivityById("activity-1", "other-user"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You are not allowed to view this activity");
    }

    @Test
    void deleteActivityDeletesOwnedActivityAndPublishesDeleteEvent() {
        Activity activity = activity("activity-1", "user-1");
        when(activityRepository.findById("activity-1")).thenReturn(Optional.of(activity));

        activityService.deleteActivity("activity-1", "user-1");

        verify(activityRepository).delete(activity);
        verify(kafkaTemplate).send("activity-delete-events", "activity-1", activity);
    }

    @Test
    void updateActivityRequiresOwnershipAndPublishesUpdateEvent() {
        Activity existing = activity("activity-1", "user-1");
        ActivityRequest request = request("user-1");
        request.setDuration(45);
        when(activityRepository.findById("activity-1")).thenReturn(Optional.of(existing));
        when(activityRepository.save(existing)).thenReturn(existing);

        ActivityResponse response = activityService.updateActivity("activity-1", request);

        assertThat(response.getDuration()).isEqualTo(45);
        verify(kafkaTemplate).send(eq("activity-events"), eq("user-1"), eq(existing));
    }

    private static ActivityRequest request(String userId) {
        ActivityRequest request = new ActivityRequest();
        request.setUserId(userId);
        request.setType(ActivityType.RUNNING);
        request.setDuration(30);
        request.setCaloriesBurned(300);
        request.setStartTime(LocalDateTime.of(2026, 5, 7, 8, 0));
        request.setAdditionalMetrics(Map.of("waterIntakeMl", 500));
        return request;
    }

    private static Activity activity(String id, String userId) {
        Activity activity = Activity.builder()
                .id(id)
                .userId(userId)
                .type(ActivityType.RUNNING)
                .duration(30)
                .caloriesBurned(300)
                .startTime(LocalDateTime.of(2026, 5, 7, 8, 0))
                .additionalMetrics(Map.of("waterIntakeMl", 500))
                .createdAt(LocalDateTime.of(2026, 5, 7, 8, 1))
                .updatedAt(LocalDateTime.of(2026, 5, 7, 8, 2))
                .build();
        return activity;
    }
}
