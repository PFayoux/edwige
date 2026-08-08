import { describe, expect, it } from 'vitest'
import { resolveConfrontation } from './resolveConfrontation'
import type { ActionStats } from './types'

const baseStats: ActionStats = { force: 60, dexterite: 60, charisme: 60, chance: 10 }
const masteryStats: ActionStats = { ...baseStats, force: 80 }

describe('resolveConfrontation', () => {
  it('triangle win, winner succeeds: attaque beats raillerie', () => {
    const result = resolveConfrontation({ die: 40, action: 'attaque' }, baseStats, { die: 50, action: 'raillerie' }, baseStats)
    expect(result.summary).toBe('player')
    expect(result.playerDamageDealt).toBe(24) // Force 60 * 0.4
    expect(result.monsterDamageDealt).toBe(0)
  })

  it('rattrapage: winner fails, loser still lands its own type effect', () => {
    // Player plays attaque (loses the triangle to defense) but rolls low enough to succeed anyway.
    const result = resolveConfrontation({ die: 30, action: 'attaque' }, baseStats, { die: 90, action: 'defense' }, baseStats)
    expect(result.summary).toBe('player')
    expect(result.playerDamageDealt).toBe(24)
    expect(result.monsterDamageDealt).toBe(0)
  })

  it('clash attaque vs attaque: both succeed, both take Force propre x 0.4', () => {
    const result = resolveConfrontation({ die: 20, action: 'attaque' }, baseStats, { die: 20, action: 'attaque' }, baseStats)
    expect(result.summary).toBe('clash')
    expect(result.playerDamageDealt).toBe(24)
    expect(result.monsterDamageDealt).toBe(24)
  })

  it('clash defense vs defense: stalemate, nothing happens', () => {
    const result = resolveConfrontation({ die: 20, action: 'defense' }, baseStats, { die: 20, action: 'defense' }, baseStats)
    expect(result.summary).toBe('clash')
    expect(result.playerDamageDealt).toBe(0)
    expect(result.monsterDamageDealt).toBe(0)
  })

  it('clash raillerie vs raillerie: best margin wins alone', () => {
    const result = resolveConfrontation({ die: 25, action: 'raillerie' }, baseStats, { die: 35, action: 'raillerie' }, baseStats)
    expect(result.summary).toBe('clash')
    expect(result.playerDamageDealt).toBe(21) // Charisme 60 * 0.35
    expect(result.monsterDamageDealt).toBe(0)
  })

  it('critique: die within the crit band doubles damage', () => {
    const result = resolveConfrontation({ die: 10, action: 'attaque' }, baseStats, { die: 50, action: 'raillerie' }, baseStats)
    expect(result.playerCrit).toBe(true)
    expect(result.playerDamageDealt).toBe(48) // 24 * 2
  })

  it('échec critique: receiver taking damage while crit-failed doubles it', () => {
    const result = resolveConfrontation({ die: 40, action: 'attaque' }, baseStats, { die: 95, action: 'raillerie' }, baseStats)
    expect(result.monsterCritFail).toBe(true)
    expect(result.playerDamageDealt).toBe(48) // 24 doubled by the receiver's crit-fail
  })

  it('bonus de maîtrise: stat >= 80 adds +20% damage', () => {
    const result = resolveConfrontation({ die: 50, action: 'attaque' }, masteryStats, { die: 50, action: 'raillerie' }, baseStats)
    expect(result.playerDamageDealt).toBeCloseTo(38.4) // 80 * 0.4 * 1.2
  })

  it('both fail: nothing happens', () => {
    const result = resolveConfrontation({ die: 90, action: 'attaque' }, baseStats, { die: 90, action: 'raillerie' }, baseStats)
    expect(result.summary).toBe('none')
    expect(result.playerDamageDealt).toBe(0)
    expect(result.monsterDamageDealt).toBe(0)
  })
})
