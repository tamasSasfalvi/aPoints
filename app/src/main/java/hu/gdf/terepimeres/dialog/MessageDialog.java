package hu.gdf.terepimeres.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MessageDialog extends DialogFragment {
  
  private static final String TITLE   = "title";
  private static final String MESSAGE = "message";
  
  public static MessageDialog newInstance(String title,
                                          String message) {
    MessageDialog frag = new MessageDialog();
    Bundle bundle = new Bundle();
    bundle.putString(TITLE, title);
    bundle.putString(MESSAGE, message);
    frag.setArguments(bundle);
    return frag;
  }
  
  @Override
  public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(getArguments().getString(MESSAGE))
           .setTitle(getArguments().getString(TITLE))
           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
             
             public void onClick(DialogInterface dialog,
                                 int id) {
               // nem reagálunk
             }
           });
    return builder.create();
  }
  
}
