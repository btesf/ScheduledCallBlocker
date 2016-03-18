package com.example.bereket.callblocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bereket on 2/18/16.
 */
public class MainAppActivity extends AppCompatActivity
        implements BlockedListFragment.OnFragmentInteractionListener,
        LogFragment.OnFragmentInteractionListener,WhiteListFragment.OnFragmentInteractionListener{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        final int fragmentId = getIntent().getIntExtra(Constants.FRAGMENT_ID, Constants.BLOCKED_LIST_FRAGMENT);

        setupViewPager(viewPager);
        viewPager.setCurrentItem(fragmentId);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        /*
        When tabbed activity is set, all the tabs are populated for the first time and all the lifecycle methods are called once.
        As a result, when a contact is deleted from a list, we can't rely on the fragment's lifecycle methods (onPause, onResume) to restart loader.

        Instead, tabLayout's setOntabSelectedListener event is called, and I called the updateContent() (for the fragments implementing this interface) to
        restart the loader

        whenever a tab is changed, the fragment lifecycle methods were not called.
         */
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(adapter != null){

                    int position = viewPager.getCurrentItem();

                    viewPager = (ViewPager) findViewById(R.id.viewpager);
                    viewPager.setCurrentItem(position);

                    Fragment tabbedFragment =  adapter.getRegisteredFragment(viewPager.getCurrentItem());

                    if(tabbedFragment instanceof UpdatableFragment){

                        ((UpdatableFragment) tabbedFragment).updateContent();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

/*                Fragment tabbedFragment =  adapter.getItem(viewPager.getCurrentItem());
                if(tabbedFragment instanceof LogFragment){

                    ((LogFragment) tabbedFragment).updateContent();
                }*/
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
/*        adapter.addFragment(new BlockedListFragment(), getResources().getString(R.string.block_list_tab_name));
        adapter.addFragment(new LogFragment(), getResources().getString(R.string.log_list_tab_name));
        adapter.addFragment(new WhiteListFragment(), getResources().getString(R.string.white_list_tab_name));*/
        viewPager.setAdapter(adapter);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        //private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        SparseArray<Fragment> mFragmentList = new SparseArray<Fragment>(); //I want this, because the fragment manager always keeps its original fragments in memory

        private List<Fragment> fragments = new ArrayList<Fragment>();

        {
            fragments.add(new BlockedListFragment());
            fragments.add(new LogFragment());
            fragments.add(new WhiteListFragment());

            mFragmentTitleList.add(getResources().getString(R.string.block_list_tab_name));
            mFragmentTitleList.add(getResources().getString(R.string.log_list_tab_name));
            mFragmentTitleList.add(getResources().getString(R.string.white_list_tab_name));
        }

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

/*        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }*/



        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragmentList.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragmentList.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return mFragmentList.get(position);
        }
    }
}
