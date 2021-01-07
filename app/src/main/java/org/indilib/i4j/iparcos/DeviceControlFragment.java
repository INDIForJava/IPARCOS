package org.indilib.i4j.iparcos;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.iparcos.prop.PropPref;

import java.util.HashMap;
import java.util.List;

public class DeviceControlFragment extends PreferenceFragmentCompat implements INDIDeviceListener {

    private static final String KEY_RECYCLER_STATE = "PrefsRecyclerViewState";
    private static final HashMap<INDIDevice, Bundle> recyclerviewBundles = new HashMap<>();
    private final HashMap<INDIProperty<?>, PropPref<?>> preferencesMap = new HashMap<>();
    private final HashMap<String, PreferenceCategory> groups = new HashMap<>();
    private INDIDevice device = null;
    private PreferenceScreen prefScreen;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.empty_preferences);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefScreen = getPreferenceScreen();
        if (device != null) {
            this.device.addINDIDeviceListener(this);
            preferencesMap.clear();
            groups.clear();
            for (String group : device.getGroupNames()) {
                List<INDIProperty<?>> props = device.getPropertiesOfGroup(group);
                if (!props.isEmpty()) {
                    PreferenceCategory prefGroup = new PreferenceCategory(context);
                    prefGroup.setIconSpaceReserved(false);
                    groups.put(group, prefGroup);
                    prefGroup.setTitle(group);
                    prefScreen.addPreference(prefGroup);
                    for (INDIProperty<?> prop : props) {
                        PropPref<?> pref = PropPref.create(context, prop);
                        if (pref != null) {
                            pref.setIconSpaceReserved(false);
                            preferencesMap.put(prop, pref);
                            prefGroup.addPreference(pref);
                        }
                    }
                }
            }
        }
        super.onViewCreated(view, savedInstanceState);
        if (device != null) {
            Bundle recyclerviewBundle = recyclerviewBundles.get(device);
            if (recyclerviewBundle != null) {
                RecyclerView.LayoutManager layoutManager = getListView().getLayoutManager();
                if (layoutManager != null)
                    layoutManager.onRestoreInstanceState(recyclerviewBundle.getParcelable(KEY_RECYCLER_STATE));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (device != null) {
            Bundle recyclerviewBundle = new Bundle();
            RecyclerView.LayoutManager layoutManager = getListView().getLayoutManager();
            if (layoutManager != null)
                recyclerviewBundle.putParcelable(KEY_RECYCLER_STATE, layoutManager.onSaveInstanceState());
            recyclerviewBundles.put(device, recyclerviewBundle);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        device.removeINDIDeviceListener(this);
    }

    public void setDevice(INDIDevice device) {
        this.device = device;
    }

    public void findPref(String newText) {
        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            PreferenceCategory group = ((PreferenceCategory) prefScreen.getPreference(i));
            for (int j = 0; j < group.getPreferenceCount(); j++) {
                Preference preference = group.getPreference(j);
                if (preference.getTitle().toString().toLowerCase().startsWith(newText.toLowerCase())) {
                    RecyclerView.LayoutManager layoutManager = getListView().getLayoutManager();
                    if (layoutManager != null) layoutManager.scrollToPosition(j);
                    return;
                }
            }
        }
    }

    @Override
    public void newProperty(INDIDevice device, final INDIProperty<?> property) {
        ((Activity) context).runOnUiThread(() -> {
            String group = property.getGroup();
            PreferenceCategory prefGroup = groups.get(group);
            if (prefGroup == null) {
                prefGroup = new PreferenceCategory(context);
                prefGroup.setIconSpaceReserved(false);
                groups.put(group, prefGroup);
                prefGroup.setTitle(group);
                prefScreen.addPreference(prefGroup);
            }
            PropPref<?> pref = PropPref.create(context, property);
            if (pref != null) {
                pref.setIconSpaceReserved(false);
                preferencesMap.put(property, pref);
                prefGroup.addPreference(pref);
            }
        });
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        PropPref<?> pref = preferencesMap.get(property);
        if (pref != null) {
            String group = property.getGroup();
            PreferenceCategory prefGroup = groups.get(group);
            if (prefGroup != null) {
                prefGroup.removePreference(pref);
                if (prefGroup.getPreferenceCount() == 0) {
                    prefScreen.removePreference(prefGroup);
                    groups.remove(group);
                }
            }
            preferencesMap.remove(property);
        }
    }

    @Override
    public void messageChanged(INDIDevice device) {

    }
}