package com.pixel.chatapp.AAA;

public class BugRead {


    //    =====================================     --- solved

    //  1. activity reopen/reload when I click on the notification chat, instead of it to bring the previous activity up.
    //  2. the data only print when I am on the app, it's not print when I am off the app, when I click on the chat notification. -- solved
    //  3. when same user send chat, it opens new one instead of continue the old previous

    //    =====================================

    //  work on uploading recording video
    //  get the total size of the video and duration

    //    =====================================

    //  country code detector
    //  get the country code of the user contact number and add it


    //    =====================================     --- solved

    //  notification username not showing when i am not on the app
    // step.    1. save the contact name of other user to my userList db    -- done
    //          2. get my name from other userList  -- done
    //          3. send it as senderName, and replace with my displayname if null or doesn't exist  -- done

    //    =====================================     --- solved

    //  scrolling to last position state doesn't respect new chat count.
    //  solution -- save the total last chat list, to help guide the last position.
    //  maybe minus the total new chat number from the scroll position.


    //    =====================================     // solved

    //  receiving new chats then to add or change to sent "today" instead of showing the chats


    //    =====================================     --- solved

    //  SOLVED.  when chat can't find the new chat card, and no more new chat, the old count is still there  --- solved
//    solution
//  when I click onBackPress, if total modelist - scroll is less 10, and ignoreNewChatCountReset = false, reset the count.




}
