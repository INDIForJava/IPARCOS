package org.indilib.i4j.iparcos.catalog;

import android.content.Context;
import android.text.Spannable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

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
    private final ArrayList<CatalogEntry> entries = new ArrayList<>();
    private boolean ready = false;
    private boolean loading = false;

    public void load(Context context) throws IOException {
        if (ready || loading) throw new IllegalStateException("Catalog already loaded/loading!");
        loading = true;
        Log.i("CatalogManager", "Loading DSO...");
        DSOEntry.loadToList(entries, context);
        Log.i("CatalogManager", "Loading stars...");
        StarEntry.loadToList(entries, context);
        Collections.sort(entries);
        loading = false;
        ready = true;
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
    public ArrayList<CatalogEntry> getEntries() {
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
        if (index < 0) {
            index = -index - 1;
        }
        return index;
    }
}