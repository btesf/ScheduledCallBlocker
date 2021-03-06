package com.example.bereket.callblocker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class WhiteListFragment extends HideNotificationListFragment implements LoaderManager.LoaderCallbacks<DataBaseHelper.ContactCursor>, SearchView.OnQueryTextListener,
    UpdatableFragment{

    private static int REQUEST_NEW_CONTACT = 1;
    private static int CONTACTS_LIST_LOADER = 2;
    private static int ADD_CONTACT_MANUALLY = 3;
    private static int CHANGE_PREFERENCE = 4;

    //Activity request codes
    private final Integer SINGLE_WHITELIST_CONTACT_ACTIVITY_RESULT = 0;
    private final Integer ADD_NEW_CONTACT_REQUEST_CODE = 1;

    private OnFragmentInteractionListener mListener;
    private ContactManager mContactManager;
    private DataBaseHelper.ContactCursor mContactCursor;

    private BroadcastReceiver mOnUpdateContactFromPhoneBook = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle args = new Bundle();
            args.putInt(ContactLoader.CONTACT_TYPE_KEY, ContactType.WHITE_LIST_CONTACT);
            //refresh the loadermanager so that the UI gets refreshed
            getLoaderManager().restartLoader(CONTACTS_LIST_LOADER, args, WhiteListFragment.this);
        }
    };

    public static WhiteListFragment newInstance() {
        WhiteListFragment fragment = new WhiteListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WhiteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContactManager = ContactManager.getInstance(getActivity());
        setHasOptionsMenu(true);

        //init loader manager is moved to onActivityCreated b/c in tabbed view, it is creating a problem when put here
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

/*        View v = super.onCreateView(inflater, parent, savedInstanceState);
        ListView listView = (ListView) v.findViewById(android.R.id.list);*/
        View v = inflater.inflate(R.layout.white_list_view, parent, false);
        ListView listView = (ListView) v.findViewById(android.R.id.list);
        FloatingActionButton addNewButton = (FloatingActionButton) v.findViewById(R.id.add_new_contact_floating_button);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        addNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddNewContactDialogFragment addNewContactDialogFragment = AddNewContactDialogFragment.newInstance(ContactType.BLOCKED_CONTACT);
                addNewContactDialogFragment.setTargetFragment(WhiteListFragment.this, ADD_NEW_CONTACT_REQUEST_CODE);
                addNewContactDialogFragment.show(getFragmentManager(), "bere.bere.bere");
            }
        });

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

                        ContactListAdaptor adapter = (ContactListAdaptor) getListAdapter();
                        boolean isContactDeleted = false;

                        for (int i = adapter.getCount() - 1; i >= 0; i--) {

                            if (getListView().isItemChecked(i)) {
                                isContactDeleted = mContactManager.deleteContact(((DataBaseHelper.ContactCursor) adapter.getItem(i)).getContact());
                            }
                        }

                        if (isContactDeleted) {

                            Bundle args = new Bundle();
                            args.putInt(ContactLoader.CONTACT_TYPE_KEY, ContactType.WHITE_LIST_CONTACT);
                            getLoaderManager().restartLoader(CONTACTS_LIST_LOADER, args, WhiteListFragment.this);
                        }

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
    public void onPause() {
        super.onPause();
        //unregister the broadcast listener that is released from SaveFromPhoneBookService
        getActivity().unregisterReceiver(mOnUpdateContactFromPhoneBook);
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle args = new Bundle();
        args.putInt(ContactLoader.CONTACT_TYPE_KEY, ContactType.WHITE_LIST_CONTACT);
        getLoaderManager().restartLoader(CONTACTS_LIST_LOADER, args, this);
        //register a receiver that gets notified when a blocked list contat is updated from phonebook
        IntentFilter filter = new IntentFilter(Constants.ACTION_REFRESH_BLOCKED_LIST_UI);
        //only receive broadcasts which are sent through the valid private permission - we don't want to receive a broadcast just matching an intent - we want the permission too
        getActivity().registerReceiver(mOnUpdateContactFromPhoneBook, filter, Constants.PRIVATE_PERMISSION, null);
    }

    @Override
    public void doOnBroadcastReceived() {
        Utility.showCallInterceptionAlertDialog(getActivity());
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

        startSingleContactActivity(contact, true);
    }

    //utility method to start single contact activity - it will be used two places
    private void startSingleContactActivity(Contact contact, boolean isContactFromPhoneBook){

        Intent intent = new Intent(getActivity(), SingleContactActivity.class);
        //Intent intent = new Intent(getActivity(), CrimePagerActivity.class);
        intent.putExtra(SingleContactFragment.CONTACT, contact);
        intent.putExtra(SingleContactFragment.ARG_CONTACT_FROM_PHONEBOOK, isContactFromPhoneBook);
        startActivityForResult(intent, SINGLE_WHITELIST_CONTACT_ACTIVITY_RESULT);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.white_list_options, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        final MenuItem menuSearch = menu.findItem(R.id.search);
        final SearchView searchView =
                (SearchView) menuSearch.getActionView();

        if(searchView != null){

            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(this);

            /** below is a trick to close/collapse/hide the searchbox whenever 'x' button is clicked
             * This is how it works:
             * when onActionViewCollapsed() is called in searchView, it does all the termination of tasks associated with closing this search box
             * calling collapseActionView() on the menuItem also collapses and hides the searchbox altogether.
             * These two methods are called when the 'x' button ('x' imageView to be specific) is clicked.
             * To find the 'x' image, search the button id by the image name (search_close_btn) and use the id to get the close button from the searchView.
             *
             * Then override the onClickListener
             */

            int searchCloseButtonId = searchView.getContext().getResources()
                    .getIdentifier("android:id/search_close_btn", null, null);

            ImageView closeButton = (ImageView)searchView.findViewById(searchCloseButtonId);

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Clear query
                    searchView.setQuery("", false);
                    //Collapse the action view
                    searchView.onActionViewCollapsed();
                    //Collapse the search widget
                    menuSearch.collapseActionView();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_item_settings:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                intent.putExtra(Constants.FRAGMENT_ID, Constants.WHITE_LIST_FRAGMENT);
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

    @Override
    public Loader<DataBaseHelper.ContactCursor> onCreateLoader(int i, Bundle bundle) {

        if(bundle == null){

            bundle = new Bundle();
        }

        bundle.putInt(ContactLoader.CONTACT_TYPE_KEY, ContactType.WHITE_LIST_CONTACT);
        return new ContactLoader(getActivity(), bundle);
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

    /*
     Support search from search bar
      */
    @Override
    public boolean onQueryTextSubmit(String searchString) {

        Bundle args = new Bundle();
        args.putString(ContactLoader.QUERY_STRING_KEY, searchString);
        args.putInt(ContactLoader.CONTACT_TYPE_KEY, ContactType.WHITE_LIST_CONTACT);
        getLoaderManager().restartLoader(CONTACTS_LIST_LOADER, args, WhiteListFragment.this);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchString) {

        Bundle args = new Bundle();
        args.putString(ContactLoader.QUERY_STRING_KEY, searchString);
        args.putInt(ContactLoader.CONTACT_TYPE_KEY, ContactType.WHITE_LIST_CONTACT);
        getLoaderManager().restartLoader(CONTACTS_LIST_LOADER, args, WhiteListFragment.this);

        return false;
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

            ImageView outGoingCallBlockStateImage = (ImageView) view.findViewById(R.id.outgoing_call_block_state_image);
            ImageView incomingCallBlockStateImage = (ImageView) view.findViewById(R.id.incoming_call_block_state_image);

            contactNameTextView.setText(contact.getContactName());
            contactPhoneTextView.setText(contact.getDisplayNumber());

            //set incoming block state image
            switch(contact.getIncomingBlockedState()){

                case BlockState.WHITE_LIST:

                    incomingCallBlockStateImage.setImageResource(R.drawable.green_arrow_left);
                    break;
            }

            //set outgoing block state image
            switch(contact.getOutGoingBlockedState()){

                case BlockState.WHITE_LIST:

                    outGoingCallBlockStateImage.setImageResource(R.drawable.green_arrow);
                    break;
            }
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
            String[] queryFields = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY/*DISPLAY_NAME*/, ContactsContract.Contacts.HAS_PHONE_NUMBER};
            //perform your query - the contactUri is like a "where" clause here
            Cursor c = cr.query(contactUri, queryFields, null, null, null, null);
            //double check that you actually got results
            if (c.getCount() == 0) {
                c.close();
                return;
            }
            //go to the first row
            c.moveToFirst();

            long id = c.getLong(c.getColumnIndex(ContactsContract.Contacts._ID));
            String contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));//DISPLAY_NAME));
            String phoneNumber = null;

            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{String.valueOf(id)}, null);

                pCur.moveToFirst();
                phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                pCur.close();
            }
            //close cursor
            c.close();
            //check if phone number is empty
            if(phoneNumber == null){
                Toast.makeText(getActivity().getApplicationContext(), R.string.phoneNumberIsEmpty, Toast.LENGTH_SHORT);
            }
            else{

                mContactManager.insertNewOrUpdateExistingContact(id, phoneNumber, contactName, false, ContactType.WHITE_LIST_CONTACT);
            }
        }
        else if(requestCode == ADD_CONTACT_MANUALLY){

            if(data != null){
                //do something about it
                String phoneNumber = data.getStringExtra(AddNewPhoneFragment.NEW_PHONE_NUMBER_EXTRA_KEY);

                mContactManager.insertNewOrUpdateExistingContact(mContactManager.getArbitraryContactId(), phoneNumber, null, true, ContactType.WHITE_LIST_CONTACT);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

/*        View  emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_white_list_view, null);

        ((ViewGroup)getListView().getParent()).addView(emptyView);
        getListView().setEmptyView(emptyView);*/


        Bundle args = new Bundle();
        args.putInt(ContactLoader.CONTACT_TYPE_KEY, ContactType.WHITE_LIST_CONTACT);
        getLoaderManager().initLoader(CONTACTS_LIST_LOADER, args, this);
    }

    @Override
    public void updateContent() {

        Activity activity = getActivity();
        if (isAdded() && activity != null) {

            getLoaderManager().restartLoader(CONTACTS_LIST_LOADER, null, this);
        }
    }
}
