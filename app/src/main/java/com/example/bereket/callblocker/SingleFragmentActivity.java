package com.example.bereket.callblocker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract android.app.Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit();
        }
    }
}
