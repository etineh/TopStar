package com.pixel.chatapp.utils

import com.pixel.chatapp.constants.AllConstants
import com.pixel.chatapp.home.MainActivity

class ProfileUtils {

    companion object {
        @JvmStatic
        fun getMyDisplayOrUsername(): String? {

            return MainActivity.myProfileShareRef.getString(
                AllConstants.PROFILE_DISNAME,
                "@" + MainActivity.getMyUserName
            )
        }

        @JvmStatic
        fun getOtherDisplayOrUsername(otherId: String, defaultName: String): String? {

            // return the contact name or displayed name of other user
            return MainActivity.contactNameShareRef.getString(otherId, defaultName)

        }

    }

}