
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../services/api";

function MaterialPage() {
  const { materialId } = useParams();

  const [material, setMaterial] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    async function loadMaterial() {
      try {
        setLoading(true);
        setError("");

        const response = await api.get(`/materials/${materialId}`, {
          signal: controller.signal,
        });

        setMaterial(response.data);
      } catch (err) {
        if (err.code !== "ERR_CANCELED") {
          setError(
            err.response?.data?.message ||
              "Материалът не може да бъде зареден."
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadMaterial();

    return () => controller.abort();
  }, [materialId]);

  if (loading) {
    return (
      <section className="page">
        <p>Зареждане на материала...</p>
      </section>
    );
  }

  if (error || !material) {
    return (
      <section className="page">
        <h1>Материалът не е намерен</h1>
        <p role="alert">{error || "Няма данни за този материал."}</p>
        <Link to="/catalog">Към каталога</Link>
      </section>
    );
  }

  return (
    <section className="page">
      <Link to="/catalog">← Обратно към каталога</Link>

      <div className="card">
        <span className="badge">{material.type}</span>

        <h1>{material.title}</h1>

        {material.description && <p>{material.description}</p>}

        <p>Код на курса: {material.courseCode || "Не е посочен"}</p>

        {material.tags?.length > 0 && (
          <p>Теми: {material.tags.join(", ")}</p>
        )}

        {material.type === "VIDEO_LINK" && material.videoUrl && (
          <p>
            <a
              href={material.videoUrl}
              target="_blank"
              rel="noreferrer"
            >
              Отвори видеото
            </a>
          </p>
        )}

        {material.fileUrl && (
          <p>
            <a
              href={material.fileUrl}
              target="_blank"
              rel="noreferrer"
            >
              Отвори файла
            </a>
          </p>
        )}

        <p className="muted">
          Текуща версия: {material.currentVersion}
        </p>
      </div>
    </section>
  );
}

export default MaterialPage;
