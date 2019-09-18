package com.hasmobi.eyerest.fragments;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hasmobi.eyerest.R;

import java.io.InputStream;

import io.noties.markwon.Markwon;

public class PrivacyPolicyFragment extends DialogFragment {

    public PrivacyPolicyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PrivacyPolicyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PrivacyPolicyFragment newInstance() {
        PrivacyPolicyFragment fragment = new PrivacyPolicyFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setPrivacyPolicy(view);

        view.findViewById(R.id.bClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }

    private void setPrivacyPolicy(View view) {
        // obtain an instance of Markwon
        final Markwon markwon = Markwon.create(getContext());

        try {
            Resources res = getResources();
            InputStream in_s = res.openRawResource(R.raw.privacy_policy);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            String sb = new String(b);
            Log.d(getClass().toString(), sb);

            // parse markdown and create styled text
            final Spanned markdown = markwon.toMarkdown(sb);
            TextView tvPrivacyPolicyBody = view.findViewById(R.id.tvPrivacyPolicyBody);

            markwon.setParsedMarkdown(tvPrivacyPolicyBody, markdown);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
