package mrkm4ntr.twitterclient.views;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class ProgressDialogFragment extends DialogFragment {

    private static ProgressDialog sDialog;

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog getDialog() {
        return sDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString("message");
        sDialog = new ProgressDialog(getActivity());
        sDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        sDialog.setMessage(message);
        return sDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sDialog = null;
    }
}
