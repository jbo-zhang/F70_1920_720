/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwatong.settings;

import java.util.HashMap;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.hwatong.settings.accounts.AuthenticatorHelper;
import com.hwatong.settings.accounts.ManageAccountsSettings;
import com.hwatong.settings.wifi.WifiEnabler;
import com.hwatong.settings.R;

/**
 * Top-level settings activity to handle single pane and double pane UI layout.
 */
public class Settings extends PreferenceActivity
        implements ButtonBarHandler, OnAccountsUpdateListener {

    private static final String LOG_TAG = "Settings";

	private static final String META_DATA_KEY_HEADER_ID =
        "com.hwatong.settings.TOP_LEVEL_HEADER_ID";
    private static final String META_DATA_KEY_FRAGMENT_CLASS =
        "com.hwatong.settings.FRAGMENT_CLASS";
    private static final String META_DATA_KEY_PARENT_TITLE =
        "com.hwatong.settings.PARENT_FRAGMENT_TITLE";
    private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS =
        "com.hwatong.settings.PARENT_FRAGMENT_CLASS";

    private static final String EXTRA_CLEAR_UI_OPTIONS = "settings:remove_ui_options";

    private static final String SAVE_KEY_CURRENT_HEADER = "com.hwatong.settings.CURRENT_HEADER";
    private static final String SAVE_KEY_CURRENT_HEADER_SELECTED = "com.hwatong.settings.CURRENT_HEADER_SELECTED";
    private static final String SAVE_KEY_PARENT_HEADER = "com.hwatong.settings.PARENT_HEADER";

    private String mFragmentClass;
    private int mTopLevelHeaderId;
    private Header mFirstHeader;
    private Header mCurrentHeader;
    private Header mParentHeader;
    private boolean mInLocalHeaderSwitch;

    // TODO: Update Call Settings based on airplane mode state.

    protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();

    private AuthenticatorHelper mAuthenticatorHelper;
    private Header mLastHeader;
    private boolean mListeningToAccountUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getBooleanExtra(EXTRA_CLEAR_UI_OPTIONS, false)) {
            getWindow().setUiOptions(0);
        }

        mAuthenticatorHelper = new AuthenticatorHelper();
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, null);

        getMetaData();
        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        mInLocalHeaderSwitch = false;

        if (!onIsHidingHeaders() && onIsMultiPane()) {
            highlightHeader(mTopLevelHeaderId);
            // Force the title so that it doesn't get overridden by a direct launch of
            // a specific settings screen.
            setTitle(R.string.settings_label);
        }

        // Retrieve any saved state
        if (savedInstanceState != null) {
            mCurrentHeader = savedInstanceState.getParcelable(SAVE_KEY_CURRENT_HEADER);
            mParentHeader = savedInstanceState.getParcelable(SAVE_KEY_PARENT_HEADER);
           	((HeaderAdapter)getListView().getAdapter()).setSelected(savedInstanceState.getInt(SAVE_KEY_CURRENT_HEADER_SELECTED));
            //
        }

        // If the current header was saved, switch to it
        if (savedInstanceState != null && mCurrentHeader != null) {
            //switchToHeaderLocal(mCurrentHeader);
            showBreadCrumbs(mCurrentHeader.title, null);
        }

        if (mParentHeader != null) {
            setParentTitle(mParentHeader.title, null, new OnClickListener() {
                public void onClick(View v) {
                    switchToParent(mParentHeader.fragment);
                }
            });
        }

        // Override up navigation for multi-pane, since we handle it in the fragment breadcrumbs
        if (onIsMultiPane()&& getActionBar()!=null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setHomeButtonEnabled(false);
        }
        if (onIsMultiPane() && getListView()!=null) {
        	getListView().setDivider(getResources().getDrawable(R.drawable.list_leftdivider));
        	getListView().setSelector(R.color.blue1);
            View v = findViewById(com.android.internal.R.id.headers);
            if (v!=null) v.setBackgroundResource(R.drawable.leftpane_bg);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current fragment, if it is the same as originally launched
        if (mCurrentHeader != null) {
            outState.putParcelable(SAVE_KEY_CURRENT_HEADER, mCurrentHeader);
        }
        if (mParentHeader != null) {
            outState.putParcelable(SAVE_KEY_PARENT_HEADER, mParentHeader);
        }
       	outState.putInt(SAVE_KEY_CURRENT_HEADER_SELECTED, ((HeaderAdapter)getListView().getAdapter()).getSelected());
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume:");
        
        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).resume();
        }
        invalidateHeaders();
    }

    @Override
    public void onPause() {
        super.onPause();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).pause();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListeningToAccountUpdates) {
            AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        }
    }

    private void switchToHeaderLocal(Header header) {
        mInLocalHeaderSwitch = true;
        switchToHeader(header);
        mInLocalHeaderSwitch = false;
    }

    @Override
    public void switchToHeader(Header header) {
    	Log.d(LOG_TAG, "switchToHeader: header="+header);
        if (!mInLocalHeaderSwitch) {
            mCurrentHeader = null;
            mParentHeader = null;
        }
        super.switchToHeader(header);
    }

    /**
     * Switch to parent fragment and store the grand parent's info
     * @param className name of the activity wrapper for the parent fragment.
     */
    private void switchToParent(String className) {
        final ComponentName cn = new ComponentName(this, className);
        try {
            final PackageManager pm = getPackageManager();
            final ActivityInfo parentInfo = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);

            if (parentInfo != null && parentInfo.metaData != null) {
                String fragmentClass = parentInfo.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                CharSequence fragmentTitle = parentInfo.loadLabel(pm);
                Header parentHeader = new Header();
                parentHeader.fragment = fragmentClass;
                parentHeader.title = fragmentTitle;
                mCurrentHeader = parentHeader;

                switchToHeaderLocal(parentHeader);
                highlightHeader(mTopLevelHeaderId);

                mParentHeader = new Header();
                mParentHeader.fragment
                        = parentInfo.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
                mParentHeader.title = parentInfo.metaData.getString(META_DATA_KEY_PARENT_TITLE);
            }
        } catch (NameNotFoundException nnfe) {
            Log.w(LOG_TAG, "Could not find parent activity : " + className);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(LOG_TAG, "onNewIntent: intent="+intent);
        
        // If it is not launched from history, then reset to top-level
        //modify by lcb at 2017-04-11
        //涓嶉噸鏂板姞杞藉ご鍒楄�?�锛屼繚鎸佸師鏉ョ殑鍒楄〃鐘舵��?
//        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0 &&
//        	(intent.getFlags() & Intent.FLAG_ACTIVITY_SINGLE_TOP) == 0) {
//            if (mFirstHeader != null && !onIsHidingHeaders() && onIsMultiPane()) {
//                switchToHeaderLocal(mFirstHeader);
//            }
//            getListView().setSelectionFromTop(0, 0);
//        }
        //
    }

    private void highlightHeader(int id) {
        if (id != 0) {
            Integer index = mHeaderIndexMap.get(id);
            if (index != null) {
            	//鐩存帴鐢╥ntent鍚姩涓�涓缃瓙椤圭洰鏃讹紝闇�瑕�?珮浜浉搴旂殑澶翠俊鎭�
                ((HeaderAdapter)getListView().getAdapter()).setSelected(index);
                //
                getListView().setItemChecked(index, true);
                if (isMultiPane()) {
                    getListView().smoothScrollToPosition(index);
                }
            }
        }
    }

    @Override
    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        // This is called from super.onCreate, isMultiPane() is not yet reliable
        // Do not use onIsHidingHeaders either, which relies itself on this method
        if (startingFragment != null && !onIsMultiPane()) {
            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getExtras();
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable("intent", superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, superIntent.getExtras());
            return modIntent;
        }
        return superIntent;
    }

    /**
     * Checks if the component name in the intent is different from the Settings class and
     * returns the class name to load as a fragment.
     */
    protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null) return mFragmentClass;

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) return null;
// by lcb at 20160630
//        if ("com.hwatong.settings.ManageApplications".equals(intentClass)
//                || "com.hwatong.settings.RunningServices".equals(intentClass)
//                || "com.hwatong.settings.applications.StorageUse".equals(intentClass)) {
//            // Old names of manage apps.
//            intentClass = com.hwatong.settings.applications.ManageApplications.class.getName();
//        }
//
        return intentClass;
    }

    /**
     * Override initial header when an activity-alias is causing Settings to be launched
     * for a specific fragment encoded in the android:name parameter.
     */
    @Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();
            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }

    @Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
            int titleRes, int shortTitleRes) {
        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args,
                titleRes, shortTitleRes);

        // some fragments want to avoid split actionbar
