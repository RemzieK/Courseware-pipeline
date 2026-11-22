import { Link } from "react-router-dom";
import { courses } from "../data/courses";

function CatalogPage() {
  return (
    <section className="page">
      <h1>Учебен каталог</h1>
      <p>Разгледайте наличните курсове и техните учебни материали.</p>

      <div className="course-list">
        {courses.map((course) => (
          <article className="course-item" key={course.id}>
            <span className="badge">{course.code}</span>

            <h2>{course.title}</h2>

            <p>{course.description}</p>
            <p>Преподавател: {course.teacher}</p>
            <p>Модули: {course.modules.length}</p>

            <Link to={`/catalog/${course.id}`}>
              Разгледай курса
            </Link>
          </article>
        ))}
      </div>
    </section>
  );
}

export default CatalogPage;