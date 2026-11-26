
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../services/api";
import MaterialCreateForm from "../components/materials/MaterialCreateForm";
import { useAuth } from "../context/AuthContext";

function CourseDetailsPage() {
  const { courseId } = useParams();
  const { user } = useAuth();

  const canManageMaterials =
    user?.role === "TEACHER" || user?.role === "ADMIN";

  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeLessonId, setActiveLessonId] = useState(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadCourse() {
      try {
        setLoading(true);
        setError("");

        const response = await api.get(`/courses/${courseId}`, {
          signal: controller.signal,
        });

        const courseData = response.data;

        const modulesWithMaterials = await Promise.all(
          (courseData.modules ?? []).map(async (module) => {
            const lessonsWithMaterials = await Promise.all(
              (module.lessons ?? []).map(async (lesson) => {
                try {
                  const materialsResponse = await api.get(
                    `/lessons/${lesson.id}/materials`,
                    { signal: controller.signal }
                  );

                  return {
                    ...lesson,
                    materials: materialsResponse.data ?? [],
                  };
                } catch (err) {
                  if (err.code === "ERR_CANCELED") {
                    throw err;
                  }

                  return {
                    ...lesson,
                    materials: [],
                    materialsLoadError: true,
                  };
                }
              })
            );

            return {
              ...module,
              lessons: lessonsWithMaterials,
            };
          })
        );

        setCourse({
          ...courseData,
          modules: modulesWithMaterials,
        });
      } catch (err) {
        if (err.code !== "ERR_CANCELED") {
          setError(
            err.response?.data?.message ||
              "Данните за курса не могат да бъдат заредени."
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadCourse();

    return () => controller.abort();
  }, [courseId]);

  function handleMaterialCreated(lessonId, material) {
    setCourse((currentCourse) => ({
      ...currentCourse,
      modules: currentCourse.modules.map((module) => ({
        ...module,
        lessons: module.lessons.map((lesson) =>
          lesson.id === lessonId
            ? {
                ...lesson,
                materials: [...(lesson.materials ?? []), material],
              }
            : lesson
        ),
      })),
    }));

    setActiveLessonId(null);
  }

  if (loading) {
    return (
      <section className="page">
        <p>Зареждане на курса...</p>
      </section>
    );
  }

  if (error || !course) {
    return (
      <section className="page">
        <h1>Курсът не е намерен</h1>
        <p role="alert">{error || "Няма данни за този курс."}</p>
        <Link to="/catalog">Обратно към каталога</Link>
      </section>
    );
  }

  return (
    <section className="page">
      <Link to="/catalog">← Обратно към каталога</Link>

      <h1 style={{ marginTop: "16px" }}>{course.title}</h1>

      {course.description && <p>{course.description}</p>}

      <div className="card">
        <dl>
          <dt>Код на курса</dt>
          <dd>{course.code}</dd>

          <dt>Дисциплина</dt>
          <dd>{course.discipline || "Не е посочена"}</dd>

          <dt>Преподавател</dt>
          <dd>{course.teacherName || "Не е посочен"}</dd>

          <dt>Семестър</dt>
          <dd>{course.semester || "Не е посочен"}</dd>

          <dt>Брой модули</dt>
          <dd>{course.modules?.length ?? 0}</dd>
        </dl>
      </div>

      <h2>Модули и учебни материали</h2>

      {course.modules?.length ? (
        <div className="module-list">
          {course.modules.map((module) => (
            <article className="module-item" key={module.id}>
              <span className="badge">
                Модул {(module.orderIndex ?? 0) + 1}
              </span>

              <h3>{module.title}</h3>

              {module.description && <p>{module.description}</p>}

              {module.lessons?.length ? (
                module.lessons.map((lesson) => (
                  <div className="lesson-item" key={lesson.id}>
                    <h4>{lesson.title}</h4>

                    {lesson.description && <p>{lesson.description}</p>}

                    {lesson.materialsLoadError ? (
                      <p role="alert">
                        Материалите за това занятие не могат да бъдат заредени.
                      </p>
                    ) : lesson.materials?.length ? (
                      <ul className="material-list">
                        {lesson.materials.map((material) => (
                          <li key={material.id}>
                            <Link to={`/materials/${material.id}`}>
                              {material.title}
                            </Link>
                            <span className="muted">
                              {" "}— {material.type}
                            </span>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="muted">
                        Няма добавени материали.
                      </p>
                    )}

                    {canManageMaterials && (
                      <>
                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() =>
                            setActiveLessonId(
                              activeLessonId === lesson.id
                                ? null
                                : lesson.id
                            )
                          }
                        >
                          {activeLessonId === lesson.id
                            ? "Затвори формата"
                            : "Добави материал"}
                        </button>

                        {activeLessonId === lesson.id && (
                          <MaterialCreateForm
                            lessonId={lesson.id}
                            onCreated={(material) =>
                              handleMaterialCreated(lesson.id, material)
                            }
                            onCancel={() => setActiveLessonId(null)}
                          />
                        )}
                      </>
                    )}
                  </div>
                ))
              ) : (
                <p className="muted">Няма добавени занятия.</p>
              )}
            </article>
          ))}
        </div>
      ) : (
        <p>В този курс все още няма добавени модули.</p>
      )}
    </section>
  );
}

export default CourseDetailsPage;
