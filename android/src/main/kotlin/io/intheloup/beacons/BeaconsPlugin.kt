//  Copyright (c) 2018 Loup Inc.
//  Licensed under Apache License v2.0

package io.intheloup.beacons

import android.app.*
import android.content.Context
import android.os.Build
import android.os.Bundle
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.intheloup.beacons.channel.Channels
import io.intheloup.beacons.data.BackgroundMonitoringEvent
import io.intheloup.beacons.logic.BeaconsClient
import io.intheloup.beacons.logic.PermissionClient
import android.util.Log
import androidx.core.app.NotificationCompat
import org.altbeacon.beacon.BeaconManager

class BeaconsPlugin(val registrar: Registrar) {

    private val permissionClient = PermissionClient()
    private val beaconClient = BeaconsClient(permissionClient)
    private val channels = Channels(permissionClient, beaconClient)

    init {

        registrar.addRequestPermissionsResultListener(permissionClient.listener)

        beaconClient.bind(registrar.context())
        permissionClient.bind(registrar.activeContext()) //the current Activity, if not null, otherwise the Application.

        Log.d("beacons", "register lifecycle callbacks $this, ${beaconClient}")

        if (registrar.activity() != null) {
            registrar.activity().application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    Log.d("beacons", "bind $beaconClient to $activity")
                    beaconClient.bind(activity)
                    permissionClient.bind(activity)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    Log.d("beacons", "unbind $beaconClient to $activity")
                    beaconClient.unbind()
                    permissionClient.unbind()
                }

                override fun onActivityResumed(activity: Activity?) {
                    Log.d("beacons", "notify resuming to $beaconClient ($activity)")
                    beaconClient.resume()
                }

                override fun onActivityPaused(activity: Activity?) {
                    Log.d("beacons", "notify pausing to $beaconClient ($activity)")
                    beaconClient.pause()
                }

                override fun onActivityStarted(activity: Activity?) {

                }

                override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

                }

                override fun onActivityStopped(activity: Activity?) {

                }
            })
        }

        channels.register(this)
    }


    companion object {

        fun init(application: Application, notificationTitle: String, notificationIcon: Int, notificationChannelId: String, notificationChannelName: String, pendingIntent: PendingIntent, callback: BackgroundMonitoringCallback) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(notificationChannelId, notificationChannelName, NotificationManager.IMPORTANCE_LOW)
                (application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            }
            val builder = NotificationCompat.Builder(application, notificationChannelId)
            builder.setSmallIcon(notificationIcon)
            builder.setContentTitle(notificationTitle)
            builder.setContentIntent(pendingIntent)

            BeaconsClient.init(application, callback, builder.build())
        }

        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            val plugin = BeaconsPlugin(registrar)
        }
    }

    object Intents {
        const val PermissionRequestId = 92749
    }

    interface BackgroundMonitoringCallback {

        /**
         * Callback on background monitoring events
         *
         * @return true if background mode will end with this event, for instance if an activity has been started.
         * Otherwise return false to continue receiving background events on the current callback
         */
        fun onBackgroundMonitoringEvent(event: BackgroundMonitoringEvent): Boolean
    }
}
