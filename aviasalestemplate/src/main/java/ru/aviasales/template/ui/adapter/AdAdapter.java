package ru.aviasales.template.ui.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import ru.aviasales.core.ads.AdsManager;
import ru.aviasales.template.R;
import ru.aviasales.template.ads.AdsImplKeeper;

public class AdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int APPODEAL_AD_BANNER_TYPE = 1;
	private static final int AS_AD_BANNER_TYPE = 2;
	private static final int AS_BANNER_POSITION = 0;
	private static final int APPODEAL_BANNER_POSITION = 3;

	private final RecyclerView.Adapter baseAdapter;
	private final AdsManager.AdListener adListener;
	private boolean shouldShowAppodealAdBanner = false;
	private boolean shouldShowAsBanner = false;

	public AdAdapter(RecyclerView.Adapter baseAdapter, AdsManager.AdListener adListener) {
		this.baseAdapter = baseAdapter;
		this.adListener = adListener;

		this.baseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				notifyDataSetChanged();
			}

			@Override
			public void onItemRangeChanged(int positionStart, int itemCount) {
				notifyItemRangeChanged(positionStart, itemCount);
			}

			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				notifyItemRangeInserted(positionStart, itemCount);
			}

			@Override
			public void onItemRangeRemoved(int positionStart, int itemCount) {
				notifyItemRangeRemoved(positionStart, itemCount);
			}
		});
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == APPODEAL_AD_BANNER_TYPE) {
			View itemView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.results_ad_view, parent, false);
			return new AppodealAdViewHolder(itemView);
		} else if (viewType == AS_AD_BANNER_TYPE) {
			final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.as_ad_container, parent, false);
			return new AsAdViewHolder(view);
		} else {
			return baseAdapter.onCreateViewHolder(parent, viewType);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (isAsBanner(position)) {
			((AsAdViewHolder) holder).bind(adListener);
		} else if (isAppodealBanner(position)) {
		} else {
			baseAdapter.onBindViewHolder(holder, getRightItemPosition(position));
		}
	}

	@Override
	public int getItemCount() {
		int count = 0;
		if (baseAdapter != null) count += baseAdapter.getItemCount();
		count += getAdBannersCount();
		return count;
	}

	@Override
	public int getItemViewType(int position) {
		if (isAsBanner(position)) {
			return AS_AD_BANNER_TYPE;
		} else if (isAppodealBanner(position)) {
			return APPODEAL_AD_BANNER_TYPE;
		} else {
			return baseAdapter.getItemViewType(getRightItemPosition(position));
		}
	}

	private int getRightItemPosition(int position) {
		return baseAdapter == null ? 0 : position - getCardsCountBeforeCurrentPosition(position);
	}

	private int getCardsCountBeforeCurrentPosition(int position) {
		int count = 0;
		if (shouldShowAppodealAdBanner && APPODEAL_BANNER_POSITION + (shouldShowAsBanner ? 1 : 0) < position) {
			count++;
		}
		if (shouldShowAsBanner && AS_BANNER_POSITION < position) count++;
		return count;
	}

	private boolean isAppodealBanner(int position) {
		return shouldShowAppodealAdBanner && APPODEAL_BANNER_POSITION == position -
				(shouldShowAsBanner ? 1 : 0);
	}

	private boolean isAsBanner(int position) {
		return shouldShowAsBanner && AS_BANNER_POSITION == position;
	}

	private int getAdBannersCount() {
		int count = 0;
		if (shouldShowAppodealAdBanner && (baseAdapter != null && baseAdapter.getItemCount() >= APPODEAL_BANNER_POSITION)) {
			count++;
		}
		if (shouldShowAsBanner) count++;
		return count;
	}

	public void setShouldShowAppodealAdBanner(boolean shouldShowAppodealAdBanner) {
		this.shouldShowAppodealAdBanner = shouldShowAppodealAdBanner;
	}

	public void setShouldShowAsBanner(boolean shouldShowAsBanner) {
		this.shouldShowAsBanner = shouldShowAsBanner;
	}

	public static class AppodealAdViewHolder extends RecyclerView.ViewHolder {
		CardView cardView;

		public AppodealAdViewHolder(View itemView) {
			super(itemView);
			cardView = (CardView) itemView.findViewById(R.id.cv_results_item);
			View adView = AdsImplKeeper.getInstance().getAdsInterface().getNativeAdView((Activity) itemView.getContext());
			if (adView != null) {
				cardView.addView(adView);
			}
		}
	}

	public static class AsAdViewHolder extends RecyclerView.ViewHolder {
		final WebView webView;

		public AsAdViewHolder(View view) {
			super(view);
			webView = AdsManager.getInstance().getWebView();
			ViewGroup oldParent = (ViewGroup) webView.getParent();
			if (oldParent != null) {
				oldParent.removeView(webView);
			}
			((FrameLayout) view.findViewById(R.id.fl_web_view_container)).addView(webView);
		}

		public void bind(@Nullable AdsManager.AdListener adListener) {
			AdsManager adsManager = AdsManager.getInstance();
			String url = adsManager.getAdsResponse().getUrl();
			if (webView.getUrl() == null || !webView.getUrl().equals(url)) {
				webView.loadUrl(url);
			}
			adsManager.onShowResultsAds();
			if (adListener != null) {
				adsManager.setAdListener(adListener);
			}
		}
	}
}
