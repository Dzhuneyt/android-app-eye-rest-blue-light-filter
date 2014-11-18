package com.hasmobi.eyerest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		final Button bEnable = (Button) rootView.findViewById(R.id.bEnable);
		final TextView tvOpacityLabel = (TextView) rootView.findViewById(R.id.tvOpacityLabel);
		final SeekBar seekbarOpacity = (SeekBar) rootView.findViewById(R.id.progressOpacity);
		final Spinner spColors = (Spinner) rootView.findViewById(R.id.spColors);

		final SharedPreferences sp = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

		if (sp.getBoolean("filter_enabled", false)) {
			// Initial button text
			bEnable.setText("Disable");
		}

		int opacityLevel = sp.getInt("opacity_percent", 20);
		seekbarOpacity.setProgress(opacityLevel);
		tvOpacityLabel.setText(opacityLevel + "%");

		seekbarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				sp.edit().putInt("opacity_percent", progress).commit();

				if (sp.getBoolean("filter_enabled", false)) {
					getActivity().startService(new Intent(getActivity(), OverlayService.class));
				}

				tvOpacityLabel.setText(progress + "%");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		bEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean currentlyEnabled = sp.getBoolean("filter_enabled", false);

				// Save the new filter state to SP
				sp.edit().putBoolean("filter_enabled", !currentlyEnabled).commit();

				if (currentlyEnabled) {
					// Disable now
					getActivity().stopService(new Intent(getActivity(), OverlayService.class));

					// Reset the button text to the other state
					bEnable.setText("Enable");
				} else {
					// Enable now
					getActivity().startService(new Intent(getActivity(), OverlayService.class));

					// Reset the button text to the other state
					bEnable.setText("Disable");
				}
			}
		});

		final List<OverlayColor> colors = new ArrayList<OverlayColor>();
		colors.add(new OverlayColor("Black", Color.BLACK));
		colors.add(new OverlayColor("Blue", Color.BLUE));
		colors.add(new OverlayColor("Red", Color.RED));
		colors.add(new OverlayColor("Green", Color.GREEN));
		colors.add(new OverlayColor("Cyan", Color.CYAN));

		ArrayAdapter<OverlayColor> spinnerArrayAdapter = new ArrayAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, colors);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spColors.setAdapter(spinnerArrayAdapter);

		// Set initial state to spinner
		int currentColor = sp.getInt("overlay_color", Color.BLACK);
		int i = 0;
		int positionOfSelectedColor = 0;
		for (OverlayColor current : colors) {
			if (current.color == currentColor) {
				positionOfSelectedColor = i;
			}
			i++;
		}
		spColors.setSelection(positionOfSelectedColor);

		spColors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				OverlayColor selectedColor = colors.get(position);

				sp.edit().putInt("overlay_color", selectedColor.color).commit();

				if (sp.getBoolean("filter_enabled", false)) {
					getActivity().startService(new Intent(getActivity().getBaseContext(), OverlayService.class));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Track Google Analytics screen view
		Tracker tracker = ((DApplication) activity.getApplication()).tracker;
		if (tracker != null) {
			// Set screen name.
			tracker.setScreenName("MainFragment");

			// Send a screen view.
			tracker.send(new HitBuilders.AppViewBuilder().build());
		}
	}
}
