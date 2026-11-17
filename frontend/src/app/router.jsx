import { Routes, Route } from "react-router-dom";
import MainLayout from "../components/layout/MainLayout";

function HomePage() {
  return (
    <section className="page">
      <h1>Добре дошли в UniCloud</h1>
      <p>
        Вашата платформа за учебни материали, курсове и образователни ресурси.
      </p>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h2>Учебен каталог</h2>
          <p>Разгледайте наличните курсове и учебни материали.</p>
        </div>

        <div className="dashboard-card">
          <h2>Търсене</h2>
          <p>Намерете необходимия учебен ресурс.</p>
        </div>

        <div className="dashboard-card">
          <h2>Моите материали</h2>
          <p>Следете материалите, които използвате.</p>
        </div>
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
    </section>
  );
}

function AppRoutes() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route
          path="/catalog"
          element={<PlaceholderPage title="Учебен каталог" />}
        />
        <Route
          path="/search"
          element={<PlaceholderPage title="Търсене на материали" />}
        />
        <Route
          path="/materials"
          element={<PlaceholderPage title="Учебни материали" />}
        />
        <Route
          path="/teacher"
          element={<PlaceholderPage title="Преподавателски панел" />}
        />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

export default AppRoutes;