package io.github.trojan_gfw.igniter.exempt.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import android.view.Window;

import io.github.trojan_gfw.igniter.IgniterApplication;
import io.github.trojan_gfw.igniter.R;
import io.github.trojan_gfw.igniter.exempt.contract.ExemptAppContract;
import io.github.trojan_gfw.igniter.exempt.data.ExemptAppDataManager;
import io.github.trojan_gfw.igniter.exempt.fragment.ExemptAppFragment;
import io.github.trojan_gfw.igniter.exempt.presenter.ExemptAppPresenter;
import io.github.trojan_gfw.igniter.persistence.Storage;

public class ExemptAppActivity extends AppCompatActivity {
    private ExemptAppContract.Presenter mPresenter;
    Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_exempt_app);

        storage = ((IgniterApplication)getApplication()).storage;
        FragmentManager fm = getSupportFragmentManager();
        ExemptAppFragment fragment = (ExemptAppFragment) fm.findFragmentByTag(ExemptAppFragment.TAG);
        if (fragment == null) {
            fragment = ExemptAppFragment.newInstance();
        }
        mPresenter = new ExemptAppPresenter(fragment,
                new ExemptAppDataManager(getApplicationContext(),
                storage.getExemptedAppListPath()));
        fm.beginTransaction()
                .replace(R.id.parent_fl, fragment, ExemptAppFragment.TAG)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (mPresenter == null || !mPresenter.handleBackPressed()) {
            super.onBackPressed();
        }
    }
}
