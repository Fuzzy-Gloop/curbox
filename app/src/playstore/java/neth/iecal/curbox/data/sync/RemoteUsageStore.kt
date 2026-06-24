package neth.iecal.curbox.data.sync

import android.content.Context
import java.io.File

/**
 * Holds other devices' usage that we pulled down, kept separate from local Room
 * so this device's own counters stay authoritative. The UI can union these in
 * to show unified totals across every device. One JSON file per device keeps it
 * simple and never touches the local usage database.
 */
class RemoteUsageStore(context: Context) {
    private val file = File(context.filesDir, "sync_remote_usage.json")
    private val records: HashMap<String, String> = load()
    private var dirty = false

    private fun load(): HashMap<String, String> {
        if (!file.exists()) return HashMap()
        return try {
            val obj = org.json.JSONObject(file.readText())
            HashMap<String, String>().apply { obj.keys().forEach { put(it, obj.getString(it)) } }
        } catch (_: Exception) {
            HashMap()
        }
    }

    fun put(namespace: String, recordKey: String, payloadJson: String) {
        records["$namespace/$recordKey"] = payloadJson
        dirty = true
    }

    fun flush() {
        if (!dirty) return
        val obj = org.json.JSONObject()
        records.forEach { (k, v) -> obj.put(k, v) }
        file.writeText(obj.toString())
        dirty = false
    }

    /** Drops every other device's cached usage. Used on sign out so a signed out
     *  device never keeps showing data that belonged to a different account. */
    fun clear() {
        records.clear()
        dirty = false
        runCatching { if (file.exists()) file.delete() }
    }

    /** Summed website milliseconds by domain for a date, across all other devices. */
    fun websiteTotals(date: String): Map<String, Long> {
        val out = HashMap<String, Long>()
        for ((key, json) in records) {
            if (!key.startsWith("usage_web/")) continue
            val o = org.json.JSONObject(json)
            if (o.optString("date") != date) continue
            val domain = o.optString("domain")
            out[domain] = (out[domain] ?: 0L) + o.optLong("ms")
        }
        return out
    }
}
