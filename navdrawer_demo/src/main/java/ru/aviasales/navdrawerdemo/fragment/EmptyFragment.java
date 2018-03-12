package ru.aviasales.navdrawerdemo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.aviasales.navdrawerdemo.R;


public class EmptyFragment extends Fragment {

	public static final String TAG = "empty_fragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setActionBarTitle();
		return inflater.inflate(R.layout.content_main, null);
	}

	private void setActionBarTitle() {
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle("Empty fragment");
		}
	}

}
