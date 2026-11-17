function ErrorState({
  title = "Възникна грешка",
  message = "Моля, опитайте отново.",
  onRetry,
}) {
  return (
    <div className="state-message error-state" role="alert">
      <h2>{title}</h2>
      <p>{message}</p>

      {onRetry && (
        <button type="button" onClick={onRetry}>
          Опитай отново
        </button>
      )}
    </div>
  );
}

export default ErrorState;