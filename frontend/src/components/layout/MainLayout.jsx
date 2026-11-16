import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";

function MainLayout() {
  return (
    <div className="app">
      <Navbar />

      <main className="main-content">
        <Outlet />
      </main>

      <footer className="footer">
        <p>© {new Date().getFullYear()} UniCloud. Всички права запазени.</p>
      </footer>
    </div>
  );
}

export default MainLayout;