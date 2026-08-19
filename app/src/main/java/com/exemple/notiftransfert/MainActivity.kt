package com.exemple.notiftransfert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnActiver = findViewById<Button>(R.id.btnActiver)
        val tvStatutSheet = findViewById<TextView>(R.id.tvStatutSheet)
        val tvListe = findViewById<TextView>(R.id.tvListe)

        btnActiver.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        afficherStatutSheet(tvStatutSheet)
        afficherTransactions(tvListe)
    }

    override fun onResume() {
        super.onResume()
        afficherStatutSheet(findViewById(R.id.tvStatutSheet))
        afficherTransactions(findViewById(R.id.tvListe))
    }

    private fun afficherStatutSheet(tv: TextView) {
        tv.text = "Google Sheets — dernier envoi :\n${SheetSync.lireDernierStatut(this)}"
    }

    private fun afficherTransactions(tv: TextView) {
        val prefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val all = prefs.all
        val sb = StringBuilder()

        val ids = all.keys.map { it.substringBefore(".") }.distinct().sortedDescending()
        for (id in ids) {
            val source = prefs.getString("$id.source", "")
            val type = prefs.getString("$id.type", "")
            val montant = prefs.getString("$id.montant", "")
            val devise = prefs.getString("$id.devise", "")
            val expediteur = prefs.getString("$id.expediteur", "")
            val numero = prefs.getString("$id.numero", "")
            sb.append("[$source | $type] $montant $devise\n")
            sb.append("De: $expediteur ($numero)\n\n")
        }

        tv.text = if (sb.isEmpty()) "Aucune transaction capturée pour le moment." else sb.toString()
    }
}
