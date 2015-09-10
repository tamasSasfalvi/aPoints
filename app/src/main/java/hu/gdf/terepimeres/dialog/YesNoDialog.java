package hu.gdf.terepimeres.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class YesNoDialog extends DialogFragment {
  
  private static final String TITLE    = "title";
  private static final String MESSAGE  = "message";
  private static final String POSITIVE = "positive";
  private static final String NEGATIVE = "negative";
  private YesNoDialogListener mListener;
  
  public static YesNoDialog newInstance(String title,
                                        String message,
                                        String positive,
                                        String negative) {
    YesNoDialog instance = new YesNoDialog();
    Bundle bundle = new Bundle();
    bundle.putString(TITLE, title);
    bundle.putString(MESSAGE, message);
    bundle.putString(POSITIVE, positive);
    bundle.putString(NEGATIVE, negative);
    instance.setArguments(bundle);
    return instance;
  }
  
  public void setListener(YesNoDialogListener listener) {
    mListener = listener;
  }
  
  @Override
  public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle arguments = getArguments();
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(arguments.getString(TITLE))
           .setMessage(arguments.getString(MESSAGE))
           .setCancelable(false)
           .setPositiveButton(arguments.getString(POSITIVE), new DialogInterface.OnClickListener() {
             
             public void onClick(DialogInterface dialog,
                                 int id) {
               if ( mListener != null ) {
                 mListener.onPositiveClick();
               }
             }
           })
           .setNegativeButton(arguments.getString(NEGATIVE), new DialogInterface.OnClickListener() {
             
             public void onClick(DialogInterface dialog,
                                 int id) {
               if ( mListener != null ) {
                 mListener.onNegativeClick();
               }
             }
           });
    return builder.create();
  }
  
  public interface YesNoDialogListener {
    
    public void onPositiveClick();
    
    public void onNegativeClick();
  }
}
