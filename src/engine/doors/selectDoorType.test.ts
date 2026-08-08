import { describe, expect, it } from 'vitest'
import { selectDoorType, initialDoorSequenceState } from './selectDoorType'
import { createRng } from '../rng/mulberry32'

describe('selectDoorType (issue #14)', () => {
  it('door 1 is always forced to Monstre', () => {
    const { type } = selectDoorType(1, initialDoorSequenceState(), createRng(1))
    expect(type).toBe('monstre')
  })

  it('doors 10, 20, 30 are always boss doors, resetting the Monstre streak', () => {
    for (const doorNumber of [10, 20, 30]) {
      const state = { previousDoorType: 'monstre' as const, monsterStreak: 3 }
      const { type, nextState } = selectDoorType(doorNumber, state, createRng(1))
      expect(type).toBe('boss')
      expect(nextState.monsterStreak).toBe(0)
      expect(nextState.previousDoorType).toBe('boss')
    }
  })

  it('a cooldown type cannot repeat on the immediately following door', () => {
    const state = { previousDoorType: 'tresor' as const, monsterStreak: 0 }
    // Draw many times with different seeds — tresor should never come up again immediately.
    for (let seed = 0; seed < 200; seed++) {
      const { type } = selectDoorType(2, state, createRng(seed))
      expect(type).not.toBe('tresor')
    }
  })

  it('Monstre is capped at 4 consecutive — a 5th door is forced non-Monstre', () => {
    const state = { previousDoorType: 'monstre' as const, monsterStreak: 4 }
    for (let seed = 0; seed < 200; seed++) {
      const { type } = selectDoorType(7, state, createRng(seed))
      expect(type).not.toBe('monstre')
    }
  })

  it('Monstre streak increments on repeat, resets on any other type', () => {
    const state = { previousDoorType: 'monstre' as const, monsterStreak: 2 }
    // Find a seed that draws monstre and one that draws something else, and check streak bookkeeping either way.
    let sawIncrement = false
    let sawReset = false
    for (let seed = 0; seed < 50 && !(sawIncrement && sawReset); seed++) {
      const { type, nextState } = selectDoorType(7, state, createRng(seed))
      if (type === 'monstre') {
        expect(nextState.monsterStreak).toBe(3)
        sawIncrement = true
      } else {
        expect(nextState.monsterStreak).toBe(0)
        sawReset = true
      }
    }
    expect(sawIncrement).toBe(true)
    expect(sawReset).toBe(true)
  })
})
