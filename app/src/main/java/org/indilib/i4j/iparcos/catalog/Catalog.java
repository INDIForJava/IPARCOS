package org.indilib.i4j.iparcos.catalog;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.util.Log;

import org.indilib.i4j.iparcos.IPARCOSApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A catalog of astronomical objects.
 *
 * @see DSOEntry
 * @see StarEntry
 */
public class Catalog {

    /**
     * Catalog objects.
     */
    private final List<CatalogEntry> entries = new ArrayList<>();
    private boolean ready = false;
    private boolean loading = false;
    private CatalogLoadingListener listener = null;

    public void load() {
        if (ready || loading) throw new IllegalStateException("Catalog already loaded/loading!");
        Resources resources = IPARCOSApp.getAppResources();
        try {
            loading = true;
            Log.i("CatalogManager", "Loading DSO...");
            DSOEntry.loadToList(entries, resources);
            Log.i("CatalogManager", "Loading stars...");
            StarEntry.loadToList(entries, resources);
            Collections.sort(entries);
            ready = true;
            callListener(true);
        } catch (Exception e) {
            Log.e("CatalogManager", "Unable to load catalog!", e);
            IPARCOSApp.log("Catalog loading error.");
            callListener(false);
        }
    }

    private void callListener(boolean success) {
        loading = false;
        if (listener != null) {
            listener.onLoaded(success);
            listener = null;
        }
    }

    public void setListener(CatalogLoadingListener listener) {
        this.listener = listener;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isReady() {
        return ready;
    }

    /**
     * @return an {@link ArrayList} containing all the entries of this catalog.
     */
    public List<CatalogEntry> getEntries() {
        return entries;
    }

    /**
     * Performs a search in the entries.
     *
     * @param query what to look for.
     * @return the first index corresponding to the given query.
     */
    public int searchIndex(final String query) {
        int index = Collections.binarySearch(entries, new CatalogEntry() {
            @Override
            public Coordinates getCoordinates() {
                return null;
            }

            @Override
            public String getName() {
                return query;
            }

            @Override
            public Spannable createDescription(Context ctx) {
                return null;
            }

            @Override
            public Spannable createSummary(Context ctx) {
                return null;
            }
        });
        if (index < 0) index = -index - 1;
        return index;
    }

    public interface CatalogLoadingListener {
        void onLoaded(boolean success);
    }
}