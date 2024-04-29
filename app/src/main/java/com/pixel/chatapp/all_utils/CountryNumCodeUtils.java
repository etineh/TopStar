package com.pixel.chatapp.all_utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class CountryNumCodeUtils {

    public static void getCountryCode(final CountryCodeCallback callback) { // Nigeria (NG) +234

        new Thread(() -> {

            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Set<String> countryCodes = phoneNumberUtil.getSupportedRegions();

            TreeMap<String, String> countryMap = new TreeMap<>();   // arrange alphabetically

            for (String countryCode : countryCodes) {
                Locale locale = new Locale("", countryCode);
                String countryName = locale.getDisplayCountry();
                int countryCodeValue = PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode);
                String phoneNumberPrefix = "+" + countryCodeValue;
                String countryDisplayName = countryName + " (" + countryCode + ") " + phoneNumberPrefix;
                countryMap.put(countryDisplayName, phoneNumberPrefix); // Use full country name as key for sorting
            }

            List<String> countryList = new ArrayList<>(countryMap.keySet());

            // Pass the result to the callback
            callback.onCountryCodeLoaded(countryList);

        }).start();

    }


    public interface CountryCodeCallback {
        void onCountryCodeLoaded(List<String> countryCodes);
    }

    public static String getUserCountry(Context context) {  // (NG) +234
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String countryCode = telephonyManager.getNetworkCountryIso().toUpperCase();
            String countryDialingCode = getCountryDialingCode(countryCode, phoneNumberUtil);
            return "(" + countryCode + ") " + countryDialingCode;
        }
        return null;
    }

    private static String getCountryDialingCode(String countryCode, PhoneNumberUtil phoneNumberUtil) {  // +234
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.getExampleNumberForType(countryCode, PhoneNumberUtil.PhoneNumberType.MOBILE);
            return "+" + phoneNumber.getCountryCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getUserCountry__(Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(0.0, 0.0, 1);
            if (!addresses.isEmpty()) {
                String countryCode = addresses.get(0).getCountryCode();
                return countryCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
