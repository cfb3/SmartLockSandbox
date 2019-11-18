package edu.uw.tcss450.cfb3.smartlocksandbox.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.navigation.Navigation;
import edu.uw.tcss450.cfb3.smartlocksandbox.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private String mEmail;
    private String mJwt;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootLayout, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootLayout, savedInstanceState);

        // Attach a listener to the sign in button
        rootLayout.findViewById(R.id.button_login_signin).setOnClickListener(this::signInAction);
    }

    private void signInAction(final View theButton) {
        EditText emailEdit = getActivity().findViewById(R.id.edit_login_email);
        EditText passwordEdit = getActivity().findViewById(R.id.edit_login_password);

        boolean hasError = false;
        if (emailEdit.getText().length() == 0) {
            hasError = true;
            emailEdit.setError("Field must not be empty.");
        }  else if (emailEdit.getText().toString().chars().filter(ch -> ch == '@').count() != 1) {
            hasError = true;
            emailEdit.setError("Field must contain a valid email address.");
        }
        if (passwordEdit.getText().length() == 0) {
            hasError = true;
            passwordEdit.setError("Field must not be empty.");
        }

        if (!hasError) {
            // All of the input fields validate. Now, do login stuffs
            mEmail = ((EditText) getActivity().findViewById(R.id.edit_login_email))
                    .getText().toString();

            doLogin();
        }
    }

    private void doLogin() {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();

        new AttemptLoginTask().execute(uri.toString());
    }

    private void navigateToSuccess(final String email, final String jwt) {
        //Login was successful. Switch to the SuccessFragment.
        LoginFragmentDirections.ActionNavLoginToNavSuccess toNavSuccess =
                LoginFragmentDirections.actionNavLoginToNavSuccess(email, jwt);

        Navigation.findNavController(getView()).navigate(toNavSuccess);
    }


    class AttemptLoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            //If using Pushy, do Pushy stuffs here...

            //attempt to log in: Send credentials AND pushy token to the web service
            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection = null;

            try {
                URL urlObject = new URL(urls[0]);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());

                EditText emailEdit = getActivity().findViewById(R.id.edit_login_email);
                EditText passwordEdit = getActivity().findViewById(R.id.edit_login_password);
                JSONObject message = new JSONObject();
                message.put("email", emailEdit.getText().toString());
                message.put("password", passwordEdit.getText().toString());

                wr.write(message.toString());
                wr.flush();
                wr.close();

                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while((s = buffer.readLine()) != null) {
                    response.append(s);
                }
                publishProgress();
            } catch (Exception e) {
                response = new StringBuilder("Unable to connect, Reason: "
                        + e.getMessage());
                cancel(true);
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return response.toString();
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
            Log.e("LOGIN_ERROR", "Error in Login Async Task: " + s);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                JSONObject resultsJSON = new JSONObject(result);
                boolean success = resultsJSON.getBoolean(getString(R.string.keys_json_login_success));

                if (success) {

                    String jwtAsString = resultsJSON.getString(getString(R.string.keys_json_login_jwt));

                    if (((Switch) getActivity().findViewById(R.id.switch_login_stay)).isChecked()) {
                        //TODO Save jwt to shared prefs

                        //TODO Save Login Credentials

                    }

                    navigateToSuccess(mEmail, jwtAsString);

                    return;
                } else {
                    //Saving the token wrong. Don’t switch fragments and inform the user
                    ((TextView) getView().findViewById(R.id.edit_login_email))
                            .setError("Login Unsuccessful");
                    //Remove the wait progress bar
                    getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                //It appears that the web service didn’t return a JSON formatted String
                //or it didn’t have what we expected in it.
                Log.e("JSON_PARSE_ERROR",  result
                        + System.lineSeparator()
                        + e.getMessage());

                ((TextView) getView().findViewById(R.id.edit_login_email))
                        .setError("Login Unsuccessful");
                //Remove the wait progress bar
                getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
            }
        }
    }

}
