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
        val tvListe = findViewById<TextView>(R.id.tvListe)

        btnActiver.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        afficherTransactions(tvListe)
    }

    override fun onResume() {
        super.onResume()
        afficherTransactions(findViewById(R.id.tvListe))
    }

    private fun afficherTransactions(tv: TextView) {
        val prefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val all = prefs.all
        val sb = StringBuilder()

        val ids = all.keys.map { it.substringBefore(".") }.distinct().sortedDescending()
        for (id in ids) {
            val montant = prefs.getString("$id.montant", "")
            val devise = prefs.getString("$id.devise", "")
            val expediteur = prefs.getString("$id.expediteur", "")
            val numero = prefs.getString("$id.numero", "")
            val reference = prefs.getString("$id.reference", "")
            sb.append("Montant: $montant $devise\n")
            sb.append("De: $expediteur ($numero)\n")
            sb.append("Réf: $reference\n\n")
        }

        tv.text = if (sb.isEmpty()) "Aucune transaction capturée pour le moment." else sb.toString()
    }
}
