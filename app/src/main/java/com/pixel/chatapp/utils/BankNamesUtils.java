package com.pixel.chatapp.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class BankNamesUtils {


    public static List<String> banksList(Context context, String code){

        String countryCode = code != null ? code : CountryNumCodeUtils.getUserCountry(context);

        if(countryCode.equals("(NG) +234")){
//            System.out.println("what is countrycode " + countryCode);
            return nigeriaBanks();
        }

        return null;
    }


    // Nigeria
    private static List<String> nigeriaBanks(){

        List<String> addBankList = new ArrayList<>();

        addBankList.add("PalmPay");
        addBankList.add("Opay");
        addBankList.add("MoniePoint");
        addBankList.add("Kuda");
        addBankList.add("Access Bank");
        addBankList.add("FCMB");
        addBankList.add("First Bank");
        addBankList.add("Fidelity Bank");
        addBankList.add("Guarantee Trust Bank");
        addBankList.add("Providus Bank");
        addBankList.add("Stanbic IBTC Bank");
        addBankList.add("Sterling Bank");
        addBankList.add("Union Bank");
        addBankList.add("Unity Bank");
        addBankList.add("United Bank of Africa");
        addBankList.add("Zenith Bank");
//        addBankList.add("PalmPay");


        return addBankList;
    }

}
