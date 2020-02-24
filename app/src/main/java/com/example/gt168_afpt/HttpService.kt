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
import com.google.gson.JsonSyntaxException
import fi.iki.elonen.NanoHTTPD
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

const val HTTP_SERVER_PORT = 5000
const val NOTIFICATION_CHANNEL_ID = "HTTP Server Service"

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
    private val badRequestResponse: String = Gson().toJson(
        mapOf(
            "error" to Response.Status.BAD_REQUEST.requestStatus,
            "message" to Response.Status.BAD_REQUEST.description
        )
    )

    override fun serve(session: IHTTPSession): Response =
        try {
            val files = HashMap<String, String>()
            session.parseBody(files)
            val postData = files["postData"]
            val request = Gson().fromJson(postData, MyRoute.MyRequest::class.java)
            val xx: KClass<*> = MyRoute.MyRequest::class;

            val response = MyRoute.run(request)
            newFixedLengthResponse(Gson().toJson(response))
        } catch (e: JsonSyntaxException) {
            newFixedLengthResponse(badRequestResponse)
        } catch (e: ResponseException) {
            newFixedLengthResponse(badRequestResponse)
        }

}



abstract class RouteModel {
    abstract val RequestModel: KClass<*>
    abstract val ResponseModel: KClass<*>
    abstract val Uri: String

    abstract fun run(req:Any) : Any
}

object MyRoute : RouteModel() {
    data class MyRequest(val name: String, val age: Int)
    data class MyResponse(val name: String, val age: Int)

    override val RequestModel = MyRequest::class
    override val ResponseModel = MyResponse::class
    override val Uri = "/"
    override fun run(req: Any): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}