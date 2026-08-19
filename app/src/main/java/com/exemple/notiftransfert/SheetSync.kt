package com.exemple.notiftransfert

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Envoie chaque transaction capturée vers un Google Apps Script (Web App)
 * qui écrit la ligne dans un Google Sheet et déduit automatiquement
 * la dette correspondante si l'expéditeur est trouvé.
 *
 * Voir README.md pour la mise en place complète du Google Sheet + Apps Script.
 */
object SheetSync {

    // Remplacez cette URL par celle de votre déploiement Apps Script (voir README)
    const val WEBHOOK_URL = "https://script.google.com/macros/s/AKfycbzOD6YLDrwXP3eifeDB-gaBzqQiI0JyBaiKF6pPAaK8zjziNGCa8JoMwwdwX40EujEs/exec"

    fun envoyer(transaction: TransactionListener.Transaction) {
        if (WEBHOOK_URL.contains("AKfycbzOD6YLDrwXP3eifeDB-gaBzqQiI0JyBaiKF6pPAaK8zjziNGCa8JoMwwdwX40EujEs")) {
            Log.w("SheetSync", "WEBHOOK_URL non configurée, envoi ignoré.")
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
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("SheetSync", "Échec de l'envoi vers Google Sheets", e)
            }
        }.start()
    }
}
