package com.example.gt168_afpt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import java.io.IOException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        object : NanoHTTPD(5000) {
            override fun serve(session: IHTTPSession): Response {
                val files = HashMap<String, String>()
                val method = session.method
                if (method == Method.POST) {
                    try {
                        session.parseBody(files)
                    } catch (ioe: IOException) {
                        return newFixedLengthResponse(
                            Response.Status.INTERNAL_ERROR,
                            MIME_PLAINTEXT,
                            "SERVER INTERNAL ERROR: IOException: $ioe"
                        )
                    } catch (re: ResponseException) {
                        return newFixedLengthResponse(re.status, MIME_PLAINTEXT, re.message)
                    }

                }
                // get the POST body
//                val postBody = session.queryParameterString
                // or you can access the POST request's parameters
//                val postParameter = session.parms["parameter"]
                return newFixedLengthResponse("OK") // Or postParameter.
            }
        }.start()
    }
}
