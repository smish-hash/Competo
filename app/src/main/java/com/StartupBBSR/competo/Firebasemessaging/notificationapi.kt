package com.StartupBBSR.competo.Firebasemessaging

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface notificationapi {

    @Headers("Authorization :key=AAAABmOW__8:APA91bFEiWxr4rRQa3M_5n-w-5XDjLnQ9nf2IgAs1r0ppfwgTLZoGgOJmRAF1pt59hHqdMZ74AmAx1lkk0HaCuLwUCsHi_M_BWEZAGwkXyp-57YJk_pGmGWwJKNEU_bnJLl7bv7VDPzy, Content-Type:application/json")
    @POST("fcm/send")
    suspend fun postnotification(
        @Body notification:pushnotification
    ):Response<okhttp3.ResponseBody>
}