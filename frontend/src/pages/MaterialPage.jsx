import { Link, useParams } from "react-router-dom";
import { courses } from "../data/courses";

function MaterialPage() {
  const { materialId } = useParams();

  const course = courses.find((item) =>
    item.modules.some((module) =>
      module.materials.some((material) => material.id === materialId)
    )
  );

  const material = course?.modules
    .flatMap((module) => module.materials)
    .find((item) => item.id === materialId);

  if (!material || !course) {
    return (
      <section className="page">
        <h1>Материалът не е намерен</h1>
        <Link to="/catalog">Обратно към каталога</Link>
      </section>
    );
  }

  return (
    <section className="page">
      <Link to={`/catalog/${course.id}`}>
        ← Обратно към курса
      </Link>

      <div className="card">
        <span className="badge">{material.type}</span>
        <h1 style={{ marginTop: "12px" }}>{material.title}</h1>
        <p>Курс: {course.title}</p>

        <p className="muted">
          Прегледът на учебното съдържание ще бъде реализиран в следваща задача.
        </p>
      </div>
    </section>
  );
}

export default MaterialPage;