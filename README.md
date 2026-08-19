# NotifTransfert

Application Android personnelle qui lit les notifications de transfert d'argent
et en extrait le montant, l'expéditeur et la référence.

## Générer l'APK en ligne (GitHub Actions — gratuit)

1. Créez un nouveau dépôt sur https://github.com/new (ex: `notif-transfert`),
   en le laissant **vide** (pas de README auto-généré).

2. Sur votre ordinateur, dans le dossier `NotifTransfert` (ce dossier),
   initialisez git et poussez le code :

   ```bash
   cd NotifTransfert
   git init
   git add .
   git commit -m "Premier commit"
   git branch -M main
   git remote add origin https://github.com/VOTRE_UTILISATEUR/notif-transfert.git
   git push -u origin main
   ```

   (Remplacez `VOTRE_UTILISATEUR` et le nom du dépôt par les vôtres.)

3. Allez sur votre dépôt GitHub → onglet **Actions**.
   Le workflow "Build APK" se lance automatiquement dès le push.
   Si ce n'est pas le cas, cliquez sur "Build APK" → "Run workflow".

4. Attendez que le build passe au vert (2-3 minutes).

5. Cliquez sur le run terminé → en bas, section **Artifacts** →
   téléchargez `app-debug` (un fichier .zip contenant l'APK).

6. Dézippez, transférez `app-debug.apk` sur votre téléphone
   (par Google Drive, USB, Bluetooth, etc.) et installez-le
   (autorisez "Sources inconnues" si demandé).

## Après installation

1. Ouvrez l'app "Notif Transfert".
2. Appuyez sur "Activer l'accès aux notifications" → trouvez
   "Notif Transfert" dans la liste → activez le bouton.
3. Provoquez une notification de transfert d'argent et vérifiez
   qu'elle apparaît dans la liste de l'app.

## Trouver le vrai package de l'app de transfert

Le filtre par `packageName` est commenté dans
`TransactionListener.kt` pour l'instant, donc toutes les notifications
du téléphone sont journalisées. Utilisez `adb logcat` (ou une app
de visualisation de logs) filtré sur "TransactionListener" pour
repérer le `packageName` exact de l'app de transfert, puis
décommentez la ligne de filtre dans le code, recommitez, et relancez
le build.

## Deux apps, deux onglets

L'app Android capte maintenant les notifications de **Sedad** et de
**Bankily**, avec un format de lecture différent pour chacune (le texte
des notifications n'est pas le même selon l'app). Chaque transaction est
envoyée vers le Google Sheet avec un champ `source` ("Sedad" ou "Bankily"),
et le script Google range automatiquement la ligne dans l'onglet
correspondant.

Packages identifiés :
- Sedad : `mr.digi.sedad`
- Bankily : `mr.bpm.digitalbanking.consumer`

## Synchronisation avec Google Sheets

### 1. Créer le Google Sheet

1. Allez sur https://sheets.new pour créer un nouveau Sheet
2. Renommez-le comme vous voulez (ex: "Suivi Transferts")
3. Créez **deux onglets**, nommés **exactement** `Sedad` et `Bankily`
   (clic droit sur un onglet en bas → Renommer, ou `+` pour en ajouter)
4. Dans **chacun** des deux onglets, ajoutez ces en-têtes en ligne 1 :

   | Date | Heure | Type | Montant | Devise | Nom | Numero | Statut |
   |---|---|---|---|---|---|---|---|

   (Laissez le reste vide, ça se remplit automatiquement, avec
   `Statut = Non envoyé` par défaut.)

### 2. Déployer le script

1. Dans le Sheet : menu **Extensions → Apps Script**
2. Supprimez le code par défaut, collez-y le contenu du fichier
   **`AppsScript_Code.gs`** (fourni dans ce projet)
3. Cliquez sur **Enregistrer** (icône disquette)
4. Cliquez sur **Déployer → Nouveau déploiement**
5. Cliquez sur l'icône ⚙️ à côté de "Sélectionner le type" → **Application Web**
6. Configurez :
   - **Exécuter en tant que** : Moi (votre compte)
   - **Qui a accès** : Tout le monde
7. Cliquez sur **Déployer**
8. Google demandera d'**autoriser l'accès** (c'est votre propre script, sur
   votre propre Sheet, donc c'est normal) → autorisez
9. **Copiez l'URL** affichée (elle se termine par `/exec`)

### 3. Connecter l'app Android à cette URL

1. Dans votre dépôt, ouvrez
   `app/src/main/java/com/exemple/notiftransfert/SheetSync.kt`
2. Remplacez la ligne :
   ```kotlin
   const val WEBHOOK_URL = "https://script.google.com/macros/s/VOTRE_ID_DE_DEPLOIEMENT/exec"
   ```
   par l'URL que vous venez de copier.
3. Committez et poussez :
   ```bash
   git add .
   git commit -m "Config URL Google Sheets"
   git push
   ```
4. Récupérez le nouvel APK depuis GitHub Actions (comme d'habitude) et
   réinstallez-le sur votre téléphone.

### Comment ça fonctionne

À chaque notification Sedad capturée, l'app envoie
`{nom, numero, montant, devise, type, date}` au script, qui ajoute une
ligne dans l'onglet "Transactions" avec la date et l'heure séparées, et
`Statut = "Non envoyé"`. Vous mettez ensuite à jour cette colonne
vous-même dans le Sheet (par ex. "Envoyé", "Déduit de la dette", etc.)
selon votre propre suivi.

### Tester sans attendre une vraie transaction

Vous pouvez tester le script directement avec `curl` (depuis un PC/Termux) :
```bash
curl -X POST "VOTRE_URL_DE_DEPLOIEMENT" \
  -H "Content-Type: application/json" \
  -d '{"source":"Sedad","nom":"Abderrahmane","numero":"46536376","montant":"50","devise":"MRU","type":"ENVOI","date":"1755600000000"}'

curl -X POST "VOTRE_URL_DE_DEPLOIEMENT" \
  -H "Content-Type: application/json" \
  -d '{"source":"Bankily","nom":"ABDELLAHI IMAM","numero":"36663529","montant":"600","devise":"MRU","type":"Transfert d'"'"'argent","date":"1755600000000"}'
```
La première commande doit ajouter une ligne dans l'onglet "Sedad", la
seconde dans l'onglet "Bankily".

### Limites à connaître
- Si le téléphone n'a pas internet au moment de la notification, l'envoi
  échoue silencieusement (rien n'est mis en file d'attente pour réessayer
  plus tard — la transaction reste quand même visible dans l'app locale).

## Note

Ce projet est prévu pour un usage strictement personnel.
