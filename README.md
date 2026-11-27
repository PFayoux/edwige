# Calendrier de l'Avent

Application Android compagnon d'un calendrier de l'Avent physique de 24 cartes photo.
Spécification : [spec.md](spec.md) · Glossaire : [CONTEXT.md](CONTEXT.md).

## Développer

- Kotlin + Jetpack Compose, JDK 17, Android 8+ (minSdk 26).
- Contenu du calendrier : `app/src/main/assets/calendrier_avent.json`.
- Textes d'interface (boutons, messages, notifications) : `app/src/main/res/values/strings.xml`.
  Après une modification, régénérer les captures d'écran.
- Tests : `./gradlew :app:testDebugUnitTest`
- Captures d'écran (plusieurs téléphones et tailles de police) :
  `./gradlew :app:recordPaparazziDebug`, puis voir `app/src/test/snapshots/images/`.
- Mode organisateur : appui long de 3 s sur le titre de l'accueil, puis le code.

## Publier une version

1. Sur `main`, créer une release GitHub avec un nouveau tag `vX.Y.Z`, supérieur au précédent.
2. La CI construit l'APK signé et l'attache à la release sous le nom `calendrier-avent.apk`.
3. Le lien de téléchargement ne change jamais :
   https://github.com/PFayoux/edwige/releases/latest/download/calendrier-avent.apk

La signature utilise un keystore unique (secrets `RELEASE_KEYSTORE_BASE64`,
`RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`).
Le garder précieusement : sans lui, impossible de mettre à jour l'appli sans la désinstaller.
