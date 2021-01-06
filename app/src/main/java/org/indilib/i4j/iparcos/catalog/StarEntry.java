package org.indilib.i4j.iparcos.catalog;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import org.indilib.i4j.iparcos.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Represents a star. This class also contains a loader to fetch stars from the app's catalog.
 */
public class StarEntry extends CatalogEntry {

    /**
     * Resource file.
     */
    private final static int RESOURCE = R.raw.stars;
    private final String names;
    private final String magnitude;

    /**
     * Create the entry from a catalog line
     */
    private StarEntry(String data) {
        String[] split = data.split("\\t");
        coord = new Coordinates(Double.parseDouble(split[0].trim()), Double.parseDouble(split[1].trim()));
        magnitude = split[2].trim();
        String hd = "HD" + split[4].trim(),
                sao = "SAO" + split[3].trim(),
                con = split[5].trim().replace("  ", " ");
        if (split.length == 7) {
            name = capitalize(split[6].trim().replace(";", ","));
            if (con.isEmpty()) {
                names = name + ", " + hd + ", " + sao;
            } else {
                names = name + ", " + con + ", " + hd + ", " + sao;
            }
        } else if (con.isEmpty()) {
            name = hd;
            names = hd + ", " + sao;
        } else {
            name = con;
            names = name + ", " + hd + ", " + sao;
        }
    }

    /**
     * Create the list of star entries
     *
     * @param context Context to access the catalog file
     */
    public static void loadToList(ArrayList<CatalogEntry> list, Context context) throws IOException {
        // Open and read the catalog file
        InputStream resourceStream = context.getResources().openRawResource(RESOURCE);
        BufferedReader br = new BufferedReader(new InputStreamReader(resourceStream));
        String line;
        while ((line = br.readLine()) != null) {
            list.add(new StarEntry(line));
        }
        resourceStream.close();
    }

    private static String capitalize(String string) {
        if (string.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        String[] split = string.split(" ");
        int i;
        for (i = 0; i < (split.length - 1); i++) {
            if (split[i].isEmpty()) continue;
            builder.append(split[i].charAt(0)).append(split[i].substring(1).toLowerCase()).append(" ");
        }
        builder.append(split[i].charAt(0)).append(split[i].substring(1).toLowerCase());
        return builder.toString();
    }

    /**
     * Create the description rich-text string
     *
     * @param ctx Context (to access resource strings)
     * @return description Spannable
     */
    @Override
    public Spannable createDescription(Context ctx) {
        Resources r = ctx.getResources();
        String str = "<b>" + r.getString(R.string.entry_names) + r.getString(R.string.colon_with_spaces) + "</b>" + names + "<br/>";
        str += "<b>" + r.getString(R.string.entry_type) + r.getString(R.string.colon_with_spaces) + "</b>" + r.getString(R.string.entry_star) + "<br/>";
        str += "<b>" + r.getString(R.string.entry_magnitude) + r.getString(R.string.colon_with_spaces) + "</b>" + magnitude + "<br/>";
        str += "<b>" + r.getString(R.string.entry_RA) + r.getString(R.string.colon_with_spaces) + "</b>" + coord.getRaStr() + "<br/>";
        str += "<b>" + r.getString(R.string.entry_DE) + r.getString(R.string.colon_with_spaces) + "</b>" + coord.getDeStr();
        return new SpannableString(Html.fromHtml(str));
    }

    /**
     * Create the summary rich-text string (1 line)
     *
     * @param ctx Context (to access resource strings)
     * @return summary Spannable
     */
    @Override
    public Spannable createSummary(Context ctx) {
        Resources r = ctx.getResources();
        String str = "<b>" + r.getString(R.string.entry_star) + "</b> " + r.getString(R.string.entry_mag) + ": " + magnitude;
        return new SpannableString(Html.fromHtml(str));
    }
}