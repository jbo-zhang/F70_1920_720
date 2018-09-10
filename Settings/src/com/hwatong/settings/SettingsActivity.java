package com.hwatong.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceActivity.Header;
import android.view.View;
import android.view.View.OnClickListener;

import com.hwatong.settings.fragment.MyRingerVolumeFragment;
import com.hwatong.settings.R;

public class SettingsActivity extends Activity implements OnClickListener{
	private final String TAG = "SettingsActivity";
	private static final String META_DATA_KEY_FRAGMENT_CLASS ="com.hwatong.settings.FRAGMENT_CLASS";

	private String mFragmentClass;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		getMetaData();

        Fragment f = Fragment.instantiate(this, mFragmentClass, null);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.container, f);
        transaction.commitAllowingStateLoss();
	}
	
	private void getMetaData() {
		try {
			ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(),
					PackageManager.GET_META_DATA);
			if (ai == null || ai.metaData == null) return;
			mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);

		} catch (NameNotFoundException nnfe) {
			// No recovery
		}
	}
	
	@Override
	public void onClick(View v) {
	}

}
