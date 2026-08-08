import { startNewRun, useRunDispatch, useRunState } from '../../state/RunContext'

export function TitleScreen() {
  const state = useRunState()
  const dispatch = useRunDispatch()

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', gap: 'var(--space-4)' }}>
      <h1>Chouette Roguelite</h1>
      <button className="accent" onClick={() => startNewRun(dispatch)}>
        Jouer
      </button>
      {state.bestScore > 0 && <p>Meilleur score : {state.bestScore} porte{state.bestScore > 1 ? 's' : ''}</p>}
    </div>
  )
}
