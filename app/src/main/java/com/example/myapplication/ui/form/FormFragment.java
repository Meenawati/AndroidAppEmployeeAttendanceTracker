package com.example.myapplication.ui.form;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FormFragment extends Fragment {
    private Button bt1, bt2;
    private Spinner spinner;
    public static String vacationType;
    public static String selectedDate;
    public static Integer vacationDetailId;
    private static final String TIMEZONE = "IST";
    private static final String DATEFORMAT = "yyyy-MM-dd";
    private String[] vacationArray = {"VACATION", "SICK", "PARENTAL", "MARRIAGE"};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_form, container, false);

        spinner = (Spinner) root.findViewById(R.id.dropDown);
        final MaterialCalendarView datepicker = (MaterialCalendarView) root.findViewById(R.id.datePicker1);
        if (vacationDetailId == null) {
            datepicker.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);
        } else {
            datepicker.setSelectedDate(formatStringToDate(selectedDate));
            spinner.setSelection(getVacationIndex(vacationType));
            datepicker.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        }

        bt1 = (Button) root.findViewById(R.id.submitButton);
        bt2 = (Button) root.findViewById(R.id.cancelButton);

        String vType = spinner.getSelectedItem().toString();
        List<CalendarDay> days = datepicker.getSelectedDates();

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vType = spinner.getSelectedItem().toString();
                List<CalendarDay> days = datepicker.getSelectedDates();
                if (isFormValid(vType, days)) {
                    List<String> dates = getDateStrings(days);
                    senddatatoserver(vType, dates);
                }
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navController.navigateUp();
            }
        });

        return root;
    }

    private int getVacationIndex(String vacation) {
        for (int i = 0; i < vacationArray.length; i++) {
            if (vacationArray[i].equals(vacation)) {
                return i;
            }
        }

        return -1;
    }

    public static Date formatStringToDate(String dateString) {
        if (dateString == null)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to parse date: " + dateString);
        }

        return date;
    }

    public List<String> getDateStrings(List<CalendarDay> days) {
        List<String> dates = new ArrayList<>();
        for (CalendarDay day : days) {
            String date = parseCalendarDayToDateString(day);
            dates.add(date);
        }

        return dates;
    }

    public String parseCalendarDayToDateString(CalendarDay day) {
        Date date = day.getDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public boolean isFormValid(String vType, List<CalendarDay> days) {
        if (days == null || days.size() == 0) {
            Toast.makeText(getContext(), "Please select the date", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public void senddatatoserver(String vacationType, List<String> dates) {
        JSONArray array = new JSONArray();
        if (vacationDetailId == null) {
            for (String date : dates) {
                array.put(convertVacationDetailToJson(vacationType, date));
            }
            if (array.length() > 0) {
                String aa = String.valueOf(array);
                new SaveData().execute("POST", aa);
            }
        } else {
            String aa = String.valueOf(convertVacationDetailToJson(vacationType, dates.get(0)));
            new SaveData().execute("UPDATE", aa);
        }

    }

    private JSONObject convertVacationDetailToJson(String vacationType, String date) {
        JSONObject post_dict = new JSONObject();
        try {
            post_dict.put("vacationType", vacationType);
            post_dict.put("date", date);
            if (vacationDetailId != null) {
                post_dict.put("vacationDetailId", vacationDetailId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return post_dict;
    }

    class SaveData extends AsyncTask<String, String, String> {
        String requestType;

        @Override
        protected String doInBackground(String... params) {
            requestType = params[0];
            String JsonDATA = params[1];
            String JsonResponse = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String path = "http://192.168.0.8:8080/employees/1/vacations";

            if (requestType.equals("POST")) {
                try {
                    URL url = new URL(path);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    // is output buffer writter
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                    writer.write(JsonDATA);
                    writer.close();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null)
                        buffer.append(inputLine + "\n");
                    if (buffer.length() == 0) {
                        return null;
                    }
                    JsonResponse = buffer.toString();
                    return JsonResponse;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException io) {
                    io.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                        }
                    }
                }
            } else if (requestType.equals("UPDATE")) {
                try {
                    path = path + "/" + vacationDetailId;
                    URL url = new URL(path);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    // is output buffer writter
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(JsonDATA);
                    out.close();
                    urlConnection.getInputStream();
                    int responseCode = urlConnection.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (requestType.equals("POST")) {
                Toast.makeText(getContext(), "Vacation applied successfully", Toast.LENGTH_LONG).show();
                MainActivity.navController.navigateUp();
            } else if (requestType.equals("UPDATE")) {
                Toast.makeText(getContext(), "Vacation updated successfully", Toast.LENGTH_LONG).show();
                MainActivity.navController.navigate(R.id.nav_details);
            }
        }
    }

}