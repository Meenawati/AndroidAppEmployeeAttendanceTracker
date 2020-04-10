package com.example.myapplication.ui.detail;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.form.FormFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {
    private static ListView listView;
    public static ProgressDialog progressDialog;
    public static String vacationType;
    public int vacationDetailId;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FormFragment.vacationDetailId = null;
                MainActivity.navController.navigate(R.id.nav_form);
            }
        });

        listView = (ListView) root.findViewById(R.id.detailListView);
        registerForContextMenu(listView);
        startLoadData();
        return root;
    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        this.getActivity().getMenuInflater().inflate(R.menu.detail_tablerow_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        VacationDetail detail = (VacationDetail) listView.getItemAtPosition(acmi.position);
        vacationDetailId = detail.vacationDetailId;
        switch (item.getItemId()) {
            case R.id.edit_row:
                FormFragment.vacationDetailId = vacationDetailId;
                FormFragment.vacationType = detail.vacationType;
                FormFragment.selectedDate = detail.date;
                MainActivity.navController.navigate(R.id.nav_form);
                return true;
            case R.id.delete_row:
                new VacationDetailTask().execute("DELETE");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void startLoadData() {
        progressDialog = new ProgressDialog(this.getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Vacation Details..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        new VacationDetailTask().execute("GET");
    }

    public void loadData(List<VacationDetail> vacationDetailsList) {
        DetailsAdapter adapter = new DetailsAdapter(this.getContext(), R.layout.adapter_details_layout, vacationDetailsList);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.details_header, listView, false);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);
    }

    class VacationDetailTask extends AsyncTask<String, Integer, String> {
        List<VacationDetail> list = new ArrayList<>();
        String requestType;

        @Override
        protected String doInBackground(String... params) {
            requestType = params[0];
            String path = "http://192.168.0.8:8080/employees/1/vacations";
            if (params[0].equals("GET")) {
                try {
                    if (vacationType != null) {
                        path = path + "/" + vacationType;
                    }

                    URL url = new URL(path);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.connect();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = bufferedReader.readLine();
                    JSONArray array = new JSONArray(line);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = (JSONObject) array.get(i);
                        Integer detailId = jsonObject.getInt("vacationDetailId");
                        String vacationType = jsonObject.getString("vacationType");
                        String date = jsonObject.getString("date");

                        VacationDetail details = new VacationDetail();
                        details.vacationDetailId = detailId;
                        details.vacationType = vacationType;
                        details.date = date;

                        list.add(details);
                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();

                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (JSONException js) {
                    js.printStackTrace();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            } else if (requestType.equals("DELETE")) {
                try {
                    path = path + "/" + vacationDetailId;
                    URL url = new URL(path);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    // is output buffer writter
                    urlConnection.setRequestMethod("DELETE");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    int responseCode = urlConnection.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (requestType.equals("GET")) {
                progressDialog.hide();
                loadData(list);
            } else if (requestType.equals("DELETE")) {
                Toast.makeText(getContext(), "Vacation Detail deleted successfully", Toast.LENGTH_LONG).show();
                MainActivity.navController.navigate(R.id.nav_details);
            }
        }
    }

}