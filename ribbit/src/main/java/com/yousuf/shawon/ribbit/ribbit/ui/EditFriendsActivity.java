package com.yousuf.shawon.ribbit.ribbit.ui;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.yousuf.shawon.ribbit.ribbit.R;
import com.yousuf.shawon.ribbit.ribbit.adapters.UserAdapter;
import com.yousuf.shawon.ribbit.ribbit.utils.Log;
import com.yousuf.shawon.ribbit.ribbit.utils.ParseConstants;
import com.yousuf.shawon.ribbit.ribbit.utils.Utility;

import java.util.List;

public class EditFriendsActivity extends AppCompatActivity {

    public  final String TAG = getClass().getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    protected GridView mGridView;

  //  protected ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);

        // Show the Up button in the action bar.
      //  setupActionBar();
    //    listView = (ListView) findViewById(R.id.list_view);
     //   listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mGridView = (GridView)findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        TextView emptyTextView = (TextView)findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        addProperty();

        setUpActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserList();
    }

    protected void addProperty(){

//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                Log.i(TAG, "select a friend at position " + position);
//
//                if (listView.isItemChecked(position)) {
//                    // add friend
//                    mFriendsRelation.add(mUsers.get(position));
//
//                } else {
//                    // remove friend
//                    mFriendsRelation.remove(mUsers.get(position));
//                }
//
//                mCurrentUser.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e != null) {
//                            // success
//                            Log.i(TAG, e.getMessage());
//                        } else {
//                            //error
//                            Log.e(TAG, "parse error");
//                        }
//                    }
//                });
//
//            }
//        });
//

    }



    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setUpActionBar() {

      //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);


    }

    private void getUserList(){

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(100);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    // Success
                    mUsers = users;
                    String[] usernames = new String[mUsers.size()];
                    int i = 0;
                    for(ParseUser user : mUsers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        mGridView.setAdapter(adapter);
                    }
                    else {
                        ((UserAdapter)mGridView.getAdapter()).refill(mUsers);
                    }


                    addFriendCheckmarks();
                }
                else {
                    Log.e(TAG, e.getMessage());
                    String dialogTitle = getResources().getString(R.string.error_title);
                    Utility.showSimpleAlertDialog(EditFriendsActivity.this, dialogTitle, e.getMessage() );
                }
            }
        });



    }


    private void addFriendCheckmarks(){
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                if (e == null) {
                    // list returned - look for a match
                    for (int i = 0; i < mUsers.size(); i++) {
                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend : friends) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                               // listView.setItemChecked(i, true);
                                mGridView.setItemChecked(i, true);
                            }
                        }
                    }
                }
                else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }






    protected OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ImageView checkImageView = (ImageView)view.findViewById(R.id.checkImageView);

            if (mGridView.isItemChecked(position)) {
                // add the friend
                mFriendsRelation.add(mUsers.get(position));
                checkImageView.setVisibility(View.VISIBLE);
            }
            else {
                // remove the friend
                mFriendsRelation.remove(mUsers.get(position));
                checkImageView.setVisibility(View.INVISIBLE);
            }

            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_friends, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
