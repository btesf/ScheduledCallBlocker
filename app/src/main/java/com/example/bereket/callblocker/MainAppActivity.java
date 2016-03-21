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

import java.lang.ref.WeakReference;
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

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new BlockedListFragment(), getResources().getString(R.string.block_list_tab_name));
        adapter.addFragment(new LogFragment(), getResources().getString(R.string.log_list_tab_name));
        adapter.addFragment(new WhiteListFragment(), getResources().getString(R.string.white_list_tab_name));

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(fragmentId);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new WorkaroundTabLayoutOnPageChangeListener(tabLayout));

        //in the tabbed view, action bar has a shadow under it that draws a black line between it and the tabs below.
        //to remove the shadow for this activity only (since the actionBar is common/application theme property), setting the elevation to 0 does the trick
        ActionBar bar = getSupportActionBar();
        bar.setElevation(0);
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

                if (adapter != null) {

                    //int position = viewPager.getCurrentItem();
                    int position = tab.getPosition();

                    viewPager = (ViewPager) findViewById(R.id.viewpager);
                    viewPager.setCurrentItem(position);

                    Fragment tabbedFragment = adapter.getRegisteredFragment(viewPager.getCurrentItem());

                    if (tabbedFragment instanceof UpdatableFragment) {

                        ((UpdatableFragment) tabbedFragment).updateContent();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    /*
    Fragent pager adapter keeps the fragments in memory. Which means, it keeps the fragments in the FragmentManager once they are created, and
     it won't recreate them anymore. This creates problem when the activity is destroyed and re-created. In such case there will be a new instance
     of the Activity but not the fragments. Since we don't have the references of the older
     fragments anymore we can't call the implemented interface method updateContent() and refresh the views of fragments.
     That's why I hold the fragment instances used by the fragmentmanager when instantiateItem method is called..
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        //private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private SparseArray<Fragment> mFragmentList = new SparseArray<Fragment>(); //I want this, because the fragment manager always keeps its original fragments in memory
        private List<Fragment> fragments = new ArrayList<Fragment>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {

            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }


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

    /**
     * this workAround listener is a fix/workaround for discovered marshalmellow (23.xxx) design library tab listener issue
     * breaking the tab selection prior to that version
     */
    public class WorkaroundTabLayoutOnPageChangeListener extends TabLayout.TabLayoutOnPageChangeListener {
        private final WeakReference<TabLayout> mTabLayoutRef;

        public WorkaroundTabLayoutOnPageChangeListener(TabLayout tabLayout) {
            super(tabLayout);
            this.mTabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            final TabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null) {
                final TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) {
                    tab.select();
                }
            }
        }
    }
}
