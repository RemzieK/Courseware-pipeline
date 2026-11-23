import { Routes, Route, Link } from "react-router-dom";
import MainLayout from "../components/layout/MainLayout";

import CatalogPage from "../pages/CatalogPage";
import CourseDetailsPage from "../pages/CourseDetailsPage";
import MaterialPage from "../pages/MaterialPage";
import SearchPage from "../pages/SearchPage";

function HomePage() {
  return (
    <section className="page">
      <h1>Добре дошли в UniCloud Portal</h1>

      <p>
        Вашата платформа за учебни курсове, материали и образователни ресурси.
      </p>

      <div className="dashboard-grid">
        <article className="dashboard-card">
          <h2>Учебен каталог</h2>
          <p>Разгледайте курсовете, модулите и учебните материали.</p>
          <Link to="/catalog">Отвори каталога →</Link>
        </article>

        <article className="dashboard-card">
          <h2>Търсене</h2>
          <p>Намерете необходимия учебен ресурс.</p>
          <Link to="/search">Към търсенето →</Link>
        </article>

        <article className="dashboard-card">
          <h2>Моите материали</h2>
          <p>Следете учебните материали, които използвате.</p>
          <Link to="/materials">Към материалите →</Link>
        </article>
      </div>
    </section>
  );
}

function PlaceholderPage({ title }) {
  return (
    <section className="page">
      <h1>{title}</h1>
      <p>Тази функционалност ще бъде реализирана в следващите задачи.</p>
    </section>
  );
}

function NotFoundPage() {
  return (
    <section className="page">
      <h1>404</h1>
      <p>Страницата не беше намерена.</p>
      <Link to="/">Към началната страница</Link>
    </section>
  );
}

function AppRoutes() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<HomePage />} />

        <Route path="/catalog" element={<CatalogPage />} />
        <Route path="/catalog/:courseId" element={<CourseDetailsPage />} />

        <Route path="/materials" element={<PlaceholderPage title="Учебни материали" />} />
        <Route path="/materials/:materialId" element={<MaterialPage />} />

        <Route path="/search" element={<SearchPage />} />
        <Route path="/teacher" element={<PlaceholderPage title="Преподавателски панел" />} />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

export default AppRoutes;