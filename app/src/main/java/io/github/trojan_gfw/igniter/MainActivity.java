package io.github.trojan_gfw.igniter;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import io.github.trojan_gfw.igniter.common.os.Task;
import io.github.trojan_gfw.igniter.common.os.Threads;
import io.github.trojan_gfw.igniter.connection.TrojanConnection;
import io.github.trojan_gfw.igniter.exempt.activity.ExemptAppActivity;
import io.github.trojan_gfw.igniter.persistence.NetWorkConfig;
import io.github.trojan_gfw.igniter.persistence.Storage;
import io.github.trojan_gfw.igniter.persistence.TrojanConfig;
import io.github.trojan_gfw.igniter.proxy.aidl.ITrojanService;
import io.github.trojan_gfw.igniter.servers.activity.ServerListActivity;
import io.github.trojan_gfw.igniter.servers.data.ServerListDataManager;
import io.github.trojan_gfw.igniter.servers.data.ServerListDataSource;

public class MainActivity extends AppCompatActivity implements TrojanConnection.Callback {
    private static final String TAG = "MainActivity";
    private static final int READ_WRITE_EXT_STORAGE_PERMISSION_REQUEST = 514;
    private static final int VPN_REQUEST_CODE = 233;
    private static final int SERVER_LIST_CHOOSE_REQUEST_CODE = 1024;
    private static final int EXEMPT_APP_CONFIGURE_REQUEST_CODE = 2077;
    private static final String CONNECTION_TEST_URL = "https://www.google.com";

    IgniterApplication app;

    private ViewGroup rootViewGroup;
    private EditText remoteAddressText;
    private EditText remotePortText;
    private EditText localOrClashPortText;
    private EditText passwordText;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch ipv6Switch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch verifySwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch clashSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch enableLanSwitch;
    private TextView clashLink;
    private Button startStopButton;
    private EditText trojanURLText;
    private @ProxyService.ProxyState
    int proxyState = ProxyService.STATE_NONE;
    private final TrojanConnection connection = new TrojanConnection(false);
    private ITrojanService trojanService;
    private ServerListDataSource serverListDataManager;
    private final TextViewListener remoteAddressTextListener = new TextViewListener() {
        @Override
        protected void onTextChanged(String before, String old, String aNew, String after) {
            // update TextView
            startUpdates(); // to prevent infinite loop.
            if (remoteAddressText.hasFocus()) {
                TrojanConfig ins = app.trojanConfig;
                ins.setRemoteAddr(remoteAddressText.getText().toString());
            }
            endUpdates();
        }
    };
    private final TextViewListener remotePortTextListener = new TextViewListener() {
        @Override
        protected void onTextChanged(String before, String old, String aNew, String after) {
            // update TextView
            startUpdates(); // to prevent infinite loop.
            if (remotePortText.hasFocus()) {
                TrojanConfig ins = app.trojanConfig;
                String portStr = remotePortText.getText().toString();
                try {
                    int port = Integer.parseInt(portStr);
                    ins.setRemotePort(port);
                } catch (NumberFormatException e) {
                    // Ignore when we get invalid number
                    e.printStackTrace();
                }
            }
            endUpdates();
        }
    };

    private final TextViewListener localOrClashPortTextListener = new TextViewListener() {
        @Override
        protected void onTextChanged(String before, String old, String aNew, String after) {
            // update TextView
            startUpdates(); // to prevent infinite loop.
            if (localOrClashPortText.hasFocus()) {
                String portStr = localOrClashPortText.getText().toString();
                int port = Integer.parseInt(portStr);
                NetWorkConfig.setPort(app, port);
            }
            endUpdates();
        }
    };

    private TextViewListener passwordTextListener = new TextViewListener() {
        @Override
        protected void onTextChanged(String before, String old, String aNew, String after) {
            // update TextView
            startUpdates(); // to prevent infinite loop.
            if (passwordText.hasFocus()) {
                app.trojanConfig.setPassword(passwordText.getText().toString());
            }
            endUpdates();
        }
    };

