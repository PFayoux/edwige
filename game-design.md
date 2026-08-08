# Doc de Game Design — Roguelite de la Chouette

## Concept

Jeu roguelite léger, ambiance humoristique avec des clins d'œil personnels. Le joueur incarne une chouette qui progresse dans un donjon, porte après porte, jusqu'à sa mort — ou jusqu'à sa victoire, en vainquant le boss du palier 3 (porte 30). Le score final est le nombre de portes franchies.

Pas de carte : progression purement linéaire (séquence d'événements).

---

## Boucle de jeu

1. Le joueur ouvre une porte
2. La porte déclenche un événement (voir Types de portes)
3. Résolution de l'événement (combat, choix, etc.)
4. Petit heal automatique si combat gagné (10–15% des PV max)
5. Porte suivante
6. Le run se termine dans l'un des deux cas suivants :
   - Les PV tombent à 0 → écran de fin avec score (nombre de portes) + meilleur score (voir Écran de fin de run)
   - Le boss du palier 3 (porte 30) est vaincu → victoire, écran dédié (voir Écran de victoire)

---

## Système de combat

Combat par **salves** : plus une simple suite de jets, le joueur doit gérer un budget de dés et les affecter stratégiquement.

### Déroulé d'une salve

1. Le joueur lance **5d100** d'un coup → il voit les 5 valeurs
2. Le monstre "lance" aussi ses 5d100 (caché)
3. Le joueur assigne à chaque dé une **action** parmi : **Attaque / Défense / Raillerie**, et choisit l'**ordre** dans lequel les 5 couples (dé + action) seront résolus
4. Les 5 résolutions s'enchaînent une par une, dans l'ordre choisi par le joueur, contre les 5 actions du monstre (générées côté monstre, avec son propre ordre)
5. Si aucun des deux n'est mort après les 5 résolutions → nouvelle salve (on relance 5d100 chacun), et ainsi de suite jusqu'à la mort d'un des deux

### Triangle d'actions

- **Défense** bat **Attaque**
- **Attaque** bat **Raillerie**
- **Raillerie** bat **Défense**

Chaque action est liée à une stat dédiée :
- **Attaque** → **Force**
- **Défense** → **Dextérité**
- **Raillerie** → **Charisme**

### Résolution d'une confrontation

Le triangle détermine toujours le **type gagnant** entre deux actions différentes. Mais désormais, chaque camp doit aussi **réussir son jet** pour que son action se déclenche réellement : succès si `dé ≤ seuil effectif`.

**Seuil effectif (opposition)** — la stat adverse pèse dans la balance, pas seulement la sienne :

`seuil effectif = stat propre − (stat adverse liée à son action / 4)`

Un monstre avec une grosse stat sur l'action qu'il joue réduit vraiment tes chances de réussir la tienne, même si le triangle t'est favorable. Cette opposition s'applique dans tous les cas (gagnant du triangle, rattrapage, et clash).

**Types différents (ex: Attaque vs Défense)**
1. Le camp gagnant du triangle tente son jet
2. **Succès** → il obtient l'effet normal de son type (dégâts, ou blocage+riposte pour Défense)
3. **Échec** → son effet n'a pas lieu ; on regarde alors le camp perdant : si SON jet réussit aussi, il obtient l'effet qu'aurait donné son propre type (**mécanique de rattrapage**)
4. Si les deux échouent → rien ne se passe ce tour

**Même type des deux côtés (clash)**
- Chaque camp teste sa réussite indépendamment (avec opposition)
- Les deux réussissent → règle de clash spécifique au type (voir ci-dessous)
- Un seul réussit → il obtient l'effet plein de son type, l'autre rien
- Aucun ne réussit → rien ne se passe

### Formules de dégâts

Le dé ne sert plus à doser les dégâts — il détermine la réussite et le critique. La magnitude dépend de la stat.

**Dégâts selon le type gagnant** :
- Attaque bat Raillerie → dégâts = `Force × 0.4`
- Raillerie bat Défense → dégâts = `Charisme × 0.35`
- Défense bat Attaque → bloque tous les dégâts, + riposte = `Dextérité × 0.15`

**Bonus de maîtrise** : si la stat utilisée est **≥ 80**, l'action associée inflige **+20% de dégâts**. Récompense l'investissement massif dans une stat sans encourager le spam d'une seule action, puisque le triangle décide toujours qui peut agir.

**Clash — règles différenciées par action** (si les deux camps réussissent leur jet) :
- **Attaque vs Attaque** → échange brutal, les deux camps encaissent = `Force propre × 0.4` chacun
- **Défense vs Défense** → stalemate, rien ne se passe
- **Raillerie vs Raillerie** → bataille de répartie, la meilleure marge de réussite (`seuil effectif − dé`, la plus grande) inflige `Charisme du gagnant × 0.35` à l'autre

*(Ratios indicatifs (0.4 / 0.35 / 0.15) — bons candidats de départ, à ajuster en playtest si les combats sont trop rapides/trop longs.)*

### Critique et échec critique

Le rôle de la stat **Chance** : elle élargit les bandes extrêmes de réussite/échec sur n'importe quelle action.

- **Critique** : `dé ≤ (stat_associée / 5 + Chance / 10)` → dégâts infligés ×2
- **Échec critique** : `dé ≥ (96 − Chance / 5)` → si ce camp encaisse des dégâts ce tour, ils sont doublés

Chaque camp (joueur et monstre) évalue sa propre réussite avec son propre dé, son propre seuil effectif (après opposition), et sa propre Chance à chaque confrontation.

**Calibrage des monstres** : la stat pertinente d'un monstre (celle utilisée quand il joue une action donnée) doit suivre sa difficulté visée :
- Monstre facile : **60**
- Monstre moyen : **70**
- Monstre difficile : **80**

Avec les stats de départ du joueur à 60 et l'opposition qui réduit encore le seuil effectif face à des monstres plus costauds, le bestiaire recalibré précédemment (PV, stats) devra être revu une fois le prototype jouable pour retrouver le bon rythme de combat (2-3 coups facile / 4-5 moyen / 5+ boss).

### Note de design — pourquoi aucune action n'est "meilleure"

Avec ces règles, jouer une seule action en boucle est risqué : Attaque en continu expose à des dégâts mutuels si le monstre attaque aussi, et à un blocage sec s'il défend — elle ne rapporte vraiment que contre une Raillerie adverse. Aucune des 3 actions n'est donc dominante par défaut sur un tirage aléatoire côté monstre.

Pour renforcer cette diversité, prévoir des **profils de comportement par monstre** (via le système de traits) : un monstre agressif qui privilégie Attaque, un monstre prudent qui privilégie Défense, etc. Ça récompense la lecture et l'adaptation plutôt qu'un pattern fixe.

### Talent Œil de nuit et les salves

Une fois par combat, la chouette peut activer Œil de nuit : révèle **les 5 actions prévues par le monstre pour la salve en cours** (Attaque/Défense/Raillerie pour chacune), mais **pas les valeurs de dé associées**. Le joueur connaît donc le pattern d'actions à contrer, mais doit encore parier sur l'ampleur des dés adverses en choisissant où placer ses propres meilleurs jets.

### Stats de départ (joueur)

| Stat | Valeur de départ | Effet |
|---|---|---|
| PV | 50 | Points de vie |
| Force | 60 | Chance de réussite de l'action Attaque (avant opposition, voir plus bas) |
| Dextérité | 60 | Chance de réussite de l'action Défense |
| Charisme | 60 | Chance de réussite de l'action Raillerie |
| Chance | 10 | Stat secondaire, élargit les bandes de critique/échec critique |

Les trois stats d'action (Force/Dextérité/Charisme) démarrent à égalité, à 60% de réussite de base, pour ne favoriser aucune action par défaut. Chance reste une stat à part, indépendante du triangle d'actions.

**Plafond et coût progressif** : chaque stat d'action est plafonnée à **95** (jamais de garantie à 100%, il reste toujours un risque résiduel). Le coût en points suit un palier progressif pour décourager le "tout miser sur une seule stat" :
- De la valeur de départ jusqu'à 80 : 1 point par palier
- De 81 à 90 : 2 points par palier
- De 91 à 95 : 3 points par palier

Après chaque victoire : 1 point à répartir librement (Force / Dextérité / Charisme / Chance / +5 PV max), **ou** un choix parmi 2–3 objets proposés (voir Table d'objets).

---

## Types de portes

| Type | Fréquence | Description |
|---|---|---|
| Monstre | 55% | Combat classique |
| Trésor gardé | 15% | Mini combat plus dur, meilleure récompense (objet garanti) |
| Piège | 15% | Choix binaire risque/gain, toujours visible avant de décider |
| Malédiction/Autel | 10% | Effet à double tranchant, activable ou non |
| Marchand/Repos | 5% | Choix entre gros soin (40–50% PV max) ou bonus de stat |

**Boss** : apparaît tous les 10 portes, tiré depuis un pool séparé du pool "monstres normaux". Le système est **paliéré sur 3 niveaux** :
- Porte 10 → boss tiré du pool **palier 1**
- Porte 20 → boss tiré du pool **palier 2**
- Porte 30 → boss tiré du pool **palier 3** — sa défaite déclenche la **victoire** (voir Écran de victoire)

Chaque palier propose **3 boss possibles**, tirés indépendamment à chaque run (pas de mémoire d'un run à l'autre — un même boss peut revenir d'un run au suivant). Voir *Bestiaire de départ* pour le détail des boss par palier.

**Principe clé** : jamais d'effet caché/surprise sur les pièges ou malédictions — le risque et le gain doivent toujours être visibles avant le choix du joueur. L'objectif est d'éviter que le jeu soit un pur jeu de hasard.

---

## Talent de personnage

Système pensé pour accueillir plusieurs personnages jouables à l'avenir, chacun avec un talent unique.

**Chouette — "Œil de nuit"** : une fois par combat, révèle les 5 actions (Attaque/Défense/Raillerie) prévues par le monstre pour la salve en cours, sans révéler les valeurs de dé associées. Voir détail dans la section Système de combat.

---

## Table d'objets / effets (tirage pondéré)

### Bonus stats simples
1. +3 Force
2. +3 Dextérité
3. +15 PV max
4. +5 Chance

### Objets à effet permanent
5. **Plume Aiguisée** — critiques à x2.5 au lieu de x2
6. **Griffe Récupératrice** — vol de vie 10% des dégâts infligés
7. **Bouclier Rouillé** — -15% dégâts subis, -10 Force
8. **Amulette du Hasard** — seuil de critique doublé ET seuil d'échec critique doublé
9. **Bourdon Chanceux** — 1 relance gratuite par combat (cumulable avec Œil de nuit)
10. **Talisman Fragile** — +40 Force, -20 PV max

### Effets à double tranchant (choix actif)
11. **Pacte du Corbeau** — +30 Force immédiat, -1 PV max par porte suivante (dégressif)
12. **Marché de Sang** — sacrifie 20 PV actuels → +2 points de stat immédiats
13. **Bénédiction Instable** — stats aléatoires re-tirées à chaque palier de boss

### Pièges (choix binaire)
14. **Pont Branlant** — "Traverser vite" (jet : échec = dégâts, réussite = objet bonus) vs "Prudent" (rien)
15. **Coffre Piégé** — "Forcer" (jet : échec = malédiction 3 portes, réussite = objet rare) vs "Laisser" (rien)

## Objets de combat (consommables)

Catégorie distincte des objets permanents ci-dessus : ce sont des **consommables à usage unique**, utilisables par le joueur au moment de son choix pendant une salve (avant d'assigner ses actions, ou entre deux confrontations).

**Inventaire limité à 3 slots** — force le joueur à choisir lesquels garder/utiliser, pas d'accumulation infinie.

### Effets sur les dés
1. **Renfort** — réduit ton dé de 15 sur l'action de ton choix (te rapproche de la réussite — un petit dé est désormais bon)
2. **Sabotage** — augmente le dé du monstre de 15 sur l'action de ton choix (l'éloigne de sa réussite)
3. **Silence** — supprime une action du monstre pour la salve en cours (elle ne se résout pas)
4. **Dédoublement** — duplique un des dés du joueur pour l'utiliser sur deux actions différentes dans la même salve
5. **Larcin** — réduit de 20 la stat du monstre liée à l'action visée, pour cette confrontation uniquement (affaiblit son opposition, augmente ton seuil effectif)

### Dégâts / soin
6. **Rage** — multiplie les dégâts d'une victoire d'action
7. **Coup Sournois** — inflige des dégâts directs au monstre, hors triangle d'actions
8. **Potion** — soigne un montant fixe de PV
9. **Poison** — inflige des dégâts au monstre à chaque salve pendant plusieurs salves, indépendamment des actions jouées

### Défense / contrôle
10. **Armure de Plumes** — annule les dégâts subis pendant 1 salve
11. **Étourdissement** — le monstre ne joue que 4 dés (au lieu de 5) à sa prochaine salve
12. **Miroir** — si le monstre gagne une confrontation ce tour, une partie des dégâts lui est renvoyée

*(Trouvables via les portes Trésor gardé, Coffre Piégé, ou récompenses de boss — à intégrer dans la table de loot pondérée globale.)*

---

## Bestiaire de départ

**Principe de calibrage** : la stat d'un monstre (Force/Dextérité selon l'action qu'il joue) fixe sa chance de réussite de base, avant opposition — 60% pour un monstre facile, 70% pour un moyen, 80% pour un difficile. La magnitude des dégâts (`stat × ratio`) reste dans le même ordre de grandeur qu'avant (~20-25 dégâts par coup réussi avec une stat autour de 60), donc les PV ci-dessous restent valables comme point de départ. Formule à garder pour tout futur monstre :

`PV cible ≈ nombre de coups voulu × dégâts moyens attendus (~20-25)`

Cible de rythme : 2-3 coups pour un monstre facile, 4-5 pour un monstre difficile, 5+ pour un boss.

*(Note : "Atq" et "Déf" ci-dessous correspondent aux équivalents Force/Dextérité côté monstre, sur l'échelle 60/70/80 de calibrage. Pas encore de Charisme dédié pour leur Raillerie — ils réutilisent leur valeur "Atq" en attendant, à affiner si besoin en playtest.)*

| # | Monstre | PV | Déf | Atq | Particularité |
|---|---|---|---|---|---|
| 1 | Souris Anxieuse | 50 | 60 | 60 | Fuit si elle rate 2 fois de suite |
| 2 | Pigeon Prétentieux | 65 | 62 | 62 | Roucoule avant d'attaquer |
| 3 | Chat de Gouttière Blasé | 90 | 68 | 68 | Attaque "mou" mais régulier |
| 4 | Corbeau Mystique | 110 | 72 | 72 | Jette un sort (préfigure les traits futurs) |
| 5 | Renard Rusé | 130 | 78 | 78 | Esquive 1 attaque sur 3 |

Après le palier 5, scaling en boucle (+10% de stats par palier, plafonné à 95 comme pour le joueur) pour les rencontres normales — le boss suit désormais son propre système de paliers, voir ci-dessous.

### Boss par palier

Depuis l'introduction du système de boss paliérés (voir *Types de portes*), le pool de boss n'est plus un pool plat unique mais **3 pools distincts**, un par palier :
- Porte 10 → boss tiré du pool **palier 1**
- Porte 20 → boss tiré du pool **palier 2**
- Porte 30 → boss tiré du pool **palier 3**, dont la défaite déclenche la **victoire** (voir Écran de victoire)

Chaque palier propose **3 boss possibles**, tirage indépendant à chaque run (pas de mémoire d'un run à l'autre).

**Calibrage par palier** (qualitatif — les valeurs précises seront ajustées en playtest, comme le reste du bestiaire) :
- **Palier 1** ≈ calibrage boss existant (~85) — pas de changement nécessaire pour les 3 boss ci-dessous, déjà dans la bonne fourchette
- **Palier 2** : monte vers la zone que le scaling en boucle (+10%/palier, voir ci-dessus) du bestiaire normal atteindrait un peu plus loin
- **Palier 3** : le plus dur, proche du plafond de stat existant (95 — même plafond que le joueur)

**Palier 1** (formalisé à partir des esquisses existantes) :

| Boss | PV | Déf | Atq | Particularité |
|---|---|---|---|---|
| Dragon de Composte | 180 | 85 | 85 | Boss "équilibré" de référence |
| Golem de Feuilles Mortes | 220+ | — | 15-20 | Archétype Tank — PV très élevé, Attaque faible. Combat long mais peu risqué, pur test d'endurance |
| Hérisson Blindé | 120-150 | 60+ | — | Archétype Défensif — Défense très élevée, gagne souvent ses confrontations en Défense, ce qui bloque beaucoup d'attaques du joueur et prolonge le combat sans nécessiter énormément de PV |

**Paliers 2 et 3** : contenu à créer (3 boss chacun), dans la bande de difficulté qualitative ci-dessus — noms, particularités et stats précises pas encore définis.

---

## Structure de données prévue (extensibilité)

Penser le moteur pour que l'ajout de contenu ne nécessite **pas** de toucher au code, uniquement au JSON.

### Monstre
```json
{
  "id": "souris_anxieuse",
  "nom": "Souris Anxieuse",
  "pv": 15,
  "defense": 15,
  "attaque": 10,
  "min_palier": 1,
  "type": "normal",
  "traits": []
}
```
`traits` : liste vide au départ, prévue pour accueillir plus tard des effets type `resistance: force`, `malus_joueur: chance -10`, `attaque_speciale: etourdit_1_tour`, etc.

### Boss
Même structure que Monstre, avec `"type": "boss"` et son propre pool (`min_palier` pour filtrer selon la profondeur).

### Personnage jouable
```json
{
  "id": "chouette",
  "nom": "Chouette",
  "pv_depart": 50,
  "attaque_depart": 20,
  "defense_depart": 20,
  "chance_depart": 10,
  "talent": {
    "id": "oeil_de_nuit",
    "nom": "Œil de nuit",
    "effet": "mulligan_gratuit_1x_par_combat"
  }
}
```

### Objet / effet
```json
{
  "id": "griffe_recuperatrice",
  "nom": "Griffe Récupératrice",
  "type": "permanent",
  "effet": "vol_de_vie_10pct",
  "description": "Soigne 10% des dégâts infligés à chaque coup réussi."
}
```

### Porte
```json
{
  "type": "piege",
  "id": "pont_branlant",
  "choix": [
    { "label": "Traverser vite", "resultat": "jet_risque_recompense" },
    { "label": "Prudent", "resultat": "rien" }
  ]
}
```

---

## Écran d'accueil / Début de run

**Écran titre**
- Nom du jeu + visuel simple de la chouette
- Bouton "Jouer"
- Meilleur score affiché (nombre de portes) si un run précédent existe

**Avant la première porte**
- Court texte d'intro (2-3 phrases, ton léger/humoristique) présentant la chouette et son talent Œil de nuit
- Fiche perso avec les stats de départ (PV/Force/Dextérité/Charisme/Chance), pas juste des chiffres bruts

**Pas de tutoriel** : le jeu doit rester compréhensible par lui-même. Ça implique côté UI de toujours afficher clairement, à chaque jet de dé, le résultat obtenu, le seuil à battre, et si c'est une réussite/échec/critique — sans texte d'explication superflu.

---

## Écran de fin de run

Déclenché quand les PV du joueur tombent à 0.

**Contenu affiché**
- Message de fin générique, ton léger (pas un "Game Over" sec — ex : *"Envol interrompu"*). Structure prévue pour accueillir plus tard des messages spécifiques par monstre/cause de mort, mais un seul message générique suffit pour le MVP.
- **Score** : nombre de portes franchies ce run
- **Meilleur score** affiché juste en dessous, avec mention "Nouveau record !" si battu ce run
- **Récap du build** : stats finales (Force/Dextérité/Charisme/Chance/PV max), objets permanents ramassés pendant le run
- Bouton **"Rejouer"** → relance directement un nouveau run

---

## Écran de victoire

Déclenché quand le boss du palier 3 (porte 30) est vaincu. Remplace l'écran de fin de run habituel — le run se termine immédiatement, sans passer par la récompense de victoire normale (le run étant terminé, un choix d'objet ou de point de stat n'aurait plus d'utilité).

**Contenu affiché**
- Message de victoire générique, ton léger et chaleureux (même registre que l'écran de fin de run, pas de fanfare "YOU WIN !" — texte exact à écrire plus tard)
- **Récap du build** : stats finales (Force/Dextérité/Charisme/Chance/PV max), objets permanents ramassés pendant le run
- Bouton **"Rejouer"** → relance directement un nouveau run
- **Pas de score affiché** : la victoire correspond toujours à 30 portes franchies, l'afficher n'apporterait aucune information. Le **meilleur score** est tout de même mis à jour à 30 en interne, pour rester cohérent avec l'écran titre et les futurs écrans de fin de run par mort.

*(Piste pour plus tard, hors scope ici : un système de score plus élaboré à la Slay the Spire, reflétant la qualité du run plutôt que juste le nombre de portes.)*

---

## Méta-progression entre les runs

**Version MVP** : pas de progression permanente entre les runs (pas de monnaie, pas de déblocage). Seul élément persistant : le **meilleur score** (nombre de portes franchies), stocké localement et affiché à l'écran titre et à l'écran de fin de run. Chaque run repart avec les stats de base.

Comme la victoire correspond toujours à exactement 30 portes franchies, un meilleur score à 30 signale de façon permanente qu'une victoire a déjà été obtenue — pas besoin d'un indicateur séparé pour l'instant.

**Piste pour plus tard** : achievements personnels (conditions liées à des clins d'œil plutôt que génériques — ex : nombre de portes précis, type de mort particulier, combinaison de choix faits pendant un run). Rien à implémenter maintenant, mais penser le système de sauvegarde du score de façon à pouvoir y ajouter facilement une liste d'achievements débloqués par la suite.

---

## Notes techniques / stack

- Cible : webapp (PWA), plus rapide à tester qu'une app Kotlin native pour ce prototype
- Contenu piloté par JSON (monstres, boss, objets, portes, personnages) pour rester extensible sans retoucher le moteur
- Moteur de combat doit gérer nativement la notion de "trait/effet" sur les monstres, même si au lancement tous les traits sont vides
- Pas de carte à gérer, uniquement une séquence de portes (simplifie beaucoup le moteur)

## Reste à faire avant/à côté du code

- Thème/nom final du jeu, univers visuel de la chouette et des monstres
- Contenu des boss des paliers 2 et 3 (noms, particularités, stats précises — dans la bande de difficulté qualitative définie dans *Bestiaire de départ*)
- Texte exact du message de l'écran de victoire (le registre/ton est fixé, le texte final reste à écrire)
