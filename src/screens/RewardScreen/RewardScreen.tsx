import { ChoiceList } from '../../components/ChoiceCard/ChoiceList'
import { useRunDispatch } from '../../state/RunContext'
import itemsData from '../../data/items.json'
import type { ItemContent } from '../../data/types'

const items = itemsData as ItemContent[]

/** After every combat win except the tier-3 victory (which skips this — #18). */
export function RewardScreen() {
  const dispatch = useRunDispatch()

  return (
    <div style={{ padding: 'var(--space-4)' }}>
      <h2>Récompense</h2>
      <ChoiceList
        choices={[
          { id: 'point', title: '1 point de stat', description: 'Répartis librement un point (Force, Dextérité, Charisme, Chance, ou +5 PV max).' },
          ...items.map((item) => ({ id: `item:${item.id}`, title: item.nom, description: item.description })),
        ]}
        onChoose={(id) => {
          if (id === 'point') {
            dispatch({ type: 'APPLY_REWARD_STAT', stat: 'force' })
          } else {
            dispatch({ type: 'APPLY_REWARD_ITEM', itemId: id.replace('item:', '') })
          }
          dispatch({ type: 'OPEN_NEXT_DOOR' })
        }}
      />
    </div>
  )
}
