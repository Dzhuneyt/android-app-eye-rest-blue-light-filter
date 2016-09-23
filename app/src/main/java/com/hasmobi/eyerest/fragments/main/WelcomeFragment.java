package com.hasmobi.eyerest.fragments.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.services.OverlayService;

public class WelcomeFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_welcome, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Button bEnable = (Button) view.findViewById(R.id.bEnable);
		bEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences sp = Prefs.get(v.getContext());
				sp.edit().putBoolean(Constants.PREF_EYEREST_ENABLED, true).apply();

				v.getContext().startService(new Intent(v.getContext(), OverlayService.class));

				Snackbar.make(getActivity().findViewById(android.R.id.content), "Done! It was that easy.", Snackbar.LENGTH_LONG).show();

				getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main, new SettingsFragment()).commit();


			}
		});
	}
}
