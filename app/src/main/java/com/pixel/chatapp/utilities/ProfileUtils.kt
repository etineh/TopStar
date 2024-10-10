package com.pixel.chatapp.utilities

import com.pixel.chatapp.constants.K
import com.pixel.chatapp.view_controller.MainActivity

class ProfileUtils {

    companion object {
        @JvmStatic
        fun getMyDisplayOrUsername(): String? {

            return MainActivity.myProfileShareRef.getString(
                K.PROFILE_DISNAME,
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