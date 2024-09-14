package com.pixel.chatapp.utils

import android.content.Context
import com.pixel.chatapp.api.Dao_interface.WalletListener
import com.pixel.chatapp.api.model.incoming.AssetsModel
import com.pixel.chatapp.constants.AllConstants
import com.pixel.chatapp.interface_listeners.WalletCallBack
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalletUtils {

    companion object {
        @JvmStatic
        fun balance(context: Context, walletCallBack: WalletCallBack) {
            val walletListener = AllConstants.retrofit.create(WalletListener::class.java)

            IdTokenUtil.generateToken( { token: String? ->
                walletListener.assets(token).enqueue(object : Callback<AssetsModel?> {
                    override fun onResponse(p0: Call<AssetsModel?>, p1: Response<AssetsModel?>) {
                        if (p1.isSuccessful) {
                            val assetsModel = p1.body()
                            walletCallBack.onSuccess(assetsModel!!)
                        } else{
                            walletCallBack.onFailure()
                        }
                    }

                    override fun onFailure(p0: Call<AssetsModel?>, p1: Throwable) {
                        walletCallBack.onFailure()
                    }
                })
            }, context)
        }
    }
}
