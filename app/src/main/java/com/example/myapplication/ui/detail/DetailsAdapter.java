package com.example.myapplication.ui.detail;

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

public class DetailsAdapter extends ArrayAdapter<VacationDetail> {

    private Context mContext;
    int mResource;
    TextView tv1, tv2;

    public DetailsAdapter(Context context, int resource, List<VacationDetail> details) {
        super(context, resource, details);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String vacationType = getItem(position).vacationType;
        String date = getItem(position).date;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        tv1 = (TextView) convertView.findViewById(R.id.vacation_type);
        tv2 = (TextView) convertView.findViewById(R.id.date);

        tv1.setText(vacationType);
        tv2.setText(date);

        return convertView;
    }
}
