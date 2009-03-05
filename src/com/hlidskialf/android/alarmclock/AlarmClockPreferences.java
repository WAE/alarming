package com.hlidskialf.android.alarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceActivity;
import android.net.Uri;
import android.os.Bundle;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.provider.Settings;

public class AlarmClockPreferences extends PreferenceActivity
{
    private AlarmPreference mAlarmPref;
    private RepeatPreference mRepeatPref;
    private SliderPreference mVolumePref;
    private SliderPreference mCrescendoPref;
    private SliderPreference mSnoozePref;
    private SliderPreference mDurationPref;
    private SliderPreference mDelayPref;

    private Alarms.DaysOfWeek mDaysOfWeek;
    private int mVolume;
    private int mCrescendo;
    private int mSnooze;
    private int mDuration;
    private int mDelay;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);


        mAlarmPref = (AlarmPreference) findPreference("default_alarm");
        mRepeatPref = (RepeatPreference) findPreference("default_repeat");
        mVolumePref = (SliderPreference) findPreference("default_volume");
        mCrescendoPref = (SliderPreference) findPreference("default_crescendo");
        mSnoozePref = (SliderPreference) findPreference("default_snooze");
        mDurationPref = (SliderPreference) findPreference("default_duration");
        mDelayPref = (SliderPreference) findPreference("default_delay");


        mSharedPrefs = getSharedPreferences(AlarmClock.PREFERENCES, 0);

        Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(mSharedPrefs.getString("default_alarm", Settings.System.DEFAULT_RINGTONE_URI.toString())) );
        mAlarmPref.setSummary(ringtone.getTitle(this));

        mDaysOfWeek = new Alarms.DaysOfWeek(mSharedPrefs.getInt("default_repeat", 0x1f)); //default to mon-fri
        mRepeatPref.setSummary(mDaysOfWeek.toString(this, true));

        mVolume = updatePreference(mVolumePref, mSharedPrefs.getInt("default_volume", 100), R.string.volume_zero, R.string.volume_unit);
        mCrescendo = updatePreference(mCrescendoPref, mSharedPrefs.getInt("default_crescendo", 0), R.string.crescendo_zero, R.string.crescendo_unit);
        mSnooze = updatePreference(mSnoozePref, mSharedPrefs.getInt("default_snooze", 10), R.string.snooze_disabled, R.string.snooze_unit);
        mDuration = updatePreference(mDurationPref, mSharedPrefs.getInt("default_duration", 0), R.string.duration_infinite, R.string.duration_unit);
        mDelay = updatePreference(mDelayPref, mSharedPrefs.getInt("default_delay", 0), R.string.delay_zero, R.string.delay_unit);


        mAlarmPref.setRingtoneChangedListener(new AlarmPreference.IRingtoneChangedListener() {
            public void onRingtoneChanged(Uri ringtoneUri) { 
              Ringtone ringtone = RingtoneManager.getRingtone(AlarmClockPreferences.this, ringtoneUri);
              if (ringtone != null) {
                  mSharedPrefs.edit().putString("default_alarm", ringtoneUri.toString()).commit();
                  mAlarmPref.setSummary(ringtone.getTitle(AlarmClockPreferences.this));
              }
            }
        });
        mRepeatPref.setOnRepeatChangedObserver( new RepeatPreference.OnRepeatChangedObserver() {
            public void onRepeatChanged(Alarms.DaysOfWeek daysOfWeek) {
                if (!mDaysOfWeek.equals(daysOfWeek)) { 
                  mDaysOfWeek.set(daysOfWeek); 
                  mSharedPrefs.edit().putInt("default_repeat", mDaysOfWeek.getCoded()).commit(); 
                }
                mRepeatPref.setSummary(mDaysOfWeek.toString(AlarmClockPreferences.this, true));

            }
            public Alarms.DaysOfWeek getDaysOfWeek() { return mDaysOfWeek; }
        });
        mVolumePref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
            public int getValue() { return mVolume; }
            public void onSliderChanged(int value) { mVolume = updateAndSavePreference(mVolumePref, value, R.string.volume_zero, R.string.volume_unit); }
            public int progressToValue(int progress) { return progress; }
            public int valueToProgress(int value) { return value; }
        });
        mCrescendoPref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
            public int getValue() { return mCrescendo; }
            public void onSliderChanged(int value) { mCrescendo = updateAndSavePreference(mCrescendoPref, value, R.string.crescendo_zero, R.string.crescendo_unit); }
            public int progressToValue(int progress) { return (int)(60.0*(progress / 100.0)); }
            public int valueToProgress(int value) { return (int)(100.0*(value/60.0)); }
        });
        mSnoozePref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
            public int getValue() { return mSnooze; }
            public void onSliderChanged(int value) { mSnooze = updateAndSavePreference(mSnoozePref, value, R.string.snooze_disabled, R.string.snooze_unit); }
            public int progressToValue(int progress) { return (int)(60.0*(progress / 100.0)); }
            public int valueToProgress(int value) { return (int)(100.0*(value/60.0)); }
        });
        mDurationPref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
            public int getValue() { return mDuration; }
            public void onSliderChanged(int value) { mDuration = updateAndSavePreference(mDurationPref, value, R.string.duration_infinite, R.string.duration_unit); }
            public int progressToValue(int progress) { return (int)(60.0*(progress / 100.0)); }
            public int valueToProgress(int value) { return (int)(100.0*(value/60.0)); }
        });
        mDelayPref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
            public int getValue() { return mDelay; }
            public void onSliderChanged(int value) { mDelay = updateAndSavePreference(mDelayPref, value, R.string.delay_zero, R.string.delay_unit); }
            public int progressToValue(int progress) { return (int)(1000.0*(progress / 100.0)); }
            public int valueToProgress(int value) { return (int)(100.0*(value/1000.0)); }
        });
    }

    private int updatePreference(Preference pref, int value, int zero_string, int unit_string) {
        if (value == 0) pref.setSummary(zero_string);
        else pref.setSummary(String.valueOf(value)+" "+getString(unit_string));
        return value;
    }
    private int updateAndSavePreference(Preference pref, int value, int zero_string, int unit_string) {
        updatePreference(pref,value,zero_string,unit_string);

        SharedPreferences.Editor edit = mSharedPrefs.edit();

        if (pref.equals(mVolumePref)) 
          edit.putInt("default_volume",value);
        else if (pref.equals(mCrescendoPref))
          edit.putInt("default_crescendo",value);
        else if (pref.equals(mSnoozePref))
          edit.putInt("default_snooze",value);
        else if (pref.equals(mDurationPref))
          edit.putInt("default_duration",value);
        else if (pref.equals(mDelayPref))
          edit.putInt("default_delay",value);
      
        edit.commit();

        return value;
    }
}