package com.example.myapplication.ui.employee;

import android.os.AsyncTask;

import com.example.myapplication.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class EmployeeModel {

    public void setEmployeeDetails() {
        new EmployeeDataTask().execute();
    }

    class EmployeeDataTask extends AsyncTask<Void, Void, Void> {
        String name;
        String department;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("http://192.168.0.8:8080/employees/1");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String data = bufferedReader.readLine();

                JSONObject jsonObject = new JSONObject(data);

                name = jsonObject.getString("name");
                department = jsonObject.getString("department");

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException js) {
                js.printStackTrace();
            } catch (Exception ee) {
                ee.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity.name.setText(this.name);
            MainActivity.department.setText(this.department);
        }
    }
}
