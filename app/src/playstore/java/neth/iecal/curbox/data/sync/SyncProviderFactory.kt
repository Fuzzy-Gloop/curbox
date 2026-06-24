package neth.iecal.curbox.data.sync

import android.content.Context

/**
 * Play Store flavor binding. Construction stays cheap so it is safe to call from
 * Application.onCreate. The provider schedules its worker later, off the main
 * thread, inside start().
 */
fun createSyncProvider(context: Context): SyncProvider = PlaystoreSyncProvider(context)
