import { describe, expect, it } from 'vitest'
import { resolveSalvo, initialTraitState } from './resolveSalvo'
import type { ActionStats, DiceAssignment, Trait } from './types'

const stats: ActionStats = { force: 60, dexterite: 60, charisme: 60, chance: 10 }

// Player wins the triangle cleanly each confrontation (attaque > raillerie), dealing 24 and taking 0.
const playerWins: DiceAssignment[] = [
  { die: 40, action: 'attaque' },
  { die: 40, action: 'attaque' },
  { die: 40, action: 'attaque' },
]
const monsterLoses: DiceAssignment[] = [
  { die: 50, action: 'raillerie' },
  { die: 50, action: 'raillerie' },
  { die: 50, action: 'raillerie' },
]

describe('resolveSalvo — trait hooks (issue #9)', () => {
  it('flee_after_misses: Souris Anxieuse flees after 2 consecutive misses, stopping the salvo early', () => {
    const traits: Trait[] = [{ type: 'flee_after_misses', threshold: 2 }]
    const result = resolveSalvo(playerWins, stats, 50, monsterLoses, stats, 100, traits, initialTraitState())

    expect(result.monsterFled).toBe(true)
    expect(result.confrontations).toHaveLength(2)
  })

  it('dodge_chance: Renard Rusé dodges every 3rd confrontation, negating damage that turn', () => {
    const traits: Trait[] = [{ type: 'dodge_chance', every: 3 }]
    const result = resolveSalvo(playerWins, stats, 50, monsterLoses, stats, 100, traits, initialTraitState())

    // 24 dealt, 24 dealt, 3rd dodged (0 dealt) — monster ends at 100 - 24 - 24 = 52.
    expect(result.monsterPv).toBe(52)
    expect(result.confrontations[2].playerDamageDealt).toBe(0)
  })
})
