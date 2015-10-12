package com.example.bereket.callblocker;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class LogFragment extends ListFragment  implements LoaderManager.LoaderCallbacks<DataBaseHelper.LogCursor>  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LogManager mLogManager;
    private DataBaseHelper.LogCursor mLogCursor;

    private OnFragmentInteractionListener mListener;

    private static int LOG_LIST_LOADER = 1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogFragment newInstance(String param1, String param2) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mLogManager = LogManager.getInstance(getActivity());
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(LOG_LIST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        ListView listView = (ListView) v.findViewById(android.R.id.list);

        return v;
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


    private class LogListAdaptor extends CursorAdapter {

        public LogListAdaptor(DataBaseHelper.LogCursor logCursor){
            super(getActivity(), logCursor, 0);
            mLogCursor = logCursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.fragment_log, viewGroup, false);
            return v; //TODO: inflate a view and return here -- inflater.inflate(R.layout.single_contact_view, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LogRecord logRecord = mLogCursor.getLog();

            //set up the start date text view
            //TODO: bind a data with view here
            TextView logContactNameTextView = (TextView) view.findViewById(R.id.log_contact_phone);
            TextView logDateTextView = (TextView) view.findViewById(R.id.log_contact_date);

            logContactNameTextView.setText(logRecord.getContactName());
            //TODO: format the stardard way. determine if the time format is 24/12 from the system and format the date accordingly
            logDateTextView.setText(new SimpleDateFormat("mm/dd/yyyy hh:mm a").format(logRecord.getLogDate()));
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

                boolean logsDeleted = mLogManager.deleteLogs();

                if(logsDeleted){

                    getLoaderManager().restartLoader(0, null, LogFragment.this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//and the most important thing in this world
    // TODO: Rename method, update argument and hook method into UI event
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
