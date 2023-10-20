package com.mayerstudios.whatsnext.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mayerstudios.whatsnext.R;
import com.mayerstudios.whatsnext.Subscription;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * DahsboardFragment class to handle the graphical and interactive elements of
 * the dashboard component of the application.
 */
public class DashboardFragment extends Fragment {
    ListView subscriptionListViewDec;
    TextView textHeading;
    FloatingActionButton addNewButton;
    Intent intent;
    Intent addIntent;

    ArrayList<Subscription> subscriptions;

    /**
     * onCreateView method to initialize the graphical elements and establish
     * handling of user interactions.
     * @param inflater Interface-required parameter.
     * @param container Interface-required parameter.
     * @param savedInstanceState Interface-required parameter.
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Establish the two intents for later operations.
        intent = new Intent(getContext(),
                onSubscriptionSelect.class); // Handle selection of a subscription.
        addIntent = new Intent(getContext(),
                addSubscription.class); // Handle addition of a new subscription.

        // Read the user preferences (currency and dark mode) from the relevant
        // data files.
        Object[] preferences;
        try {
            FileInputStream fileIn = new FileInputStream(
                    "/data/data/com.mayerstudios.whatsnext/files/prefData.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            preferences = (Object[]) (in.readObject());
            in.close();
            fileIn.close();
        } catch (Exception i) {
            // It doesn't look like the user has any preferences yet, so create
            // the defaults.
            preferences = new Object[2];
            preferences[0] = false; // Dark mode off
            preferences[1] = "USD"; // Primary currency USD
        }
        // Activate/deactivate dark mode as necessary.
        if ((Boolean) preferences[0]) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Read the user's subscription data from the relevant file.
        try {
            FileInputStream fileIn = new FileInputStream(
                    "/data/data/com.mayerstudios.whatsnext/files/subData.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            subscriptions = (ArrayList<Subscription>) (in.readObject());
            in.close();
            fileIn.close();
        } catch (Exception i) {
            // It doesn't look like the ArrayList exists, so let's make a new
            // one. No need to save it.
            subscriptions = new ArrayList<>();
        }

        // Set up/declare variables to access the graphical elements.
        View root =
                inflater.inflate(R.layout.fragment_dashboard, container, false);
        subscriptionListViewDec = root.findViewById(R.id.reminderListView);
        textHeading = root.findViewById(R.id.textHeading);
        addNewButton = root.findViewById(R.id.addnew);
        TextView totalAmountText = root.findViewById(R.id.totalAmount);
        TextView yearlyView = root.findViewById(R.id.totalAmountYearly);

        // Loop through all of the subscriptions and determine the monthly and
        // yearly totals.
        double monthlyTotal = 0.00;
        double yearlyTotal = 0.00;
        for (Subscription s : subscriptions) {
            if (s.getPriority().equals("MONTHLY")
                    && s.getCurrency().equals((String) preferences[1])) {
                monthlyTotal += s.getPrice();
            } else if (s.getCurrency().equals((String) preferences[1])) {
                yearlyTotal += s.getPrice();
            }
        }

        // Determine which currency symbol to use for the total
        String currencySymbol;
        currencySymbol = getCurrencySymbol((String) preferences[1]);
        totalAmountText.setText(
                String.format("%s%.02f", currencySymbol, monthlyTotal));
        yearlyView.setText(
                String.format("%s%.02f", currencySymbol, yearlyTotal));

        // Create a HashMap to use with the subscription list adapter.
        ArrayList<HashMap<String, String>> list =
                new ArrayList<HashMap<String, String>>();
        if (subscriptions.size() != 0) {
            for (Subscription s : subscriptions) {
                HashMap<String, String> item;
                item = new LinkedHashMap<>();
                item.put("line1", s.getName());
                item.put("line2",
                        String.format("%s%.02f • %s",
                                getCurrencySymbol(s.getCurrency()), s.getPrice(),
                                s.getPriority()));
                item.put("line3", Integer.toString(s.getIcon()));
                item.put("line4", "#000000");
                list.add(item);
            }
        }

        // Set up the subscription list adapter.
        subscriptionListViewDec.setOnItemClickListener(listClick);

        SimpleAdapter myAdapter =
                new SimpleAdapter(getContext(), list, R.layout.list_row,
                        new String[] {"line1", "line2", "line3", "line4"},
                        new int[] {R.id.textHeading, R.id.textInfo, R.id.imageView4,
                                R.layout.list_row}) {
                    // Set the correct image for the subscription.
                    public void setViewImage(ImageView v, String value) {
                        list.indexOf(value);
                        v.setImageResource(getImage(Integer.parseInt(value)));
                    }
                };
        ((ListView) root.findViewById(R.id.reminderListView))
                .setAdapter(myAdapter);

        // Handle the case that the subscription fields are empty.
        TextView emptyText = root.findViewById(R.id.emptyTitle);
        TextView emptyMore = root.findViewById(R.id.emptyMore);

        subscriptionListViewDec.setEmptyView(emptyText);
        subscriptionListViewDec.setEmptyView(emptyMore);
        // Create a listener to allow new subscription creation.
        addNewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(addIntent);
            }
        });

        return root;
    }
    // Create a listener to handle the response when the user selects a
    // subscription.
    private AdapterView.OnItemClickListener listClick =
            new AdapterView.OnItemClickListener() {
                public void onItemClick(
                        AdapterView parent, View v, int position, long id) {
                    // Just send the index in the list to the next activity -- it
                    // can open/modify the files itself.
                    intent.putExtra("LIST_POSITION", Integer.toString(position));
                    startActivity(intent);
                }
            };

    /**
     * Convert an icon code to an Android element id for the correct image.
     * @param iconCode The icon code stored as part of the subscription.
     * @return The Android element id for the image.
     */
    public static int getImage(int iconCode) {
        int myImage;
        switch (iconCode) {
            case 0:
                myImage = R.drawable.question;
                break;

            case 1:
                myImage = R.drawable.tv;
                break;
            case 2:
                myImage = R.drawable.phone;
                break;
            case 3:
                myImage = R.drawable.smartphone;
                break;
            case 4:
                myImage = R.drawable.game;
                break;
            case 5:
                myImage = R.drawable.ic_gplay;
                break;
            case 6:
                myImage = R.drawable.ic_google;
                break;
            case 7:
                myImage = R.drawable.ic_office;
                break;
            case 8:
                myImage = R.drawable.ic_spotify;
                break;
            case 9:
                myImage = R.drawable.ic_netflix;
                break;
            case 10:
                myImage = R.drawable.ic_cloud;
                break;
            case 11:
                myImage = R.drawable.ic_news;
                break;
            case 12:
                myImage = R.drawable.wifi;
                break;
            case 13:
                myImage = R.drawable.ic_cc;
                break;
            case 14:
                myImage = R.drawable.pandora;
                break;
            case 15:
                myImage = R.drawable.amazon;
                break;
            case 16:
                myImage = R.drawable.vpn;
                break;
            case 17:
                myImage = R.drawable.math;
                break;
            case 18:
                myImage = R.drawable.amusic;
                break;
            case 19:
                myImage = R.drawable.video;
                break;
            case 20:
                myImage = R.drawable.box;
                break;
            case 21:
                myImage = R.drawable.hbo;
                break;
            case 22:
                myImage = R.drawable.autodesk;
                break;

            default:
                myImage = R.drawable.ic_launcher_foreground;
                break;
        }
        return myImage;
    }

    /**
     * Convert currency codes (e.g. USD) to symbols (e.g. $).
     * @param currencyCode The currency code to be converted.
     * @return The currency symbol.
     */
    public static String getCurrencySymbol(String currencyCode) {
        String symbol;

        switch (currencyCode) {
            case "CAD":
                symbol = "C$";
                break;
            case "EUR":
                symbol = "€";
                break;
            case "GBP":
                symbol = "₤";
                break;
            case "KRW":
                symbol = "₩";
                break;
            case "JPY":
                symbol = "¥";
                break;
            default:
                symbol = "$";
                break;
        }
        return symbol;
    }
}
