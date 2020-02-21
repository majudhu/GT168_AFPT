package com.example.gt168_afpt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import com.google.gson.reflect.TypeToken
import kotlin.Exception


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        object : NanoHTTPD(5000) {
            override fun serve(session: IHTTPSession): Response {
                val files = HashMap<String, String>()
                session.parseBody(files)
                val requestJson: Map<String, Any> =
                    try {
                        Gson().fromJson(
                            files["postData"],
                            object : TypeToken<Map<String, Any>>() {}.type
                        )
                    } catch (e: Exception) {
                        mapOf(
                            "error" to 0,
                            "message" to "invalid request"
                        )
                    }
                return newFixedLengthResponse(Gson().toJson(requestJson))
            }
        }.start()
    }
}
