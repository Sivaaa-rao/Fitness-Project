import React from 'react';
import { Activity, Clock, Flame, Trophy } from 'lucide-react';

const ActivityStats = ({ activities = [] }) => {
  const totals = activities.reduce(
    (summary, activity) => {
      const duration = Number(activity.duration) || 0;
      const calories = Number(activity.caloriesBurned) || 0;

      summary.duration += duration;
      summary.calories += calories;

      if (!summary.favoriteType || (summary.typeCounts[activity.type] || 0) + 1 > summary.favoriteCount) {
        summary.favoriteType = activity.type;
        summary.favoriteCount = (summary.typeCounts[activity.type] || 0) + 1;
      }

      summary.typeCounts[activity.type] = (summary.typeCounts[activity.type] || 0) + 1;
      return summary;
    },
    {
      duration: 0,
      calories: 0,
      favoriteType: null,
      favoriteCount: 0,
      typeCounts: {}
    }
  );

  const averageCalories = activities.length
    ? Math.round(totals.calories / activities.length)
    : 0;

  const stats = [
    {
      label: 'Activities',
      value: activities.length,
      detail: 'total logged',
      icon: Activity,
      color: 'text-blue-300',
      bg: 'bg-blue-500/15'
    },
    {
      label: 'Training Time',
      value: `${totals.duration}`,
      detail: 'minutes',
      icon: Clock,
      color: 'text-emerald-300',
      bg: 'bg-emerald-500/15'
    },
    {
      label: 'Calories',
      value: `${totals.calories}`,
      detail: 'kcal burned',
      icon: Flame,
      color: 'text-orange-300',
      bg: 'bg-orange-500/15'
    },
    {
      label: 'Top Type',
      value: totals.favoriteType || 'None',
      detail: activities.length ? `${averageCalories} avg kcal` : 'log one activity',
      icon: Trophy,
      color: 'text-violet-300',
      bg: 'bg-violet-500/15'
    }
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-8">
      {stats.map((stat) => {
        const Icon = stat.icon;

        return (
          <div
            key={stat.label}
            className="border border-gray-700 bg-gray-900/50 rounded-lg p-5"
          >
            <div className="flex items-center justify-between gap-4">
              <div className="min-w-0">
                <p className="text-sm font-medium text-gray-400">{stat.label}</p>
                <p className="text-2xl font-bold text-white mt-1 truncate">{stat.value}</p>
                <p className="text-sm text-gray-500 mt-1">{stat.detail}</p>
              </div>
              <div className={`w-11 h-11 ${stat.bg} rounded-lg flex items-center justify-center flex-shrink-0`}>
                <Icon className={stat.color} size={22} />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default ActivityStats;
