package org.indilib.i4j.iparcos;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import static org.indilib.i4j.iparcos.MainActivity.TELESCOPE_TOUCH_ID;

/**
 * @author marcocipriani01
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }

        new AlertDialog.Builder(this).setTitle("Move to Telescope.Touch!")
                .setMessage("Dear user, thanks for using IPARCOS. Good news for you! " +
                        "To improve the app, I've decided to merge the project with Sky Map and create Telescope.Touch, " +
                        "so IPARCOS will be no longer updated.\nUpgrade to Telescope.Touch now!")
                .setIcon(R.drawable.new_icon)
                .setPositiveButton("Open Play Store", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + TELESCOPE_TOUCH_ID));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + TELESCOPE_TOUCH_ID));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }
}