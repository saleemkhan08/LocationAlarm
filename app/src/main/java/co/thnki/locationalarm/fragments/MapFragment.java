package co.thnki.locationalarm.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.locationalarm.R;
import co.thnki.locationalarm.doas.LocationAlarmDao;
import co.thnki.locationalarm.interfaces.GeoCodeListener;
import co.thnki.locationalarm.pojos.LocationAlarm;
import co.thnki.locationalarm.receivers.InternetConnectivityListener;
import co.thnki.locationalarm.services.LocationTrackingService;
import co.thnki.locationalarm.singletons.Otto;
import co.thnki.locationalarm.utils.GeoCoderTask;
import co.thnki.locationalarm.utils.LocationUtil;
import co.thnki.locationalarm.utils.MarkerAndCirclesUtil;
import co.thnki.locationalarm.utils.PermissionUtil;
import co.thnki.locationalarm.utils.TouchableWrapper;
import co.thnki.locationalarm.utils.TransitionUtil;

import static co.thnki.locationalarm.utils.LocationUtil.distFrom;

public class MapFragment extends SupportMapFragment implements
        TouchableWrapper.OnMapTouchListener,
        GeoCodeListener,
        GoogleMap.OnCameraChangeListener
{
    public static final String DIALOG_DISMISS = "dialogDismiss";

    @BindString(R.string.noInternet)
    String NO_INTERNET;

    private View mOriginalContentView;
    private int searchBarMargin;
    public boolean isSubmitButtonShown;
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";

    public static final String MAP_TYPE = "mapType";
    private static final String KEY_TRAVELLING_MODE_DISP_COUNTER = "KEY_TRAVELLING_MODE_DISP_COUNTER";
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 91;
    public static final String ACCURACY = "ACCURACY";

    private Marker myLocMarker;
    private Circle myLocCircle;

    private GoogleMap mGoogleMap;

    private AppCompatActivity mActivity;

    private static SharedPreferences preferences;


    private static boolean mTravelModeOn;
    @BindColor(R.color.travel_mode)
    int travelModeColor;

    @BindColor(R.color.colorAccent)
    int accentColor;

    @BindColor(R.color.my_location_radius)
    int radiusColor;

    @Bind(R.id.map_fab_buttons)
    ViewGroup map_fab_buttons;

    @Bind(R.id.searchBarInnerWrapper)
    RelativeLayout searchBar;

    @Bind(R.id.my_loc)
    FloatingActionButton buttonMyLoc;

    @Bind(R.id.submitButton)
    FloatingActionButton mSubmitButton;

    @Bind(R.id.searchBar)
    RelativeLayout mToolbar;

    @Bind(R.id.hoverPlaceName)
    TextView searchText;

    @Bind(R.id.searchProgress)
    ProgressBar searchProgress;

    @Bind(R.id.radiusSeekBarValueWrapper)
    View radiusSeekBarValueWrapper;

    @Bind(R.id.radiusSeekBarInnerWrapper)
    ViewGroup radiusSeekBarInnerWrapper;

    @Bind(R.id.titleBar)
    RelativeLayout mTitleBar;

    @Bind(R.id.radiusSeekBar)
    SeekBar radiusSeekBar;

    @Bind(R.id.radiusSeekBarValue)
    TextView radiusSeekBarValue;

    @Bind(R.id.select_location)
    View select_location;

//---------------------------------------------------------

    @Bind(R.id.searchIcon)
    ImageView searchIcon;

    private static int retryAttemptsCount;
    private GeoCoderTask mGeoCoderTask;
    private LatLng mGeoCodeLatLng, mOnActionDownLatLng;
    private int radiusType;
    private float currentZoom;
    private boolean moveCameraToMyLocOnLocUpdate;
    private boolean isPlacesApiResult;
    private MarkerAndCirclesUtil mMarkerAndCircle;
    private Circle mActionCircle;

    @BindString(R.string.unknownPlace)
    String UNKNOWN_PLACE;

    @BindDrawable(R.drawable.save_icon_alt)
    Drawable mSaveIcon;

    @BindDrawable(R.drawable.plus_white)
    Drawable mAddIcon;

    //Implementation done
    public MapFragment()
    {
        Log.d("MapFragmentFlowLogs", "Constructor");
    }

    //Implementation done
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        Log.d("MapFragmentFlowLogs", "onCreateView");
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        TouchableWrapper mTouchView = new TouchableWrapper(getActivity(), this);
        mTouchView.addView(mOriginalContentView);
        moveCameraToMyLocOnLocUpdate = true;
        return mTouchView;
    }

    //Implementation done
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d("MapFragmentFlowLogs", "onActivityCreated");
        mActivity = (AppCompatActivity) getActivity();
        ButterKnife.bind(this, mActivity);
        mTravelModeOn = false;
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        preferences.edit()
                .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 9)
                .putBoolean(LocationTrackingService.KEY_TRAVELLING_MODE, false)
                .apply();
        startLocationTrackingService();
        Log.d("MapFragmentFlowLogs", "Start Service Called");

        setupRadiusSeekBar();
        setUpMyLocationButton();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        Otto.register(this);
        Log.d("MapFragmentFlowLogs", "onResume");
        mGoogleMap = getMap();
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mMarkerAndCircle = new MarkerAndCirclesUtil(mGoogleMap, accentColor, radiusColor);
        if (PermissionUtil.isLocationPermissionAvailable())
        {
            mGoogleMap.setMyLocationEnabled(false);
        }
        mGoogleMap.setOnCameraChangeListener(this);
        if (isPlacesApiResult)
        {
            isPlacesApiResult = false;
        }
        else
        {
            showMyLocOnMap(false);
        }
    }

    private void showMyLocOnMap(boolean animate)
    {
        Log.d("MapFragmentFlowLogs", "showMyLocOnMap");
        LatLng latLng = getLatLng();
        showMyLocationMarkerAndCircle(latLng, getAccuracy());
        gotoLatLng(latLng, animate);
    }

    @Subscribe
    public void showAlarmOnMap(LocationAlarm alarm)
    {
        Log.d("MapFragmentFlowLogs", "showAlarmOnMap");
        LatLng latLng = LocationUtil.getLatLng(alarm.latitude, alarm.longitude);
        mMarkerAndCircle.addMarkerAndCircle(alarm);
        gotoLatLng(latLng, false);
    }

    private void setupRadiusSeekBar()
    {
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                setRadiusSeekBarValue();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        radiusSeekBarValueWrapper.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.radius_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        radiusType = item.getItemId();
                        setRadiusSeekBarValue();
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void setRadiusSeekBarValue()
    {
        String radiusSeekBarText;
        int radius = getSeekBarValue();
        switch (radiusType)
        {
            case R.id.radius_km:
                radiusSeekBarText = radius + getText(R.string.kilometer).toString();
                break;
            case R.id.radius_mi:
                radiusSeekBarText = radius + getText(R.string.miles).toString();
                break;
            case R.id.radius_fts:
                radiusSeekBarText = radius + getText(R.string.feet).toString();
                break;
            default:
                radiusSeekBarText = radius + getText(R.string.meter).toString();
                break;
        }

        radiusSeekBarValue.setText(radiusSeekBarText);
        drawCircleOnMap();
    }

    @OnClick(R.id.submitButton)
    public void alarmButton()
    {
        if (isSubmitButtonShown)
        {
            saveAlarm();
        }
        else
        {
            showSubmitButtonAndHideAddButton();
        }
    }

    private void showSubmitButtonAndHideAddButton()
    {
        if (PermissionUtil.isConnected(mActivity))
        {
            setupRadiusSeekBar();
            mSubmitButton.setIconDrawable(mSaveIcon);
            isSubmitButtonShown = true;

            TransitionUtil.slideTransition(radiusSeekBarInnerWrapper);
            TransitionUtil.slideTransition(mTitleBar);

            radiusSeekBarInnerWrapper.setVisibility(View.VISIBLE);
            mTitleBar.setVisibility(View.INVISIBLE);
            drawCircleOnMap();
        }
        else
        {
            Toast.makeText(mActivity, NO_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }

    public void hideSubmitButtonAndShowAddButton()
    {
        isSubmitButtonShown = false;
        mSubmitButton.setIconDrawable(mAddIcon);

        TransitionUtil.slideTransition(radiusSeekBarInnerWrapper);
        TransitionUtil.slideTransition(mTitleBar);
        radiusSeekBarInnerWrapper.setVisibility(View.INVISIBLE);
        mTitleBar.setVisibility(View.VISIBLE);
        removeActionCircle();
    }

    private void removeActionCircle()
    {
        if (mActionCircle != null)
        {
            mActionCircle.remove();
            mActionCircle = null;
        }
    }


    @Override
    public void onStop()
    {
        super.onStop();
        Log.d("MapFragmentFlowLogs", "onStop");
        mMarkerAndCircle.unregister();
        Otto.unregister(this);
        cancelAsyncTask(mGeoCoderTask);
    }

    private void setUpMyLocationButton()
    {
        buttonMyLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                moveCameraToMyLocOnLocUpdate = true;
                showTravellingModeHint();
                startLocationTrackingService();
                Log.d("MapFragmentFlowLogs", "My Loc : onClick");
                showMyLocOnMap(true);
            }
        });

        buttonMyLoc.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                moveCameraToMyLocOnLocUpdate = true;
                Log.d("MapFragmentFlowLogs", "My Loc onLongClick");
                showMyLocOnMap(true);
                if (!mTravelModeOn)
                {
                    Toast.makeText(mActivity, "Travelling Mode : On", Toast.LENGTH_SHORT).show();
                    mTravelModeOn = true;
                    updateTravellingModeUI();
                    showTravellingModeHint();
                    preferences.edit()
                            .putBoolean(LocationTrackingService.KEY_TRAVELLING_MODE, true)
                            .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 6)
                            .apply();
                    startLocationTrackingService();
                }
                else
                {
                    Toast.makeText(mActivity, "Travelling Mode : Off", Toast.LENGTH_SHORT).show();
                    mTravelModeOn = false;
                    updateTravellingModeUI();
                    preferences.edit()
                            .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 9)
                            .putBoolean(LocationTrackingService.KEY_TRAVELLING_MODE, false)
                            .apply();
                    Otto.post(LocationTrackingService.STOP_SERVICE);
                }
                return true;
            }
        });
        updateTravellingModeUI();
    }

    private void updateTravellingModeUI()
    {

        if (mTravelModeOn)
        {
            buttonMyLoc.setColorNormal(travelModeColor);
            buttonMyLoc.setColorPressedResId(R.color.travel_mode_pressed);
        }
        else
        {
            buttonMyLoc.setColorNormal(accentColor);
            buttonMyLoc.setColorPressedResId(R.color.colorAccentPressed);
        }
    }

    private void showTravellingModeHint()
    {
        int travellingModeInfoCounter = preferences.getInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 0);
        if (travellingModeInfoCounter < 5)
        {
            Toast.makeText(mActivity, "Click and hold to turn \"On\" Travelling Mode", Toast.LENGTH_SHORT).show();
            preferences.edit().putInt(KEY_TRAVELLING_MODE_DISP_COUNTER
                    , ++travellingModeInfoCounter).apply();
        }
        else if (travellingModeInfoCounter > 5 && travellingModeInfoCounter < 8)
        {
            Toast.makeText(mActivity, "Click and hold to turn \"Off\" Travelling Mode", Toast.LENGTH_SHORT).show();
            preferences.edit().putInt(KEY_TRAVELLING_MODE_DISP_COUNTER
                    , ++travellingModeInfoCounter).apply();
        }
    }

    private void startLocationTrackingService()
    {
        Log.d("MapFragmentFlowLogs", "startLocationTrackingService");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        mActivity.startService(intent);
    }

    private void saveAlarm()
    {
        Log.d("MapFragmentFlowLogs", "setAlarm");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        LocationAlarm alarm = new LocationAlarm();
        alarm.status = 1;
        alarm.radius = getRadiusInMeter();
        alarm.address = searchText.getText().toString();
        alarm.latitude = mGeoCodeLatLng.latitude + "";
        alarm.longitude = mGeoCodeLatLng.longitude + "";

        ArrayList<LocationAlarm> list = LocationAlarmDao.getList();
        boolean isDuplicate = false;
        for (LocationAlarm locationAlarm : list)
        {
            double dist = LocationUtil.distFrom(locationAlarm.latitude, locationAlarm.longitude, alarm.latitude, alarm.longitude);
            if (dist < (locationAlarm.radius))
            {
                isDuplicate = true;
                break;
            }
        }
        if (isDuplicate)
        {
            toast("Current location cannot fall under the radius of another Alarm");
        }
        else
        {
            LocationAlarmDao.insert(alarm);
            intent.putExtra(LocationTrackingService.KEY_ALARM_SET, true);
            mActivity.startService(intent);

            mMarkerAndCircle.addMarkerAndCircle(alarm);
            toast("Alarm Set : \n" + searchText.getText());
            hideSubmitButtonAndShowAddButton();
            Otto.post(LocationAlarmListFragment.RELOAD_LIST);
        }
    }


    private static CameraPosition getCameraPos(LatLng latLng)
    {
        return new CameraPosition(latLng, getZoom(), getTilt(), getBearing());
    }

    private static LatLng getLatLng()
    {
        double latitude = Double.parseDouble(preferences.getString(LATITUDE, "12.9667"));
        double longitude = Double.parseDouble(preferences.getString(LONGITUDE, "77.5667"));
        return new LatLng(latitude, longitude);
    }

    private static float getBearing()
    {
        return preferences.getFloat(BEARING, 0);
    }

    private static float getZoom()
    {
        float zoom = preferences.getFloat(ZOOM, 16);
        if (zoom < 3)
        {
            zoom = 16;
        }
        return zoom;
    }

    private static float getTilt()
    {
        return preferences.getFloat(TILT, 0);
    }

    private float getAccuracy()
    {
        return preferences.getFloat(ACCURACY, 500);
    }

    private void setCamera(CameraUpdate update, boolean animate)
    {
        if (animate)
        {
            mGoogleMap.animateCamera(update);
        }
        else
        {
            mGoogleMap.moveCamera(update);
        }
    }

    private void setCameraPosition(Location location)
    {
        preferences.edit().putString(LATITUDE, "" + location.getLatitude())
                .putString(LONGITUDE, "" + location.getLongitude())
                .putFloat(ACCURACY, location.getAccuracy())
                .apply();
    }

    private void gotoLatLng(LatLng latLng, boolean animate)
    {
        setMapType();
        setCamera(CameraUpdateFactory.newCameraPosition(getCameraPos(latLng)), animate);
    }

    private void setMapType()
    {
        int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "1"));
        if (mGoogleMap.getMapType() != mapType)
        {
            mGoogleMap.setMapType(mapType);
        }
    }

    private int getSeekBarValue()
    {
        int progress = radiusSeekBar.getProgress() + 1;
        switch (radiusType)
        {
            case R.id.radius_fts:
                return progress * 100;
            case R.id.radius_km:
                return progress;
            case R.id.radius_mi:
                return progress;
            default:
                return progress * 100;
        }
    }

    private void toast(String str)
    {
        Toast toast = Toast.makeText(mActivity, str, Toast.LENGTH_SHORT);
        ViewGroup view = (ViewGroup) toast.getView();
        ((TextView) view.getChildAt(0)).setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();
    }

    @Override
    public View getView()
    {
        return mOriginalContentView;
    }

    @Override
    public void onActionUp()
    {
        TransitionUtil.slideTransition(map_fab_buttons);
        map_fab_buttons.setVisibility(View.VISIBLE);

        TransitionUtil.defaultTransition(searchBar);

        RelativeLayout.LayoutParams searchBarLayoutParams = (RelativeLayout.LayoutParams) searchBar.getLayoutParams();
        searchBarLayoutParams.topMargin = searchBarMargin;
        searchBar.setLayoutParams(searchBarLayoutParams);

        mToolbar.animate().translationY(0).start();

        if (isSubmitButtonShown && mOnActionDownLatLng.equals(mGeoCodeLatLng))
        {
            drawCircleOnMap();
        }
    }

    @Override
    public void onActionDown()
    {
        mOnActionDownLatLng = mGeoCodeLatLng;
        TransitionUtil.slideTransition(map_fab_buttons);
        map_fab_buttons.setVisibility(View.GONE);
        mToolbar.animate().translationY(-mToolbar.getBottom()).start();

        TransitionUtil.defaultTransition(searchBar);

        RelativeLayout.LayoutParams searchBarLayoutParams = (RelativeLayout.LayoutParams) searchBar.getLayoutParams();
        searchBarMargin = searchBarLayoutParams.topMargin;
        searchBarLayoutParams.topMargin = 0;
        searchBar.setLayoutParams(searchBarLayoutParams);

        if (isSubmitButtonShown)
        {
            removeActionCircle();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                Place place = PlaceAutocomplete.getPlace(mActivity, data);
                Log.d("PlacesApi", "" + place);
                isPlacesApiResult = true;
                moveCameraToMyLocOnLocUpdate = false;
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        mGeoCodeLatLng = place.getLatLng();
                        Log.d("PlacesApi", "" + place.getAddress());
                        Log.d("PlacesApi", "" + place.getLatLng());
                        Log.d("PlacesApi", "" + place.getName());

                        gotoLatLng(mGeoCodeLatLng, true);
                        searchText.setText(place.getAddress());
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        Log.d("PlacesApi", "RESULT_ERROR");
                        Toast.makeText(mActivity, UNKNOWN_PLACE, Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("PlacesApi", "RESULT_CANCELED");
                        break;
                }
                break;
        }
    }

    @Subscribe
    public void onLocationChanged(Location location)
    {
        Log.d("MapFragmentFlowLogs", "onLocationChanged : moveCameraToMyLocOnLocUpdate : " + moveCameraToMyLocOnLocUpdate);
        setCameraPosition(location);
        if (moveCameraToMyLocOnLocUpdate)
        {
            showMyLocOnMap(true);
        }
    }

    private void showMyLocationMarkerAndCircle(LatLng latLng, float accuracy)
    {
        if (myLocMarker != null)
        {
            myLocMarker.remove();
        }

        myLocMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(LocationUtil.getMapMarker(mActivity, R.mipmap.my_loc_dot_48, 17)));

        if (myLocCircle != null)
        {
            myLocCircle.remove();
        }
        myLocCircle = mGoogleMap.addCircle(new CircleOptions()
                .radius(accuracy)
                .strokeWidth(2)
                .strokeColor(accentColor)
                .fillColor(radiusColor)
                .center(latLng));
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        double dist = distFrom(cameraPosition.target, getLatLng());
        currentZoom = cameraPosition.zoom;
        mGeoCodeLatLng = cameraPosition.target;
        moveCameraToMyLocOnLocUpdate = dist < 100;
        updateLocationInfo();
        retryAttemptsCount = 0;
        Log.d("MapFragmentFlowLogs", "Circle : isSubmitButtonShown : " + isSubmitButtonShown + ", Dist : " + dist);
        if (isSubmitButtonShown)
        {
            drawCircleOnMap();
        }
    }

    private void drawCircleOnMap()
    {
        int mRadius = getRadiusInMeter();
        Log.d("MapFragmentFlowLogs", "zoom : radius : " + mRadius);
        showActionCircle(mGeoCodeLatLng, mRadius);
        setZoomLevel(mRadius);
    }

    private int getRadiusInMeter()
    {
        int radius = getSeekBarValue();
        switch (radiusType)
        {
            case R.id.radius_fts : return (int) Math.floor(radius /  3.28084);
            case R.id.radius_km : return (int) Math.floor(radius * 1000);
            case R.id.radius_mi : return (int) Math.floor(radius * 1609.344051499);
            default: return radius;
        }
    }

    private void showActionCircle(LatLng latLng, int radius)
    {
        removeActionCircle();
        mActionCircle = mGoogleMap.addCircle(new CircleOptions()
                .radius(radius)
                .strokeWidth(2)
                .strokeColor(accentColor)
                .fillColor(radiusColor)
                .center(latLng));
    }

    private void setZoomLevel(int radius)
    {
        double zoom = 16;
        if (radius < 400)
        {
            zoom = 16;
        }
        else if (radius < 600)
        {
            zoom = 15.5;
        }
        else if (radius < 800)
        {
            zoom = 15;
        }
        else if (radius < 1000)
        {
            zoom = 14.5;
        }
        else if (radius < 2000)
        {
            zoom = 14;
        }
        else if (radius < 3000)
        {
            zoom = 13;
        }
        else if (radius < 5000)
        {
            zoom = 12.5;
        }
        else if (radius < 6000)
        {
            zoom = 12;
        }
        else if (radius < 8000)
        {
            zoom = 11.5;
        }
        else if (radius < 10000)
        {
            zoom = 11;
        }
        else if (radius < 12000)
        {
            zoom = 10.5;
        }else
        {
            zoom = 10;
        }

        if (!isCurrentZoomSetByUser(currentZoom))
        {
            if (zoom != currentZoom)
            {
                if (mGoogleMap != null && mGeoCodeLatLng != null)
                {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mGeoCodeLatLng, (float) zoom));
                }
            }
        }
    }

    private boolean isCurrentZoomSetByUser(float currentZoom)
    {
        double[] zooms = {16, 15.5, 15, 14.5, 14, 13, 12.5, 12, 11.5, 11, 10.5, 10};
        for (double zoom : zooms)
        {
            if (zoom == currentZoom)
            {
                return false;
            }
        }
        return true;
    }

    private void updateLocationInfo()
    {
        Log.d("MapFragmentFlowLogs", "updateLocationInfo");
        if (PermissionUtil.isConnected(mActivity))
        {
            if (mGeoCoderTask != null)
            {
                double dist = distFrom(mGeoCoderTask.mLatLng, mGeoCodeLatLng);
                if (dist > 5)
                {
                    showSearchProgress();
                    cancelAsyncTask(mGeoCoderTask);
                    mGeoCoderTask = new GeoCoderTask(mActivity, mGeoCodeLatLng, this);
                    mGeoCoderTask.execute(retryAttemptsCount++);
                }
            }
            else
            {
                showSearchProgress();
                mGeoCoderTask = new GeoCoderTask(mActivity, mGeoCodeLatLng, this);
                mGeoCoderTask.execute(retryAttemptsCount++);
            }
        }
        else
        {
            searchText.setText(NO_INTERNET);
        }
    }

    private void cancelAsyncTask(AsyncTask task)
    {
        if (task != null)
        {
            AsyncTask.Status status = task.getStatus();
            if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING)
            {
                task.cancel(true);
            }
        }
    }


    @Override
    public void onAddressObtained(String result)
    {
        hideSearchProgress();
        Log.d("MapFragmentFlowLogs", "onAddressObtained");
        if (result == null)
        {
            searchText.setText(UNKNOWN_PLACE);
        }
        else
        {
            searchText.setText(result);
        }
    }

    private void showSearchProgress()
    {
        searchProgress.setVisibility(View.VISIBLE);
    }

    private void hideSearchProgress()
    {
        searchProgress.setVisibility(View.GONE);
    }

    @Override
    public void onGeoCodingFailed()
    {
        hideSearchProgress();
        if (retryAttemptsCount < 10)
        {
            updateLocationInfo();
        }
        else
        {
            onAddressObtained(null);
        }
    }

    @Override
    public void onCancelled()
    {
        hideSearchProgress();
        retryAttemptsCount = 0;
    }

    @OnClick({R.id.searchIcon, R.id.hoverPlaceName, R.id.searchProgress})
    public void searchPlace()
    {
        if (PermissionUtil.isConnected(mActivity))
        {
            try
            {
                PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN);
                startActivityForResult(builder.build(mActivity), PLACE_AUTOCOMPLETE_REQUEST_CODE);
            }
            catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e)
            {
                //TODO
            }
        }
        else
        {
            Toast.makeText(mActivity, NO_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onDismiss(String action)
    {
        switch (action)
        {
            case DIALOG_DISMISS:
                mMarkerAndCircle = new MarkerAndCirclesUtil(mGoogleMap, accentColor, radiusColor);
                showMyLocationMarkerAndCircle(getLatLng(), getAccuracy());
                break;
            case InternetConnectivityListener.INTERNET_CONNECTED:
                updateLocationInfo();
                break;
        }
    }
}