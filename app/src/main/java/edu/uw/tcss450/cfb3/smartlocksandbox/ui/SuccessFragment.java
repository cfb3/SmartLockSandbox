package edu.uw.tcss450.cfb3.smartlocksandbox.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.cfb3.smartlocksandbox.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SuccessFragment extends Fragment {

    private String mJwt;
    private String mUserName;

    public SuccessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootLayout, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootLayout, savedInstanceState);

        SuccessFragmentArgs args = SuccessFragmentArgs.fromBundle(getArguments());
        mJwt = args.getJwt();
        mUserName = args.getUserName();

        rootLayout.findViewById(R.id.button_success_signout).setOnClickListener(this::signOutAction);

        TextView tv = rootLayout.findViewById(R.id.text_success_msg);
        tv.setText("Welcome back " + mUserName);
    }

    private void signOutAction(final View theButton) {
        getActivity().finishAndRemoveTask();
    }
}
