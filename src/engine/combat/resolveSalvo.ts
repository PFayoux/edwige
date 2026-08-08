import type { ActionStats, ConfrontationOutcome, DiceAssignment, Trait } from './types'
import { resolveConfrontation } from './resolveConfrontation'

export interface TraitState {
  /** For flee_after_misses: consecutive monster-roll failures so far this combat. */
  consecutiveMonsterMisses: number
  /** For dodge_chance: how many confrontations have been played this combat. */
  confrontationsPlayed: number
}

export function initialTraitState(): TraitState {
  return { consecutiveMonsterMisses: 0, confrontationsPlayed: 0 }
}

export interface SalvoResult {
  confrontations: ConfrontationOutcome[]
  playerPv: number
  monsterPv: number
  /** True if a `flee_after_misses` trait triggered — combat ends, monster counted as defeated. */
  monsterFled: boolean
  traitState: TraitState
}

/**
 * Resolves one salvo: up to 5 confrontations, player's dice/actions/order vs
 * monster's own 5 dice/actions/order (already rolled — RNG injection happens
 * one level up, per #3). Stops early on death or a flee trigger.
 */
export function resolveSalvo(
  playerOrder: DiceAssignment[],
  playerStats: ActionStats,
  playerPv: number,
  monsterOrder: DiceAssignment[],
  monsterStats: ActionStats,
  monsterPv: number,
  traits: Trait[],
  traitState: TraitState,
): SalvoResult {
  const confrontations: ConfrontationOutcome[] = []
  let hpPlayer = playerPv
  let hpMonster = monsterPv
  let state = { ...traitState }
  let monsterFled = false

  const fleeTrait = traits.find((t): t is Trait & { type: 'flee_after_misses' } => t.type === 'flee_after_misses')
  const dodgeTrait = traits.find((t): t is Trait & { type: 'dodge_chance' } => t.type === 'dodge_chance')

  for (let i = 0; i < Math.min(playerOrder.length, monsterOrder.length); i++) {
    const outcome = resolveConfrontation(playerOrder[i], playerStats, monsterOrder[i], monsterStats)

    state.confrontationsPlayed += 1

    // dodge_chance (Renard Rusé, issue #9): dodges 1 confrontation in 3 —
    // negates damage the monster would have taken this confrontation.
    let playerDamageDealt = outcome.playerDamageDealt
    if (dodgeTrait && state.confrontationsPlayed % (dodgeTrait.every ?? 3) === 0) {
      playerDamageDealt = 0
    }

    hpMonster = Math.max(0, hpMonster - playerDamageDealt)
    hpPlayer = Math.max(0, hpPlayer - outcome.monsterDamageDealt)
    confrontations.push({ ...outcome, playerDamageDealt })

    // flee_after_misses (Souris Anxieuse, issue #9): monster's own die failed
    // its own threshold this confrontation (it dealt no damage AND didn't
    // land a catch-up hit) counts as a "miss" for this trait.
    if (fleeTrait) {
      const monsterMissed = outcome.monsterDamageDealt === 0 && outcome.summary !== 'monster' && outcome.summary !== 'clash'
      state.consecutiveMonsterMisses = monsterMissed ? state.consecutiveMonsterMisses + 1 : 0
      if (state.consecutiveMonsterMisses >= (fleeTrait.threshold ?? 2)) {
        monsterFled = true
        break
      }
    }

    if (hpPlayer <= 0 || hpMonster <= 0) break
  }

  return { confrontations, playerPv: hpPlayer, monsterPv: hpMonster, monsterFled, traitState: state }
}
