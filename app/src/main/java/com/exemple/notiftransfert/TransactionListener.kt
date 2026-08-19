package com.exemple.notiftransfert

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class TransactionListener : NotificationListenerService() {

    data class Transaction(
        val type: String,
        val montant: String,
        val devise: String,
        val nomExpediteur: String,
        val numeroExpediteur: String,
        val date: Long = System.currentTimeMillis()
    )

    companion object {
        // Package officiel de l'app Sedad (Banque Mauritanienne de l'Investissement)
        const val PACKAGE_SEDAD = "mr.digi.sedad"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != PACKAGE_SEDAD) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        Log.d("TransactionListener", "Package: ${sbn.packageName}")
        Log.d("TransactionListener", "Titre: $title | Texte: $bigText")

        extraireTransaction(title, bigText)
    }

    private fun extraireTransaction(titre: String, texte: String): Transaction? {
        // Format observé: "Vous avez reçu 5.0 MRU de Abderrahmane ( 46536376 )"
        val regex = Regex(
            "re[cç]u\\s*([\\d.,]+)\\s*(\\w+)\\s*de\\s*([A-Za-zÀ-ÿ\\s]+?)\\s*\\(\\s*(\\d+)\\s*\\)",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(texte) ?: run {
            Log.d("TransactionListener", "Format non reconnu, ignoré: $texte")
            return null
        }

        val transaction = Transaction(
            type = titre.trim(),
            montant = match.groupValues[1],
            devise = match.groupValues[2],
            nomExpediteur = match.groupValues[3].trim(),
            numeroExpediteur = match.groupValues[4]
        )

        Log.d("TransactionListener", "Transaction capturée: $transaction")
        sauvegarderTransaction(transaction)
        return transaction
    }

    private fun sauvegarderTransaction(t: Transaction) {
        val prefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val id = t.date.toString()
        prefs.edit()
            .putString("$id.type", t.type)
            .putString("$id.montant", t.montant)
            .putString("$id.devise", t.devise)
            .putString("$id.expediteur", t.nomExpediteur)
            .putString("$id.numero", t.numeroExpediteur)
            .apply()
    }
}
