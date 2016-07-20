package com.deepred.subworld.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.R;
import com.deepred.subworld.model.Treasure;
import com.deepred.subworld.model.User;
import com.deepred.subworld.utils.Fx;
import com.deepred.subworld.utils.MyUserManager;

import java.util.Map;


/**
 *
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    private static final int TYPE_STATS = 1;
    private static final int TYPE_LEVEL = 2;
    private static final int TYPE_OPTIONS = 3;
    private static final int TYPE_ITEM = 4;

    private String mNavTitles[]; // String Array to store the passed titles Values
    private int mIcons[];       // Int Array to store the passed icons resource values
    private int profile;        //int Resource for header view profile picture
    //private AppCompatActivity ctx;
    private User usr;

    MenuAdapter(/*AppCompatActivity _ctx*/) {
        String titles[] = {"Backpack", "Hidden"/*, "Thefts", "Lost"*/};
        int icons[] = {android.R.drawable.ic_media_play, android.R.drawable.ic_media_play/*, android.R.drawable.ic_media_play, android.R.drawable.ic_media_play*/};
        //ctx = _ctx;

        // Menu data
        usr = MyUserManager.getInstance().getUser();
        profile = -1;
        if (usr.getChrType() == ICommon.CHRS_ARCHEOLOGIST)
            profile = R.drawable.c1;
        else if (usr.getChrType() == ICommon.CHRS_FORT_TELLER)
            profile = R.drawable.c2;
        else if (usr.getChrType() == ICommon.CHRS_SPY)
            profile = R.drawable.c3;
        else if (usr.getChrType() == ICommon.CHRS_THIEF)
            profile = R.drawable.c4;
        mNavTitles = titles;                //have seen earlier
        mIcons = icons;                    //here we assign those passed values to the values we declared here
    }

    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = -1;
        ViewHolder vhItem = null;
        switch (viewType) {
            case TYPE_HEADER:
                layoutId = R.layout.header; break;
            case TYPE_ITEM:
                layoutId = R.layout.item_row;  break;
            case TYPE_LEVEL:
                layoutId = R.layout.level; break;
            case TYPE_STATS:
                layoutId = R.layout.stats; break;
            case TYPE_OPTIONS:
                layoutId = R.layout.options; break;
        }
        if(layoutId > -1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false); //Inflating the layout
            vhItem = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view
        }
        return vhItem;
    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(MenuAdapter.ViewHolder holder, int position) {
        if (holder.Holderid == TYPE_HEADER) {
            holder.profile.setImageResource(profile);           // Similarly we set the resources for header view
            holder.Name.setText(usr.getName());
            holder.email.setText(usr.getEmail());
            holder.rank.setText(usr.getRank());
        } else if (holder.Holderid >= TYPE_ITEM) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
            holder.imageView.setImageResource(mIcons[position - 1]);// Settimg the image with array of our icons

            Map<String, Treasure> ref;
            if (position == 1) {
                ref = usr.getBackpack();
            } else {
                ref = usr.getHidden();
            }
            int tam = ref.size();
            holder.textQuantity.setText(Integer.toString(tam));

            int value = 0;
            for (Treasure t : ref.values()) {
                value += t.getValue();
            }
            holder.textValue.setText(Integer.toString(value));
        } else if (holder.Holderid == TYPE_STATS) {

        } else if (holder.Holderid == TYPE_LEVEL) {

        } else if (holder.Holderid == TYPE_OPTIONS) {

        }
    }


    // Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return mNavTitles.length+4; // the number of items in the list will be +1 the titles including the header view.
    }

    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;
        else if (position == 1 || position == 2)
            return TYPE_ITEM;
        else if (position == 3)
            return TYPE_STATS;
        else if (position == 4)
            return TYPE_LEVEL;
        else
            return TYPE_OPTIONS;
    }


    public interface OnAnimationEndCompleteListener {
        void onAnimationComplete();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        int Holderid;

        // Header
        ImageView profile;
        TextView Name;
        TextView email;
        TextView rank;
        // Item
        TextView textView;
        LinearLayout detail;
        TextView textQuantity;
        TextView textValue;
        ImageView imageView;
        // Stats

        // Levels

        // Options


        // Creating a ViewHolder which extends the RecyclerView View Holder
        // ViewHolder are used to to store the inflated views in order to recycle them

        public ViewHolder(final View itemView, int viewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);

            switch (viewType) {
                case TYPE_HEADER:
                    Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
                    email = (TextView) itemView.findViewById(R.id.email);       // Creating Text View object from header.xml for email
                    profile = (ImageView) itemView.findViewById(R.id.imageProfile);// Creating Image view object from header.xml for profile pic
                    rank = (TextView) itemView.findViewById(R.id.rank);
                    Holderid = TYPE_HEADER;
                    break;
                case TYPE_ITEM:
                    textView = (TextView) itemView.findViewById(R.id.rowText); // Creating TextView object with the id of textView from item_row.xml
                    imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
                    detail = (LinearLayout) itemView.findViewById(R.id.rowDetail);
                    textQuantity = (TextView) itemView.findViewById(R.id.rowQuantity);
                    textValue = (TextView) itemView.findViewById(R.id.rowValue);

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toggleDetail(itemView.getContext());
                        }
                    });
                    detail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toggleDetail(itemView.getContext());
                        }
                    });

                    detail.setVisibility(View.GONE);
                    Holderid = TYPE_ITEM;
                    break;
                case TYPE_STATS:

                    Holderid = TYPE_STATS;
                    break;
                case TYPE_LEVEL:

                    Holderid = TYPE_LEVEL;
                    break;
                case TYPE_OPTIONS:

                    Holderid = TYPE_OPTIONS;
                    break;
            }
        }

        private void toggleDetail(Context ctx) {
            if (detail.isShown()) {
                Fx.slide_up(ctx, detail, new OnAnimationEndCompleteListener() {
                    @Override
                    public void onAnimationComplete() {
                        detail.setVisibility(View.GONE);
                    }
                });
            } else {
                detail.setVisibility(View.VISIBLE);
                Fx.slide_down(ctx, detail);
            }
        }
    }
}
