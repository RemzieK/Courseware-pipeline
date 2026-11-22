
import { NavLink } from "react-router-dom";

function Navbar() {
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
      </nav>
    </header>
  );
}

export default Navbar;
