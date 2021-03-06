package com.vijay.jsonwizard.comparisons;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexComparison extends Comparison {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public boolean compare(String a, String type, String b) {
        if (a != null && b != null) {
            try {
                Pattern pattern = Pattern.compile(b);
                Matcher matcher = pattern.matcher(a);
                return matcher.matches();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            return false;
        }

        return false;
    }

    @Override
    public String getFunctionName() {
        return "regex";
    }
}
