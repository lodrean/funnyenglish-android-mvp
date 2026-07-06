plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("gemma_model")
    dynamicDelivery {
        deliveryType.set("install-time") // скачается при установке
    }
}
