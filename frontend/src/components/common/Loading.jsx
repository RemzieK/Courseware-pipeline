function Loading({ message = "Зареждане..." }) {
  return (
    <div className="state-message" role="status">
      <div className="spinner" />
      <p>{message}</p>
    </div>
  );
}

export default Loading;