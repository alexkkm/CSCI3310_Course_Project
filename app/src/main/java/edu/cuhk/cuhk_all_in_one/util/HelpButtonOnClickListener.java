package edu.cuhk.cuhk_all_in_one.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class HelpButtonOnClickListener implements View.OnClickListener {

    private AlertDialog.Builder builder;
    private int titleResId;
    private int descriptionResId;

    public HelpButtonOnClickListener(AlertDialog.Builder builder, int titleRes, int descriptionRes) {
        this.builder = builder;
        this.titleResId = titleRes;
        this.descriptionResId = descriptionRes;
    }

    @Override
    public void onClick(View view) {
        this.builder.setMessage(this.descriptionResId)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setTitle(this.titleResId);
        dialog.show();
    }
}
