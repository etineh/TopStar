package com.pixel.chatapp.interface_listeners

import com.pixel.chatapp.api.model.incoming.AssetsModel

interface WalletCallBack {

    fun onSuccess(assetsModel: AssetsModel)

    fun onFailure()

}