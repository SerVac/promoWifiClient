package me.loc2.loc2me.ui.md;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.loc2.loc2me.R;
import me.loc2.loc2me.ui.list.EuclidListAdapter;

public class OfferListAdapter extends RecyclerView.Adapter<OfferListAdapter.ViewHolder> {

    private static final String TAG = "OfferListAdapter";

    private List<OfferStub> mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mProfileName;
        private final TextView mProfileDescription;
        private final ImageView mAvatarView;
        private final Context context;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            mProfileName = (TextView) v.findViewById(R.id.text_view_name);
            mProfileDescription = (TextView) v.findViewById(R.id.text_view_description);
            mAvatarView = (ImageView) v.findViewById(R.id.image_view_avatar);
            context = v.getContext();
        }

        public void loadData(OfferStub offerStub) {
            mProfileName.setText(offerStub.getName());
            mProfileDescription.setText(offerStub.getDescriptionShort());
            Picasso.with(context).load(offerStub.getAvatar())
                    .resize(offerStub.getsScreenWidth(),
                            offerStub.getsProfileImageHeight()).centerCrop()
                    .placeholder(R.color.blue)
                    .into(mAvatarView);
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public OfferListAdapter(List<OfferStub> dataSet) {
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.loadData(mDataSet.get(position));
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public int add(OfferStub item) {
        mDataSet.add(item);
        notifyItemInserted(mDataSet.size() - 1);
        return mDataSet.size();
    }

    public void remove(OfferStub item) {
        int position = mDataSet.indexOf(item);
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }
}
