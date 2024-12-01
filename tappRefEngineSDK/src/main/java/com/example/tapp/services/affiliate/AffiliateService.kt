package com.example.tapp.services.affiliate

interface AffiliateService {
    fun initialize():Boolean
    fun handleCallback(
        deepLink: String,
    )
    fun handleEvent(eventId:String)

}
