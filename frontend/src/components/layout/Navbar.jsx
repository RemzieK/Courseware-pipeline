
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header className="navbar">
      <NavLink to="/" className="logo">
        UniCloud Portal
      </NavLink>

      <nav className="nav-links">
        <NavLink to="/" end>
          Начало
        </NavLink>

        <NavLink to="/catalog">
          Каталог
        </NavLink>

        <NavLink to="/search">
          Търсене
        </NavLink>

        <NavLink to="/materials">
          Материали
        </NavLink>

        <NavLink to="/teacher">
          Преподаватели
        </NavLink>

        {isAuthenticated ? (
          <>
            <span className="nav-user">
              {user?.username || user?.email || user?.role}
            </span>

            <button
              type="button"
              className="nav-auth-button"
              onClick={handleLogout}
            >
              Изход
            </button>
          </>
        ) : (
          <NavLink to="/login" className="nav-login">
            Вход
          </NavLink>
        )}
      </nav>
    </header>
  );
}

export default Navbar;
