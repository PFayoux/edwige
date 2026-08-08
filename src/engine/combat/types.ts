export type ActionType = 'attaque' | 'defense' | 'raillerie'

export interface ActionStats {
  force: number
  dexterite: number
  charisme: number
  chance: number
}

/** Recognized trait `type`s for MVP — issue #9's narrow, real hook. */
export type TraitType = 'flee_after_misses' | 'dodge_chance'

export interface Trait {
  type: TraitType
  /** flee_after_misses: number of consecutive misses before fleeing. */
  threshold?: number
  /** dodge_chance: dodges automatically every Nth confrontation it plays defense-adjacent. */
  every?: number
}

export interface Combatant {
  nom: string
  pv: number
  pvMax: number
  stats: ActionStats
  traits: Trait[]
}

export interface DiceAssignment {
  die: number
  action: ActionType
}

export interface ConfrontationOutcome {
  playerDamageDealt: number
  monsterDamageDealt: number
  playerCrit: boolean
  monsterCrit: boolean
  playerCritFail: boolean
  monsterCritFail: boolean
  /** Which side's action actually resolved with its full type-effect (for UI/summary). */
  summary: 'player' | 'monster' | 'clash' | 'none'
}

export const TRIANGLE_WINNER: Record<ActionType, ActionType> = {
  // key beats value
  defense: 'attaque',
  attaque: 'raillerie',
  raillerie: 'defense',
}

export function statFor(action: ActionType, stats: ActionStats): number {
  switch (action) {
    case 'attaque':
      return stats.force
    case 'defense':
      return stats.dexterite
    case 'raillerie':
      return stats.charisme
  }
}
