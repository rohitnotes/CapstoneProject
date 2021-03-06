package com.keyeswest.trackme;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


import java.util.Objects;
import java.util.UUID;


public class ConfirmDeleteDialogFragment extends DialogFragment {

    private static final String ARG_SEGMENT_ID = "com.keyeswest.trackme.argSegmentId";

    private static final String EXTRA_CONFIRM = "com.keyeswest.trackme.confirm";

    private UUID mSegmentId;

    public static ConfirmDeleteDialogFragment newInstance(UUID segmentId){
        Bundle args = new Bundle();
        args.putString(ARG_SEGMENT_ID, segmentId.toString());

        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static UUID getSegmentId(Intent data){
        return UUID.fromString(data.getStringExtra(ARG_SEGMENT_ID));
    }

    public static boolean getConfirmation(Intent data){
        return data.getBooleanExtra(EXTRA_CONFIRM, false);
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mSegmentId = UUID.fromString(Objects.requireNonNull(getArguments())
                .getString(ARG_SEGMENT_ID));

        AlertDialog.Builder builder = new AlertDialog
                .Builder(Objects.requireNonNull(getActivity()));

        builder.setMessage(R.string.confirm_trip_delete)
                .setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mSegmentId != null){

                            sendResult(Activity.RESULT_OK, true);
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendResult(Activity.RESULT_OK, false);
                    }
                });

        return builder.create();
    }


    private void sendResult(int resultCode, Boolean deleted){

        if (getTargetFragment() == null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONFIRM, deleted);
        intent.putExtra(ARG_SEGMENT_ID, mSegmentId.toString());
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

}
