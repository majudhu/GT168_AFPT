package com.example.gt168_afpt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        object : NanoHTTPD(5000) {
            override fun serve(session: IHTTPSession): Response {
                return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    Gson().toJson(mapOf("hello" to "world"))
                )
            }
        }.start()
    }
}
