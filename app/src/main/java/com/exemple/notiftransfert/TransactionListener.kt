package com.exemple.notiftransfert

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class TransactionListener : NotificationListenerService() {

    data class Transaction(
        val source: String,        // "Sedad" ou "Bankily" -> nom de l'onglet cible
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
        // Package officiel de l'app Bankily (Banque Populaire de Mauritanie)
        const val PACKAGE_BANKILY = "mr.bpm.digitalbanking.consumer"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        when (sbn.packageName) {
            PACKAGE_SEDAD -> {
                Log.d("TransactionListener", "[Sedad] Titre: $title | Texte: $bigText")
                extraireSedad(title, bigText)
            }
            PACKAGE_BANKILY -> {
                Log.d("TransactionListener", "[Bankily] Titre: $title | Texte: $bigText")
                extraireBankily(title, bigText)
            }
            else -> return
        }
    }

    // Format Sedad observé: "Vous avez reçu 5.0 MRU de Abderrahmane ( 46536376 )"
    private fun extraireSedad(titre: String, texte: String): Transaction? {
        val regex = Regex(
            "re[cç]u\\s*([\\d.,]+)\\s*(\\w+)\\s*de\\s*([A-Za-zÀ-ÿ\\s]+?)\\s*\\(\\s*(\\d+)\\s*\\)",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(texte) ?: run {
            Log.d("TransactionListener", "[Sedad] Format non reconnu, ignoré: $texte")
            return null
        }

        val transaction = Transaction(
            source = "Sedad",
            type = titre.trim(),
            montant = match.groupValues[1],
            devise = match.groupValues[2],
            nomExpediteur = match.groupValues[3].trim(),
            numeroExpediteur = match.groupValues[4]
        )

        traiterTransaction(transaction)
        return transaction
    }

    // Format Bankily observé:
    // "Montant : 600 MRU
    //  Expediteur : ABDELLAHI IMAM,36663529
    //  0126081623371754016"
    private fun extraireBankily(titre: String, texte: String): Transaction? {
        if (!titre.contains("Transfert d'argent", ignoreCase = true)) return null

        val montantRegex = Regex("Montant\\s*:\\s*([\\d.,]+)\\s*(\\w+)", RegexOption.IGNORE_CASE)
        val montantMatch = montantRegex.find(texte) ?: run {
            Log.d("TransactionListener", "[Bankily] Montant non trouvé, ignoré: $texte")
            return null
        }

        val expediteurRegex = Regex(
            "Expediteur\\s*:\\s*([A-Za-zÀ-ÿ\\s'-]+),(\\d+)",
            RegexOption.IGNORE_CASE
        )
        val expediteurMatch = expediteurRegex.find(texte) ?: run {
            Log.d("TransactionListener", "[Bankily] Expéditeur non trouvé, ignoré: $texte")
            return null
        }

        val transaction = Transaction(
            source = "Bankily",
            type = titre.trim(),
            montant = montantMatch.groupValues[1],
            devise = montantMatch.groupValues[2],
            nomExpediteur = expediteurMatch.groupValues[1].trim(),
            numeroExpediteur = expediteurMatch.groupValues[2]
        )

        traiterTransaction(transaction)
        return transaction
    }

    private fun traiterTransaction(t: Transaction) {
        Log.d("TransactionListener", "Transaction capturée: $t")
        sauvegarderTransaction(t)
        SheetSync.envoyer(t)
    }

    private fun sauvegarderTransaction(t: Transaction) {
        val prefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val id = t.date.toString()
        prefs.edit()
            .putString("$id.source", t.source)
            .putString("$id.type", t.type)
            .putString("$id.montant", t.montant)
            .putString("$id.devise", t.devise)
            .putString("$id.expediteur", t.nomExpediteur)
            .putString("$id.numero", t.numeroExpediteur)
            .apply()
    }
}
