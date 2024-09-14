package com.pixel.chatapp.interface_listeners;


public interface ChatListener {

    void sendMessage(final String text, final String emojiOnly, final int type, final String vnPath_,
                     final String durationOrSizeVN, final String otherId, final boolean addReply);

    void openAddPlayerLayout(String playerName);
}
