package com.example.srujana.security;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

abstract class RepoFragment extends ListFragment {

    protected static final int ACT_RESULT_FAV = 1003;

    protected String[] mFirstLines;
    protected String[] mSecondLines;

    protected ImageButton btnShare;
    protected ImageButton btnCopy;
    protected ImageButton btnMap;
    protected ImageButton btnFav;

    String actionCoords;

    abstract void rebuildList();

    abstract void setLongClickHandler();

    abstract void dialogAdjustment(Dialog dialog);

    abstract void addExtraButtons(Dialog dialog);

    @Override
    public void onResume() {
        super.onResume();
        rebuildList();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        rebuildList();
        setLongClickHandler();
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case ACT_RESULT_FAV:
                DBHelper.updateFavIcon(getActivity(), btnFav);
                break;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        // selected coordinates
        actionCoords = GpsHelper.extractCoordinates((String) getListAdapter().getItem(position));

        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.repo_buttons_dialog);
        dialog.setTitle(getDialogTitle(mFirstLines[position]));
        btnMap = (ImageButton) dialog.findViewById(R.id.btnMap2);
        btnShare = (ImageButton) dialog.findViewById(R.id.btnShare2);

        dialogAdjustment(dialog);
        DBHelper.updateFavIcon(getActivity(), btnFav);
        dialog.show();

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                GpsHelper.shareCoordinates(getActivity(), GpsHelper.getShareBody(getActivity(), actionCoords, ""));
            }
        });



        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                GpsHelper.openOnMap(getActivity().getApplicationContext(), actionCoords);
            }
        });





        addExtraButtons(dialog);

    }

    protected String getDialogTitle(String s) {
        return getString(R.string.mysms_actions);
    }


}
