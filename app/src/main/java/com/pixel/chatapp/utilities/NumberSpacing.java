package com.pixel.chatapp.utilities;

public class NumberSpacing {

    public static String formatPhoneNumber(String phoneNumber, int spaceInterval, int numberOfSpaces) {
        StringBuilder formattedNumber = new StringBuilder();
        int count = 0;

        for (char c : phoneNumber.toCharArray()) {
            if (c != '+') {
                formattedNumber.append(c);
                count++;

                if (count % spaceInterval == 0 && numberOfSpaces > 0) {
                    formattedNumber.append(" ");
                    numberOfSpaces--;
                }
            } else {
                formattedNumber.append(c);
            }
        }

        return formattedNumber.toString().trim();
    }

    public static String formatNumberWithCommas(String number) {
        try {
            double parsedNumber = Double.parseDouble(number);
            return String.format("%,.2f", parsedNumber); // Format as double with commas and 2 decimal places
        } catch (NumberFormatException e) {
            return "Invalid number ";
        }
    }


}
