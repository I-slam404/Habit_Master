package com.example.habitmaster;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private final ArrayList<Habit> habits;
    private final DBHelper dbHelper;
    private final Context context;

    HabitAdapter(Context context, ArrayList<Habit> habits) {
        this.context = context;
        this.habits = habits;
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_item, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.bind(habit);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName, tvHabitFrequency, tvHabitDate, tvTaskDateTime;
        Button btnDelete, btnComplete;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvHabitFrequency = itemView.findViewById(R.id.tvHabitFrequency);
            tvHabitDate = itemView.findViewById(R.id.tvHabitDate);
            tvTaskDateTime = itemView.findViewById(R.id.tvTaskDateTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnComplete = itemView.findViewById(R.id.btnComplete);

            // Handle item click to open DetailsActivity
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Habit habit = habits.get(position);
                    Intent intent = new Intent(context,Details.class);
                    intent.putExtra("habit_id", habit.getId());
                    intent.putExtra("habit_name", habit.getName());
                    intent.putExtra("habit_date", habit.getDateTime());
                    context.startActivity(intent);
                }
            });
        }

        @SuppressLint("SetTextI18n")
        void bind(Habit habit) {
            tvHabitName.setText(habit.getName());
            tvHabitFrequency.setText("Frequency: " + habit.getFrequency());

            // Display the date and time set by the user, or a default message if null
            if (habit.getDateTime() != null && !habit.getDateTime().isEmpty()) {
                tvTaskDateTime.setText("Task Date and Time: " + habit.getDateTime());
                setReminder(habit); // إعداد تذكير قبل 5 دقائق
            } else {
                tvTaskDateTime.setText("Task Date and Time: Not Set");
            }

            // Retrieve the last completion date and update UI
            String lastCompletionDate = dbHelper.getLastCompletionDate(habit.getId());
            if (lastCompletionDate != null) {
                tvHabitDate.setText("Last Completed: " + lastCompletionDate);
                setCompleteButtonState(false);
            } else {
                tvHabitDate.setText("Not Completed Yet");
                setCompleteButtonState(true);
            }

            // Delete button logic
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (habit == null || habit.getId() <= 0) {
                        Toast.makeText(context, "Invalid habit", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Habit")
                            .setMessage("Are you sure you want to delete this habit?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                cancelNotification(habit.getId());

                                boolean isDeleted = dbHelper.deleteHabit(habit.getId());
                                if (isDeleted) {
                                    if (position >= 0 && position < habits.size()) {
                                        habits.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.e("DeleteHabit", "Invalid position: " + position);
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to delete habit", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });

            // Complete button logic
            btnComplete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (habit == null || habit.getId() <= 0) {
                        Toast.makeText(context, "Invalid habit", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String dateCompleted = getCurrentDateTime();
                    Log.d("HabitCompletion", "dateCompleted: " + dateCompleted);

                    try {
                        boolean success = dbHelper.addHabitHistory(habit.getId(), dateCompleted);
                        boolean markedCompleted = dbHelper.markHabitAsCompleted(habit.getId(), dateCompleted);
                        Log.d("HabitCompletion", "addHabitHistory success: " + success);
                        Log.d("HabitCompletion", "markHabitAsCompleted success: " + markedCompleted);

                        if (success && markedCompleted) {
                            Toast.makeText(context, "Habit marked as completed on " + dateCompleted, Toast.LENGTH_SHORT).show();
                            setCompleteButtonState(false);
                            tvHabitDate.setText("Last Completed: " + dateCompleted);
                            cancelNotification(habit.getId()); // Cancel notification
                            // Play sound when button is clicked
                            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.completesound); // Replace with your sound file
                            mediaPlayer.start(); // Start playing the sound
                        } else {
                            Toast.makeText(context, "Failed to mark habit as completed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("HabitCompletion", "Error marking habit as completed: " + e.getMessage(), e);
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        // Method to set a reminder 5 minutes before the task time
        @SuppressLint("ScheduleExactAlarm")
        private void setReminder(Habit habit) {
            long taskTimeInMillis = getDateTimeInMillis(habit.getDateTime());
            long reminderTime = taskTimeInMillis - (5 * 60 * 1000);

            Intent intent = new Intent(context, MyNotificationReceiver.class);
            intent.putExtra("habit_name", habit.getName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );



            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent); // ضبط التنبيه
            }
        }

        // Method to cancel notification
        private void cancelNotification(int habitId) {

            Intent intent = new Intent(context, MyNotificationReceiver.class);
            intent.putExtra("habit_id", habitId);
            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getBroadcast(context, habitId, intent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getBroadcast(context, habitId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // Cancel the pending intent with AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);  // Cancel the alarm (pending intent)
            }
            pendingIntent.cancel();
        }


        // Utility method to parse date and time to milliseconds
        private long getDateTimeInMillis(String dateTime) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = sdf.parse(dateTime);
                return date != null ? date.getTime() : 0;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        // Set the state of the complete button
        private void setCompleteButtonState(boolean isEnabled) {
            if (isEnabled) {
                btnComplete.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                btnComplete.setEnabled(true);
            } else {
                btnComplete.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
                btnComplete.setEnabled(false);
            }
        }

        // Utility method to get the current date and time in yyyy-MM-dd HH:mm format
        private String getCurrentDateTime() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        }
    }
}
