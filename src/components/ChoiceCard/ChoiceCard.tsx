interface ChoiceCardProps {
  title: string
  description: string
  onSelect: () => void
}

export function ChoiceCard({ title, description, onSelect }: ChoiceCardProps) {
  return (
    <button className="choice-card" onClick={onSelect} style={{ display: 'block', width: '100%', textAlign: 'left', marginBottom: 'var(--space-2)' }}>
      <strong>{title}</strong>
      <div style={{ fontSize: '0.85em', color: 'var(--color-text-muted)', marginTop: 'var(--space-1)' }}>{description}</div>
    </button>
  )
}