//        if (DataUsageSummary.class.getName().equals(fragmentName) ||
//                PowerUsageSummary.class.getName().equals(fragmentName) ||
//                AccountSyncSettings.class.getName().equals(fragmentName) ||
//                UserDictionarySettings.class.getName().equals(fragmentName) ||
//                Memory.class.getName().equals(fragmentName) ||
//                ManageApplications.class.getName().equals(fragmentName) ||
//                WirelessSettings.class.getName().equals(fragmentName) ||
//                SoundSettings.class.getName().equals(fragmentName) ||
//                PrivacySettings.class.getName().equals(fragmentName) ||
//                ManageAccountsSettings.class.getName().equals(fragmentName) ||
//                VpnSettings.class.getName().equals(fragmentName) ||
//                SecuritySettings.class.getName().equals(fragmentName) ||
//                InstalledAppDetails.class.getName().equals(fragmentName) ||
//                ChooseLockGenericFragment.class.getName().equals(fragmentName) ||
//                TetherSettings.class.getName().equals(fragmentName) ||
//                ApnSettings.class.getName().equals(fragmentName) ||
//                LocationSettings.class.getName().equals(fragmentName) ||
//                ZonePicker.class.getName().equals(fragmentName)) {
//            intent.putExtra(EXTRA_CLEAR_UI_OPTIONS, true);
//        }

        intent.setClass(this, SubSettings.class);
        return intent;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> headers) {
        loadHeadersFromResource(R.xml.settings_headers, headers);

        updateHeaderList(headers);
    }

    private void updateHeaderList(List<Header> target) {
        int i = 0;

        mHeaderIndexMap.clear();
        while (i < target.size()) {
            Header header = target.get(i);
            // Ids are integers, so downcasting
            int id = (int) header.id;
            if (id == R.id.wifi_settings) {
                // Remove WiFi Settings if WiFi service is not available.
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                    target.remove(i);
                }
            }

            // Increment if the current one wasn't removed by the Utils code.
            if (target.get(i) == header) {
                // Hold on to the first header, when we need to reset to the top-level
                if (mFirstHeader == null &&
                        HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                    mFirstHeader = header;
                }
                mHeaderIndexMap.put(id, i);
                i++;
            }
        }
    }

    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(),
                    PackageManager.GET_META_DATA);
            if (ai == null || ai.metaData == null) return;
            mTopLevelHeaderId = ai.metaData.getInt(META_DATA_KEY_HEADER_ID);
            mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);

            // Check if it has a parent specified and create a Header object
            final int parentHeaderTitleRes = ai.metaData.getInt(META_DATA_KEY_PARENT_TITLE);
            String parentFragmentClass = ai.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
            if (parentFragmentClass != null) {
                mParentHeader = new Header();
                mParentHeader.fragment = parentFragmentClass;
                if (parentHeaderTitleRes != 0) {
                    mParentHeader.title = getResources().getString(parentHeaderTitleRes);
                }
            }
        } catch (NameNotFoundException nnfe) {
            // No recovery
        }
    }

    @Override
    public boolean hasNextButton() {
        return super.hasNextButton();
    }

    @Override
    public Button getNextButton() {
        return super.getNextButton();
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        static final int HEADER_TYPE_SWITCH = 2;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;

        private final WifiEnabler mWifiEnabler;
        private AuthenticatorHelper mAuthHelper;
        private View mWifiView;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            Switch switch_;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
//            } else if (header.id == R.id.wifi_settings) {
//                return HEADER_TYPE_SWITCH;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private int mSelected=0;
        public void setSelected(int selected) {mSelected=selected;}
        public int getSelected() { return mSelected;}
        
        public HeaderAdapter(Context context, List<Header> objects,
                AuthenticatorHelper authenticatorHelper) {
            super(context, 0, objects);

            mAuthHelper = authenticatorHelper;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Temp Switches provided as placeholder until the adapter replaces these with actual
            // Switches inflated from their layouts. Must be done before adapter is set in super
            mWifiEnabler = new WifiEnabler(context, new Switch(context));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_SWITCH:
	                        view = mInflater.inflate(R.layout.preference_header_switch_item, parent,
	                                false);
	                        holder.icon = (ImageView) view.findViewById(R.id.icon);
	                        holder.title = (TextView)
	                                view.findViewById(com.android.internal.R.id.title);
	                        holder.summary = (TextView)
	                                view.findViewById(com.android.internal.R.id.summary);
	                        holder.switch_ = (Switch) view.findViewById(R.id.switchWidget);

                        break;

                    default:
                        view = mInflater.inflate(
                                R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(com.android.internal.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(com.android.internal.R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;

                case HEADER_TYPE_SWITCH:
                    // Would need a different treatment if the main menu had more switches
                    if (header.id == R.id.wifi_settings) {
                        mWifiEnabler.setSwitch(holder.switch_);
                    }
                    // No break, fall through on purpose to update common fields

                    //$FALL-THROUGH$
                default:
                    if (header.extras != null
                            && header.extras.containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
                        String accType = header.extras.getString(
                                ManageAccountsSettings.KEY_ACCOUNT_TYPE);
                        ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
                        lp.width = getContext().getResources().getDimensionPixelSize(
                                R.dimen.header_icon_width);
                        lp.height = lp.width;
                        
                        holder.icon.setLayoutParams(lp);
                        Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
                        holder.icon.setImageDrawable(icon);
                    } else {
                        holder.icon.setImageResource(header.iconRes);
                    
                    }
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    holder.summary.setMaxLines(5);
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                    break;
            }

            Log.d(LOG_TAG, "getView: position="+position +", view.isActivated()" + view.isActivated());
            
            if (mSelected==position) {
            	holder.title.setTextColor(Color.rgb(0x00, 0xff, 0xff));
            	holder.icon.setImageLevel(0);
            	view.setBackgroundColor(Color.rgb(0x00, 0x0B, 0x18));
            }else {
            	holder.title.setTextColor(Color.rgb(0xff, 0xff, 0xff));
            	holder.icon.setImageLevel(1);
            	view.setBackgroundColor(Color.TRANSPARENT);
            }
            
            return view;
        }

        public void resume() {
            mWifiEnabler.resume();
        }

        public void pause() {
            mWifiEnabler.pause();
        }
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        boolean revert = false;
//        if (header.id == R.id.account_add) {
//            revert = true;
//        }

        super.onHeaderClick(header, position);

        Log.d(LOG_TAG, "onHeaderClick: position="+ position);
        if (revert && mLastHeader != null) {
            highlightHeader((int) mLastHeader.id);
        } else {
            mLastHeader = header;
        }
        ((HeaderAdapter)getListView().getAdapter()).setSelected(position);
        ((HeaderAdapter)getListView().getAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        // Override the fragment title for Wallpaper settings
        int titleRes = pref.getTitleRes();
//        if (pref.getFragment().equals(WallpaperTypeSettings.class.getName())) {
//            titleRes = R.string.wallpaper_settings_fragment_title;
//        } else if (pref.getFragment().equals(OwnerInfoSettings.class.getName())
//                && UserHandle.myUserId() != UserHandle.USER_OWNER) {
//            titleRes = R.string.user_info_settings_title;
//        }
        startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, pref.getTitle(),null, 0);
        return true;
    }

    public boolean shouldUpRecreateTask(Intent targetIntent) {
        return super.shouldUpRecreateTask(new Intent(this, Settings.class));
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, getHeaders(), mAuthenticatorHelper));
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        // TODO: watch for package upgrades to invalidate cache; see 7206643
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, accounts);
        invalidateHeaders();
    }

	@Override
	public boolean isMultiPane() {
		return true;
	}
    @Override
	public boolean onIsMultiPane() {
    	return true;
	}


	/*
     * Settings subclasses for launching independently.
     */
    public static class BluetoothSettingsActivity extends Settings { /* empty */ }
    public static class WirelessSettingsActivity extends Settings { /* empty */ }
    public static class TetherSettingsActivity extends Settings { /* empty */ }
    public static class VpnSettingsActivity extends Settings { /* empty */ }
    public static class DateTimeSettingsActivity extends Settings { /* empty */ }
    public static class StorageSettingsActivity extends Settings { /* empty */ }
    public static class WifiSettingsActivity extends Settings { /* empty */ }
    public static class WifiP2pSettingsActivity extends Settings { /* empty */ }
    public static class InputMethodAndLanguageSettingsActivity extends Settings { /* empty */ }
    public static class KeyboardLayoutPickerActivity extends Settings { /* empty */ }
    public static class InputMethodAndSubtypeEnablerActivity extends Settings { /* empty */ }
    public static class SpellCheckersSettingsActivity extends Settings { /* empty */ }
    public static class LocalePickerActivity extends Settings { /* empty */ }
    public static class UserDictionarySettingsActivity extends Settings { /* empty */ }
    public static class SoundSettingsActivity extends Settings { /* empty */ }
    public static class DisplaySettingsActivity extends Settings { /* empty */ }
    public static class DeviceInfoSettingsActivity extends Settings { /* empty */ }
    public static class ApplicationSettingsActivity extends Settings { /* empty */ }
    public static class ManageApplicationsActivity extends Settings { /* empty */ }
    public static class StorageUseActivity extends Settings { /* empty */ }
    public static class DevelopmentSettingsActivity extends Settings { /* empty */ }
    public static class AccessibilitySettingsActivity extends Settings { /* empty */ }
    public static class SecuritySettingsActivity extends Settings { /* empty */ }
    public static class LocationSettingsActivity extends Settings { /* empty */ }
    public static class PrivacySettingsActivity extends Settings { /* empty */ }
    public static class RunningServicesActivity extends Settings { /* empty */ }
    public static class ManageAccountsSettingsActivity extends Settings { /* empty */ }
    public static class PowerUsageSummaryActivity extends Settings { /* empty */ }
    public static class AccountSyncSettingsActivity extends Settings { /* empty */ }
    public static class AccountSyncSettingsInAddAccountActivity extends Settings { /* empty */ }
    public static class CryptKeeperSettingsActivity extends Settings { /* empty */ }
    public static class DeviceAdminSettingsActivity extends Settings { /* empty */ }
    public static class DataUsageSummaryActivity extends Settings { /* empty */ }
    public static class AdvancedWifiSettingsActivity extends Settings { /* empty */ }
    public static class TextToSpeechSettingsActivity extends Settings { /* empty */ }
    public static class AndroidBeamSettingsActivity extends Settings { /* empty */ }
    public static class WifiDisplaySettingsActivity extends Settings { /* empty */ }
    public static class DreamSettingsActivity extends Settings { /* empty */ }
    public static class WallpaperSettingsActivity extends Settings { /* empty */ }
    public static class WallpaperShowActivity extends Settings { /* empty */ }
}
