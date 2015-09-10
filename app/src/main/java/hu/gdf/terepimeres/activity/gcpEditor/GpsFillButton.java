package hu.gdf.terepimeres.activity.gcpEditor;

import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.dialog.MessageDialog;
import hu.gdf.terepimeres.location.LocationProvider;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class GpsFillButton extends ToggleButton {
  
  private LocationProvider locationProvider;
  private FragmentActivity fragmentActivity;
  private Context          context;
  
  public GpsFillButton(android.content.Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
  }
  
  public LocationProvider getLocationProvider() {
    return locationProvider;
  }
  
  public void setLocationProvider(LocationProvider locationProvider) {
    this.locationProvider = locationProvider;
  }
  
  public FragmentActivity getFragmentActivity() {
    return fragmentActivity;
  }
  
  public void setFragmentActivity(FragmentActivity fragmentActivity) {
    this.fragmentActivity = fragmentActivity;
  }
  
  @Override
  public void setChecked(boolean checked) {
    if ( locationProvider != null && !locationProvider.isGpsEnabled() ) {
      MessageDialog.newInstance(context.getString(R.string.gps_disabled_title),
                                context.getString(R.string.gps_disabled_message))
                   .show(fragmentActivity.getSupportFragmentManager(), "dialog");
      return;
    }
    super.setChecked(checked);
  }
  
}
