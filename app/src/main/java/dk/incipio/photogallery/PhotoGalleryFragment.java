package dk.incipio.photogallery;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    GridView mGridView;
    ArrayList<GalleryItem> mItems;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // Start AsyncTask, fire up the background thread and call doInBackground(...)
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        setupAdapter();

        return v;
    }

    private void setupAdapter() {
        if (getActivity()==null || mGridView == null)
            return;
        if (mItems!=null) {
            mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),android.R.layout.simple_gallery_item,mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }
    private class FetchItemsTask extends AsyncTask<Void,Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... voids) {
            return new FlickerFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

}
