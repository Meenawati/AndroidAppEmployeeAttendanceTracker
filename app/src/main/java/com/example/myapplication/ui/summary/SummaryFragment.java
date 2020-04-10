package com.example.myapplication.ui.summary;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.detail.DetailFragment;
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

public class SummaryFragment extends Fragment {
    private static ListView listView;
    public static ProgressDialog mProgressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FormFragment.vacationDetailId = null;
                MainActivity.navController.navigate(R.id.nav_form);
            }
        });

        listView = (ListView) root.findViewById(R.id.listView);
        registerForContextMenu(listView);
        startLoadData();
        return root;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        this.getActivity().getMenuInflater().inflate(R.menu.summary_tablerow_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.details_for_type:
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                VacationSummary summary = (VacationSummary) listView.getItemAtPosition(acmi.position);
                DetailFragment.vacationType = summary.vacationType;
                MainActivity.navController.navigate(R.id.nav_details);
                return true;
                default:
            return super.onContextItemSelected(item);
        }
    }

    public void startLoadData(){
        mProgressBar = new ProgressDialog(this.getContext());
        mProgressBar.setCancelable(false);
        mProgressBar.setMessage("Fetching Vacation Summary..");
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressBar.show();
        new VacationSummaryTask().execute();
    }

    public void loadData(List<VacationSummary> vacationSummaryList){
        SummaryListAdapter adapter = new SummaryListAdapter(this.getContext(), R.layout.adapter_view_layout, vacationSummaryList);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.summary_header,listView,false);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);
    }

    class VacationSummaryTask extends AsyncTask<Integer, Integer, String> {
        List<VacationSummary> list = new ArrayList<>();
        @Override
        protected String doInBackground(Integer... params){
            try {
                URL url =  new URL("http://192.168.0.8:8080/employees/1/vacations/summary");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = bufferedReader.readLine();
                JSONArray array = new JSONArray(line);
                for (int i = 0; i < array.length(); i++){
                    JSONObject jsonObject = (JSONObject) array.get(i);
                    String vacationType = jsonObject.getString("vacationType");
                    Integer daysTaken = jsonObject.getInt("daysTaken");
                    Integer totalDays = jsonObject.getInt("totalDays");
                    Integer availableDays = (totalDays - daysTaken);

                    VacationSummary summary = new VacationSummary();
                    summary.availableLeave = availableDays;
                    summary.vacationType = vacationType;
                    summary.totalDays = totalDays;
                    summary.leaveTaken = daysTaken;
                    list.add(summary);
                }
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException js){
                js.printStackTrace();
            } catch(Exception ee) {
                ee.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            mProgressBar.hide();
            loadData(list);
        }
    }
}