package com.yousuf.shawon.ribbit.ribbit.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.yousuf.shawon.ribbit.ribbit.R;
import com.yousuf.shawon.ribbit.ribbit.adapters.UserAdapter;
import com.yousuf.shawon.ribbit.ribbit.utils.FileHelper;
import com.yousuf.shawon.ribbit.ribbit.utils.Log;
import com.yousuf.shawon.ribbit.ribbit.utils.ParseConstants;
import com.yousuf.shawon.ribbit.ribbit.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class RecipientsActivity extends AppCompatActivity {


    public  final String TAG = getClass().getSimpleName();

    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected List<ParseUser> mFriends;

    protected MenuItem mSendMenuItem;
    protected Uri mMediaUri;
    protected String mFileType;
    protected GridView mGridView;

 //   private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_grid);

        // Show the Up button in the action bar.
      //  setupActionBar();

      //  getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        iniUI();


    }



    private void iniUI(){

     //   listView = (ListView) findViewById(R.id.listView);

        mGridView = (GridView)findViewById(R.id.friendsGrid);


        TextView emptyTextView = (TextView)findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        setUIProperty();
    }


    private void setUIProperty(){

     //   listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (listView.getCheckedItemCount() > 0) {
//                    mSendMenuItem.setVisible(true);
//                }
//                else {
//                    mSendMenuItem.setVisible(false);
//                }
//            }
//        });
//
//

    }


    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for(ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }


                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else {
                        ((UserAdapter)mGridView.getAdapter()).refill(mFriends);
                    }


                }
                else {
                    Log.e(TAG, e.getMessage());

                    String dialogTitle = getString(R.string.error_title);
                    Utility.showSimpleAlertDialog(RecipientsActivity.this, dialogTitle, e.getMessage());
                }
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }



    protected ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null) {
            return null;
        }
        else {
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);

            return message;
        }


    }


    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        for (int i = 0; i < mGridView.getCount(); i++) {
            if (mGridView.isItemChecked(i)) {
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
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

            case R.id.action_send:
                ParseObject message = createMessage();
                if (message == null) {
                    // error
                    String dialogTitle = getString(R.string.error_selecting_file_title);
                    String dialogMessage = getString(R.string.error_selecting_file);
                    Utility.showSimpleAlertDialog(this, dialogTitle, dialogMessage);
                }else {
                    send(message);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // success!
                    Toast.makeText(RecipientsActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
                }
                else {

                    String dialogTitle = getString(R.string.error_selecting_file_title);
                    String dialogMessage = getString(R.string.error_sending_message);
                    Utility.showSimpleAlertDialog(RecipientsActivity.this, dialogTitle, dialogMessage);

                }
            }
        });
    }


    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (mGridView.getCheckedItemCount() > 0) {
                mSendMenuItem.setVisible(true);
            }
            else {
                mSendMenuItem.setVisible(false);
            }

            ImageView checkImageView = (ImageView)view.findViewById(R.id.checkImageView);

            if (mGridView.isItemChecked(position)) {
                // add the recipient
                checkImageView.setVisibility(View.VISIBLE);
            }
            else {
                // remove the recipient
                checkImageView.setVisibility(View.INVISIBLE);
            }
        }
    };

}
