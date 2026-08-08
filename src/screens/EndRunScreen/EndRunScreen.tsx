import { startNewRun, useRunDispatch, useRunState } from '../../state/RunContext'

/**
 * Death and Victory share this screen via `runOutcome` (issue #23) — the
 * victory content decision (#19) drops the score line and skips straight
 * here from the tier-3 boss kill.
 */
export function EndRunScreen() {
  const state = useRunState()
  const dispatch = useRunDispatch()
  const isVictory = state.runOutcome === 'victory'

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', gap: 'var(--space-4)', padding: 'var(--space-6)' }}>
      <h2>{isVictory ? 'Nid conquis' : 'Envol interrompu'}</h2>

      {!isVictory && (
        <>
          <p>Score : {state.doorsCleared} porte{state.doorsCleared > 1 ? 's' : ''}</p>
          <p>
            Meilleur score : {state.bestScore} {state.doorsCleared >= state.bestScore && state.doorsCleared > 0 ? '— Nouveau record !' : ''}
          </p>
        </>
      )}

      <div>
        <h3>Récap du build</h3>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li>Force : {state.stats.force}</li>
          <li>Dextérité : {state.stats.dexterite}</li>
          <li>Charisme : {state.stats.charisme}</li>
          <li>Chance : {state.stats.chance}</li>
          <li>PV max : {state.stats.pvMax}</li>
        </ul>
        {state.permanentItems.length > 0 && (
          <>
            <h3>Objets permanents</h3>
            <ul>{state.permanentItems.map((id, i) => <li key={i}>{id}</li>)}</ul>
          </>
        )}
      </div>

      <button className="accent" onClick={() => startNewRun(dispatch)}>
        Rejouer
      </button>
    </div>
  )
}
