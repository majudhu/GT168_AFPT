package com.example.gt168_afpt

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.content.BroadcastReceiver
import android.content.Context
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD

const val HTTP_SERVER_PORT = 5000
const val NOTIFICATION_CHANNEL_ID = "HTTP Server Service"
const val MIME_JSON = "application/json"
private val GSON = Gson()

class HttpService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Http Server")
            .setContentText("starting server on $HTTP_SERVER_PORT")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        HttpServer().start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}

class StartOnBoot : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent) {
        val intent = Intent(context, HttpService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}

class HttpServer : NanoHTTPD(HTTP_SERVER_PORT) {
    override fun serve(session: IHTTPSession): Response =
        try {
            val files = HashMap<String, String>()
            session.parseBody(files)
            val postData = files["postData"] ?: throw Exception()
            val route = ROUTES[session.uri] ?: throw Exception()
            val response = route(postData) ?: throw Exception()
            newFixedLengthResponse(Response.Status.OK, MIME_JSON, response)
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                Response.Status.BAD_REQUEST.description
            )
        }
}

val ROUTES: Map<String, (String) -> String?> = mapOf(
    "/" to { _ -> Gson().toJson("hello") },

    "/create" to fun(postData): String? {
        data class RequestModel(val name: String?, val age: Int?)

        val newData = GSON.fromJson(postData, RequestModel::class.java)
        if (newData.name == null) return null
        return "created ${newData.name}"
    },

    "/update" to fun(postData): String? {
        data class RequestModel(val name: String?, val age: Int?)

        val newData = GSON.fromJson(postData, RequestModel::class.java)
        if (newData.age == null) return null
        return "updated ${newData.name}"
    },

    "/delete" to fun(postData): String? {
        data class RequestModel(val name: String?, val age: Int?)

        val newData = GSON.fromJson(postData, RequestModel::class.java)
        if (newData.name == null) return null
        return "deleted ${newData.name}"
    }

)