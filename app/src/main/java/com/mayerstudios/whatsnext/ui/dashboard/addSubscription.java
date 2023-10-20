package com.mayerstudios.whatsnext.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mayerstudios.whatsnext.DueDate;
import com.mayerstudios.whatsnext.MainActivity;
import com.mayerstudios.whatsnext.R;
import com.mayerstudios.whatsnext.Subscription;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * AddSubscription class to provide a GUI for users to add new subscriptions.
 * The class also stores new subscriptions to a data file for later use.
 */
public class addSubscription
        extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // Data fields (and closing intent)
    Intent closeIntent;
    String[] currencies = {"USD", "EUR", "AUD", "CAD", "GBP", "JPY", "KRW"};
    int imageValue;
    String freqButtonData;

    // Graphical elements
    private EditText title;
    private EditText moneyAmount;
    private RadioGroup radioGroupFrequency;
    private RadioButton freqButton;

    /**
     * Interface-required method to draw the GUI and handle other interactive
     * elements.
     * @param savedInstanceState savedInstanceState to handle re-launches,
     *     crashes, etc.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the basic graphical framework for the addSubscription screen.
        setContentView(R.layout.activity_add_subscription);
        GridView gridview = findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
        imageValue = -1;
        // Set up a click listener to record which subscription icon the user
        // has selected.
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(
                    AdapterView<?> parent, View v, int position, long id) {
                imageValue = position;
                gridview.setDrawSelectorOnTop(false);
                gridview.setSelector(
                        getResources().getDrawable(R.drawable.greybox));
            }
        });
        super.onCreate(savedInstanceState);
        closeIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Set up the currency selection spinner.
        Spinner spin = findViewById(R.id.spinner1);
        // Create an ArrayAdapter containing the currencies.
        ArrayAdapter aa = new ArrayAdapter(
                this, android.R.layout.simple_spinner_item, currencies);
        aa.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        // Make the spinner contain data from the ArrayAdapter.
        spin.setAdapter(aa);

        title = findViewById(R.id.editTitle);
        final DatePicker date = findViewById(R.id.datePicker1);
        moneyAmount = findViewById(R.id.addMoneyAmount);

        // Set up the save button.
        Button button = findViewById(R.id.savebutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Recover the data and create a subscription.
                String subName = "";
                DueDate subDueDate;
                double amount = 0.00;
                String currency = "";
                int icon = 0;
                // Validate the subscription name and store it to be added
                // later.
                if (!title.getText().toString().equals("")
                        && !title.getText().toString().contains(",")) {
                    subName = title.getText().toString();
                } else {
                    error("Bad name!"); // Display an error toast if the name is
                    // blank.
                    return;
                }

                // Read and save the first due date for the subscription.
                subDueDate = new DueDate(
                        date.getMonth() + 1, date.getDayOfMonth(), date.getYear());

                // Read and save the monetary amount
                try {
                    amount =
                            Double.parseDouble(moneyAmount.getText().toString());
                    if (amount < 0) {
                        throw new RuntimeException(); // Impossible amount, so
                        // throw an error.
                    }
                } catch (Exception e) {
                    error("Invalid amount! "
                            + moneyAmount.getText()
                            .toString()); // Show a toast if the money amount
                    // is invalid in some way.
                    return;
                }

                // Get the selected currency from the spinner.
                currency = spin.getSelectedItem().toString();

                // Get the selected icon from the icon grid.
                icon = imageValue;

                // Determine whether the user selected monthly or yearly for
                // frequency.
                radioGroupFrequency = findViewById(R.id.radio_group_subs);
                int selectedId = radioGroupFrequency.getCheckedRadioButtonId();
                freqButton = findViewById(selectedId);
                if (radioGroupFrequency.getCheckedRadioButtonId() == -1) {
                    error("Invalid frequency!"); // The user didn't select an
                    // option,
                    // so we should display a toast indicating such.
                    return;
                } else {
                    freqButtonData = freqButton.getText().toString();
                }

                // Initialize the new subscription being created.
                Subscription current = new Subscription(subName, amount,
                        subDueDate, icon, currency, freqButtonData);
                try {
                    // Try to save the subscription.
                    saveSubscriptionToFile(current);
                } catch (Exception e) {
                    error(
                            "Couldn't save the subscription!"); // Display a toast
                    // warning that the
                    // subscription
                    // could not be
                    // saved.
                    return;
                }

                // Close the screen, returning to the main screen of the app.
                startActivity(closeIntent);
            }
        });
    }

    /**
     * Function to display toasts if the user makes an error (e.g. blank title,
     * negative price, etc.).
     * @param error The error message to display on the toast.
     */
    public void error(String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG)
                .show();
    }

    /**
     * Save the new subscription to the relevant data file.
     * @param current The subscription that should be added to the file.
     * @throws Exception All possible exceptions, which should be handled
     *     elsewhere.
     */

    public static void saveSubscriptionToFile(Subscription current)
            throws Exception {
        // Declare an ArrayList of subscriptions.
        ArrayList<Subscription> subscriptions;

        // Open the data file.
        File dataFile =
                new File("/data/data/com.mayerstudios.whatsnext/files/subData.dat");
        try {
            FileInputStream fis = new FileInputStream(dataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            subscriptions = (ArrayList<Subscription>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            // It appears the file doesn't exist.
            // We'll create it when we go to export the new subscription later.
            subscriptions = new ArrayList<>();
        }

        // Add the new element to the subscription list
        if (subscriptions.contains(current)) {
            // It's a duplicate, don't do anything. Just return.
            return;
        } else {
            subscriptions.add(current);
        }
        // Write the updated ArrayList to the file, overwriting its contents.
        FileOutputStream fos = new FileOutputStream(dataFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(subscriptions);
        oos.close();
    }

    @Override
    public void onItemSelected(
            AdapterView<?> adapterView, View view, int i, long l) {}

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}
