import type { ActionStats, ActionType, ConfrontationOutcome, DiceAssignment } from './types'
import { TRIANGLE_WINNER, statFor } from './types'

/** `seuil effectif = stat propre − (stat adverse liée à son action / 4)` — game-design.md. */
function effectiveThreshold(ownAction: ActionType, ownStats: ActionStats, opponentAction: ActionType, opponentStats: ActionStats): number {
  return statFor(ownAction, ownStats) - statFor(opponentAction, opponentStats) / 4
}

function isCrit(die: number, action: ActionType, stats: ActionStats): boolean {
  return die <= statFor(action, stats) / 5 + stats.chance / 10
}

function isCritFail(die: number, stats: ActionStats): boolean {
  return die >= 96 - stats.chance / 5
}

/** Base damage for a type's effect, before crit — game-design.md's damage formulas. */
function typeEffectDamage(action: ActionType, stats: ActionStats): number {
  const base = action === 'attaque' ? stats.force * 0.4 : action === 'raillerie' ? stats.charisme * 0.35 : stats.dexterite * 0.15
  const masteryBonus = statFor(action, stats) >= 80 ? 1.2 : 1
  return base * masteryBonus
}

function applyCrit(damage: number, crit: boolean): number {
  return crit ? damage * 2 : damage
}

/**
 * Resolves one confrontation (one die + action from each side) per
 * game-design.md's "Résolution d'une confrontation". Pure — no RNG calls,
 * dice are passed in already rolled (per issue #3's injection pattern).
 */
export function resolveConfrontation(
  player: DiceAssignment,
  playerStats: ActionStats,
  monster: DiceAssignment,
  monsterStats: ActionStats,
): ConfrontationOutcome {
  const playerCrit = isCrit(player.die, player.action, playerStats)
  const monsterCrit = isCrit(monster.die, monster.action, monsterStats)
  const playerCritFail = isCritFail(player.die, playerStats)
  const monsterCritFail = isCritFail(monster.die, monsterStats)

  let playerDamageDealt = 0
  let monsterDamageDealt = 0
  let summary: ConfrontationOutcome['summary'] = 'none'

  const playerThreshold = effectiveThreshold(player.action, playerStats, monster.action, monsterStats)
  const monsterThreshold = effectiveThreshold(monster.action, monsterStats, player.action, playerStats)
  const playerSucceeds = player.die <= playerThreshold
  const monsterSucceeds = monster.die <= monsterThreshold

  if (player.action !== monster.action) {
    const playerWinsTriangle = TRIANGLE_WINNER[player.action] === monster.action
    const winnerIsPlayer = playerWinsTriangle
    const winnerSucceeds = winnerIsPlayer ? playerSucceeds : monsterSucceeds
    const loserSucceeds = winnerIsPlayer ? monsterSucceeds : playerSucceeds

    if (winnerSucceeds) {
      applyTypeEffect(winnerIsPlayer ? 'player' : 'monster')
    } else if (loserSucceeds) {
      // Rattrapage: the loser still gets their own type's effect.
      applyTypeEffect(winnerIsPlayer ? 'monster' : 'player')
    }
  } else {
    // Clash — same action on both sides.
    if (playerSucceeds && monsterSucceeds) {
      resolveClash(player.action)
    } else if (playerSucceeds) {
      applyTypeEffect('player')
    } else if (monsterSucceeds) {
      applyTypeEffect('monster')
    }
  }

  function applyTypeEffect(side: 'player' | 'monster') {
    summary = side
    if (side === 'player') {
      const dmg = applyCrit(typeEffectDamage(player.action, playerStats), playerCrit)
      if (player.action === 'defense') {
        // Bloque tous les dégâts de l'attaquant + riposte.
        monsterDamageDealt = 0
        playerDamageDealt = applyCritFailDoubling(dmg, monsterCritFail)
      } else {
        playerDamageDealt = applyCritFailDoubling(dmg, monsterCritFail)
      }
    } else {
      const dmg = applyCrit(typeEffectDamage(monster.action, monsterStats), monsterCrit)
      if (monster.action === 'defense') {
        playerDamageDealt = 0
        monsterDamageDealt = applyCritFailDoubling(dmg, playerCritFail)
      } else {
        monsterDamageDealt = applyCritFailDoubling(dmg, playerCritFail)
      }
    }
  }

  function applyCritFailDoubling(damage: number, receiverCritFailed: boolean): number {
    return receiverCritFailed ? damage * 2 : damage
  }

  function resolveClash(action: ActionType) {
    summary = 'clash'
    if (action === 'attaque') {
      // Échange brutal : chacun encaisse Force propre × 0.4.
      monsterDamageDealt = applyCrit(playerStats.force * 0.4, playerCrit)
      playerDamageDealt = applyCrit(monsterStats.force * 0.4, monsterCrit)
    } else if (action === 'defense') {
      // Stalemate — rien ne se passe.
    } else {
      // Raillerie vs Raillerie : la meilleure marge de réussite l'emporte seule.
      const playerMargin = playerThreshold - player.die
      const monsterMargin = monsterThreshold - monster.die
      if (playerMargin > monsterMargin) {
        playerDamageDealt = applyCrit(playerStats.charisme * 0.35, playerCrit)
      } else if (monsterMargin > playerMargin) {
        monsterDamageDealt = applyCrit(monsterStats.charisme * 0.35, monsterCrit)
      }
      // Exact tie: no documented tiebreak in game-design.md — treated as no effect.
    }
  }

  return {
    // PV/dégâts sont des entiers dans game-design.md — arrondi ici, à la
    // frontière du moteur, pour que les formules internes restent exactes.
    playerDamageDealt: Math.round(playerDamageDealt),
    monsterDamageDealt: Math.round(monsterDamageDealt),
    playerCrit,
    monsterCrit,
    playerCritFail,
    monsterCritFail,
    summary,
  }
}
