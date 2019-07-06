package com.hasmobi.eyerest.fragments.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.activities.MainActivity;
import com.hasmobi.eyerest.base.Application;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.custom_views.DiscreeteSeekBar;
import com.hasmobi.eyerest.custom_views.SquareImageView;
import com.hasmobi.eyerest.fragments.PrivacyPolicyFragment;
import com.hasmobi.eyerest.services.SchedulerService;
import com.thebluealliance.spectrum.SpectrumDialog;

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

        /*final ImageView ivEnableDisable = (ImageView) root.findViewById(R.id.ivEnableDisable);

        if (OverlayService.isEnabled(getContext())) {
            ivEnableDisable.setAlpha(1.0f);
        } else {
            ivEnableDisable.setAlpha(0.5f);
        }

        ivEnableDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enable or disable the dimmer service
                if (OverlayService.isEnabled(getContext())) {
                    OverlayService.disable(getContext());
                    v.setAlpha(0.5f);
                } else {
                    OverlayService.enable(getContext());
                    v.setAlpha(1);
                }
            }
        });*/

        root.findViewById(R.id.bPrivacyPolicy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrivacyPolicyFragment fPrivacyPolicy = PrivacyPolicyFragment.newInstance();
                fPrivacyPolicy.show(getActivity().getSupportFragmentManager(), this.getClass().toString());
            }
        });

        final Button bColorPicker = root.findViewById(R.id.bColorPicker);
        bColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypedArray ta = getResources().obtainTypedArray(R.array.mdcolor_500);
                int[] colors = new int[ta.length()];
                for (int i = 0; i < ta.length(); i++) {
                    String hex = ta.getString(i);
                    colors[i] = Color.parseColor(hex);
                }
                ta.recycle();

                SharedPreferences prefs = Prefs.get(view.getContext());
                int preselectColor = prefs.getInt(Constants.PREF_OVERLAY_COLOR, colors[0]);

                DialogFragment colorPickerDialog = new SpectrumDialog.Builder(getContext())
                        .setTitle("Select a color")
                        .setColors(colors)
                        .setSelectedColor(preselectColor)
                        .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                                if (!positiveResult) return;

                                SharedPreferences sp = Prefs.get(getContext());

                                sp.edit().putInt(Constants.PREF_OVERLAY_COLOR, color).apply();

                                Application.refreshServices(getContext());

                                refreshUI();
                            }
                        })
                        .build();
                colorPickerDialog.show(getFragmentManager(), "color_picker");
            }
        });

        refreshUI();

        final GridView gridview = root.findViewById(R.id.gridColors);
        final DiscreeteSeekBar sb = root.findViewById(R.id.sbDarkenIntensity);

        final ColorsAdapter adapter = new ColorsAdapter(getContext());
        gridview.setAdapter(adapter);

        SharedPreferences sp = Prefs.get(getContext());

        int opacityPercent = sp.getInt(Constants.PREF_DIM_LEVEL, 20);
        final int currentColor = sp.getInt("overlay_color", Color.BLACK);

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

                Application.refreshServices(v.getContext());

                Log.d(getClass().toString(), "Setting new color to " + selectedItem.hex);
            }
        });

        if (SchedulerService.isEnabled(getContext())) {
            ((MainActivity) getActivity()).showOrHideSchedulerUI(true);
        } else {
            ((MainActivity) getActivity()).showOrHideSchedulerUI(false);
        }
    }

    private void refreshUI() {
        SharedPreferences sp = Prefs.get(getContext());

        final int currentColor = sp.getInt("overlay_color", Color.BLACK);

        final Button bColorPicker = getView().findViewById(R.id.bColorPicker);
        bColorPicker.setBackgroundColor(currentColor);
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

    static private class ColorsAdapter extends BaseAdapter {

        private Context mContext;

        private int selectedPosition = 0;

        private List<OverlayColor> overlayColors = new ArrayList<>();

        public ColorsAdapter(Context c) {
            mContext = c;

            overlayColors.add(new OverlayColor("Black", "#000000"));
            overlayColors.add(new OverlayColor("Brown", "#3E2723"));
            overlayColors.add(new OverlayColor("Indigo", "#3949AB"));
            overlayColors.add(new OverlayColor("Blue", "#0D47A1"));
            overlayColors.add(new OverlayColor("Red", "#B71C1C"));
            overlayColors.add(new OverlayColor("Teal", "#004D40"));
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
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
                lp.setMargins(100, 100, 100, 100);
                imageView.setLayoutParams(lp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (SquareImageView) convertView;
            }

            final OverlayColor overlayColor = getItem(position);
            final int color = android.graphics.Color.parseColor(overlayColor.hex);

            // Show or hide the overlaying "check" icon over
            // the specific color that was selected
            if (position == selectedPosition) {
//                if (android.os.Build.VERSION.SDK_INT < 16) {
                imageView.setAlpha(1f);
//                } else {
//                    imageView.setImageAlpha(255);
//                }
            } else {
//                if (android.os.Build.VERSION.SDK_INT < 16) {
                imageView.setAlpha(0.7f);
//                } else {
//                    imageView.setImageAlpha(0);
//                }
            }

            imageView.setImageResource(R.drawable.ic_check_white_48dp);

            final Bitmap bmp = Bitmap.createBitmap(85, 85, Bitmap.Config.ARGB_8888);
            bmp.eraseColor(color); // fill bitmap
            final BitmapDrawable ob = new BitmapDrawable(mContext.getResources(), bmp);
            imageView.setBackground(ob);

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
