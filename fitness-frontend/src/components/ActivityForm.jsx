import React, { useState } from 'react'
import { addActivity } from '../services/api';
import { isImageUploadConfigured, uploadActivityImage } from '../services/imageUpload';

const ActivityForm = ({ onActivityAdded }) => {
  const imageUploadReady = isImageUploadConfigured();

  const [activity, setActivity] = useState({
    type: "RUNNING",
    duration: '',
    caloriesBurned: '',
    description: '',
    imageUrl: '',
    additionalMetrics: {
      timeOfDay: '',
      mealTiming: '',
      waterIntakeMl: ''
    }
  });

  const [errors, setErrors] = useState({
    duration: '',
    caloriesBurned: ''
  });
  const [submitState, setSubmitState] = useState({
    loading: false,
    message: '',
    type: ''
  });
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState('');

  const handleAdditionalMetricChange = (field, value) => {
    setActivity(prev => ({
      ...prev,
      additionalMetrics: {
        ...prev.additionalMetrics,
        [field]: value
      }
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate before submitting
    if (!isFormValid()) {
      return;
    }

    try {
      setSubmitState({
        loading: true,
        message: '',
        type: ''
      });

      const uploadedImageUrl = imageFile
        ? await uploadActivityImage(imageFile)
        : null;

      const payload = {
        ...activity,
        duration: Number(activity.duration),
        caloriesBurned: Number(activity.caloriesBurned),
        additionalMetrics: {
            ...activity.additionalMetrics,
            waterIntakeMl: activity.additionalMetrics.waterIntakeMl
            ? Number(activity.additionalMetrics.waterIntakeMl)
            : null
        },
        description: activity.description.trim() || null,
        imageUrl: uploadedImageUrl || activity.imageUrl || null
      };

      await addActivity(payload);
      setActivity({
        type: "RUNNING",
        duration: '',
        caloriesBurned: '',
        description: '',
        imageUrl: '',
        additionalMetrics: {
          timeOfDay: '',
          mealTiming: '',
          waterIntakeMl: ''
        }
      });
      setErrors({
        duration: '',
        caloriesBurned: ''
      });
      setSubmitState({
        loading: false,
        message: 'Activity added successfully.',
        type: 'success'
      });
      setImageFile(null);
      setImagePreview('');
      onActivityAdded();
    } catch (error) {
      console.error(error);
      setSubmitState({
        loading: false,
        message: error.response?.data?.error || 'Could not add activity. Please try again.',
        type: 'error'
      });
    }
  }

  const handleImageChange = (event) => {
    const file = event.target.files?.[0];
    setSubmitState({
      loading: false,
      message: '',
      type: ''
    });

    if (!file) {
      setImageFile(null);
      setImagePreview('');
      setActivity({...activity, imageUrl: ''});
      return;
    }

    if (!imageUploadReady) {
      setSubmitState({
        loading: false,
        message: 'Image upload is not configured yet. Add the activity without an image for now.',
        type: 'error'
      });
      event.target.value = '';
      return;
    }

    if (!file.type.startsWith('image/')) {
      setSubmitState({
        loading: false,
        message: 'Please choose an image file.',
        type: 'error'
      });
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setSubmitState({
        loading: false,
        message: 'Image must be 5 MB or smaller.',
        type: 'error'
      });
      return;
    }

    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
    setActivity({...activity, imageUrl: ''});
  };

  const isFormValid = () => {
    return (
      activity.type &&
      activity.duration &&
      activity.duration > 0 &&
      activity.caloriesBurned &&
      activity.caloriesBurned > 0
    );
  }

  const handleDurationChange = (e) => {
    const value = e.target.value;
    setActivity({...activity, duration: value});
    
    if (value && value <= 0) {
      setErrors({...errors, duration: 'Duration must be greater than 0'});
    } else if (value && value > 1440) {
      setErrors({...errors, duration: 'Duration cannot exceed 24 hours (1440 minutes)'});
    } else {
      setErrors({...errors, duration: ''});
    }
  }

  const handleCaloriesChange = (e) => {
    const value = e.target.value;
    setActivity({...activity, caloriesBurned: value});
    
    if (value && value <= 0) {
      setErrors({...errors, caloriesBurned: 'Calories must be greater than 0'});
    } else if (value && value > 10000) {
      setErrors({...errors, caloriesBurned: 'Calories seem too high. Please check.'});
    } else {
      setErrors({...errors, caloriesBurned: ''});
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mb-8">
      {/* Activity Type */}
      <div className="mb-4">
        <label className="block text-white text-sm font-medium mb-2">
          Activity Type <span className="text-red-400">*</span>
        </label>
        <select 
          value={activity.type}
          onChange={(e) => setActivity({...activity, type: e.target.value})}
          className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition"
          required
        >
          <option value="RUNNING">Running</option>
          <option value="WALKING">Walking</option>
          <option value="CYCLING">Cycling</option>
          <option value="SWIMMING">SWIMMING</option>
          <option value="WEIGHT_TRAINING">WEIGHT_TRAINING</option>
          <option value="YOGA">YOGA</option>
          <option value="HIIT">HIIT</option>
          <option value="CARDIO">CARDIO</option>
          <option value="STRETCHING">STRETCHING</option>
          <option value="OTHER">OTHER</option>
        </select>
      </div>

      {/* Duration */}
      <div className="mb-4">
        <label className="block text-white text-sm font-medium mb-2">
          Duration (Minutes) <span className="text-red-400">*</span>
        </label>
        <input 
          type="number"
          value={activity.duration}
          onChange={handleDurationChange}
          className={`w-full px-4 py-3 bg-gray-800 border rounded-lg text-white focus:outline-none focus:ring-2 transition ${
            errors.duration 
              ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' 
              : 'border-gray-600 focus:border-blue-500 focus:ring-blue-500/20'
          }`}
          placeholder="Enter duration in minutes"
          min="1"
          max="1440"
          required
        />
        {errors.duration && (
          <p className="text-red-400 text-sm mt-1">{errors.duration}</p>
        )}
      </div>

      {/* Calories */}
      <div className="mb-4">
        <label className="block text-white text-sm font-medium mb-2">
          Calories Burned <span className="text-red-400">*</span>
        </label>
        <input 
          type="number"
          value={activity.caloriesBurned}
          onChange={handleCaloriesChange}
          className={`w-full px-4 py-3 bg-gray-800 border rounded-lg text-white focus:outline-none focus:ring-2 transition ${
            errors.caloriesBurned 
              ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' 
              : 'border-gray-600 focus:border-blue-500 focus:ring-blue-500/20'
          }`}
          placeholder="Enter calories burned"
          min="1"
          max="10000"
          required
        />
        {errors.caloriesBurned && (
          <p className="text-red-400 text-sm mt-1">{errors.caloriesBurned}</p>
        )}
      </div>

      <div className="mb-4">
        <label className="block text-white text-sm font-medium mb-2">
          Activity Note
        </label>
        <textarea
          value={activity.description}
          onChange={(e) => setActivity({...activity, description: e.target.value})}
          className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition min-h-28 resize-y"
          placeholder="How did it feel? Add effort, mood, route, soreness, or anything you want to remember."
          maxLength="500"
        />
        <p className="text-gray-500 text-xs mt-1">{activity.description.length}/500 characters</p>
      </div>

      <div className="mb-4">
        <label className="block text-white text-sm font-medium mb-2">
          Activity Image
        </label>
        <input
          type="file"
          accept="image/*"
          disabled={!imageUploadReady}
          onChange={handleImageChange}
          className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white file:mr-4 file:rounded-md file:border-0 file:bg-blue-600 file:px-4 file:py-2 file:text-white hover:file:bg-blue-700 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition disabled:cursor-not-allowed disabled:opacity-60"
        />
        {!imageUploadReady && (
          <p className="text-yellow-300 text-sm mt-2">
            Image upload needs Cloudinary config. You can still add the activity without an image.
          </p>
        )}
        {imagePreview && (
          <div className="mt-3 overflow-hidden rounded-lg border border-gray-700 bg-gray-950">
            <img
              src={imagePreview}
              alt="Activity preview"
              className="h-56 w-full object-contain"
            />
          </div>
        )}
      </div>

      {/* Additional Metrics Section */}
      <div className="mb-6 border border-gray-700 rounded-lg p-4 bg-gray-900/40">
        <p className="text-white font-medium mb-3">
          Additional Details (for better AI recommendations)
        </p>

        {/* Time of Day */}
        <div className="mb-3">
          <label className="block text-gray-300 text-sm mb-1">
            When did you do this activity?
          </label>
          <select
            value={activity.additionalMetrics.timeOfDay}
            onChange={(e) => handleAdditionalMetricChange('timeOfDay', e.target.value)}
            className="w-full px-3 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white text-sm"
          >
            <option value="">Select time of day</option>
            <option value="MORNING">Morning</option>
            <option value="AFTERNOON">Afternoon</option>
            <option value="EVENING">Evening</option>
            <option value="NIGHT">Night</option>
          </select>
        </div>

        {/* Meal Timing */}
        <div className="mb-3">
          <label className="block text-gray-300 text-sm mb-1">
            Was this before or after eating?
          </label>
          <select
            value={activity.additionalMetrics.mealTiming}
            onChange={(e) => handleAdditionalMetricChange('mealTiming', e.target.value)}
            className="w-full px-3 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white text-sm"
          >
            <option value="">Select option</option>
            <option value="BEFORE_LUNCH">Before lunch</option>
            <option value="AFTER_LUNCH">After lunch</option>
          </select>
        </div>

        {/* Water Intake */}
        <div>
          <label className="block text-gray-300 text-sm mb-1">
            Water drank before/during activity (ml)
          </label>
          <input
            type="number"
            min="0"
            value={activity.additionalMetrics.waterIntakeMl}
            onChange={(e) => handleAdditionalMetricChange('waterIntakeMl', e.target.value)}
            className="w-full px-3 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white text-sm"
            placeholder="e.g., 250, 500"
          />
        </div>
      </div>

      <button 
        type="submit"
        disabled={submitState.loading || !isFormValid() || errors.duration || errors.caloriesBurned}
        className={`px-8 py-3 font-medium rounded-lg transition transform ${
          !submitState.loading && isFormValid() && !errors.duration && !errors.caloriesBurned
            ? 'bg-blue-600 hover:bg-blue-700 text-white hover:scale-105 active:scale-95 cursor-pointer'
            : 'bg-gray-600 text-gray-400 cursor-not-allowed opacity-60'
        }`}
      >
        {submitState.loading ? 'Adding...' : 'Add Activity'}
      </button>

      {submitState.message && (
        <p
          className={`text-sm mt-3 ${
            submitState.type === 'success' ? 'text-green-400' : 'text-red-400'
          }`}
        >
          {submitState.message}
        </p>
      )}

      {!isFormValid() && (
        <p className="text-gray-400 text-sm mt-3">
          * Please fill in all required fields to add an activity
        </p>
      )}
    </form>
  )
}

export default ActivityForm
