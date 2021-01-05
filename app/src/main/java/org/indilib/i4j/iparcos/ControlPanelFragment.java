package org.indilib.i4j.iparcos;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControlPanelFragment extends Fragment implements INDIServerConnectionListener {

    private final ArrayList<Pair<INDIDevice, Fragment>> devicesAndFragments = new ArrayList<>();
    /**
     * Manages the connection with the INDI server.
     *
     * @see ConnectionManager
     */
    private ConnectionManager connectionManager;
    private DevicesFragmentAdapter devicesFragmentAdapter;
    private LinearLayout controlLayout;
    private TextView noDevicesText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_control_panel, container, false);
        controlLayout = rootView.findViewById(R.id.indi_control_layout);
        noDevicesText = rootView.findViewById(R.id.no_devices_label);
        devicesAndFragments.clear();
        ViewPager2 viewPager = rootView.findViewById(R.id.indi_control_pager);
        viewPager.setAdapter(devicesFragmentAdapter = new DevicesFragmentAdapter(this));
        viewPager.setSaveEnabled(false);
        new TabLayoutMediator(rootView.findViewById(R.id.indi_control_tabs), viewPager,
                (tab, position) -> tab.setText(devicesAndFragments.get(position).first.getName())).attach();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up INDI connection
        connectionManager = IPARCOSApp.getConnectionManager();
        connectionManager.addListener(this);
        // Enumerate existing properties
        if (!connectionManager.isConnected()) {
            noDevices();
        } else {
            List<INDIDevice> list = connectionManager.getConnection().getDevicesAsList();
            if (list.isEmpty()) {
                noDevices();
            } else {
                devicesAndFragments.clear();
                for (INDIDevice device : list) {
                    newDevice(device);
                }
                devicesFragmentAdapter.notifyDataSetChanged();
                devices();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noDevices();
        connectionManager.removeListener(this);
    }

    private void noDevices() {
        devicesAndFragments.clear();
        noDevicesText.post(() -> noDevicesText.setVisibility(View.VISIBLE));
        controlLayout.post(() -> controlLayout.setVisibility(View.GONE));
        devicesFragmentAdapter.notifyDataSetChanged();
    }

    private void devices() {
        noDevicesText.post(() -> noDevicesText.setVisibility(View.GONE));
        controlLayout.post(() -> controlLayout.setVisibility(View.VISIBLE));
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        noDevices();
        // Move to the connection tab
        IPARCOSApp.goToConnectionTab();
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        Log.i("ControlPanelFragment", "New device: " + device.getName());
        devicesFragmentAdapter.notifyItemInserted(newDevice(device));
        devices();
    }

    private int newDevice(INDIDevice device) {
        PrefsFragment fragment = new PrefsFragment();
        fragment.setDevice(device);
        Pair<INDIDevice, Fragment> pair = new Pair<>(device, fragment);
        devicesAndFragments.add(pair);
        return devicesAndFragments.indexOf(pair);
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        Log.d("ControlPanelFragment", "Device removed: " + device.getName());
        for (int i = 0; i < devicesAndFragments.size(); i++) {
            Pair<INDIDevice, Fragment> pair = devicesAndFragments.get(i);
            if (pair.first == device) {
                devicesAndFragments.remove(pair);
                if (devicesAndFragments.isEmpty()) {
                    noDevices();
                } else {
                    devicesFragmentAdapter.notifyItemRemoved(i);
                }
                return;
            }
        }
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {

    }

    private class DevicesFragmentAdapter extends FragmentStateAdapter {

        public DevicesFragmentAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return devicesAndFragments.get(position).second;
        }

        @Override
        public int getItemCount() {
            return devicesAndFragments.size();
        }
    }
}