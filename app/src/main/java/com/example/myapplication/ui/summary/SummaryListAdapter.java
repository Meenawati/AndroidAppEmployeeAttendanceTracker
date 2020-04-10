package com.example.myapplication.ui.summary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;

import java.util.List;

public class SummaryListAdapter extends ArrayAdapter<VacationSummary> {

    private Context mContext;
    int mResource;
    TextView tv1, tv2, tv3, tv4;

    public SummaryListAdapter(Context context, int resource, List<VacationSummary> object) {
        super(context, resource, object);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String vacationType = getItem(position).vacationType;
        Integer leaveTaken = getItem(position).leaveTaken;
        Integer availableLeave = getItem(position).availableLeave;
        Integer total = getItem(position).totalDays;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        tv1 = (TextView) convertView.findViewById(R.id.type);
        tv2 = (TextView) convertView.findViewById(R.id.leaveTaken);
        tv3 = (TextView) convertView.findViewById(R.id.available);
        tv4 = (TextView) convertView.findViewById(R.id.total);

        tv1.setText(vacationType);
        tv2.setText(leaveTaken.toString());
        tv3.setText(availableLeave.toString());
        tv4.setText(total.toString());

        return convertView;
    }
}
