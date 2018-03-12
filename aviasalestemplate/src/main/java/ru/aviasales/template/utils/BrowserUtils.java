package ru.aviasales.template.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.support.annotation.Nullable;

import ru.aviasales.template.BrowserActivity;
import ru.aviasales.template.ui.fragment.BrowserFragment;

public class BrowserUtils {

	public static void openBrowser(@Nullable Activity activity,
	                               @Nullable String url,
	                               @Nullable String title,
	                               @Nullable String host,
	                               boolean showLoadingDialog) {
		if (url == null || title == null) return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			openInternalBrowser(activity, url, title, host, showLoadingDialog);
		} else {
			openExternalBrowser(activity, url, host);
		}
	}

	public static void openExternalBrowser(@Nullable Activity activity, @Nullable String url, @Nullable String host) {
		if (activity == null) return;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		if (host != null) {
			Bundle bundle = new Bundle();
			bundle.putString(BrowserFragment.REFERER_HEADER, BrowserFragment.HTTP + host);
			intent.putExtra(Browser.EXTRA_HEADERS, bundle);
		}
		activity.startActivity(intent);
	}

	public static void openInternalBrowser(@Nullable Activity activity, @Nullable String url, @Nullable String title, @Nullable String host, boolean showLoadingDialog) {
		if (activity == null) return;
		Utils.getPreferences(activity)
				.edit()
				.putString(BrowserFragment.URL, url)
				.putString(BrowserFragment.TITLE, title)
				.commit();
		launchInternalBrowser(activity, showLoadingDialog, host);
	}

	private static void launchInternalBrowser(Activity activity, boolean showLoadingDialog, String host) {
		Intent intent = new Intent(activity, BrowserActivity.class);
		intent.putExtra(BrowserActivity.SHOW_LOADING_DIALOG, showLoadingDialog);
		intent.putExtra(BrowserActivity.HOST, host);
		activity.startActivity(intent);
	}
}
