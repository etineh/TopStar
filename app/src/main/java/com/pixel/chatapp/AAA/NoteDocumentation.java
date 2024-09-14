package com.pixel.chatapp.AAA;

public class NoteDocumentation {


    //  ============    Documentation

    //  1.  PIN     // I use same chat card for No. 1, 2, 3
    //  when I pin a chat, I send a message to indicate I pin a chat,.

    //  2.  New chat count number
    //  When I receive a new chat, I first check if I have new chat from user I haven't read, then add to it.

    //  3. New date
    //  When I receive a chat, I compare the date to the last chat date (via outside room db for speed) if it's a new date

    //  4. Username
    //  on saving, I don't add @, but on retrieving, I add @


    // type 0 - text-chat, type 1 is voice_note, type 2 is photo, type 3 is document,
    // type 4 is audio (mp3), type 5 is video, 6 is call, 7 is game, 8 is pin, 10 is empty card
    // type 8 - // use for pin, number of new chat and new chat date


    // 1 - typing, 2 - editing, 3 - sending photo, 4 - sending file, 5 - sending voice note


    //  ============    Game signal

    // 1.   hosting user send game alert.   setHomeLastViews() -> sendToDataBase() -> server
    //      server do the signal (first check if user grant permission),
    // 2.   each user in recyclerView start with "signalling"
    // 3.   if delivers to receiving user, "signalling" changes to "Awaiting"
    // 4.   if user rejects, change "Awaiting" to "rejected"
    // 5.   if user accepted, change "Awaiting" to "joined"


}

















