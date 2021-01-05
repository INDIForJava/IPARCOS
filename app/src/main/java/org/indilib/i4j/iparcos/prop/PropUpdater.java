package org.indilib.i4j.iparcos.prop;

import android.util.Log;

import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.iparcos.IPARCOSApp;
import org.indilib.i4j.iparcos.R;

/**
 * Thread to send updates to the server
 */
public class PropUpdater extends Thread {

    public PropUpdater(INDIProperty<?> prop) {
        super(() -> {
            try {
                prop.sendChangesToDriver();
            } catch (Exception e) {
                Log.e("PropertyUpdater", "Property update error!", e);
                IPARCOSApp.log(IPARCOSApp.getContext().getResources().getString(R.string.error) + " " + e.getLocalizedMessage());
            }
        }, "INDI property updater");
    }
}