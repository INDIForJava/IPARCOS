package org.indilib.i4j.iparcos;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDINumberProperty;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.client.INDISwitchElement;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.client.INDIValueException;
import org.indilib.i4j.iparcos.catalog.Catalog;
import org.indilib.i4j.iparcos.catalog.CatalogEntry;
import org.indilib.i4j.iparcos.catalog.Coordinates;
import org.indilib.i4j.iparcos.prop.PropUpdater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Allows the user to look for an astronomical object and slew the telescope.
 */
public class GoToFragment extends ListFragment
        implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Boolean>,
        INDIServerConnectionListener, INDIPropertyListener, INDIDeviceListener {

    private static final Catalog catalog = new Catalog();
    private static ArrayAdapter<CatalogEntry> entriesAdapter;
    private ConnectionManager connectionManager;
    private Context context;
    // INDI properties
    private INDINumberProperty telescopeCoordP = null;
    private INDINumberElement telescopeCoordRA = null;
    private INDINumberElement telescopeCoordDE = null;
    private INDISwitchProperty telescopeOnCoordSetP = null;
    private INDISwitchElement telescopeOnCoordSetSync = null;
    private INDISwitchElement telescopeOnCoordSetSlew = null;
    private INDISwitchElement telescopeOnCoordSetTrack = null;

    // ------ Astronomy utils ------

    /**
     * Equation taken from the book "Astronomical Algorithms" by Jean Meeus
     * (published by Willmann-Bell, Inc., Richmond, VA). Chapter 7, Julian Day.
     *
     * @see <a href="https://www.willbell.com/math/mc1.HTM">"Astronomical Algorithms" by Jean Meeus</a>
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private static double jd() {
        Calendar calendar = GregorianCalendar.getInstance();
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH) + 1;
        double d = calendar.get(Calendar.DATE) +
                (calendar.get(Calendar.HOUR) / 24.0) +
                (calendar.get(Calendar.MINUTE) / 1440.0) +
                (calendar.get(Calendar.SECOND) / 86400.0);
        if ((m == 1) || (m == 2)) {
            y--;
            m += 12;
        }
        return ((int) (365.25 * (y + 4716.0))) +
                ((int) (30.6001 * (m + 1.0))) +
                d + (2 - ((3 * y) / 400)) - 1524.5;
    }

    /**
     * Equation taken from the book "Astronomical Algorithms" by Jean Meeus
     * (published by Willmann-Bell, Inc., Richmond, VA). Chapter 21, Precession.
     *
     * @see <a href="https://www.willbell.com/math/mc1.HTM">"Astronomical Algorithms" by Jean Meeus</a>
     */
    private static Coordinates precess(Coordinates in) {
        Log.d("AstroUtils", "Precessing: " + in.toString());
        double jd = jd();
        Log.d("AstroUtils", "JD: " + jd);
        double dec0 = toRadians(in.getDec()),
                t = (jd - 2451545.0) / 36525.0,
                t2 = t * t, t3 = t2 * t,
                zeta = (0.6406161389 * t) + (0.00008385555556 * t2) + (0.000004999444444 * t3),
                theta = toRadians((0.5567530278 * t) - (0.0001185138889 * t2) - (0.00001162027778 * t3)),
                sinTheta = sin(theta),
                cosTheta = cos(theta),
                tmp = cos(dec0) * cos(toRadians(in.getRa() + zeta));
        Coordinates out = new Coordinates(toDegrees(
                atan2(cos(dec0) * sin(toRadians(in.getRa() + zeta)), (cosTheta * tmp) - (sinTheta * sin(dec0)))) +
                (0.6406161389 * t) + (0.0003040777778 * t2) + (0.000005056388889 * t3),
                toDegrees(asin((sinTheta * tmp) + (cosTheta * sin(dec0)))));
        Log.d("AstroUtils", "Result: " + out.toString());
        return out;
    }

    // ------ Android UI ------

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up INDI connection
        connectionManager = IPARCOSApp.getConnectionManager();
        connectionManager.addListener(this);
        // Enumerate existing properties
        if (connectionManager.isConnected()) {
            List<INDIDevice> list = connectionManager.getConnection().getDevicesAsList();
            if (list != null) {
                for (INDIDevice device : list) {
                    device.addINDIDeviceListener(this);
                    for (INDIProperty<?> property : device.getPropertiesAsList()) {
                        newProperty(device, property);
                    }
                }
            }
        } else {
            clearVars();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_catalog));
        setHasOptionsMenu(true);
        ArrayList<CatalogEntry> entries = catalog.getEntries();
        entriesAdapter = new ArrayAdapter<CatalogEntry>(context,
                android.R.layout.simple_list_item_2, android.R.id.text1, entries) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view.findViewById(android.R.id.text1))
                        .setText(entries.get(position).getName());
                ((TextView) view.findViewById(android.R.id.text2))
                        .setText(entries.get(position).createSummary(context));
                return view;
            }
        };
        setListAdapter(entriesAdapter);
        if (!catalog.isReady()) {
            // List loading
            setListShown(false);
            if (!catalog.isLoading())
                LoaderManager.getInstance(this).initLoader(0, null, this).forceLoad();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.add(R.string.mount_goto);
        item.setIcon(R.drawable.search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = new SearchView(context);
        searchView.setOnQueryTextListener(this);
        item.setActionView(searchView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectionManager.removeListener(this);
    }

    private void clearVars() {
        telescopeCoordP = null;
        telescopeCoordRA = null;
        telescopeCoordDE = null;
        telescopeOnCoordSetP = null;
        telescopeOnCoordSetSlew = null;
        telescopeOnCoordSetTrack = null;
        telescopeOnCoordSetSync = null;
    }

    /**
     * Called when the user changes the search string.
     *
     * @param newText the new query.
     * @return {@code false}, because the action is being handled by this listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (catalog.isReady()) setSelection(catalog.searchIndex(newText));
        return false;
    }

    /**
     * Called when the user submits the query. Nothing to do since it is done in {@link #onQueryTextChange(String)}
     *
     * @param query the new query. Ignored.
     * @return always {@code false}.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        ArrayList<CatalogEntry> entries = catalog.getEntries();
        final Coordinates coord = entries.get(position).getCoordinates();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(entries.get(position).createDescription(context)).setTitle(entries.get(position).getName());
        // Only display buttons if the telescope is ready
        if ((telescopeCoordP != null) && (telescopeOnCoordSetP != null)) {
            builder.setPositiveButton(R.string.GOTO, (dialog, which) -> {
                try {
                    telescopeOnCoordSetTrack.setDesiredValue(Constants.SwitchStatus.ON);
                    telescopeOnCoordSetSlew.setDesiredValue(Constants.SwitchStatus.OFF);
                    telescopeOnCoordSetSync.setDesiredValue(Constants.SwitchStatus.OFF);
                    new PropUpdater(telescopeOnCoordSetP).start();
                    Coordinates precessed = precess(coord);
                    telescopeCoordRA.setDesiredValue(precessed.getRaStr());
                    telescopeCoordDE.setDesiredValue(precessed.getDeStr());
                    new PropUpdater(telescopeCoordP).start();
                    Toast.makeText(context, context.getString(R.string.slew_ok), Toast.LENGTH_LONG).show();
                } catch (INDIValueException e) {
                    Toast.makeText(context, context.getString(R.string.sync_slew_error), Toast.LENGTH_LONG).show();
                }
            });
            builder.setNeutralButton(R.string.sync, (dialog, which) -> {
                try {
                    telescopeOnCoordSetSync.setDesiredValue(Constants.SwitchStatus.ON);
                    telescopeOnCoordSetTrack.setDesiredValue(Constants.SwitchStatus.OFF);
                    telescopeOnCoordSetSlew.setDesiredValue(Constants.SwitchStatus.OFF);
                    new PropUpdater(telescopeOnCoordSetP).start();
                    Coordinates precessed = precess(coord);
                    telescopeCoordRA.setDesiredValue(precessed.getRaStr());
                    telescopeCoordDE.setDesiredValue(precessed.getDeStr());
                    new PropUpdater(telescopeCoordP).start();
                    Toast toast = Toast.makeText(context, context.getString(R.string.sync_ok), Toast.LENGTH_LONG);
                    toast.show();
                } catch (INDIValueException e) {
                    Toast toast = Toast.makeText(context, context.getString(R.string.sync_slew_error), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {

        });
        builder.create().show();
    }

    /**
     * @return a catalog loader.
     * @see CatalogLoader
     */
    @NonNull
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new CatalogLoader(context);
    }

    /**
     * Binds the given catalog, loaded using {@link CatalogLoader}, to the Fragment's ListView.
     *
     * @param loader the loader.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Boolean> loader, Boolean result) {
        if (result) {
            entriesAdapter.notifyDataSetChanged();
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        } else if (isVisible()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.catalog_manager)
                    .setMessage(R.string.catalog_error)
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.error)
                    .show();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Boolean> loader) {

    }

    // ------ Listener functions from INDI ------

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        String name = property.getName();
        Log.i("SearchFragment", "New Property (" + name + ") added to device " + device.getName()
                + ", elements: " + Arrays.toString(property.getElementNames()));
        switch (name) {
            case "EQUATORIAL_EOD_COORD":
                if (((telescopeCoordDE = (INDINumberElement) property.getElement("DEC")) != null) &&
                        ((telescopeCoordRA = (INDINumberElement) property.getElement("RA")) != null) &&
                        (property instanceof INDINumberProperty)) {
                    property.addINDIPropertyListener(this);
                    telescopeCoordP = (INDINumberProperty) property;
                }
                break;
            case "ON_COORD_SET":
                if (((telescopeOnCoordSetTrack = (INDISwitchElement) property.getElement("TRACK")) != null) &&
                        ((telescopeOnCoordSetSlew = (INDISwitchElement) property.getElement("SLEW")) != null) &&
                        ((telescopeOnCoordSetSync = (INDISwitchElement) property.getElement("SYNC")) != null)) {
                    property.addINDIPropertyListener(this);
                    telescopeOnCoordSetP = (INDISwitchProperty) property;
                }
                break;
        }
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        String name = property.getName();
        Log.d("SearchFragment", "Removed property (" + name + ") to device " + device.getName());
        switch (name) {
            case "EQUATORIAL_EOD_COORD":
                telescopeOnCoordSetP = null;
                telescopeOnCoordSetSlew = null;
                telescopeOnCoordSetTrack = null;
                telescopeOnCoordSetSync = null;
                break;
            case "ON_COORD_SET":
                telescopeCoordP = null;
                telescopeCoordRA = null;
                telescopeCoordDE = null;
                break;
        }
    }

    @Override
    public void messageChanged(INDIDevice device) {

    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {

    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        Log.i("SearchFragment", "New device: " + device.getName());
        device.addINDIDeviceListener(this);
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        Log.d("SearchFragment", "Device removed: " + device.getName());
        device.removeINDIDeviceListener(this);
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        clearVars();
        // Move to the connection tab
        IPARCOSApp.goToConnectionTab();
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {

    }

    /**
     * Catalog loader.
     *
     * @author marcocipriani01
     */
    private static class CatalogLoader extends AsyncTaskLoader<Boolean> {

        /**
         * Class constructor.
         *
         * @param context the context that will be used to fetch entries.
         */
        CatalogLoader(@NonNull Context context) {
            super(context);
        }

        @Nullable
        @Override
        public Boolean loadInBackground() {
            Log.i("CatalogManager", "Loading catalog...");
            try {
                catalog.load(getContext());
                return true;
            } catch (NumberFormatException | IOException e) {
                Log.e("CatalogManager", "Unable to load catalog!", e);
                return false;
            }
        }
    }
}