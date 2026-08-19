package com.exemple.notiftransfert

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Envoie chaque transaction capturée vers un Google Apps Script (Web App)
 * qui écrit la ligne dans le bon onglet du Google Sheet.
 *
 * Le résultat (succès ou erreur) est aussi sauvegardé localement pour être
 * affiché dans MainActivity, afin de diagnostiquer sans avoir besoin d'adb.
 *
 * Voir README.md pour la mise en place complète du Google Sheet + Apps Script.
 */
object SheetSync {

    // Remplacez cette URL par celle de votre déploiement Apps Script (voir README)
    const val WEBHOOK_URL = "https://script.google.com/macros/s/AKfycbzOD6YLDrwXP3eifeDB-gaBzqQiI0JyBaiKF6pPAaK8zjziNGCa8JoMwwdwX40EujEs/exec"

    fun envoyer(context: Context, transaction: TransactionListener.Transaction) {
        val appContext = context.applicationContext

        if (WEBHOOK_URL.contains("AKfycbzOD6YLDrwXP3eifeDB-gaBzqQiI0JyBaiKF6pPAaK8zjziNGCa8JoMwwdwX40EujEs")) {
            Log.w("SheetSync", "WEBHOOK_URL non configurée, envoi ignoré.")
            sauvegarderStatut(appContext, "❌ URL non configurée dans SheetSync.kt")
            return
        }

        Thread {
            try {
                val json = JSONObject().apply {
                    put("source", transaction.source)
                    put("date", transaction.date)
                    put("type", transaction.type)
                    put("montant", transaction.montant)
                    put("devise", transaction.devise)
                    put("nom", transaction.nomExpediteur)
                    put("numero", transaction.numeroExpediteur)
                }

                val url = URL(WEBHOOK_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.doOutput = true
                conn.instanceFollowRedirects = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000

                conn.outputStream.use { os ->
                    os.write(json.toString().toByteArray(Charsets.UTF_8))
                }

                val code = conn.responseCode
                val response = if (code in 200..299) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    conn.errorStream?.bufferedReader()?.readText() ?: "Erreur HTTP $code"
                }

                Log.d("SheetSync", "Réponse ($code): $response")

                if (code in 200..299) {
                    sauvegarderStatut(appContext, "✅ Envoyé (${transaction.source}) - HTTP $code - $response")
                } else {
                    sauvegarderStatut(appContext, "❌ Erreur HTTP $code - $response")
                }

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("SheetSync", "Échec de l'envoi vers Google Sheets", e)
                sauvegarderStatut(appContext, "❌ Exception: ${e.javaClass.simpleName} - ${e.message}")
            }
        }.start()
    }

    private fun sauvegarderStatut(context: Context, statut: String) {
        val prefs = context.getSharedPreferences("sheet_sync_status", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("dernier_statut", statut)
            .putLong("dernier_essai", System.currentTimeMillis())
            .apply()
    }

    fun lireDernierStatut(context: Context): String {
        val prefs = context.getSharedPreferences("sheet_sync_status", Context.MODE_PRIVATE)
        val statut = prefs.getString("dernier_statut", null) ?: return "Aucun envoi tenté pour le moment."
        val date = prefs.getLong("dernier_essai", 0L)
        val dateStr = if (date > 0) {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.util.Date(date))
        } else "?"
        return "[$dateStr] $statut"
    }
}
