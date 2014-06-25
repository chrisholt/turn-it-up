package com.need2.turnitup.beta;
import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String>{
	
	private final Activity context;
	private final Integer imageId;
	private final ArrayList<String> names;
	private final ArrayList<String> times;
	private final ArrayList<String> ids;
	
	public CustomList(Activity context, ArrayList<String> names, ArrayList<String> times,  ArrayList<String> ids, Integer imageId) {
		super(context, R.layout.tiu_list_item, names);
		this.context = context;
		this.imageId = imageId;
		this.names = names;
		this.times = times;
		this.ids = ids;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.tiu_list_item, null, true);
				
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
		imageView.setImageResource(imageId);
		
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
		txtTitle.setText(names.get(position));
		
		TextView txtTime = (TextView) rowView.findViewById(R.id.time);
		txtTime.setText(times.get(position));
		
		TextView idText = (TextView) rowView.findViewById(R.id.idText);
		idText.setText(ids.get(position));
		
		return rowView;
	}
}