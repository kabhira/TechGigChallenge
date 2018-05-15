package com.example.abhi.techexplorer.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.abhi.techexplorer.R;
import com.example.abhi.techexplorer.utilities.LocationHelper;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity {
    private PositioningManager posManager;
    private Map map;
    private boolean paused = false;
    private TextView distance_text;
    private LocationHelper locationHelper;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locationHelper = new LocationHelper(this);
        final MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.mapfragment);
        distance_text = (TextView)findViewById(R.id.distance_text);
        spinner = (Spinner) findViewById(R.id.spinner);

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath("/sdcard/TechExplorer/myservice", "com.here.android.mpa.service.MapService.techexplorer");
        // initialize the Map Fragment and
        // retrieve the map that is associated to the fragment
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // now the map is ready to be used
                    map = mapFragment.getMap();
                    posManager = PositioningManager.getInstance();
                    GeoCoordinate geoCoordinate = null;
                    if(posManager.hasValidPosition()) {
                        geoCoordinate = posManager.getPosition().getCoordinate();
                    }else {
                        geoCoordinate = new GeoCoordinate(locationHelper.getLatitude(), locationHelper.getLongitude());
                    }
                    map.setCenter(geoCoordinate, Map.Animation.NONE);
                    map.getPositionIndicator().setVisible(true);

                    // Get the maximum,minimum zoom level.
                    double maxZoom = map.getMaxZoomLevel();
                    double minZoom = map.getMinZoomLevel();
                    map.setZoomLevel(14);
                    // Set the tilt to 45 degrees
                    map.setTilt(45);

                    Image carImage_500C = new Image();
                    Image carImage_500D = new Image();
                    try {
                        carImage_500C.setImageResource(R.drawable.marker_500_c);
                        carImage_500D.setImageResource(R.drawable.marker_500_d);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    GeoCoordinate markerCoordinate1 = new GeoCoordinate(13.021557, 77.648171);
                    GeoCoordinate markerCoordinate2 = new GeoCoordinate(13.019791, 77.654151);
                    GeoCoordinate markerCoordinate3 = new GeoCoordinate(13.030369, 77.649516);
                    GeoCoordinate markerCoordinate4 = new GeoCoordinate(13.022006, 77.637929);
                    final MapMarker mapMarker1 = new MapMarker(markerCoordinate1, carImage_500C);
                    final MapMarker mapMarker2 = new MapMarker(markerCoordinate2, carImage_500D);
                    final MapMarker mapMarker3 = new MapMarker(markerCoordinate3, carImage_500D);
                    final MapMarker mapMarker4 = new MapMarker(markerCoordinate4, carImage_500D);
                    map.addMapObject(mapMarker1);
                    map.addMapObject(mapMarker2);
                    map.addMapObject(mapMarker3);
                    map.addMapObject(mapMarker4);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if(position == 0) {
                                map.addMapObject(mapMarker1);
                                map.addMapObject(mapMarker2);
                                map.addMapObject(mapMarker3);
                                map.addMapObject(mapMarker4);
                            }
                            else if(position == 1) {
                                map.removeMapObject(mapMarker1);
                                map.addMapObject(mapMarker2);
                                map.addMapObject(mapMarker3);
                                map.addMapObject(mapMarker4);
                            }
                            else if(position == 2) {
                                map.addMapObject(mapMarker1);
                                map.removeMapObject(mapMarker2);
                                map.removeMapObject(mapMarker3);
                                map.removeMapObject(mapMarker4);

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });


                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                        }
                    },1000, 1000);


                    CoreRouter coreRouter = new CoreRouter();
                    RoutePlan routePlan = new RoutePlan();
                    routePlan.addWaypoint(new RouteWaypoint(geoCoordinate));
                    RouteWaypoint destWaypoint = new RouteWaypoint(new GeoCoordinate(13.021557, 77.648171));
                    routePlan.addWaypoint(destWaypoint);
                    // Create the RouteOptions and set its transport mode & routing type
                    RouteOptions routeOptions = new RouteOptions();
                    routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
                    routeOptions.setRouteType(RouteOptions.Type.FASTEST);
                    routePlan.setRouteOptions(routeOptions);

                    // Calculate the route
                    coreRouter.calculateRoute(routePlan, new RouteListener());

                    posManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(onPositionChangedListener));

                } else {
                    System.out.println("ERROR: Cannot initialize MapFragment..."+error);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        if (posManager != null) {
            posManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
            }
    }

    @Override
    protected void onPause() {
        if (posManager != null) {
            posManager.stop();
        }
        super.onPause();
        paused = true;
    }

    @Override
    protected void onDestroy() {
        if (posManager != null) {
            // Cleanup
            posManager.removeListener(onPositionChangedListener);
        }
        map = null;
        super.onDestroy();
    }

    private class RouteListener implements CoreRouter.Listener {

        // Method defined in Listener
        public void onProgress(int percentage) {
            // Display a message indicating calculation progress
        }

        // Method defined in Listener
        public void onCalculateRouteFinished(List<RouteResult> routeResult, RoutingError error) {
            // If the route was calculated successfully
            if (error == RoutingError.NONE) {
                // Render the route on the map
                MapRoute mapRoute = new MapRoute(routeResult.get(0).getRoute());
                if(map != null)
                    map.addMapObject(mapRoute);
                NavigationManager navigationManager = NavigationManager.getInstance();
                navigationManager.simulate(routeResult.get(0).getRoute(), 25);


            }
            else {
                // Display a message indicating route calculation failure
            }
        }
    }

    // Define positioning listener
    private PositioningManager.OnPositionChangedListener onPositionChangedListener = new
            PositioningManager.OnPositionChangedListener() {

                public void onPositionUpdated(PositioningManager.LocationMethod method, GeoPosition position, boolean isMapMatched) {
                    // set the center only when the app is in the foreground
                    // to reduce CPU consumption
                    if (!paused) {
                        map.setCenter(position.getCoordinate(), Map.Animation.NONE);
                    }

                    NavigationManager navigationManager = NavigationManager.getInstance();
                    if(navigationManager != null)
                        distance_text.setText("Distance Travelled "+navigationManager.getElapsedDistance()+" m");
                }

                public void onPositionFixChanged(PositioningManager.LocationMethod method, PositioningManager.LocationStatus status) {
                }
            };






}
