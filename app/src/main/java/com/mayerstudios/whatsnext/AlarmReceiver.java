package com.mayerstudios.whatsnext;

import static java.lang.Integer.parseInt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/***
 * AlarmReceiver to handle the daily push notifications for users.
 * Receives an "alarm" event at approx. 2:00 AM each day and checks whether
 * there are any subscriptions for that day.
 */
public class AlarmReceiver extends BroadcastReceiver {
    ArrayList<Subscription> subscriptions;
    String todayReminders;
    String todaySubscriptions;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    /***
     * Handle the "alarm" event by sending a notification, as necessary.
     */
    public void onReceive(Context context, Intent intent) {
        // First, read the existing subscriptions.
        try {
            FileInputStream fis = new FileInputStream(
                    "/data/data/com.mayerstudios.whatsnext/files/subData.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            subscriptions = (ArrayList<Subscription>) ois.readObject();
            ois.close();
        } catch (Exception i) {
            i.printStackTrace();
            return;
            // Return if the file doesn't exist (i.e. no subscriptions), as
            // there is no need for a notification.
        }

        // Initialize general quantities needed throughout.
        LocalDate today = LocalDate.now();
        int subsToday = 0;
        double subsTotalToday = 0.00;

        // Identify subscriptions due today.
        for (Subscription sub : subscriptions) {
            // Monthly: Need only check whether day is correct/last day of
            // shorter month
            if (sub.getPriority().equals("MONTHLY")) {
                if ((sub.getDueDate().getDay() == today.getDayOfMonth()
                        || (sub.getDueDate().getDay() > today.getDayOfMonth()
                        && isLastDayOfMonth(today.getMonthValue(),
                        today.getDayOfMonth(), today.getYear())))) {
                    subsToday++;
                    subsTotalToday += sub.getPrice();
                }
            } else {
                // Yearly: Check whether month is correct and all considerations
                // of previous.
                if (sub.getDueDate().getMonth() == today.getMonthValue()
                        && (sub.getDueDate().getDay() == today.getDayOfMonth()
                        || (sub.getDueDate().getDay() > today.getDayOfMonth()
                        && isLastDayOfMonth(today.getMonthValue(),
                        today.getDayOfMonth(), today.getYear())))) {
                    subsToday++;
                    subsTotalToday += sub.getPrice();
                }
            }
        }

        // Determine the current local time in order to decide whether to fire
        // the notification.
        LocalTime lt = LocalTime.now();
        int hour = lt.getHour();
        int minute = lt.getMinute();

        // Only actually fire the notification if it is before 3:30 AM.
        if ((hour <= 3 && (minute < 30))) {
            PendingIntent pIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder b =
                    new NotificationCompat.Builder(context);
            // Set the notification content.
            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_notif)
                    .setTicker("notification")
                    .setContentTitle("Subscriptions Due Today")
                    .setContentText(String.format(
                            "You have %d subscriptions today, for a total of $%.02f.",
                            subsToday, subsTotalToday))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(
                            todaySubscriptions + todayReminders))
                    .setDefaults(
                            Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setContentIntent(pIntent)
                    .setContentInfo("Info")
                    .setChannelId("SUB");
            // Create the notification manager.
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(
                            Context.NOTIFICATION_SERVICE);
            // Actually fire the notification if and only if there is a
            // subscription to notify about.
            if (subsToday != 0) {
                notificationManager.notify(1, b.build());
            }
        }
    }

    /**
     * Utility function to determine whether it is the last day of the month.
     * @param month Current month
     * @param day Current day
     * @param year Current year (to account for leap years)
     * @return A boolean representing whether it is the last day of the month.
     */
    public static boolean isLastDayOfMonth(int month, int day, int year) {
        return day == getMonthDays(month, year);
    }

    /***
     * Utility function to determine the number of days in the given month.
     * @param month The month in question.
     * @param year The year of the month in question (to account for leap years)
     * @return The number of days in the given month.
     */
    public static int getMonthDays(int month, int year) {
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                return isLeapYear(year) ? 29 : 28;
        }
        return 0;
    }
    /***
     * Determine whether a given year is a leap year.
     * @param year Year to check.
     * @return Boolean representing whether the given year is a leap year.
     */
    public static boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true;
        }
        return year % 4 == 0 && year % 100 != 0;
    }
}
