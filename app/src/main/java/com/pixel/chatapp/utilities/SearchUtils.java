package com.pixel.chatapp.utilities;

import com.pixel.chatapp.dataModel.ContactModel;

import java.util.ArrayList;
import java.util.List;

public class SearchUtils {
    private final List<ContactModel> contacts;
    private String searchQuery;

    public SearchUtils(List<ContactModel> contacts) {
        this.contacts = contacts;
    }
    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
    }

    public List<ContactModel> searchContacts() {
        // Make a copy of the contacts list
        List<ContactModel> contactsCopy = new ArrayList<>(contacts);
        List<ContactModel> filteredContacts = new ArrayList<>();
        for (ContactModel contact : contactsCopy) {
            if (contact != null && contact.getContactName() != null) {
                if (contact.getContactName().toLowerCase().contains(searchQuery)) {
                    filteredContacts.add(contact);
                }
            }
        }

        filteredContacts.sort((c1, c2) -> {
            String name1 = c1.getContactName().toLowerCase();
            String name2 = c2.getContactName().toLowerCase();
            int distance1 = calculateLevenshteinDistance(name1, searchQuery);
            int distance2 = calculateLevenshteinDistance(name2, searchQuery);

            // Compare the Levenshtein distances
            return Integer.compare(distance1, distance2); // Simplified comparison
        });


        return filteredContacts;
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
}

