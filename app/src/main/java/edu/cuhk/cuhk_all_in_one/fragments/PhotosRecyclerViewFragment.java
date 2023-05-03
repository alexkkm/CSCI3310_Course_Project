package edu.cuhk.cuhk_all_in_one.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.activities.PinsActivity;
import edu.cuhk.cuhk_all_in_one.adapters.PinPhotosAdapter;
import edu.cuhk.cuhk_all_in_one.datatypes.Photo;

public class PhotosRecyclerViewFragment extends Fragment {
    private static final String TAG = "PhotosRecyclerViewFragment";

    protected RecyclerView mRecyclerView;
    protected PinPhotosAdapter mAdapter;

    protected List<Photo> mDataset = new ArrayList<>();
    private int pinUid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPinId();
        loadPinPhotos();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photos_rv_frag, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.photos_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mAdapter = new PinPhotosAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);

        PinsActivity parentActivity = (PinsActivity) getActivity();
        assert parentActivity != null;
        parentActivity.notifyRefreshActivityUi();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPinPhotos();
    }

    private void loadPinId() {
        Bundle bundleArguments = getArguments();
        if (bundleArguments != null) {
            this.pinUid = bundleArguments.getInt("pinUid");
            Log.d("TAG", "Recycler view: pin UID " + this.pinUid);
        }
    }

    private void loadPinPhotos() {
        mDataset.clear();

        // Load from sdcard
        File path = new File(this.requireContext().getExternalFilesDir(null).toString(), "images/" + String.valueOf(pinUid));
        if(path.exists()) {
            String[] fileNames = path.list();
            for (String fileName : fileNames) {
                Bitmap bitmap = BitmapFactory.decodeFile(path.getPath() + "/" + fileName);
                mDataset.add(new Photo(bitmap));
            }
        } else {
            Log.d(TAG, "Path not exist: " + path.getPath());
        }
    }

    public PinPhotosAdapter getPinPhotosAdapter() {
        return mAdapter;
    }
}
