package com.mayerstudios.whatsnext.ui.settings;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.mayerstudios.whatsnext.R;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * SettingsFragment class to handle the graphical elements of the settings
 * component of the application.
 */
public class SettingsFragment extends Fragment {
    // All of the currencies the user can select.
    String[] currencies = {"USD", "EUR", "AUD", "CAD", "GBP", "JPY", "KRW"};
    // Array containing the user's dark mode (index 0) and currency (index 1)
    // preferences.
    Object[] preferences = new Object[2];

    /**
     * onCreateView method to draw the graphical elements and handle inputs to
     * them.
     * @param inflater Interface required parameter.
     * @param container Interface required parameter.
     * @param savedInstanceState Interface required parameter.
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Set up the root graphical elements and the attribution.
        View root =
                inflater.inflate(R.layout.fragment_settings, container, false);
        TextView textView = root.findViewById(R.id.attribution);
        textView.setText(
                "Icons from Icons8. Link to Icons8: https://icons8.com/");
        Linkify.addLinks(textView, Linkify.WEB_URLS);

        // Open the user's preference data file and read in the existing
        // contents.
        try {
            FileInputStream fileIn = new FileInputStream(
                    "/data/data/com.mayerstudios.whatsnext/files/prefData.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            preferences = (Object[]) in.readObject();
            in.close();
            fileIn.close();

        } catch (Exception i) {
            // No preferences have been saved, so choose the defaults.
            preferences[0] = false;
            preferences[1] = "USD";
        }

        // Set up the currency spinner.

        Spinner spin = root.findViewById(R.id.spinner1);
        ArrayAdapter aa = new ArrayAdapter(
                getContext(), android.R.layout.simple_spinner_item, currencies);
        aa.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
        spin.setSelection(
                Arrays.asList(currencies).indexOf((String) preferences[1]));

        // Set up the dark mode switch and process changes to it.
        Switch darkSwitch = root.findViewById(R.id.darkSwitch);
        darkSwitch.setChecked((Boolean) preferences[0]);
        darkSwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(
                            CompoundButton buttonView, boolean isChecked) {
                        // Update preferences
                        preferences[0] = isChecked;
                        try {
                            FileOutputStream fos = new FileOutputStream(
                                    "/data/data/com.mayerstudios.whatsnext/files/prefData.dat");
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(preferences);
                            oos.close();
                        } catch (Exception i) {
                            error("Couldn't save preferences!"); // Show a toast
                            // error message.
                        }
                        // Actually make the change in dark mode settings.
                        if (isChecked) {
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    }
                });
        // Handle changes in the user's selected primary currency.
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                String value;
                switch (Integer.parseInt(Long.toString(id))) {
                    case 1:
                        value = "EUR";
                        break;
                    case 2:
                        value = "AUD";
                        break;
                    case 3:
                        value = "CAD";
                        break;
                    case 4:
                        value = "GBP";
                        break;
                    case 5:
                        value = "JPY";
                        break;
                    case 6:
                        value = "KRW";
                        break;
                    default:
                        value = "USD";
                        break;
                }
                preferences[1] = value;
                // Save the updated currency to the data file.
                try {
                    ObjectOutputStream objOut =
                            new ObjectOutputStream(new FileOutputStream(
                                    "/data/data/com.mayerstudios.whatsnext/files/prefData.dat"));
                    objOut.writeObject(preferences);
                    objOut.close();
                } catch (Exception e) {
                    error("Could not save currency!");
                    // Show a toast indicating that there was an error.
                }
            }

            // Interface function; not used in this app.
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        return root;
    }

    /**
     * Function to display toasts if the user makes an error (e.g. blank title,
     * negative price, etc.).
     * @param error The error message to display on the toast.
     */
    public void error(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }
}
