package com.example.bereket.callblocker;


public class WhiteListActivity extends SingleFragmentActivity implements WhiteListFragment.OnFragmentInteractionListener{

    @Override
    protected android.app.Fragment createFragment() {

        String arg1 = "";
        String arg2 = "";

        return WhiteListFragment.newInstance(arg1, arg2);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
