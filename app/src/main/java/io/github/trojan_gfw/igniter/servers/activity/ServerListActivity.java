package io.github.trojan_gfw.igniter.servers.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import io.github.trojan_gfw.igniter.IgniterApplication;
import io.github.trojan_gfw.igniter.R;
import io.github.trojan_gfw.igniter.persistence.Storage;
import io.github.trojan_gfw.igniter.servers.data.ServerListDataManager;
import io.github.trojan_gfw.igniter.servers.fragment.ServerListFragment;
import io.github.trojan_gfw.igniter.servers.presenter.ServerListPresenter;

public class ServerListActivity extends AppCompatActivity {
    public static final String KEY_TROJAN_CONFIG = "trojan_config";

    public static Intent create(Context context) {
        return new Intent(context, ServerListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);

        FragmentManager fm = getSupportFragmentManager();
        ServerListFragment fragment = (ServerListFragment) fm.findFragmentByTag(ServerListFragment.TAG);
        if (fragment == null) {
            fragment = ServerListFragment.newInstance();
        }
        Storage storage = IgniterApplication.getApplication().storage;
        new ServerListPresenter(fragment, new ServerListDataManager(storage.getTrojanConfigListPath()));
        fm.beginTransaction()
                .replace(R.id.parent_fl, fragment, ServerListFragment.TAG)
                .commitAllowingStateLoss();
    }
}
