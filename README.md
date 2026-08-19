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

## Note

Ce projet est prévu pour un usage strictement personnel.
