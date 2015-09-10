package hu.gdf.terepimeres.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public class InputDialog extends DialogFragment {
  
  private static final String TITLE    = "title";
  private static final String MESSAGE  = "message";
  private static final String POSITIVE = "positive";
  private static final String NEGATIVE = "negative";
  private InputDialogListener mListener;
  
  public static InputDialog newInstance(String title,
                                        String message,
                                        String positive,
                                        String negative) {
    InputDialog inputDialog = new InputDialog();
    Bundle bundle = new Bundle();
    bundle.putString(TITLE, title);
    bundle.putString(MESSAGE, message);
    bundle.putString(POSITIVE, positive); // a pozitív gomb felirata
    inputDialog.setArguments(bundle);
    return inputDialog;
  }
  
  @Override
  public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    final EditText input = new EditText(getActivity());
    input.setSingleLine();
    Bundle arguments = getArguments();
    builder.setMessage(arguments.getString(MESSAGE))
           .setTitle(arguments.getString(TITLE))
           .setView(input)
           .setPositiveButton(arguments.getString(POSITIVE), new DialogInterface.OnClickListener() {
             
             public void onClick(DialogInterface dialog,
                                 int id) {
               mListener.onInputPositiveClick(input.getText().toString());
               
             }
           })
           .setNegativeButton(arguments.getString(NEGATIVE), new DialogInterface.OnClickListener() {
             
             public void onClick(DialogInterface dialog,
                                 int id) {
               mListener.onInputNegativeClick();
             }
           });
    return builder.create();
  }
  
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (InputDialogListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement InputDialogListener");
    }
  }
  
  public interface InputDialogListener {
    
    public void onInputPositiveClick(String input);
    
    public void onInputNegativeClick();
  }
}
