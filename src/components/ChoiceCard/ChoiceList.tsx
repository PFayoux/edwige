import { ChoiceCard } from './ChoiceCard'

export interface Choice {
  id: string
  title: string
  description: string
}

interface ChoiceListProps {
  choices: Choice[]
  onChoose: (id: string) => void
}

/** Shared by EventChoice and Reward screens — issue #5. */
export function ChoiceList({ choices, onChoose }: ChoiceListProps) {
  return (
    <div role="list" aria-label="Choix disponibles">
      {choices.map((choice) => (
        <ChoiceCard key={choice.id} title={choice.title} description={choice.description} onSelect={() => onChoose(choice.id)} />
      ))}
    </div>
  )
}
