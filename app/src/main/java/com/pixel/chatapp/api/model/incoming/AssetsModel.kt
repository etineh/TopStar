package com.pixel.chatapp.api.model.incoming

data class AssetsModel(
    val totalAsset: String = "",
    val localAsset: String = "",
    val usdtAsset: String = "",
    val gameAsset: String = "",
    val lockAsset: String = "",
    val airdropAsset: String = "",
    val p2pMerchant: String = ""
)
