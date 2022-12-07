package com.example.forwardsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val bundle = intent?.extras
            val messages = smsMessageParse(bundle!!)
            messages?.forEach {
                val message = it?.messageBody ?: ""
                Log.d("JSTEST", "message: $message")
                sendWebexMessage(message = message)
            }
        }
    }

    private fun smsMessageParse(bundle: Bundle): Array<SmsMessage?>? {
        val objs = bundle["pdus"] as Array<Any>?
        val messages: Array<SmsMessage?> = arrayOfNulls(objs!!.size)
        for (i in objs!!.indices) {
            messages[i] = SmsMessage.createFromPdu(objs[i] as ByteArray)
        }
        return messages
    }

    private fun sendWebexMessage(message: String) {
        val webexRoomID = ""
        val webexToken = ""

        val auth = "Bearer $webexToken"
        val body = hashMapOf(
            "roomId" to webexRoomID,
            "markdown" to message,
            )

        RetrofitService.service.sendMessage(
            auth = auth,
            params = body
        ).enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>?, t: Throwable?) {
                Log.e("JSTEST", t.toString())
            }

            override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
                Log.d("JSTEST", response?.body().toString())
            }
        })
    }
}

class RetrofitService {
    companion object {
        private const val baseUrl = "https://webexapis.com"
        private val retrofit: Retrofit = Retrofit.Builder().baseUrl(this.baseUrl)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val service: WebexService = retrofit.create(WebexService::class.java)
    }
}

interface WebexService {
    @Headers("accept: application/json", "content-type: application/json")
    @POST("/v1/messages")
    fun sendMessage(
        @Header("Authorization") auth: String,
        @Body params: HashMap<String, String>
    ): Call<Any>
}