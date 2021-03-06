package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class LogFragment extends HideNotificationListFragment  implements LoaderManager.LoaderCallbacks<DataBaseHelper.LogCursor>, UpdatableFragment  {

    private LogManager mLogManager;
    private TimeHelper mTimeHelper;
    private DataBaseHelper.LogCursor mLogCursor;

    private OnFragmentInteractionListener mListener;

    private static int LOG_LIST_LOADER = 1;
    private static int CHANGE_PREFERENCE = 2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment LogFragment.
     */
    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogManager = LogManager.getInstance(getActivity());
        mTimeHelper = TimeHelper.getInstance(getActivity());
        setHasOptionsMenu(true);
        //reset BlockCallCounter variables if the log is displayed

        BlockedCallCounter blockedCallCounter = new BlockedCallCounter(getActivity());
        blockedCallCounter.resetCounter();

        //enable the 'UP' ancestoral navigation button, if parent is set in manifest for this activity
        if (NavUtils.getParentActivityName(getActivity()) != null) {

            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {

                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        //init loader manager is moved to onActivityCreated b/c in tabbed view, it is creating a problem when put here
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.log_list_view, container, false);
        ListView listView = (ListView) v.findViewById(android.R.id.list);

        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            // mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }

        LogRecord logRecord = ((DataBaseHelper.LogCursor)((getListAdapter()).getItem(position))).getLog();
        Intent intent = new Intent(getActivity(), SingleContactLogActivity.class);

        intent.putExtra(SingleContactLogFragment.ARG_PARAM1, logRecord);
        startActivity(intent);
    }

    @Override
    public Loader<DataBaseHelper.LogCursor> onCreateLoader(int i, Bundle bundle) {
        return new LogLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<DataBaseHelper.LogCursor> logCursorLoader, DataBaseHelper.LogCursor logCursor) {
        //Create an adapter to point at this cursor
        LogListAdaptor adapter = new LogListAdaptor((DataBaseHelper.LogCursor)logCursor);
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<DataBaseHelper.LogCursor> logCursorLoader) {
        setListAdapter(null);
    }

    @Override
    public void updateContent() {

        Activity activity = getActivity();
        if (isAdded() && activity != null) {

            getLoaderManager().restartLoader(LOG_LIST_LOADER, null, this);
        }
    }


    private class LogListAdaptor extends CursorAdapter {

        public LogListAdaptor(DataBaseHelper.LogCursor logCursor){
            super(getActivity(), logCursor, 0);
            mLogCursor = logCursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.fragment_log, viewGroup, false);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LogRecord logRecord = mLogCursor.getLog();

            //set up the start date text view
            TextView logContactNameTextView = (TextView) view.findViewById(R.id.log_contact_phone);
            TextView logDateTextView = (TextView) view.findViewById(R.id.log_contact_date);

            ImageView logTypeImage = (ImageView) view.findViewById(R.id.block_type_icon);

            logContactNameTextView.setText(logRecord.getContactName());
            logDateTextView.setText(mTimeHelper.formattedLogDate(logRecord.getLogDate()));

            logTypeImage.setImageResource(logRecord.getBlockType() == BlockType.INCOMING ? R.drawable.incoming : R.drawable.outgoing);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.log_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_log:

                mLogManager.deleteLogs();
                getLoaderManager().restartLoader(0, null, LogFragment.this);

                return true;
            case R.id.menu_item_settings:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                intent.putExtra(Constants.FRAGMENT_ID, Constants.LOG_LIST_FRAGMENT);
                startActivityForResult(intent, CHANGE_PREFERENCE);

                return true;
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {

                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        getLoaderManager().restartLoader(LOG_LIST_LOADER, null, this);
    }

    @Override
    public void doOnBroadcastReceived() {
        //since this is a log fragment, just update the list and show a toast that a call has been intercepted
        //Utility.showCallInterceptionAlertDialog(getActivity());
        getLoaderManager().restartLoader(LOG_LIST_LOADER, null, this);
        Toast.makeText(getActivity(), R.string.new_call_intercepted_message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOG_LIST_LOADER, null, this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
