import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";

function LoginPage() {
  const navigate = useNavigate();
  const { user, login, logout } = useAuth();

  const [username, setUsername] = useState("teacher1");
  const [role, setRole] = useState("TEACHER");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      setLoading(true);

      const response = await api.post("/auth/token", {
        username: username.trim(),
        role,
      });

      login(response.data, username.trim());
      navigate("/catalog");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Неуспешно получаване на токен. Проверете дали backend-ът работи."
      );
    } finally {
      setLoading(false);
    }
  }

  if (user) {
    return (
      <section className="page">
        <div className="card auth-card">
          <h1>Вече сте влезли</h1>
          <p><strong>Потребител:</strong> {user.username}</p>
          <p><strong>Имейл:</strong> {user.email}</p>
          <p><strong>Роля:</strong> {user.role}</p>
          <p>
            <strong>Валидност до:</strong>{" "}
            {new Date(user.expiresAt).toLocaleString("bg-BG")}
          </p>

          <div className="auth-actions">
            <Link to="/catalog" className="auth-link-button">
              Към каталога
            </Link>
            <button type="button" onClick={logout}>
              Изход
            </button>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="page">
      <div className="card auth-card">
        <h1>Вход в UniCloud</h1>
        <p className="muted">
          Генериране на тестов токен за разработка.
        </p>

        <form className="material-form" onSubmit={handleSubmit}>
          <label>
            Потребителско име
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </label>

          <label>
            Роля
            <select
              value={role}
              onChange={(event) => setRole(event.target.value)}
            >
              <option value="STUDENT">Студент</option>
              <option value="TEACHER">Преподавател</option>
              <option value="ADMIN">Администратор</option>
            </select>
          </label>

          {error && (
            <p className="material-form-error" role="alert">
              {error}
            </p>
          )}

          <button type="submit" disabled={loading}>
            {loading ? "Получаване на токен..." : "Вход"}
          </button>
        </form>

        <p className="muted auth-note">
          Тестовият токен е валиден 30 минути.
        </p>
      </div>
    </section>
  );
}

export default LoginPage;