package com.mayerstudios.whatsnext;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/***
 * Used to store subscription objects. Contains all fields for each
 * subscription, in addition to a a method to get the next due date.
 */
public class Subscription implements Serializable {
    private String name;
    private double price;
    private DueDate dueDate;
    private int icon;
    private String currency;
    private String priority;

    public Subscription(String name, double price, DueDate dueDate, int icon,
                        String currency, String priority) {
        this.name = name;
        this.price = price;
        this.dueDate = dueDate;
        this.icon = icon;
        this.currency = currency;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public DueDate getDueDate() {
        return dueDate;
    }

    public int getIcon() {
        return icon;
    }

    public String getPriority() {
        return priority;
    }

    public String getCurrency() {
        return currency;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDueDate(DueDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    /***
     * Gets the next due date of the subscription.
     * @return DueDate object representing the due date of the subscription.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public DueDate getNextDueDate() {
        // Create a current date for use throughout.
        LocalDate today = LocalDate.now();
        int todayDay = today.getDayOfMonth();
        int todayMonth = today.getMonthValue();

        // Break into monthly and yearly cases.
        if (this.priority.equals("MONTHLY")) {
            // If today's date is before the subscription's initial date, don't
            // do anything.
            if (this.getDueDate().getMonth() > todayMonth
                    || this.getDueDate().getYear() > today.getYear()) {
                return this.getDueDate();
            }
            // Case 1: The month has enough days, and we aren't to the due date
            // yet. Just make the month the current one, and return the other
            // fields as-is.
            if (todayDay <= this.getDueDate().getDay()) {
                return new DueDate(
                        todayMonth, this.getDueDate().getDay(), today.getYear());
            }
            // Case 2: We're past the due date but within the month.
            // Update the month and day.
            if (todayDay > this.getDueDate().getDay()) {
                int newDay = 0;
                int newMonth = 0;
                int newYear = 0;
                // Update the month to represent an increase, making sure to
                // handle December.
                if (todayMonth == 12) {
                    newMonth = 1;
                    newYear = today.getYear() + 1;
                } else {
                    newYear = today.getYear();
                    newMonth = todayMonth + 1;
                }

                // Make sure that there are enough days in the new month. If
                // not, fix the day at the last day of the month.
                if (getMonthDays(newMonth, newYear)
                        >= this.getDueDate().getDay()) {
                    newDay = this.getDueDate().getDay();
                } else {
                    newDay = getMonthDays(newMonth, newYear);
                }

                // Return the new due date.
                return new DueDate(newMonth, newDay, newYear);
            }
        } else {
            // Handle the yearly case.
            // If today's date is before the initial subscription date, don't do
            // anything.
            if (today.getYear() <= this.getDueDate().getYear()
                    && todayMonth <= this.getDueDate().getMonth()
                    && todayDay <= this.getDueDate().getDay()) {
                return this.getDueDate();
            }

            int newYear = 0;
            int newMonth = 0;
            int newDay = 0;
            // If today's date is after the due date (in this year), go to the
            // next year.
            if ((this.getDueDate().getMonth() == 2
                    && this.getDueDate().getDay() == 29)) {
                // The date is not Feburary 29, so there are no leap year
                // problems here.
                newYear = today.getYear() + 1;
                newMonth = this.getDueDate().getMonth();
                newDay = this.getDueDate().getDay();
            } else {
                // The date is February 29, so we need to handle the leap year
                // adjustment.
                if (!isLeapYear(today.getYear() + 1)) {
                    // Next year is not a leap year.
                    newYear = today.getYear() + 1;
                    newMonth = 3;
                    newDay = 1;
                } else {
                    // Next year is a leap year.
                    newYear = today.getYear() + 1;
                    newMonth = this.getDueDate().getMonth();
                    newDay = this.getDueDate().getDay();
                }
            }
            return new DueDate(newMonth, newDay, newYear);
        }
        // Return the default if some invalid input is passed.
        return new DueDate(0, 0, 0);
    }

    /***
     * Get the days in a given month.
     * @param month The month to get the number of days in.
     * @param year The year of the month (to handle leap years).
     * @return The number of days in the month.
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
