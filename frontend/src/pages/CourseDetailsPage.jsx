import { Link, useParams } from "react-router-dom";
import { courses } from "../data/courses";

function CourseDetailsPage() {
  const { courseId } = useParams();

  const course = courses.find((item) => item.id === courseId);

  if (!course) {
    return (
      <section className="page">
        <h1>Курсът не е намерен</h1>
        <Link to="/catalog">Обратно към каталога</Link>
      </section>
    );
  }

  return (
    <section className="page">
      <Link to="/catalog">← Обратно към каталога</Link>

      <h1 style={{ marginTop: "16px" }}>{course.title}</h1>

      <p>{course.description}</p>

      <div className="card">
        <dl>
          <dt>Код на курса</dt>
          <dd>{course.code}</dd>

          <dt>Преподавател</dt>
          <dd>{course.teacher}</dd>

          <dt>Брой модули</dt>
          <dd>{course.modules.length}</dd>
        </dl>
      </div>

      <h2>Модули и учебни материали</h2>

      <div className="module-list">
        {course.modules.map((module) => (
          <article className="module-item" key={module.id}>
            <span className="badge">{module.week}</span>

            <h3>{module.title}</h3>

            <ul className="material-list">
              {module.materials.map((material) => (
                <li key={material.id}>
                  <Link to={`/materials/${material.id}`}>
                    {material.title}
                  </Link>
                  <span className="muted"> — {material.type}</span>
                </li>
              ))}
            </ul>
          </article>
        ))}
      </div>
    </section>
  );
}

export default CourseDetailsPage;