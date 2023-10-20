package com.mayerstudios.whatsnext;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * MainActivity class to handle fundamental program functions (in particular,
 * this class creates and sets the "alarm" for push notifications)
 */
public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "SUB";

    Object[] preferences;
    ArrayList<Subscription> subscriptions;

    /**
     * Register the notification channel, requesting permission as necessary.
     */
    private void createNotificationChannel() {
        // Check that the Android version is at least Oreo and, if so, register
        // the notification channel with the operating system. The OS handles
        // persmission checking as part of this.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Due Date Updates";
            String description =
                    "Allows What's Next by Mayer Studios to send alerts about upcoming "
                            + "subscriptions.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Handle the "creation" of a new instance of the app. This includes setting
     * the dark mode and registering the notification channel.
     * @param savedInstanceState Interface-required input to handle re-launches
     *     and intra-app
     * changes.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create and set the main content views; register notification handler.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        createNotificationChannel();

        // Read in the dat file containing the user's subscriptions.
        try {
            FileInputStream fileIn = new FileInputStream(
                    "/data/data/com.mayerstudios.whatsnext/files/subData.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            subscriptions = (ArrayList<Subscription>) in.readObject();
            in.close();
            fileIn.close();

        } catch (Exception i) {
            i.printStackTrace();
            subscriptions = new ArrayList<>();
        }
        // Read in the dat file containing the user's preferences.
        try {
            FileInputStream fileIn = new FileInputStream(
                    "/data/data/com.mayerstudios.whatsnext/files/prefData.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            preferences = (Object[]) in.readObject();
            in.close();
            fileIn.close();

        } catch (Exception i) {
            i.printStackTrace();
            preferences = new Object[2];
            preferences[0] = false;
            preferences[1] = "USD";
        }

        // Set dark mode to the setting stored in the prefData.dat file.
        if ((Boolean) preferences[0]) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Create the bottom navigation menu.
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration
                        .Builder(
                        R.id.navigation_dashboard, R.id.navigation_notifications)
                        .build();
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(
                this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Create the alarm to allow the app to send notifications on a regular
        // schedule.
        createAlarm(this);
    }

    /**
     * Create the daily "alarm" that fires daily to send a notification with the
     * subscriptions due that day.
     * @param context Application context for the alarm.
     */
    public static void createAlarm(Context context) {
        try {
            // Try to create the alarm manager and register the new alarm with
            // said AlarmManager.
            AlarmManager alarmMgr =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            Calendar calendar = Calendar.getInstance();
            // Set time to 12:00 midnight so that, even after Android dynamic
            // alarm firing, the notification is sent before 3:30 AM.
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    (86400000), // Fire every 24 hours.
                    alarmIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
