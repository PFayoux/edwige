import monstersData from '../../data/monsters.json'
import bossesData from '../../data/bosses.json'
import type { BossContent, MonsterContent } from '../../data/types'
import type { CombatMonster } from '../../state/CombatContext'

const monsters = monstersData as MonsterContent[]
const bosses = bossesData as BossContent[]

export function getEncounterMonster(monsterId: string): CombatMonster {
  const content = monsters.find((m) => m.id === monsterId) ?? bosses.find((b) => b.id === monsterId)
  if (!content) throw new Error(`Unknown monster id: ${monsterId}`)
  return {
    nom: content.nom,
    pv: content.pv,
    stats: { force: content.attaque, dexterite: content.defense, charisme: content.attaque, chance: 0 },
    traits: content.traits,
  }
}
