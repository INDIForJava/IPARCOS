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
import java.util.List;

/**
 * Represents a deep sky object. This class also contains a loader to fetch DSO from the app's catalog.
 */
public class DSOEntry extends CatalogEntry {

    /**
     * Resource file.
     */
    private final static int RESOURCE = R.raw.ngc_ic_b;
    /**
     * The length of each line in the resource file.
     */
    private final static int ENTRY_LENGTH = 54;
    /**
     * The length of the name in each line.
     */
    private final static int NAME_LENGTH = 25;
    /**
     * The length of the magnitude in each line.
     */
    private final static int MAGNITUDE_LENGTH = 2;
    /**
     * The length of the type string in each line.
     */
    private final static int TYPE_LENGTH = 3;
    /**
     * The length of the object size in each line.
     */
    private final static int SIZE_LENGTH = 5;
    /**
     * The length of the RA coordinate in each line.
     */
    private final static int RA_LENGTH = 10;
    /**
     * The length of the DEC coordinate in each line.
     */
    //private final static int DEC_LENGTH = 9;

    private final String type;
    private final String size;
    private final String magnitude;

    /**
     * Create the entry from a formatted line
     * (ie. "Dumbbell nebula          8 Pl 15.2 19 59 36.1+22 43 00")
     *
     * @param data a line
     */
    private DSOEntry(String data) {
        int i = 0;
        name = data.substring(i, i + NAME_LENGTH).trim();
        i += NAME_LENGTH;
        magnitude = data.substring(i, i + MAGNITUDE_LENGTH).trim();
        i += MAGNITUDE_LENGTH;
        type = data.substring(i, i + TYPE_LENGTH).trim();
        i += TYPE_LENGTH;
        size = data.substring(i, i + SIZE_LENGTH).trim();
        i += SIZE_LENGTH;
        String raString = data.substring(i, i + RA_LENGTH).trim();
        i += RA_LENGTH;
        String decString = data.substring(i).trim();
        coord = new Coordinates(raString, decString);
    }

    public static void loadToList(List<CatalogEntry> list, Resources resources) throws IOException {
        // Open and read the catalog file
        InputStream resourceStream = resources.openRawResource(RESOURCE);
        BufferedReader br = new BufferedReader(new InputStreamReader(resourceStream));
        String line;
        while ((line = br.readLine()) != null) {
            list.add(new DSOEntry(line));
        }
        resourceStream.close();
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
        String str = "<b>" + r.getString(R.string.entry_type) + r.getString(R.string.colon_with_spaces) + "</b>" + r.getString(getType()) + "<br/>";
        if (!magnitude.equals("")) {
            str += "<b>" + r.getString(R.string.entry_magnitude) + r.getString(R.string.colon_with_spaces) + "</b>" + magnitude + "<br/>";
        }
        if (!size.equals("")) {
            str += "<b>" + r.getString(R.string.entry_size) + r.getString(R.string.colon_with_spaces) + "</b>" +
                    size + " " + r.getString(R.string.arcmin) + "<br/>";
        }
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
        String str = "<b>" + r.getString(getTypeShort()) + "</b> ";
        if (!magnitude.equals("")) {
            str += r.getString(R.string.entry_mag) + ": " + magnitude + " ";
        }
        if (!size.equals("")) {
            str += r.getString(R.string.entry_size).toLowerCase() + ": " + size + r.getString(R.string.arcmin);
        }
        return new SpannableString(Html.fromHtml(str));
    }

    /**
     * Return the string resource which correspond to the type acronym
     *
     * @return text
     */
    private int getType() {
        if (name.matches("B\\d+")) return R.string.dark_nebula;
        switch (type) {
            case "Gx":
                return R.string.entry_Gx;
            case "OC":
                return R.string.entry_OC;
            case "Gb":
                return R.string.entry_Gb;
            case "Nb":
                return R.string.entry_Nb;
            case "Pl":
                return R.string.entry_Pl;
            case "C+N":
                return R.string.entry_cluster_nebulosity;
            case "Ast":
                return R.string.entry_Ast;
            case "Kt":
                return R.string.entry_Kt;
            case "***":
                return R.string.entry_triStar;
            case "D*":
                return R.string.entry_doubleStar;
            case "*":
                return R.string.entry_star;
            case "?":
                return R.string.entry_uncertain;
            case "-":
                return R.string.entry_minus;
            case "PD":
                return R.string.entry_PD;
            case "":
            default:
                return R.string.entry_blank;
        }
    }

    /**
     * Return the string resource which correspond to the type acronym
     *
     * @return short text
     */
    private int getTypeShort() {
        if (name.matches("B\\d+")) return R.string.dark_nebula;
        switch (type) {
            case "Gx":
                return R.string.entry_short_Gx;
            case "OC":
                return R.string.entry_short_OC;
            case "Gb":
                return R.string.entry_short_Gb;
            case "Nb":
                return R.string.entry_short_Nb;
            case "Pl":
                return R.string.entry_short_Pl;
            case "C+N":
                return R.string.entry_short_cluster_nebula;
            case "Ast":
                return R.string.entry_short_Ast;
            case "Kt":
                return R.string.entry_short_Kt;
            case "***":
                return R.string.entry_short_triStar;
            case "D*":
                return R.string.entry_short_doubleStar;
            case "*":
                return R.string.entry_short_star;
            case "?":
                return R.string.entry_short_uncertain;
            case "-":
                return R.string.entry_short_minus;
            case "PD":
                return R.string.entry_short_PD;
            case "":
            default:
                return R.string.entry_short_blank;
        }
    }
}