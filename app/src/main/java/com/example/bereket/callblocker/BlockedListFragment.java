package com.example.bereket.callblocker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.ListFragment;
import android.provider.ContactsContract;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class BlockedListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<DataBaseHelper.ContactCursor> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static int REQUEST_NEW_CONTACT = 1;
    private static int CONTACTS_LIST_LOADER = 2;
    private static int ADD_CONTACT_MANUALLY = 3;

    //Activity request codes
    private final Integer SINGLE_CONTACT_ACTIVITY_RESULT = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ContactManager mContactManager;
    private DataBaseHelper.ContactCursor mContactCursor;

    // TODO: Rename and change types of parameters
    public static BlockedListFragment newInstance(String param1, String param2) {
        BlockedListFragment fragment = new BlockedListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BlockedListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mContactManager = ContactManager.getInstance(getActivity());
        setHasOptionsMenu(true);

        getLoaderManager().initLoader(CONTACTS_LIST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, parent, savedInstanceState);
        ListView listView = (ListView) v.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contact_list_item_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.context_menu_delete_contact:

                        ContactListAdaptor adapter = (ContactListAdaptor)getListAdapter();
                        boolean isContactDeleted = false;

                        for(int i=adapter.getCount()-1; i>=0; i--){

                            if(getListView().isItemChecked(i)){
                                isContactDeleted = mContactManager.deleteContact(((DataBaseHelper.ContactCursor)adapter.getItem(i)).getContact());
                            }
                        }

                       if(isContactDeleted) getLoaderManager().restartLoader(0, null, BlockedListFragment.this);

                       return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        getLoaderManager().restartLoader(CONTACTS_LIST_LOADER, null, this);
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


/*    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
    }
    */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
           // mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }

        Contact contact = ((DataBaseHelper.ContactCursor)((getListAdapter()).getItem(position))).getContact();
        Intent intent = new Intent(getActivity(), SingleContactActivity.class);
        //Intent intent = new Intent(getActivity(), CrimePagerActivity.class);
        intent.putExtra(SingleContactFragment.ARG_PARAM1, contact);
        startActivityForResult(intent, SINGLE_CONTACT_ACTIVITY_RESULT);
        //startActivity(intent);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.blocked_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_add_from_contact:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_NEW_CONTACT);
                return true;
            case R.id.menu_item_manually_add_contact:
                intent = new Intent(getActivity(), AddNewPhoneActivity.class);
                startActivityForResult(intent, ADD_CONTACT_MANUALLY);
                return true;
            case R.id.menu_item_show_log:
                intent = new Intent(getActivity(), LogActivity.class);
                startActivityForResult(intent, CONTACTS_LIST_LOADER);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<DataBaseHelper.ContactCursor> onCreateLoader(int i, Bundle bundle) {
        return new ContactLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<DataBaseHelper.ContactCursor> contactCursorLoader, DataBaseHelper.ContactCursor contactCursor) {
        //Create an adapter to point at this cursor
        ContactListAdaptor adapter = new ContactListAdaptor((DataBaseHelper.ContactCursor)contactCursor);
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<DataBaseHelper.ContactCursor> contactCursorLoader) {
        setListAdapter(null);
    }


    private class ContactListAdaptor extends CursorAdapter{

        private DataBaseHelper.ContactCursor contactCursor;

        public ContactListAdaptor(DataBaseHelper.ContactCursor contactCursor){
            super(getActivity(), contactCursor, 0);
            this.contactCursor = contactCursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.single_contact_view, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Contact contact = contactCursor.getContact();

            //set up the start date text view
            TextView contactNameTextView = (TextView) view.findViewById(R.id.contact_name);
            TextView contactPhoneTextView = (TextView) view.findViewById(R.id.contact_phone_number);

            CheckBox outGoingCheckBox = (CheckBox) view.findViewById(R.id.outgoing_call_blocked_checkbox);
            CheckBox inComingCheckBox = (CheckBox) view.findViewById(R.id.incoming_call_blocked_checkbox);

            contactNameTextView.setText(contact.getContactName() + "  (" + contact.getIncomingBlockedCount() + ", " +  contact.getOutgoingBlockedCount() + " )");
            contactPhoneTextView.setText(contact.getDisplayNumber());
            outGoingCheckBox.setChecked(contact.getOutGoingBlockedState() == 1 ? true : false);
            inComingCheckBox.setChecked(contact.getIncomingBlockedState() == 1 ? true : false);
        }
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
        public void onFragmentInteraction(String id);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_NEW_CONTACT) {
            Uri contactUri = data.getData();

            ContentResolver cr = getActivity().getContentResolver();

            //specify which fields you want your query to return values for
            String[] queryFields = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
            //perform your query - the contactUri is like a "where" clause here
            Cursor c = cr.query(contactUri, queryFields, null, null, null, null);
            //double check that you actually got results
            if (c.getCount() == 0) {
                c.close();
                return;
            }
            //go to the first row
            c.moveToFirst();

            String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            String contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String phoneNumber = null;

            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);

                pCur.moveToFirst();

                phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

/*              int type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    // do something with the Home number here...
                    break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    // do something with the Mobile number here...
                    break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    // do something with the Work number here...
                    break;
                }*/
                pCur.close();
            }

            //check if phone number is empty
            if(phoneNumber == null){
                Toast.makeText(getActivity().getApplicationContext(), R.string.phoneNumberIsEmpty, Toast.LENGTH_SHORT);
            }
            else{

                mContactManager.insertNewOrUpdateExistingContact(id,  phoneNumber, contactName);
            }
        }
        else if(requestCode == ADD_CONTACT_MANUALLY){

            if(data != null){
                //do something about it
                String phoneNumber = data.getStringExtra(AddNewPhoneFragment.NEW_PHONE_NUMBER_EXTRA_KEY);

                mContactManager.insertNewOrUpdateExistingContact(String.valueOf((new Date()).getTime()),  phoneNumber, null);
            }
        }
    }
}
