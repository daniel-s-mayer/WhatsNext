package com.mayerstudios.whatsnext;

import java.io.Serializable;
/***
* Allows for the creation of DueDate objects, used to store the due dates of subscriptions.
*/
public class DueDate implements Serializable {
    int month;
    int day;
    int year;

    public DueDate(int month, int day, int year) {
        this.month = month;
        this.day = day;
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getYear() {
        return year;
    }
}
