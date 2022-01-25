package com.hxty.schoolnet.utils.sphelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 说明：sp工具类
 */
public class ConfigHelper {
    private final static String TAG = ConfigHelper.class.getName();
    private static ConfigHelper configHelper;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mSettingsEditor;
    private boolean isTestMode;

    private ConfigHelper(Context context) {
        if (context != null) {
            mSettings = PreferenceManager.getDefaultSharedPreferences(context);
            mSettingsEditor = mSettings.edit();
        } else {
            Log.error(TAG, "context is null");
        }
    }

    public static synchronized ConfigHelper getDefaultConfigHelper(Context context) {
        if (configHelper == null) {
            configHelper = new ConfigHelper(context);
        }
        return configHelper;
    }

    public boolean isTestMode() {
        return isTestMode;
    }

    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    /**
     * #####################configuration Helper#####################
     * 配置保存和获取方法
     */
    private boolean putString(final String entry, String value, boolean commit) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return false;
        }
        mSettingsEditor.putString(entry.toString(), value);
        if (commit) {
            return mSettingsEditor.commit();
        }
        return true;
    }

    public boolean putString(final String entry, String value) {
        return putString(entry, value, true);
    }

    private boolean putInt(final String entry, int value, boolean commit) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return false;
        }
        mSettingsEditor.putInt(entry.toString(), value);
        if (commit) {
            return mSettingsEditor.commit();
        }
        return true;
    }

    public boolean putInt(final String entry, int value) {
        return putInt(entry, value, true);
    }

    private boolean putFloat(final String entry, float value, boolean commit) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return false;
        }
        mSettingsEditor.putFloat(entry.toString(), value);
        if (commit) {
            return mSettingsEditor.commit();
        }
        return true;
    }

    public boolean putFloat(final String entry, float value) {
        return putFloat(entry, value, true);
    }

    private boolean putBoolean(final String entry, boolean value, boolean commit) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return false;
        }
        mSettingsEditor.putBoolean(entry.toString(), value);
        if (commit) {
            return mSettingsEditor.commit();
        }
        return true;
    }

    public boolean putBoolean(final String entry, boolean value) {
        return putBoolean(entry, value, true);
    }

    public String getString(final String entry, String defaultValue) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return defaultValue;
        }
        try {
            return mSettings.getString(entry.toString(), defaultValue);
        } catch (Exception e) {
            Log.exception(TAG, e);
            return defaultValue;
        }
    }

    public int getInt(final String entry, int defaultValue) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return defaultValue;
        }
        try {
            return mSettings.getInt(entry.toString(), defaultValue);
        } catch (Exception e) {
            Log.exception(TAG, e);
            return defaultValue;
        }
    }

    public float getFloat(final String entry, float defaultValue) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return defaultValue;
        }
        try {
            return mSettings.getFloat(entry.toString(), defaultValue);
        } catch (Exception e) {
            Log.exception(TAG, e);
            return defaultValue;
        }
    }

    public boolean getBoolean(final String entry, boolean defaultValue) {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return defaultValue;
        }
        try {
            return mSettings.getBoolean(entry.toString(), defaultValue);
        } catch (Exception e) {
            Log.exception(TAG, e);
            return defaultValue;
        }
    }

    private boolean commit() {
        if (mSettingsEditor == null) {
            Log.error(TAG, "Settings are null");
            return false;
        }
        return mSettingsEditor.commit();
    }

    public void saveInfo(String key, List<HashMap<String, String>> datas) {
        JSONArray mJsonArray = new JSONArray();
        for (int i = 0; i < datas.size(); i++) {
            Map<String, String> itemMap = datas.get(i);
            Iterator<Map.Entry<String, String>> iterator = itemMap.entrySet().iterator();

            JSONObject object = new JSONObject();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                try {
                    object.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {

                }
            }
            mJsonArray.put(object);
        }
        putString("finals", mJsonArray.toString());

//        SharedPreferences sp = context.getSharedPreferences("finals", Context.MODE_PRIVATE);
//        Editor editor = sp.edit();
//        editor.putString(key, mJsonArray.toString());
//        editor.commit();
    }

    public List<HashMap<String, String>> getInfo(Context context, String key) {
        List<HashMap<String, String>> datas = new ArrayList<>();
//        SharedPreferences sp = context.getSharedPreferences("finals", Context.MODE_PRIVATE);
//        String result = sp.getString(key, "");
        String result = getString("finals", "");
        try {
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                JSONObject itemObject = array.getJSONObject(i);
                Map<String, String> itemMap = new HashMap<>();
                JSONArray names = itemObject.names();
                if (names != null) {
                    for (int j = 0; j < names.length(); j++) {
                        String name = names.getString(j);
                        String value = itemObject.getString(name);
                        itemMap.put(name, value);
                    }
                }
                datas.add((HashMap<String, String>) itemMap);
            }
        } catch (JSONException e) {

        }

        return datas;
    }

}
