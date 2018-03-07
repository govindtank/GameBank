package com.codernauti.gamebank;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.PlayerProfile;
import com.codernauti.gamebank.util.EditTextActivity;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private static final int NICKNAME_RESULT = 10;

    @BindView(R.id.settings_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NICKNAME_RESULT && resultCode == RESULT_OK) {

            String newNickname = data.getExtras().getString(EditTextActivity.TEXT_RESULT);
            Log.d(TAG, "From EditTextActivity: " + newNickname);

            PlayerProfile pp = GameBank.getPlayerProfile();

            if (pp != null) {

                Log.d(TAG, "PlayerProfile already existing. Updating the old one");
                
                pp.setNickname(newNickname);
            } else {

                Log.d(TAG, "No PlayerProfile. Setting a new one");
                
                GameBank.setPlayerProfile(new PlayerProfile(newNickname));
            }

        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Result Canceled");
        }
    }

    @OnClick(R.id.change_name_button)
    public void onChangeNameClickedButton() {

        Intent intent = new Intent(this, EditTextActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(EditTextActivity.TOOLBAR_TITLE, getString(R.string.username));
        bundle.putString(EditTextActivity.EDIT_FIELD_NAME, getString(R.string.edit_username));
        bundle.putString(EditTextActivity.DESCRIPTION, getString(R.string.set_username_description));

        intent.putExtras(bundle);

        startActivityForResult(intent, NICKNAME_RESULT);
    }

    @OnClick(R.id.settings_change_image_button)
    public void onChangeProfilePictureClickedButton() {

        Log.e(TAG, "Change profile picture still work in progress");

        // TODO Before using this, we need permissions and glide support!

        /*
         Matisse.from(MainActivity.this)
        .choose(MimeType.allOf())
        .countable(true)
        .maxSelectable(9)
        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        .thumbnailScale(0.85f)
        .imageEngine(new GlideEngine())
        .forResult(REQUEST_CODE_CHOOSE);
        */
    }

}
