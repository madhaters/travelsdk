package ru.aviasales.template.ui.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.aviasales.core.AviasalesSDK;
import ru.aviasales.core.ads.AdsManager;
import ru.aviasales.core.search.params.Passengers;
import ru.aviasales.core.search.params.SearchParams;
import ru.aviasales.core.search.searching.SimpleSearchListener;
import ru.aviasales.template.R;
import ru.aviasales.template.ui.dialog.DatePickerDialogFragment;
import ru.aviasales.template.ui.dialog.PassengersDialogFragment;
import ru.aviasales.template.ui.dialog.TripClassDialogFragment;
import ru.aviasales.template.ui.listener.AviasalesImpl;
import ru.aviasales.template.ui.model.ComplexSearchParamsSegment;
import ru.aviasales.template.ui.model.SearchFormData;
import ru.aviasales.template.ui.model.SimpleSearchParams;
import ru.aviasales.template.ui.view.ComplexSearchFormView;
import ru.aviasales.template.ui.view.SearchFormPassengersButton;
import ru.aviasales.template.ui.view.SimpleSearchFormView;
import ru.aviasales.template.utils.CurrencyUtils;
import ru.aviasales.template.utils.DateUtils;
import ru.aviasales.template.utils.Utils;

public class SearchFormFragment extends BaseFragment{

    private final static String EXTRA_DIALOG_SEGMENT_SHOWED = "extra_dialog_segment_showed";
    private final static String EXTRA_IS_COMPLEX_SEARCH_SELECTED = "extra_is_complex_search_selected";

    private final static int DIALOG_DEPART_SEGMENT_NUMBER = 0;
    private final static int DIALOG_RETURN_SEGMENT_NUMBER = 1;

    private boolean isComplexSearchSelected = false;
    private AviasalesImpl aviasalesImpl;

    private View btnPassengers;
    private TextView tvPassengerAdults;
    private TextView tvPassengerInfants;
    private TextView tvPassengerChildern;

    private TextView tvReturnDate;
    private TextView tvDeparatureDate;
    private View btnReturnDate;
    private View btnDepartureDate;

    private View btnFrom;
    private View btnTo;
    private TextView tvFromCode;
    private TextView tvFromName;
    private TextView tvToCode;
    private TextView tvToName;





    private Button btnSearch;
    private RadioButton btnEconomy;
    private RadioButton btnBusiness;


    private SearchFormData searchFormData;


    private int dialogSegmentNumber;

    public static SearchFormFragment newInstance() {
        return new SearchFormFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setTextToActionBar(getString(getActivity().getApplicationInfo().labelRes));

        if (savedInstanceState != null) {
            dialogSegmentNumber = savedInstanceState.getInt(EXTRA_DIALOG_SEGMENT_SHOWED);
            isComplexSearchSelected = savedInstanceState.getBoolean(EXTRA_IS_COMPLEX_SEARCH_SELECTED);
        }

        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.simple_search_new, container, false);

        setHasOptionsMenu(true);
        showActionBar(true);

        aviasalesImpl = (AviasalesImpl) getParentFragment();

