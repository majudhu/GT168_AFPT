package com.example.gt168_afpt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import fi.iki.elonen.NanoHTTPD
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        object : NanoHTTPD(5000) {
            override fun serve(session: IHTTPSession): Response {
                try {
                    val files = HashMap<String, String>()
                    session.parseBody(files)
                    val json: Map<String, Any> = Gson().fromJson(
                        files["postData"],
                        object : TypeToken<Map<String, Any>>() {}.type
                    )
                    return newFixedLengthResponse(Gson().toJson(json))
                } catch (e: JsonSyntaxException) {
                    return newFixedLengthResponse(
                        Gson().toJson(
                            mapOf(
                                "error" to Response.Status.BAD_REQUEST.requestStatus,
                                "message" to Response.Status.BAD_REQUEST.description
                            )
                        )
                    )
                }
            }
        }.start()
    }
}
