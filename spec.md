# Calendrier de l'Avent : spécification fonctionnelle

*Version 2, sans détail technique.*

## 1. Contexte et objectif

Un calendrier de l'Avent physique de 24 cartes accompagne cette application. Chaque carte porte une photo d'elle ou de nous deux en voyage, avec un court texte. Pour chaque carte, elle doit retrouver **le lieu** où la photo a été prise. Une bonne réponse débloque un **ticket cadeau** (massage, soirée raclette, week-end…).

L'application sert à :
- saisir ses réponses ;
- savoir si elles sont justes ;
- découvrir et collectionner les tickets gagnés.

Il y a **une seule utilisatrice**, sur **un seul téléphone Android**. Le ton est tendre, complice et ludique, avec un peu d'enjeu : un cadeau peut être perdu.

## 2. Contenu

Chaque carte contient :

| Élément | Description |
|---|---|
| Jour | De 1 à 24 |
| Texte | Le texte imprimé sur la carte, affiché dans l'appli en rappel |
| Lieu | La bonne réponse, telle qu'elle sera affichée une fois trouvée |
| Réponses acceptées | Les variantes d'écriture acceptées (ex. « St-Tropez », « Saint Tropez ») |
| Mauvaises réponses | 3 lieux plausibles mais faux, pour le QCM |
| Cadeau | L'intitulé du ticket |

Les textes, lieux, réponses, mauvaises réponses du QCM et cadeaux sont rédigés dans `app/src/main/assets/calendrier_avent.json`.

La photo n'est pas affichée dans l'appli : elle est sur la carte physique, que l'utilisatrice a en main.

## 3. Règles du jeu

### 3.1 Déblocage jour par jour

- La carte N devient disponible le **N décembre** à partir de minuit.
- Une carte d'un jour futur est visible mais verrouillée : on voit son numéro, et rien d'autre.
- Une carte d'un jour passé reste disponible : si elle a manqué un jour, elle peut **rattraper** le lendemain ou plus tard.
- Il n'y a pas de date limite. Toutes les cartes restent jouables après le 24.
- L'appli ne vérifie pas que la carte physique a été ouverte.

### 3.2 Répondre : 5 tentatives par carte