        setupViews(layout);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupData();
    }

    private void setupViews(final ViewGroup layout) {
        btnFrom=layout.findViewById(R.id.btnFrom);
        btnTo=layout.findViewById(R.id.btnTo);
        tvFromCode= (TextView) layout.findViewById(R.id.tvFrom);
        tvFromName= (TextView) layout.findViewById(R.id.tvFromName);
        tvToCode= (TextView) layout.findViewById(R.id.tvDestination);
        tvToName= (TextView) layout.findViewById(R.id.tvDestinationName);

        btnTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destinationButtonPressed();
            }
        });
        btnFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                originButtonPressed();
            }
        });

        btnPassengers = layout.findViewById(R.id.btn_passengers);
        tvPassengerAdults = (TextView) layout.findViewById(R.id.tvMalePassengers);
        tvPassengerChildern = (TextView) layout.findViewById(R.id.tvFemalePassengers);
        tvPassengerInfants = (TextView) layout.findViewById(R.id.tvKidPassengers);
        btnPassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPassengersDialog();
            }
        });

        btnReturnDate=layout.findViewById(R.id.btnDateReturn);
        btnDepartureDate=layout.findViewById(R.id.btnDepartData);
        tvDeparatureDate= (TextView) layout.findViewById(R.id.tvDepartDate);
        tvReturnDate= (TextView) layout.findViewById(R.id.tvReturnDate);
        btnDepartureDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDateDialog(new Date(),new Date(),DIALOG_DEPART_SEGMENT_NUMBER);
            }
        });
        btnReturnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDateDialog(new Date(),new Date(),DIALOG_RETURN_SEGMENT_NUMBER);
            }
        });

        btnEconomy = (RadioButton) layout.findViewById(R.id.btn_economy);
        btnBusiness = (RadioButton) layout.findViewById(R.id.btn_business);
        setTripClassListeners();


        btnSearch = (Button) layout.findViewById(R.id.btn_search);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() == null) return;


                if (isHaveRestrictions()) {
                    return;
                }

                if (!Utils.isOnline(getActivity())) {
                    Toast.makeText(getActivity(), getString(R.string.search_no_internet_connection), Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                SearchParams searchParams = searchFormData.createSearchParams(isComplexSearchSelected);
                AviasalesSDK aviasalesSDK = AviasalesSDK.getInstance();
                AdsManager.getInstance().prepareResultsAds(searchParams, CurrencyUtils.getAppCurrency(getActivity()));
                aviasalesSDK.startTicketsSearch(searchParams, new SimpleSearchListener() {

                });
                startFragment(SearchingFragment.newInstance(), true);
            }
        });

    }


    private void setupData() {
        if (getActivity() == null) return;

        searchFormData = aviasalesImpl.getSearchFormData();
        if (searchFormData.getPassengers()!=null){
            tvPassengerInfants.setText(String.valueOf(searchFormData.getPassengers().getInfants()));
            tvPassengerChildern.setText(String.valueOf(searchFormData.getPassengers().getChildren()));
            tvPassengerAdults.setText(String.valueOf(searchFormData.getPassengers().getAdults()));
        }
        if (searchFormData.getTripClass()!=null && searchFormData.getTripClass().equals(SearchParams.TRIP_CLASS_BUSINESS)){
            btnBusiness.setSelected(true);
        }
        if (searchFormData.getTripClass()!=null && searchFormData.getTripClass().equals(SearchParams.TRIP_CLASS_ECONOMY)){
            btnEconomy.setSelected(true);
        }

        SimpleSearchParams params=searchFormData.getSimpleSearchParams();
        if (params!=null) {
            if (params.getDestination()!=null) {
                tvToName.setText(params.getDestination().getCityName());
                tvToCode.setText(params.getDestination().getIata());
            }else{
                tvToCode.setText("Select");
                tvToName.setText("Select");
            }
            if (params.getOrigin()!=null) {
                tvFromCode.setText(params.getOrigin().getIata());
                tvFromName.setText(params.getOrigin().getCityName());
            }else{
                tvFromName.setText("Select");
                tvFromCode.setText("Select");
            }
            if (params.getDepartDate()!=null){
                tvDeparatureDate.setText(formatDate(params.getDepartDate()));
            }else{
                tvDeparatureDate.setText("Select");
            }
            if (params.getReturnDate()!=null){
                tvReturnDate.setText(formatDate(params.getReturnDate()));
            }else{
                tvReturnDate.setText("Select");
            }

        }
    }

    private void createPassengersDialog() {
        PassengersDialogFragment passengersDialogFragment = new PassengersDialogFragment(
                searchFormData.getPassengers(),
                new PassengersDialogFragment.OnPassengersChangedListener() {

                    @Override
                    public void onPassengersChanged(Passengers passengers) {
                        tvPassengerAdults.setText(String.valueOf(passengers.getAdults()));
                        tvPassengerChildern.setText(String.valueOf(passengers.getChildren()));
                        tvPassengerInfants.setText(String.valueOf(passengers.getInfants()));

                        searchFormData.setPassengers(passengers);
                        dismissDialog();
                    }

                    @Override
                    public void onCancel() {
                        dismissDialog();
                    }
                }
        );

        createDialog(passengersDialogFragment);
    }


    private void setTripClassListeners() {
        btnEconomy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFormData.setTripClass(SearchParams.TRIP_CLASS_ECONOMY);
            }
        });
        btnBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFormData.setTripClass(SearchParams.TRIP_CLASS_BUSINESS);
            }
        });
    }

    private void createDatePickerDialog(Calendar minDate, Calendar maxDate, Calendar currentDate, DatePickerDialogFragment.OnDateChangedListener listener) {
        if (getActivity() == null) return;

        DatePickerDialogFragment dateDialog = DatePickerDialogFragment.newInstance(minDate, maxDate, currentDate);
        dateDialog.setOnDateChangedListener(listener);
        createDialog(dateDialog);
    }

    private void createDateDialog(final Date currentDate, Date minDate, final int segmentNumber) {
        Calendar minCalendarDate = DateUtils.convertToCalendar(minDate);
        Calendar maxCalendarDate = DateUtils.getMaxCalendarDate();
        Calendar currentCalendarDate = DateUtils.convertToCalendar(currentDate);

        if (currentCalendarDate == null) {
            currentCalendarDate = minCalendarDate;
        }

        createDatePickerDialog(minCalendarDate, maxCalendarDate, currentCalendarDate, new DatePickerDialogFragment.OnDateChangedListener() {
            @Override
            public void onDateChanged(Calendar calendar) {

                if (segmentNumber == DIALOG_DEPART_SEGMENT_NUMBER) {
                    searchFormData.getSimpleSearchParams().setDepartDate(calendar);
                    searchFormData.getSimpleSearchParams().checkReturnDate();
                    tvDeparatureDate.setText(formatDate(calendar.getTime()));

                } else if (segmentNumber == DIALOG_RETURN_SEGMENT_NUMBER) {
                    searchFormData.getSimpleSearchParams().setReturnDate(calendar);
                    searchFormData.getSimpleSearchParams().setReturnEnabled(true);
                    tvReturnDate.setText(formatDate(calendar.getTime()));
                }

                dismissDialog();
            }

            @Override
            public void onCancel() {
                dismissDialog();
            }
        });
    }
    private String formatDate(Date date){
        return new SimpleDateFormat("dd MMM", Locale.ENGLISH).format(date);
    }

    private void createDateDialogFromSearchData(int dialogSegmentNumber) {
        this.dialogSegmentNumber = dialogSegmentNumber;

        SimpleSearchParams searchParams = searchFormData.getSimpleSearchParams();
        switch (dialogSegmentNumber) {
            case DIALOG_DEPART_SEGMENT_NUMBER:
                createDateDialog(searchParams.getDepartDate(), DateUtils.getMinDate(), dialogSegmentNumber);
                break;
            case DIALOG_RETURN_SEGMENT_NUMBER:
                createDateDialog(searchParams.getReturnDate(), searchParams.getDepartDate(), dialogSegmentNumber);
                break;
        }

    }


    private boolean isHaveRestrictions() {
        if (searchFormData.areDestinationsSet(isComplexSearchSelected)) {
            showConditionFailedToast(R.string.search_toast_destinations);
            return true;
        }
        if (searchFormData.areDestinationsEqual(isComplexSearchSelected)) {
            showConditionFailedToast(R.string.search_toast_destinations_equality);
            return true;
        }

        if (searchFormData.isDepartureDateNotSet(isComplexSearchSelected)) {
            showConditionFailedToast(R.string.search_toast_depart_date);
            return true;
        }

        if (searchFormData.isDepartDatePassed(isComplexSearchSelected)) {
            showConditionFailedToast(R.string.search_toast_wrong_depart_date);
            return true;
        }

        if (!isComplexSearchSelected && searchFormData.getSimpleSearchParams().isReturnEnabled()) {
            if (searchFormData.isSimpleParamsNoReturnDateSet()) {
                showConditionFailedToast(R.string.search_toast_return_date);
                return true;
            }

            if (searchFormData.isSimpleSearchReturnDatePassed()) {
                showConditionFailedToast(R.string.search_toast_wrong_return_date);
                return true;
            }

            if (searchFormData.isSimpleSearchReturnEarlierThanDeparture()) {
                showConditionFailedToast(R.string.search_toast_return_date_less_than_depart);
                return true;
            }

            if (searchFormData.isSimpleSearchDatedMoreThanYearAhead()) {
                showConditionFailedToast(R.string.search_toast_dates_more_than_1year);
                return true;
            }
        }
        return false;
    }

    private void showConditionFailedToast(int stringId) {
        Toast.makeText(getActivity(), getResources().getString(stringId), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        aviasalesImpl.saveState();
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.white)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_DIALOG_SEGMENT_SHOWED, dialogSegmentNumber);
        outState.putBoolean(EXTRA_IS_COMPLEX_SEARCH_SELECTED, isComplexSearchSelected);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void resumeDialog(String removedDialogFragmentTag) {
        switch (removedDialogFragmentTag) {
            case PassengersDialogFragment.TAG:
                createPassengersDialog();
                break;
            case TripClassDialogFragment.TAG:
                setTripClassListeners();
                break;
            case DatePickerDialogFragment.TAG:
                createDateDialogFromSearchData(dialogSegmentNumber);
                break;
        }
    }


    public void originButtonPressed() {
        if (getActivity() == null) return;
        startFragment(SelectAirportFragment.newInstance(SelectAirportFragment.TYPE_ORIGIN, isComplexSearchSelected, -1), true);
    }

    public void destinationButtonPressed() {
        if (getActivity() == null) return;
        startFragment(SelectAirportFragment.newInstance(SelectAirportFragment.TYPE_DESTINATION, isComplexSearchSelected, -1), true);
    }
}
