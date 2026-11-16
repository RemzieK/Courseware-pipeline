function EmptyState({
  title = "Няма намерени резултати",
  message = "Опитайте отново по-късно.",
}) {
  return (
    <div className="state-message">
      <h2>{title}</h2>
      <p>{message}</p>
    </div>
  );
}

export default EmptyState;