    private void updateViews(int state) {
        proxyState = state;
        boolean inputEnabled;
        switch (state) {
            case ProxyService.STARTING: {
                inputEnabled = false;
                startStopButton.setText(R.string.button_service__starting);
                startStopButton.setEnabled(false);
                break;
            }
            case ProxyService.STARTED: {
                inputEnabled = false;
                startStopButton.setText(R.string.button_service__stop);
                startStopButton.setEnabled(true);
                break;
            }
            case ProxyService.STOPPING: {
                inputEnabled = false;
                startStopButton.setText(R.string.button_service__stopping);
                startStopButton.setEnabled(false);
                break;
            }
            default: {
                inputEnabled = true;
                startStopButton.setText(R.string.button_service__start);
                startStopButton.setEnabled(true);
                break;
            }
        }
        remoteAddressText.setEnabled(inputEnabled);
        remotePortText.setEnabled(inputEnabled);
        localOrClashPortText.setEnabled(inputEnabled);
        ipv6Switch.setEnabled(inputEnabled);
        passwordText.setEnabled(inputEnabled);
        trojanURLText.setEnabled(inputEnabled);
        verifySwitch.setEnabled(inputEnabled);
        clashSwitch.setEnabled(inputEnabled);
        enableLanSwitch.setEnabled(inputEnabled);
        clashLink.setEnabled(inputEnabled);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = IgniterApplication.getApplication();

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        rootViewGroup = findViewById(R.id.rootScrollView);
        Button saveServerIb = findViewById(R.id.saveConfigBtn);
        remoteAddressText = findViewById(R.id.remoteAddrText);
        remotePortText = findViewById(R.id.remotePortText);
        localOrClashPortText = findViewById(R.id.localOrClashPortText);
        passwordText = findViewById(R.id.passwordText);
        trojanURLText = findViewById(R.id.trojanURLText);
        ipv6Switch = findViewById(R.id.ipv6Switch);
        verifySwitch = findViewById(R.id.verifySwitch);
        clashSwitch = findViewById(R.id.clashSwitch);
        enableLanSwitch = findViewById(R.id.switch_enable_lan);
        clashLink = findViewById(R.id.clashLink);
        clashLink.setMovementMethod(LinkMovementMethod.getInstance());
        startStopButton = findViewById(R.id.startStopButton);

        remoteAddressText.addTextChangedListener(remoteAddressTextListener);

        remotePortText.addTextChangedListener(remotePortTextListener);
        localOrClashPortText.addTextChangedListener(localOrClashPortTextListener);
        passwordText.addTextChangedListener(passwordTextListener);

        passwordText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                passwordText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                // place cursor on the end
                passwordText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                passwordText.setSelection(passwordText.getText().length());
            }
        });

        clashSwitch.setChecked(app.trojanPreferences.getEnableClash());
        clashSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.trojanPreferences.setEnableClash(isChecked);
            int port;
            if (app.trojanPreferences.getEnableClash()) {
                port = app.clashConfig.getPort();
            } else {
                port = app.trojanConfig.getLocalPort();
            }
            localOrClashPortText.setText(String.valueOf(port));
            NetWorkConfig.setPort(app, port);
            Log.wtf("MAIN", "" + app.clashConfig.getPort());
            Log.wtf("MAIN", "" + app.trojanConfig.getLocalPort());
        });

        enableLanSwitch.setChecked(app.trojanPreferences.isEnableLan());
        enableLanSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.trojanPreferences.setEnableLan(isChecked);
        });

        ipv6Switch.setOnCheckedChangeListener((buttonView, isChecked) -> app.trojanPreferences.setEnableIPV6(isChecked));

        verifySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TrojanConfig ins = app.trojanConfig;
            ins.setVerifyCert(isChecked);
        });


        initURIEditor();
        startStopButton.setOnClickListener(v -> {
            if (!app.trojanConfig.isValidRunningConfig()) {
                Toast.makeText(MainActivity.this,
                        R.string.invalid_configuration,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (proxyState == ProxyService.STATE_NONE || proxyState == ProxyService.STOPPED) {
                TrojanConfig.write(
                        app.trojanConfig,
                        app.storage.getTrojanConfigPath()
                );
                Storage.print(app.storage.getTrojanConfigPath(), TrojanConfig.SINGLE_CONFIG_TAG);
                // start ProxyService
                Intent i = VpnService.prepare(getApplicationContext());
                if (i != null) {
                    startActivityForResult(i, VPN_REQUEST_CODE);
                } else {
                    app.startProxyService();
                }
            } else if (proxyState == ProxyService.STARTED) {
                // stop ProxyService
                app.stopProxyService();
            }
        });
        saveServerIb.setOnClickListener(v -> {
            if (!app.trojanConfig.isValidRunningConfig()) {
                Toast.makeText(MainActivity.this, R.string.invalid_configuration, Toast.LENGTH_SHORT).show();
                return;
            }
            Threads.instance().runOnWorkThread(new Task() {
                @Override
                public void onRun() {
                    TrojanConfig config = app.trojanConfig;
                    TrojanConfig.write(config, app.storage.getTrojanConfigPath());
                    try {
                        app.clashConfig.save(app.storage.getClashConfigPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    serverListDataManager.saveServerConfig(config);
                    showSaveConfigResult();
                }
            });
        });
        serverListDataManager = new ServerListDataManager(app.storage.getTrojanConfigListPath());
        connection.connect(this, this);
        if (!app.storage.isExternalWritable() && ActivityCompat
                .shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestReadWriteExternalStoragePermission();
        }
    }

    private void requestReadWriteExternalStoragePermission() {
        new AlertDialog.Builder(this).setTitle(R.string.common_alert)
                .setMessage(R.string.main_write_external_storage_permission_requirement)
                .setPositiveButton(R.string.common_confirm, (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, READ_WRITE_EXT_STORAGE_PERMISSION_REQUEST);
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public void onServiceConnected(final ITrojanService service) {
        LogHelper.i(TAG, "onServiceConnected");
        trojanService = service;
        Threads.instance().runOnWorkThread(new Task() {
            @Override
            public void onRun() {
                try {
                    final int state = service.getState();
                    runOnUiThread(() -> updateViews(state));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onServiceDisconnected() {
        LogHelper.i(TAG, "onServiceConnected");
        trojanService = null;
    }

    @Override
    public void onStateChanged(int state, String msg) {
        LogHelper.i(TAG, "onStateChanged# state: " + state + " msg: " + msg);
        updateViews(state);
    }

    @Override
    public void onTestResult(final String testUrl, final boolean connected, final long delay, @NonNull final String error) {
        runOnUiThread(() -> showTestConnectionResult(testUrl, connected, delay, error));
    }

    private void showTestConnectionResult(String testUrl, boolean connected, long delay, @NonNull String error) {
        if (connected) {
            Toast.makeText(getApplicationContext(), getString(R.string.connected_to__in__ms,
                    testUrl, String.valueOf(delay)), Toast.LENGTH_LONG).show();
        } else {
            LogHelper.e(TAG, "TestError: " + error);
            Toast.makeText(getApplicationContext(),
                    getString(R.string.failed_to_connect_to__,
                            testUrl, error),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBinderDied() {
        LogHelper.i(TAG, "onBinderDied");
        connection.disconnect(this);
        // connect the new binder
        // todo is it necessary to re-connect?
        connection.connect(this, this);
    }

    /**
     * Test connection by invoking {@link ITrojanService#testConnection(String)}. Since {@link ITrojanService}
     * is from remote process, a {@link RemoteException} might be thrown. Test result will be delivered
     * to {@link #onTestResult(String, boolean, long, String)} by {@link TrojanConnection}.
     */
    private void testConnection() {
        ITrojanService service = trojanService;
        if (service == null) {
            showTestConnectionResult(CONNECTION_TEST_URL, false, 0L, "Trojan service is not available.");
        } else {
            try {
                service.testConnection(CONNECTION_TEST_URL);
            } catch (RemoteException e) {
                showTestConnectionResult(CONNECTION_TEST_URL, false, 0L, "Trojan service throws RemoteException.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Show develop info in Logcat by invoking {@link ITrojanService#showDevelopInfoInLogcat}. Since {@link ITrojanService}
     * is from remote process, a {@link RemoteException} might be thrown.
     */
    private void showDevelopInfoInLogcat() {
        ITrojanService service = trojanService;
        if (service != null) {
            try {
                service.showDevelopInfoInLogcat();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearEditTextFocus() {
        remoteAddressText.clearFocus();
        remotePortText.clearFocus();
        localOrClashPortText.clearFocus();
        passwordText.clearFocus();
        trojanURLText.clearFocus();
    }

    private void showSaveConfigResult() {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                R.string.main_save_success,
                Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SERVER_LIST_CHOOSE_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK && data != null) {
            trojanURLText.setText("");
            final TrojanConfig config = data.getParcelableExtra(ServerListActivity.KEY_TROJAN_CONFIG);
            if (config != null) {
                config.setCaCertPath(app.storage.getCaCertPath());
                TrojanConfig.setInstance(config);
                runOnUiThread(() -> {
                    remoteAddressText.setText(config.getRemoteAddr());
                    remotePortText.setText(String.valueOf(config.getRemotePort()));
                    if (app.trojanPreferences.getEnableClash()) {
                        localOrClashPortText.setText(String.valueOf(app.clashConfig.getPort()));
                    } else {
                        localOrClashPortText.setText(String.valueOf(config.getLocalPort()));
                    }
                    passwordText.setText(config.getPassword());
                });
                trojanURLText.setText(TrojanConfig.toURIString(config));
                ipv6Switch.setChecked(app.trojanPreferences.getEnableIPV6());
                verifySwitch.setChecked(config.getVerifyCert());
            }
        } else if (EXEMPT_APP_CONFIGURE_REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            if (ProxyService.STARTED == proxyState) {
                Snackbar.make(rootViewGroup, R.string.main_restart_proxy_service_tip, Snackbar.LENGTH_LONG).show();
            }
        } else if (VPN_REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            app.startProxyService();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Bind menu items to their relative actions
        switch (item.getItemId()) {
            case R.id.action_test_connection:
                testConnection();
                return true;
            case R.id.action_show_develop_info_logcat:
                // log of this process
                LogHelper.showDevelopInfoInLogcat();
                // log of other processes
                showDevelopInfoInLogcat();
                return true;
            case R.id.menu_clash_editor:
                Intent intent = new Intent(this, ClashFileEditorActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_view_server_list:
                clearEditTextFocus();
                startActivityForResult(ServerListActivity.create(MainActivity.this), SERVER_LIST_CHOOSE_REQUEST_CODE);
                return true;
            case R.id.action_exempt_app:
                if (app.storage.isExternalWritable()) {
                    startActivityForResult(ExemptAppActivity.create(this), EXEMPT_APP_CONFIGURE_REQUEST_CODE);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_WRITE_EXT_STORAGE_PERMISSION_REQUEST);
                    } else {
                        Snackbar.make(rootViewGroup, R.string.main_exempt_feature_permission_requirement, Snackbar.LENGTH_LONG).show();
                    }
                }
                return true;
            default:
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        TrojanConfig trojanConfig = app.trojanConfig;
        remoteAddressText.setText(trojanConfig.getRemoteAddr());
        remotePortText.setText(String.valueOf(trojanConfig.getRemotePort()));
        if (app.trojanPreferences.getEnableClash()) {
            localOrClashPortText.setText(String.valueOf(app.clashConfig.getPort()));
        } else {
            localOrClashPortText.setText(String.valueOf(app.trojanConfig.getLocalPort()));
        }
        passwordText.setText(trojanConfig.getPassword());
        ipv6Switch.setChecked(app.trojanPreferences.getEnableIPV6());
        verifySwitch.setChecked(trojanConfig.getVerifyCert());
        remoteAddressText.setSelection(remoteAddressText.length());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.disconnect(this);
    }

    public void initURIEditor() {

        trojanURLText.setOnLongClickListener(v -> {
            trojanURLText.selectAll();
            return false;
        });

        trojanURLText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                trojanURLText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                // it seems we don't have to place cursor on the end for Trojan URL
                trojanURLText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            }
        });

        trojanURLText.addTextChangedListener(new TextViewListener() {
            @Override
            protected void onTextChanged(String before, String old, String aNew, String after) {
                // update TextView
                startUpdates(); // to prevent infinite loop.
                TrojanConfig ins = app.trojanConfig;
                TrojanConfig parsedConfig = TrojanConfig.fromURIString(before + aNew + after);
                if (parsedConfig != null) {
                    String remoteAddress = parsedConfig.getRemoteAddr();
                    int remotePort = parsedConfig.getRemotePort();
                    String password = parsedConfig.getPassword();

                    ins.setRemoteAddr(remoteAddress);
                    ins.setRemotePort(remotePort);
                    ins.setPassword(password);
                }
                endUpdates();
            }
        });

        TextViewListener trojanConfigChangedTextViewListener = new TextViewListener() {
            @Override
            protected void onTextChanged(String before, String old, String aNew, String after) {
                startUpdates();
                String str = TrojanConfig.toURIString(app.trojanConfig);
                if (str != null) {
                    trojanURLText.setText(str);
                }
                endUpdates();
            }
        };

        remoteAddressText.addTextChangedListener(trojanConfigChangedTextViewListener);
        remotePortText.addTextChangedListener(trojanConfigChangedTextViewListener);
        passwordText.addTextChangedListener(trojanConfigChangedTextViewListener);
    }
}
