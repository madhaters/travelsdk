package ru.aviasales.template.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import ru.aviasales.adsinterface.AdsInterface;
import ru.aviasales.core.AviasalesSDK;
import ru.aviasales.core.http.exception.ApiExceptions;
import ru.aviasales.core.search.object.SearchData;
import ru.aviasales.core.search.searching.SearchListener;
import ru.aviasales.template.R;
import ru.aviasales.template.ads.AdsImplKeeper;
import ru.aviasales.template.filters.manager.FiltersManager;
import ru.aviasales.template.utils.SortUtils;

public class SearchingFragment extends BaseFragment {

	public static final int ANIMATION_FINISH_DURATION = 1000;
	public static final int PROGRESS_BAR_LENGTH = 1000;

	private ProgressBar progressBar;
	private LinearLayout mrecContainer;
	private boolean isPaused = false;

	public static SearchingFragment newInstance() {
		return new SearchingFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.searching_fragment, container, false);

		setupViews(rootView);
		showActionBar(true);
		setTextToActionBar(getString(R.string.searching_information));
		setUpMrecAd();
		setupLoadingLogo(rootView);
		return rootView;
	}

	private void setUpMrecAd() {
		AdsInterface adsInterface = AdsImplKeeper.getInstance().getAdsInterface();
		View mrecView = adsInterface.getMrecView(getActivity());
		if (mrecView != null) {
			mrecContainer.addView(mrecView);
			adsInterface.showWaitingScreenAdsIfAvailable(getActivity());
		}
	}

	private void setupViews(View rootView) {
		progressBar = (ProgressBar) rootView.findViewById(R.id.pb_searching);
		mrecContainer = (LinearLayout) rootView.findViewById(R.id.mrec_container);

		progressBar.setMax(PROGRESS_BAR_LENGTH);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

		switch (AviasalesSDK.getInstance().getSearchingTicketsStatus()) {
			case SEARCHING:
				AviasalesSDK.getInstance().setOnTicketsSearchListener(new SearchListener() {
					@Override
					public void onSuccess(SearchData searchData) {
						SortUtils.resetSavedSortingType();
						FiltersManager.getInstance().initFilter(searchData, getActivity());

						onSearchSuccessFinish();
					}

					@Override
					public void onProgressUpdate(int i) {
						progressBar.setProgress(i);
					}

					@Override
					public void onCanceled() {

					}

					@Override
					public void onError(int errorCode, int responseCode, String searchId) {
						if (getActivity() == null) {
							return;
						}

						switch (errorCode) {
							case ApiExceptions.NO_RESULTS_EXCEPTION:
								showToastAndReturnToSearchForm(getString(R.string.alert_no_results));
								break;
							case ApiExceptions.SERVER_EXCEPTION:
							case ApiExceptions.API_EXCEPTION:
								showToastAndReturnToSearchForm(getString(R.string.toast_error_api));
								break;
							case ApiExceptions.CONNECTION_EXCEPTION:
								if (isDetached()) return;
								showToastAndReturnToSearchForm(getString(R.string.toast_error_connection));
								break;
							case ApiExceptions.WRONG_SIGNATURE_EXCEPTION:
								showToastAndReturnToSearchForm(getString(R.string.signature_toast));
								break;
							case ApiExceptions.IO_EXCEPTION:
							default:
								showToastAndReturnToSearchForm(getString(R.string.toast_error_unknown));
						}
					}
				});
				break;
		}
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	protected void resumeDialog(String removedDialogFragmentTag) {
	}

	private void onSearchSuccessFinish() {
		if (getActivity() == null) {
			return;
		}

		if (progressBar.getProgress() == progressBar.getMax()) {
			showResults();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ValueAnimator progressAnimator = ValueAnimator.ofInt(progressBar.getProgress(), PROGRESS_BAR_LENGTH);
			progressAnimator.setDuration(ANIMATION_FINISH_DURATION);
			progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
			progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@TargetApi(Build.VERSION_CODES.HONEYCOMB)
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					int progress = (Integer) animation.getAnimatedValue();
					progressBar.setProgress(progress);
				}
			});
			progressAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					showResults();
				}
			});
			progressAnimator.start();
		} else {
			showResults();
		}

	}

	private void showToastAndReturnToSearchForm(String toast) {
		if (getActivity() == null) return;
		Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
		if (!isPaused) {
			getActivity().onBackPressed();
		}
	}

	private void showResults() {
		if (!isPaused) {
			popFragmentFromBackStack();
			startFragment(ResultsFragment.newInstance(), true);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		isPaused = false;
		switch (AviasalesSDK.getInstance().getSearchingTicketsStatus()) {
			case CANCELED:
			case ERROR:
				getActivity().onBackPressed();
				break;
			case FINISHED:
				showResults();
				break;
		}
	}

	@Override
	public void onPause() {
		isPaused = true;
		super.onPause();
	}

	public void setupLoadingLogo(View rootView) {
		final ImageView loadingView= (ImageView) rootView.findViewById(R.id.flyLogo);
		Animation animation= AnimationUtils.loadAnimation(getContext(),R.anim.progress_logo);
		animation.setRepeatCount(Animation.INFINITE);
		final Animation toRight=outToRightAnimation();
		final Animation inLeft=inFromLeftAnimation();
		toRight.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				loadingView.startAnimation(inLeft);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		inLeft.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				loadingView.startAnimation(toRight);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		loadingView.startAnimation(inLeft);
	}
	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -.1f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(300);
		inFromLeft.setInterpolator(new LinearInterpolator());
		return inFromLeft;
	}
	private Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(3000);
		outtoRight.setInterpolator(new LinearInterpolator());
		return outtoRight;
	}
}
