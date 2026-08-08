import { ChoiceList } from '../../components/ChoiceCard/ChoiceList'
import { useRunDispatch, useRunState } from '../../state/RunContext'
import { restoreRng } from '../../engine/rng/mulberry32'

/**
 * Simplified event resolution for Piège/Malédiction/Marchand doors — the
 * full named effect tables in game-design.md's "Table d'objets" aren't
 * wired up individually (scope reduction); each door type gets one
 * representative generic outcome instead.
 */
export function EventChoiceScreen() {
  const state = useRunState()
  const dispatch = useRunDispatch()
  const doorType = state.activeDoor?.doorType

  if (doorType === 'marchand') {
    return (
      <div style={{ padding: 'var(--space-4)' }}>
        <h2>Marchand / Repos</h2>
        <ChoiceList
          choices={[
            { id: 'soin', title: 'Se reposer', description: 'Récupère 40 à 50% des PV max.' },
            { id: 'stat', title: 'Bonus de stat', description: '+1 point dans une statistique aléatoire.' },
          ]}
          onChoose={(id) => {
            if (id === 'soin') {
              const rng = restoreRng(state.rngState)
              const pct = 0.4 + rng.next() * 0.1
              dispatch({ type: 'RESOLVE_EVENT', pvDelta: Math.round(state.stats.pvMax * pct) })
            } else {
              dispatch({ type: 'RESOLVE_EVENT', statDelta: { force: state.stats.force + 1 } })
            }
            dispatch({ type: 'OPEN_NEXT_DOOR' })
          }}
        />
      </div>
    )
  }

  if (doorType === 'piege') {
    return (
      <div style={{ padding: 'var(--space-4)' }}>
        <h2>Piège</h2>
        <ChoiceList
          choices={[
            { id: 'risque', title: 'Forcer', description: 'Jet risqué : échec = dégâts, réussite = petit soin.' },
            { id: 'prudent', title: 'Laisser', description: 'Rien ne se passe.' },
          ]}
          onChoose={(id) => {
            if (id === 'risque') {
              const rng = restoreRng(state.rngState)
              const success = rng.next() > 0.5
              dispatch({ type: 'RESOLVE_EVENT', pvDelta: success ? 10 : -15 })
            } else {
              dispatch({ type: 'RESOLVE_EVENT' })
            }
            dispatch({ type: 'OPEN_NEXT_DOOR' })
          }}
        />
      </div>
    )
  }

  // malediction
  return (
    <div style={{ padding: 'var(--space-4)' }}>
      <h2>Malédiction / Autel</h2>
      <ChoiceList
        choices={[
          { id: 'activer', title: 'Activer', description: '+10 Force immédiat, -5 PV max.' },
          { id: 'ignorer', title: 'Ignorer', description: 'Rien ne se passe.' },
        ]}
        onChoose={(id) => {
          if (id === 'activer') {
            dispatch({ type: 'RESOLVE_EVENT', statDelta: { force: state.stats.force + 10, pvMax: state.stats.pvMax - 5 } })
          } else {
            dispatch({ type: 'RESOLVE_EVENT' })
          }
          dispatch({ type: 'OPEN_NEXT_DOOR' })
        }}
      />
    </div>
  )
}
