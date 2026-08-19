package com.exemple.notiftransfert

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class TransactionListener : NotificationListenerService() {

    data class Transaction(
        val montant: String,
        val devise: String,
        val nomExpediteur: String,
        val numeroExpediteur: String,
        val reference: String,
        val date: Long = System.currentTimeMillis()
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Une fois que vous connaissez le vrai packageName de l'app de transfert,
        // decommentez la ligne suivante et remplacez la valeur pour filtrer.
        // if (sbn.packageName != "com.exemple.appdetransfert") return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        Log.d("TransactionListener", "Package: ${sbn.packageName}")
        Log.d("TransactionListener", "Titre: $title | Texte: $bigText")

        if (title.contains("Transfert d'argent", ignoreCase = true)) {
            extraireTransaction(bigText)
        }
    }

    private fun extraireTransaction(texte: String): Transaction? {
        val montantRegex = Regex("Montant\\s*:\\s*(\\d+)\\s*(\\w+)")
        val montantMatch = montantRegex.find(texte) ?: return null

        val expediteurRegex = Regex("Expediteur\\s*:\\s*([A-Za-zÀ-ÿ\\s'-]+),(\\d+)")
        val expediteurMatch = expediteurRegex.find(texte) ?: return null

        val referenceRegex = Regex("\\b(\\d{15,20})\\b")
        val referenceMatch = referenceRegex.find(texte)

        val transaction = Transaction(
            montant = montantMatch.groupValues[1],
            devise = montantMatch.groupValues[2],
            nomExpediteur = expediteurMatch.groupValues[1].trim(),
            numeroExpediteur = expediteurMatch.groupValues[2],
            reference = referenceMatch?.groupValues?.get(1) ?: ""
        )

        Log.d("TransactionListener", "Transaction capturée: $transaction")
        sauvegarderTransaction(transaction)
        return transaction
    }

    private fun sauvegarderTransaction(t: Transaction) {
        val prefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val id = t.date.toString()
        prefs.edit()
            .putString("$id.montant", t.montant)
            .putString("$id.devise", t.devise)
            .putString("$id.expediteur", t.nomExpediteur)
            .putString("$id.numero", t.numeroExpediteur)
            .putString("$id.reference", t.reference)
            .apply()
    }
}
