package neth.iecal.curbox.data.sync

import android.content.Context

/**
 * F-Droid flavor binding. There is no sync here, so the gateway always returns
 * the no-op provider. No network or Supabase code exists in this build.
 */
fun createSyncProvider(context: Context): SyncProvider = NoopSyncProvider
