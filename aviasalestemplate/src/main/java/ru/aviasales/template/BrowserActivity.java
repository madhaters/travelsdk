package ru.aviasales.template;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ru.aviasales.template.ui.fragment.BrowserFragment;

public class BrowserActivity extends AppCompatActivity {
	public static final String SHOW_LOADING_DIALOG = "show_loading_dialog";
	public static final String HOST = "HOST";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aviasales_fragment_layout);
		initFragment();
	}

	private void initFragment() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_child_place, BrowserFragment.newInstance(needToShowLoadingDialog(), getHost()), null);
		fragmentTransaction.commit();
	}

	private boolean needToShowLoadingDialog() {
		Intent intent = getIntent();
		return intent != null && intent.getExtras() != null && intent.getExtras().getBoolean(SHOW_LOADING_DIALOG, false);
	}

	private String getHost() {
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			return intent.getExtras().getString(HOST);
		}
		return null;
	}
}
