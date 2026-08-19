/**
 * Google Apps Script - à coller dans Extensions > Apps Script de votre Google Sheet.
 *
 * Prérequis dans le Sheet : DEUX onglets, un par app, avec les MÊMES colonnes
 * en ligne 1 dans chacun :
 *     A: Date | B: Heure | C: Type | D: Montant | E: Devise | F: Nom | G: Numero | H: Statut
 *
 * - Onglet "Sedad"
 * - Onglet "Bankily"
 *
 * (Les noms d'onglets doivent correspondre EXACTEMENT à "Sedad" et "Bankily",
 * c'est ce que l'app Android envoie dans le champ "source".)
 *
 * Déploiement :
 * 1. Extensions > Apps Script
 * 2. Collez ce code (remplacez le contenu par défaut)
 * 3. Déployer > Nouveau déploiement > type "Application Web"
 *    - Exécuter en tant que : Moi
 *    - Qui a accès : Tout le monde
 * 4. Copiez l'URL du déploiement (se termine par /exec)
 * 5. Collez cette URL dans SheetSync.kt (constante WEBHOOK_URL) côté Android
 *
 * Si vous modifiez ce code plus tard, il faut redéployer une NOUVELLE VERSION
 * (Déployer > Gérer les déploiements > crayon > Nouvelle version) pour que
 * les changements soient pris en compte par l'URL existante.
 */

function doPost(e) {
  var lock = LockService.getScriptLock();
  lock.waitLock(10000);

  try {
    var data = JSON.parse(e.postData.contents);
    var ss = SpreadsheetApp.getActiveSpreadsheet();

    var source = (data.source || '').toString().trim();
    var sheet = ss.getSheetByName(source);

    if (!sheet) {
      return ContentService.createTextOutput(
        JSON.stringify({ success: false, error: 'Onglet introuvable pour source: ' + source })
      ).setMimeType(ContentService.MimeType.JSON);
    }

    var nom = (data.nom || '').toString().trim();
    var numero = (data.numero || '').toString().trim();
    var montant = parseFloat(data.montant) || 0;
    var devise = data.devise || '';
    var type = data.type || '';
    var dateRecue = data.date ? new Date(Number(data.date)) : new Date();

    var dateStr = Utilities.formatDate(dateRecue, Session.getScriptTimeZone(), 'dd/MM/yyyy');
    var heureStr = Utilities.formatDate(dateRecue, Session.getScriptTimeZone(), 'HH:mm:ss');

    sheet.appendRow([dateStr, heureStr, type, montant, devise, nom, numero, 'Non envoyé']);

    return ContentService.createTextOutput(
      JSON.stringify({ success: true })
    ).setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    return ContentService.createTextOutput(
      JSON.stringify({ success: false, error: err.toString() })
    ).setMimeType(ContentService.MimeType.JSON);
  } finally {
    lock.releaseLock();
  }
}

function doGet(e) {
  return ContentService.createTextOutput('OK - Web App active');
}
