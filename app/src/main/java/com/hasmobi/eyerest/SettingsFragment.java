package com.hasmobi.eyerest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	@Override
	public void onViewCreated(View root, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final GridView gridview = (GridView) root.findViewById(R.id.gridColors);
		DiscreteSeekBar sb = (DiscreteSeekBar) root.findViewById(R.id.sbDarkenIntensity);

		final ColorsAdapter adapter = new ColorsAdapter(getContext());
		gridview.setAdapter(adapter);

		SharedPreferences sp = Prefs.get(getContext());

		int opacityPercent = sp.getInt(Constants.PREF_DIM_LEVEL, 20);
		int currentColor = sp.getInt("overlay_color", Color.BLACK);

		Log.d(getClass().toString(), "Restoring darkening intensity to " + opacityPercent + " from preferences");
		sb.setProgress(opacityPercent);

		int totalColors = adapter.getCount();
		for (int i = 0; totalColors > i; i += 1) {
			OverlayColor c = adapter.getItem(i);
			if (c.color == currentColor) {
				Log.d(getClass().toString(), "Restored color from preferences: " + c.hex);
				adapter.setSelectedPosition(i);
				break;
			}
		}

		// When a different color is selected, preserve it in SharedPreferences
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				adapter.setSelectedPosition(position);
				OverlayColor selectedItem = adapter.getItem(position);

				SharedPreferences sp = Prefs.get(v.getContext());

				sp.edit().putInt("overlay_color", selectedItem.color).apply();

				if (sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
					if (sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
						v.getContext().startService(new Intent(v.getContext(), SchedulerService.class));
					} else {
						v.getContext().startService(new Intent(v.getContext(), OverlayService.class));
					}
				}

				Log.d(getClass().toString(), "Setting new color to " + selectedItem.hex);
			}
		});

		sb.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
			@Override
			public void onProgressChanged(DiscreteSeekBar seekBar, int intensityPercent, boolean fromUser) {
				if (!fromUser) return;

				SharedPreferences sp = Prefs.get(seekBar.getContext());

				sp.edit().putInt(Constants.PREF_DIM_LEVEL, intensityPercent).apply();

				if (sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
					getActivity().getBaseContext().startService(new Intent(getActivity().getBaseContext(), SchedulerService.class));
				} else {
					getActivity().getBaseContext().startService(new Intent(getActivity().getBaseContext(), OverlayService.class));
				}
			}

			@Override
			public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

			}
		});
	}

	static private class OverlayColor {
		public String label;
		public String hex;
		public int color;

		public OverlayColor(String label, String hex) {
			this.label = label;
			this.hex = hex;
			this.color = Color.parseColor(hex);
		}
	}

	static class ColorsAdapter extends BaseAdapter {

		private Context mContext;

		private int selectedPosition = 0;

		private List<OverlayColor> overlayColors = new ArrayList<>();

		public ColorsAdapter(Context c) {
			mContext = c;

			overlayColors.add(new OverlayColor("Black", "black"));
			overlayColors.add(new OverlayColor("Brown", "#49311c"));
			overlayColors.add(new OverlayColor("navy", "navy"));
			overlayColors.add(new OverlayColor("Red", "#c70202"));
			overlayColors.add(new OverlayColor("Green", "#019504"));
			overlayColors.add(new OverlayColor("Cyan", "cyan"));
			// Color picker: https://github.com/yukuku/ambilwarna
		}

		@Override
		public int getCount() {
			return overlayColors.size();
		}

		@Override
		public OverlayColor getItem(int position) {
			return overlayColors.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SquareImageView imageView;
			if (convertView == null) {
				// if it's not recycled, initialize some attributes
				imageView = new SquareImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (SquareImageView) convertView;
			}

			OverlayColor overlayColor = getItem(position);
			int color = android.graphics.Color.parseColor(overlayColor.hex);

			// Show or hide the overlaying "check" icon over
			// the specific color that was selected
			if (position == selectedPosition) {
				if (android.os.Build.VERSION.SDK_INT < 16) {
					imageView.setAlpha(255);
				} else {
					imageView.setImageAlpha(255);
				}
			} else {
				if (android.os.Build.VERSION.SDK_INT < 16) {
					imageView.setAlpha(0);
				} else {
					imageView.setImageAlpha(0);
				}
			}

			imageView.setImageResource(R.drawable.ic_check_white_48dp);

			final Bitmap bmp = Bitmap.createBitmap(85, 85, Bitmap.Config.ARGB_8888);
			bmp.eraseColor(color); // fill bitmap
			final BitmapDrawable ob = new BitmapDrawable(mContext.getResources(), bmp);

			if (android.os.Build.VERSION.SDK_INT < 16) {
				imageView.setBackgroundDrawable(ob);
			} else {
				imageView.setBackground(ob);
			}

			return imageView;
		}

		/**
		 * A helper method in the adapter that shows or hides
		 * the overlaying "check" icon above the selected color
		 *
		 * @param selectedPosition
		 */
		public void setSelectedPosition(int selectedPosition) {
			this.selectedPosition = selectedPosition;
			notifyDataSetChanged();
		}
	}
}