1. **Tentatives 1 à 3 : champ libre.** Elle tape le lieu. L'appli tolère :
   - les majuscules et les accents (« schonbrunn » = « Schönbrunn ») ;
   - les tirets, apostrophes et articles (« la plage de Split » = « Split ») ;
   - les petites fautes d'orthographe (« Porquerole » = « Porquerolles »). Aucune faute n'est tolérée sur les noms très courts (Tours, Paris), sinon ils deviennent trop faciles à deviner ;
   - une réponse noyée dans une phrase (« je crois que c'était à Plitvise »).
2. **Résultat après chaque essai en champ libre :**
   - **Correct** : on passe à la révélation du cadeau.
   - **Presque !** : la réponse est très proche, mais pas assez. Un message l'invite à vérifier l'orthographe. **Cet essai ne compte pas** comme une tentative.
   - **Plus précis !** : sur certaines cartes, le lieu exact est exigé (voir 3.5). Si elle donne seulement la ville, un message « Bonne piste, sois plus précise ! » s'affiche. **Cet essai ne compte pas** non plus.
   - **Faux** : un message bienveillant, et le nombre de tentatives restantes est affiché.
3. **Tentatives 4 et 5 : QCM.** Après 3 réponses fausses, le champ libre est remplacé par un QCM de 4 lieux (le bon et 3 mauvais), dans un ordre aléatoire. Elle a 2 essais. Un mauvais choix est grisé.
4. **Après 5 tentatives fausses**, la carte est **manquée** : le cadeau est perdu. On lui révèle quand même le lieu, comme un souvenir, avec un message doux (« Ce cadeau-là s'envole… mais pas le souvenir ! »).

Le nombre de tentatives restantes est toujours visible sur l'écran de réponse.

### 3.3 Étoiles (score de fierté)

| Comment la carte a été trouvée | Étoiles |
|---|---|
| En champ libre, du premier coup | ⭐⭐⭐ |
| En champ libre, au 2ᵉ ou 3ᵉ essai | ⭐⭐ |
| Grâce au QCM | ⭐ |
| Carte manquée | Aucune |

Les étoiles s'affichent sur la carte dans le calendrier, et le total apparaît à la fin.

### 3.4 Progression conservée

- Les tentatives utilisées sont enregistrées immédiatement. Fermer et rouvrir l'appli ne redonne pas de tentatives.
- Si elle quitte l'appli au milieu d'une carte, elle la retrouve dans le même état (même nombre de tentatives restantes, QCM déjà affiché s'il l'était, avec les mêmes choix grisés).

### 3.5 Cartes à lieu précis

Sur certaines cartes, la ville ne suffit pas : il faut donner le lieu exact.

| Carte | Réponse exigée | Refusé, avec « Plus précis ! » |
|---|---|---|
| 7 | Kunsthistorisches Museum | Vienne |
| 8 | Schönbrunn | Vienne |
| 9 | Schönbrunn (marché de Noël) | Vienne, « marché de Noël » |
| 14 | Arc de triomphe du Carrousel (ou Tuileries, Louvre) | Paris, « arc de triomphe » |
| 24 | Musée des Arts forains | Paris, Bercy |

Sur ces cartes, le QCM propose aussi des lieux précis (par exemple : Kunsthistorisches Museum, Belvédère, Schönbrunn, Musée des Arts forains).

## 4. Écrans

### 4.1 Accueil : le calendrier

- Une grille de 24 cases.
- Chaque case a un état visible au premier coup d'œil :
  - **verrouillée** (jour futur) : cadenas, non cliquable, avec un message « Patience… » au toucher ;
  - **disponible** (jour arrivé, pas encore jouée ou en cours) : mise en avant, et la carte du jour est encore plus visible ;
  - **trouvée** : le lieu et les étoiles sont affichés ;
  - **manquée** : le lieu est affiché, avec un style discret (grisé), sans étoile.
- En haut, la progression (« 7 / 24 lieux trouvés ») et le total d'étoiles.
- Un accès à l'écran « Mes tickets ».

### 4.2 Carte : répondre

- Le numéro de la carte et **son texte, en rappel**.
- Le nombre de tentatives restantes (ex. ●●●○○).
- **Tentatives 1 à 3** : un champ de saisie et un bouton « Valider ».
- **Tentatives 4 et 5** : les 4 propositions du QCM, sous forme de boutons.
- Le retour après chaque essai (Correct / Presque ! / Faux), avec des messages variés pour ne pas répéter toujours la même phrase.
- Une carte trouvée ou manquée peut être rouverte en lecture seule : texte, lieu, étoiles, ticket.

### 4.3 Révélation du cadeau

- Un moment de fête : animation de ticket qui se découvre, confettis…
- Le lieu trouvé, rappelé en titre (ex. « Porquerolles ! »).
- Le ticket cadeau, avec un visuel de vrai ticket (bord dentelé, numéro, intitulé).
- Le nombre d'étoiles obtenues.
- Un bouton pour revenir au calendrier ou voir tous ses tickets.

### 4.4 Mes tickets

- La liste de tous les tickets gagnés, dans l'ordre des cartes.
- Chaque ticket a deux états : **à utiliser** ou **utilisé**.
- Elle peut marquer un ticket comme utilisé, par exemple après le massage. Il reste visible, mais tamponné « Utilisé », comme un souvenir.
- Les tickets pas encore gagnés et ceux des cartes manquées n'apparaissent pas, pour garder la surprise.

### 4.5 Fin du calendrier

Quand les 24 cartes ont été jouées (trouvées ou manquées) :
- un écran de félicitations ;
- le nombre de lieux trouvés et le total d'étoiles ;
- la liste des 24 lieux, comme le parcours d'un carnet de voyage.

### 4.6 Mode organisateur (caché)

Il est réservé à toi, et accessible par un geste discret (par exemple un appui long sur le titre, puis un code).

Il permet de :
- **valider une carte manuellement**, si l'appli a refusé une réponse que tu juges bonne ;
- **rendre des tentatives** sur une carte (ou annuler une carte manquée), si tu veux être indulgent ;
- **débloquer toutes les cartes**, pour tester avant décembre ;
- **changer la date simulée**, pour tester le déblocage jour par jour ;
- **réinitialiser** toute la progression, pour remettre l'appli à zéro avant de la lui donner.

## 5. Notifications

- Un rappel chaque matin à **9 h**, du 1er au 24 décembre : « Une nouvelle carte t'attend ! » (avec plusieurs formulations).
- Elle peut les désactiver.

## 6. Ambiance

- Un thème carnet de voyage et Noël : papier, tampons, cartes postales, tickets.
- Des messages toujours bienveillants, même en cas d'erreur ou de carte manquée, jamais culpabilisants.
- Des messages variés : plusieurs formulations pour « Faux », « Presque ! », « Correct » et « Manquée ».

## 7. Hors périmètre

- Pas de mois à deviner, seulement le lieu.
- Pas d'indice.
- Pas de bouton pour te contacter depuis l'appli.
- Pas de compte, pas de connexion, pas de serveur : tout reste sur le téléphone.
- Pas de photos dans l'appli.
- Pas de version iPhone.

## 8. Décisions prises

| Question | Décision |
|---|---|
| Bouton « Demander à… » | Retiré |
| Texte de la carte dans l'appli | Affiché en rappel sur l'écran de réponse |
| Aide | Pas d'indice. 3 essais en champ libre, puis QCM pour les essais 4 et 5 |
| Échec | Après 5 essais faux, la carte est manquée et le cadeau perdu |
| Notifications | Oui, chaque matin à 9 h |
| Jouer avant d'ouvrir la carte physique | Possible, l'appli ne le vérifie pas |
| Reprise en cours de carte | Tentatives et QCM conservés |
| Lieu précis | Exigé sur les cartes 7, 8, 9, 14 et 24. La ville seule déclenche « Plus précis ! », sans consommer de tentative |
| Plateforme | Application Android native (Kotlin + Jetpack Compose), Android 8 minimum, portrait, adaptée à toutes les tailles de téléphone et à la police agrandie |
| Contenu | Le JSON est embarqué dans l'appli. Le repo public est assumé |
| Distribution | Un APK signé avec un keystore unique (secrets GitHub), attaché à chaque release publiée depuis un tag `vX.Y.Z` sur `main`. Le lien `releases/latest/download/calendrier-avent.apk` est stable |
| Tolérance du champ libre | Normalisation (casse, accents, ponctuation, articles), recherche dans la phrase. Seuils : ≤ 5 lettres → 0 faute (1 = Presque) ; 6 à 9 → 1 faute (2 = Presque) ; ≥ 10 → 2 fautes (3-4 = Presque). Ordre : acceptée → trop vague → Presque → Faux |
| QCM | Ordre tiré une fois et conservé. Les mauvais choix restent grisés |
| Date | Horloge du téléphone, déblocage à minuit heure locale, pas d'anti-triche |
| Ticket « rejouer un bon » (carte 10) | Ticket ordinaire, réglé dans la vraie vie |
| Mode organisateur | Appui long de 3 s sur le titre, puis le code 1337 |
| Notifications | Permission demandée au premier lancement après un écran explicatif, rappel entre 9 h 00 et 9 h 10, aucun rappel si la carte du jour est déjà jouée, interrupteur sur l'accueil |
