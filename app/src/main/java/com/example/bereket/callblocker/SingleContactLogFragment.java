package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.NavUtils;
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

import java.text.SimpleDateFormat;

public class SingleContactLogFragment extends Fragment implements LoaderManager.LoaderCallbacks<DataBaseHelper.LogCursor>{

    public static final String ARG_PARAM1 = "param1";

    private LogRecord mLog;
    private Contact mContact;
    private ContactManager mContactManager;
    private LogManager mLogManager;
    private TimeHelper mTimeHelper;

    private TextView contactNameTextView;
    private ListView listView;

    private static int SINGLE_CONTACT_LOG_LIST_LOADER = 1;

    private OnFragmentInteractionListener mListener;

    public static SingleContactLogFragment newInstance(LogRecord param1) {
        SingleContactLogFragment fragment = new SingleContactLogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public SingleContactLogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContactManager = ContactManager.getInstance(getActivity());
        mLogManager = LogManager.getInstance(getActivity());
        mTimeHelper = TimeHelper.getInstance(getActivity());

        if (getArguments() != null) {

            mLog = (LogRecord)getArguments().getSerializable(ARG_PARAM1);

            if(mLog != null){

                mContact = mContactManager.getContactById(Long.valueOf(mLog.getContactId()));
            }
        }

        setHasOptionsMenu(true);
        //enable the 'UP' ancestoral navigation button, if parent is set in manifest for this activity
        if (NavUtils.getParentActivityName(getActivity()) != null) {

            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {

                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        getLoaderManager().initLoader(SINGLE_CONTACT_LOG_LIST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_single_contact_log, container, false);

        listView = (ListView) view.findViewById(R.id.log_list_view_id);
        contactNameTextView = (TextView) view.findViewById(R.id.contact_name_textview_id);

        contactNameTextView.setText(mContact.getContactName());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_single_contact_log, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_single_contact_delete_log:

                //TODO need a separate thread for this?
                mLogManager.deleteLogForContact(mContact.getId());
                //after delete, restart loader to refresh the list view
                getLoaderManager().restartLoader(SINGLE_CONTACT_LOG_LIST_LOADER, null, this);
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

    @Override
    public Loader<DataBaseHelper.LogCursor> onCreateLoader(int i, Bundle bundle) {
        return new LogLoader(getActivity(), mContact.getId());
    }

    @Override
    public void onLoadFinished(Loader<DataBaseHelper.LogCursor> logCursorLoader, DataBaseHelper.LogCursor logCursor) {
        //Create an adapter to point at this cursor
        LogListAdaptor adapter = new LogListAdaptor((DataBaseHelper.LogCursor)logCursor);
        listView.setAdapter(adapter);

        View emptyTextView = getActivity().findViewById(R.id.empty_contact_log_text);

        if(emptyTextView != null) {

            if (adapter.getCount() == 0) emptyTextView.setVisibility(View.VISIBLE);
            else emptyTextView.setVisibility(View.GONE);
        }


    }

    @Override
    public void onLoaderReset(Loader<DataBaseHelper.LogCursor> logCursorLoader) {

        listView.setAdapter(null);

        View emptyTextView = getActivity().findViewById(R.id.empty_contact_log_text);

        if(emptyTextView != null){ //sometimes empty text view is null

            emptyTextView.setVisibility(View.VISIBLE);
        }
    }


    private class LogListAdaptor extends CursorAdapter {

        private DataBaseHelper.LogCursor logCursor;

        public LogListAdaptor(DataBaseHelper.LogCursor logCursor){
            super(getActivity(), logCursor, 0);
            this.logCursor = logCursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.fragment_single_contact_log_listview, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            LogRecord log = logCursor.getLog();

            //set up the start date text view
            TextView logContactDate = (TextView) view.findViewById(R.id.log_contact_date);
            logContactDate.setText(mTimeHelper.formattedLogDate(log.getLogDate()));

            ImageView logTypeImage = (ImageView) view.findViewById(R.id.block_type_icon);

            logTypeImage.setImageResource(log.getBlockType() == BlockType.INCOMING ? R.drawable.incoming : R.drawable.outgoing);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
