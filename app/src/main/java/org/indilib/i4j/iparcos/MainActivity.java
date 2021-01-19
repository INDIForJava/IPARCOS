package org.indilib.i4j.iparcos;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The main activity of the application, that manages all the fragments.
 *
 * @author marcocipriani01
 */
public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TELESCOPE_TOUCH_ID = "io.github.marcocipriani01.telescopetouch";
    private static final String TELESCOPE_TOUCH_MOVE_ACCEPTED = "TELESCOPE_TOUCH";
    /**
     * Last open page.
     */
    private Pages currentPage = Pages.CONNECTION;
    /**
     * The activity's toolbar.
     */
    private Toolbar toolbar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new ConnectionFragment()).commit();
        final BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        IPARCOSApp.setGoToConnectionTab(() -> runOnUiThread(() -> {
            currentPage = Pages.CONNECTION;
            toolbar.setElevation(8);
            navigation.setOnNavigationItemSelectedListener(null);
            navigation.setSelectedItemId(currentPage.itemId);
            navigation.setOnNavigationItemSelectedListener(this);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, Pages.CONNECTION.instance).commit();
        }));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(TELESCOPE_TOUCH_MOVE_ACCEPTED, false)) {
            new AlertDialog.Builder(this).setTitle("Move to Telescope.Touch!")
                    .setMessage("Dear user, thanks for using IPARCOS. Good news for you! " +
                            "To improve the app, I've decided to merge the project with Sky Map and create Telescope.Touch, " +
                            "so IPARCOS will be no longer updated.\nUpgrade to Telescope.Touch now!")
                    .setIcon(R.drawable.new_icon)
                    .setPositiveButton("Open Play Store", (dialog, which) -> {
                        preferences.edit().putBoolean(TELESCOPE_TOUCH_MOVE_ACCEPTED, true).apply();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Pages newPage = Pages.fromId(item.getItemId());
        if ((newPage != null) && (newPage != currentPage)) {
            if (newPage == Pages.GENERIC) {
                toolbar.setElevation(0);
            } else {
                toolbar.setElevation(8);
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, Pages.values()[newPage.ordinal()].instance).commit();
            currentPage = newPage;
            return true;
        }
        return false;
    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        CONNECTION(R.id.menu_connection, new ConnectionFragment()),
        MOTION(R.id.menu_move, new MountControlFragment()),
        GENERIC(R.id.menu_generic, new ControlPanelFragment()),
        SEARCH(R.id.menu_search, new GoToFragment()),
        FOCUSER(R.id.menu_focuser, new FocuserFragment());

        private final int itemId;
        private final Fragment instance;

        Pages(int itemId, Fragment instance) {
            this.itemId = itemId;
            this.instance = instance;
        }

        private static Pages fromId(int id) {
            for (Pages p : Pages.values()) {
                if (p.itemId == id) return p;
            }
            return null;
        }
    }
}