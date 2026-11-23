
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

function CatalogPage() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    async function loadCourses() {
      try {
        setLoading(true);
        setError("");

        const response = await api.get("/courses", {
          params: {
            page: 0,
            size: 100,
            archived: false,
          },
          signal: controller.signal,
        });

        setCourses(response.data.content ?? []);
      } catch (err) {
        if (err.code !== "ERR_CANCELED") {
          setError(
            err.response?.data?.message ||
              "Курсовете не могат да бъдат заредени."
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadCourses();

    return () => controller.abort();
  }, []);

  if (loading) {
    return (
      <section className="page">
        <h1>Учебен каталог</h1>
        <p>Зареждане на курсовете...</p>
      </section>
    );
  }

  if (error) {
    return (
      <section className="page">
        <h1>Учебен каталог</h1>
        <p role="alert">{error}</p>
      </section>
    );
  }

  return (
    <section className="page">
      <h1>Учебен каталог</h1>
      <p>Разгледайте наличните курсове и техните учебни материали.</p>

      {courses.length === 0 ? (
        <p>Няма налични курсове.</p>
      ) : (
        <div className="course-list">
          {courses.map((course) => (
            <article className="course-item" key={course.id}>
              <span className="badge">{course.code}</span>

              <h2>{course.title}</h2>

              {course.description && <p>{course.description}</p>}

              <p>Дисциплина: {course.discipline || "Не е посочена"}</p>
              <p>Преподавател: {course.teacherName || "Не е посочен"}</p>
              <p>Семестър: {course.semester || "Не е посочен"}</p>

              <Link to={`/catalog/${course.id}`}>
                Разгледай курса
              </Link>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default CatalogPage;
