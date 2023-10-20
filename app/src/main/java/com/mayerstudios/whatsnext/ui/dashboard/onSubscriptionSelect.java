package com.mayerstudios.whatsnext.ui.dashboard;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.mayerstudios.whatsnext.MainActivity;
import com.mayerstudios.whatsnext.R;
import com.mayerstudios.whatsnext.Subscription;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Handles the selection of subscriptions for the main fragment of the app,
 * allowing users to view information about the subscription and delete it.
 */

public class onSubscriptionSelect extends AppCompatActivity {
    Intent closeIntent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_subscription_select);

        Intent secondIntent = getIntent();

        closeIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Read in the ArrayList of Subscription objects from the relevant file
        // so that the contained data can be displayed.
        ArrayList<Subscription> subscriptions;

        try {
            FileInputStream fileIn = new FileInputStream(
                    "/data/data/com.mayerstudios.whatsnext/files/subData.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            subscriptions = (ArrayList<Subscription>) in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception i) {
            subscriptions = new ArrayList<>();
        }

        // Read in the ArrayList position of the subscription being opened, and
        // read that subscription.
        int position =
                Integer.parseInt(secondIntent.getStringExtra("LIST_POSITION"));
        Subscription currentSub = subscriptions.get(position);

        // Define graphical elements for later manipulation.
        TextView myText = findViewById(R.id.myText);
        TextView dateView = findViewById(R.id.dateView);
        TextView frequencyView = findViewById(R.id.frequencyText);
        TextView importanceText = findViewById(R.id.importanceText);

        // Set the graphical elements to the correct values.
        importanceText.setText(String.format("%s%.02f",
                getCurrencySymbol(currentSub.getCurrency()),
                currentSub.getPrice()));
        myText.setText(currentSub.getName());
        frequencyView.setText(currentSub.getPriority());
        dateView.setText(String.format("%02d/%02d/%02d",
                currentSub.getNextDueDate().getMonth(),
                currentSub.getNextDueDate().getDay(),
                currentSub.getNextDueDate().getYear()));

        // Handle the delete button.
        Button deleteButton = findViewById(R.id.buttonDelete);
        ArrayList<Subscription> finalSubscriptions = subscriptions;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Remove the unwanted element from the subscriptions array and
                // save it to the file.
                finalSubscriptions.remove(position);
                try {
                    FileOutputStream fos = new FileOutputStream(
                            "/data/data/com.mayerstudios.whatsnext/files/subData.dat");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(finalSubscriptions);
                    oos.close();
                    // Close the page, as the file was successfully updated.
                    startActivity(closeIntent);
                } catch (Exception e) {
                    // Show a toast describing the error.
                    Toast errorToast = Toast.makeText(
                            onSubscriptionSelect.super.getBaseContext(),
                            "Couldn't delete the subscription!",
                            Toast.LENGTH_SHORT);
                    errorToast.show();
                }
            }
        });
    }

    /**
     * Utility function to convert between currency codes and symbols.
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
