import { useEffect } from 'react'
import { useRunDispatch, useRunState } from '../../state/RunContext'
import { CombatProvider, useCombatDispatch, useCombatState } from '../../state/CombatContext'
import { getEncounterMonster } from './getEncounterMonster'
import { ItemTray } from '../../components/ItemTray/ItemTray'
import type { ActionType } from '../../engine/combat/types'

const ACTIONS: { action: ActionType; label: string }[] = [
  { action: 'attaque', label: 'Attaque' },
  { action: 'defense', label: 'Défense' },
  { action: 'raillerie', label: 'Raillerie' },
]

export function CombatScreen() {
  const runState = useRunState()
  const monsterId = runState.activeDoor?.monsterId
  if (!monsterId) return null
  const monster = getEncounterMonster(monsterId)

  return (
    <CombatProvider monster={monster} playerStats={runState.stats} playerPv={runState.stats.pv} playerPvMax={runState.stats.pvMax} rngState={runState.rngState}>
      <CombatBody />
    </CombatProvider>
  )
}

function CombatBody() {
  const combat = useCombatState()
  const combatDispatch = useCombatDispatch()
  const runDispatch = useRunDispatch()

  useEffect(() => {
    if (combat.outcome === 'win' || combat.outcome === 'fled') {
      runDispatch({ type: 'WIN_COMBAT' })
    } else if (combat.outcome === 'lose') {
      runDispatch({ type: 'LOSE_COMBAT' })
    }
  }, [combat.outcome, runDispatch])

  return (
    <div style={{ padding: 'var(--space-4)' }}>
      <h2>{combat.monster.nom}</h2>
      <p>PV monstre : {Math.max(0, combat.monsterPv)}</p>
      <p>PV chouette : {Math.max(0, combat.playerPv)}</p>

      {combat.phase === 'assigning' && (
        <div>
          <p>Assigne une action à chaque dé, dans l'ordre où tu veux les résoudre :</p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
            {combat.playerDice.map((die, i) => {
              const assigned = combat.assignments.find((a) => a.die === die)
              return (
                <div key={i} style={{ border: '2px solid var(--color-border)', borderRadius: 'var(--radius)', padding: 'var(--space-2)' }}>
                  <div>Dé : {die}</div>
                  {assigned ? (
                    <div>{ACTIONS.find((a) => a.action === assigned.action)?.label}</div>
                  ) : (
                    <div style={{ display: 'flex', gap: 'var(--space-1)' }}>
                      {ACTIONS.map(({ action, label }) => (
                        <button key={action} onClick={() => combatDispatch({ type: 'ASSIGN_DIE', dieIndex: i, action })}>
                          {label}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
          {combat.assignments.length > 0 && <button onClick={() => combatDispatch({ type: 'UNASSIGN_LAST' })}>Annuler le dernier</button>}
          <div style={{ marginTop: 'var(--space-2)' }}>
            <button className="accent" disabled={combat.assignments.length !== 5} onClick={() => combatDispatch({ type: 'RESOLVE_SALVO' })}>
              Résoudre la salve
            </button>
          </div>
        </div>
      )}

      {combat.phase === 'resolved' && (
        <div>
          <h3>Résultat de la salve</h3>
          <ol>
            {combat.lastConfrontations.map((c, i) => (
              <li key={i} aria-label={`Confrontation ${i + 1} : ${c.summary === 'none' ? 'aucun effet' : c.summary}, ${c.playerDamageDealt} dégâts infligés, ${c.monsterDamageDealt} dégâts subis${c.playerCrit ? ', critique' : ''}${c.playerCritFail ? ', échec critique' : ''}`}>
                {c.summary} — infligé {c.playerDamageDealt}, subi {c.monsterDamageDealt}
                {c.playerCrit && ' (critique !)'}
                {c.playerCritFail && ' (échec critique !)'}
              </li>
            ))}
          </ol>
          {combat.outcome === 'ongoing' && (
            <button className="accent" onClick={() => combatDispatch({ type: 'ROLL_SALVO' })}>
              Salve suivante
            </button>
          )}
        </div>
      )}

      <ItemTray />
    </div>
  )
}
