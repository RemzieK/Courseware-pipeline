
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";

const MAX_FILE_SIZE = 50 * 1024 * 1024;

const ALLOWED_EXTENSIONS = [
  "pdf",
  "ppt",
  "pptx",
  "doc",
  "docx",
  "png",
  "jpg",
  "jpeg",
];

function MaterialPage() {
  const { materialId } = useParams();
  const { user } = useAuth();

  const canManageMaterial =
    user?.role === "TEACHER" || user?.role === "ADMIN";

  const [material, setMaterial] = useState(null);
  const [versions, setVersions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [editing, setEditing] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [tags, setTags] = useState("");
  const [videoUrl, setVideoUrl] = useState("");
  const [hidden, setHidden] = useState(false);

  const [newVersionFile, setNewVersionFile] = useState(null);
  const [saving, setSaving] = useState(false);

  async function loadMaterial(signal) {
    const [materialResponse, versionsResponse] = await Promise.all([
      api.get(`/materials/${materialId}`, { signal }),
      api.get(`/materials/${materialId}/versions`, { signal }),
    ]);

    const data = materialResponse.data;

    setMaterial(data);
    setVersions(versionsResponse.data ?? []);
    setTitle(data.title ?? "");
    setDescription(data.description ?? "");
    setTags((data.tags ?? []).join(", "));
    setVideoUrl(data.videoUrl ?? "");
    setHidden(Boolean(data.hidden));
  }

  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      try {
        setLoading(true);
        setError("");

        await loadMaterial(controller.signal);
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

    load();

    return () => controller.abort();
  }, [materialId]);

  async function handleUpdate(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!title.trim()) {
      setError("Заглавието е задължително.");
      return;
    }

    try {
      setSaving(true);

      const updatedData = {
        title: title.trim(),
        description: description.trim(),
        videoUrl:
          material.type === "VIDEO_LINK" ? videoUrl.trim() : null,
        tags: tags
          .split(",")
          .map((tag) => tag.trim())
          .filter(Boolean),
        hidden,
      };

      const response = await api.patch(
        `/materials/${materialId}`,
        updatedData
      );

      setMaterial((current) => ({
        ...current,
        ...updatedData,
        ...(response.data ?? {}),
      }));

      setEditing(false);
      setSuccess("Промените са запазени.");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          (err.response?.status === 403
            ? "Нямате права да редактирате този материал."
            : "Промените не можаха да бъдат запазени.")
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleUploadVersion(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!newVersionFile) {
      setError("Изберете файл за новата версия.");
      return;
    }

    const extension = newVersionFile.name
      .split(".")
      .pop()
      .toLowerCase();

    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      setError("Неподдържан файлов формат.");
      return;
    }

    if (newVersionFile.size > MAX_FILE_SIZE) {
      setError("Файлът не трябва да надвишава 50 MB.");
      return;
    }

    const formData = new FormData();
    formData.append("file", newVersionFile);

    try {
      setSaving(true);

      await api.post(
        `/materials/${materialId}/versions`,
        formData
      );

      setNewVersionFile(null);

      await loadMaterial();

      setSuccess("Новата версия е качена успешно.");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          (err.response?.status === 403
            ? "Нямате права да качвате нови версии."
            : "Новата версия не можа да бъде качена.")
      );
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <section className="page">
        <p>Зареждане на материала...</p>
      </section>
    );
  }

  if (error && !material) {
    return (
      <section className="page">
        <h1>Материалът не е намерен</h1>
        <p role="alert">{error}</p>
        <Link to="/catalog">Към каталога</Link>
      </section>
    );
  }

  if (!material) {
    return null;
  }

  return (
    <section className="page">
      <Link to="/catalog">← Обратно към каталога</Link>

      <div className="card material-details">
        <span className="badge">{material.type}</span>

        <h1>{material.title}</h1>

        {material.description && <p>{material.description}</p>}

        <p>
          Код на курса: {material.courseCode || "Не е посочен"}
        </p>

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
              Отвори текущия файл
            </a>
          </p>
        )}

        <p className="muted">
          Текуща версия: {material.currentVersion}
        </p>

        <p className="muted">
          Статус: {material.hidden ? "Скрит" : "Видим"}
        </p>

        {canManageMaterial && (
          <>
            <button
              type="button"
              className="secondary-button"
              onClick={() => {
                setEditing(!editing);
                setError("");
                setSuccess("");
              }}
            >
              {editing
                ? "Отказ от редакция"
                : "Редактирай материала"}
            </button>

            {editing && (
              <form
                className="material-form"
                onSubmit={handleUpdate}
              >
                <h2>Редакция на материал</h2>

                <label>
                  Заглавие *
                  <input
                    value={title}
                    maxLength={255}
                    onChange={(event) =>
                      setTitle(event.target.value)
                    }
                    required
                  />
                </label>

                <label>
                  Описание
                  <textarea
                    value={description}
                    rows={3}
                    onChange={(event) =>
                      setDescription(event.target.value)
                    }
                  />
                </label>

                <label>
                  Тагове (разделени със запетая)
                  <input
                    value={tags}
                    onChange={(event) =>
                      setTags(event.target.value)
                    }
                  />
                </label>

                {material.type === "VIDEO_LINK" && (
                  <label>
                    URL адрес на видеото
                    <input
                      type="url"
                      value={videoUrl}
                      onChange={(event) =>
                        setVideoUrl(event.target.value)
                      }
                    />
                  </label>
                )}

                <label className="material-checkbox">
                  <input
                    type="checkbox"
                    checked={hidden}
                    onChange={(event) =>
                      setHidden(event.target.checked)
                    }
                  />
                  Скрит материал
                </label>

                <button type="submit" disabled={saving}>
                  {saving
                    ? "Запазване..."
                    : "Запази промените"}
                </button>
              </form>
            )}
          </>
        )}
      </div>

      {canManageMaterial && material.type !== "VIDEO_LINK" && (
        <div className="card material-version-section">
          <h2>Качване на нова версия</h2>

          <form
            className="material-form"
            onSubmit={handleUploadVersion}
          >
            <label>
              Избери файл
              <input
                type="file"
                accept=".pdf,.ppt,.pptx,.doc,.docx,.png,.jpg,.jpeg"
                onChange={(event) =>
                  setNewVersionFile(
                    event.target.files?.[0] ?? null
                  )
                }
                required
              />

              <small>
                PDF, DOC, DOCX, PPT, PPTX, PNG, JPG или JPEG.
                До 50 MB.
              </small>
            </label>

            <button type="submit" disabled={saving}>
              {saving
                ? "Качване..."
                : "Качи нова версия"}
            </button>
          </form>
        </div>
      )}

      <div className="card material-version-section">
        <h2>История на версиите</h2>

        {versions.length ? (
          <ul className="version-list">
            {versions.map((version) => (
              <li key={version.id}>
                <div>
                  <strong>
                    Версия {version.versionNumber}
                  </strong>

                  <p className="muted">
                    {version.fileName || "Файл"}
                    {version.uploadedByName
                      ? ` · Качена от: ${version.uploadedByName}`
                      : ""}
                  </p>

                  {version.uploadedAt && (
                    <p className="muted">
                      {new Date(
                        version.uploadedAt
                      ).toLocaleString("bg-BG")}
                    </p>
                  )}
                </div>

                {version.fileUrl && (
                  <a
                    href={version.fileUrl}
                    target="_blank"
                    rel="noreferrer"
                  >
                    Отвори
                  </a>
                )}
              </li>
            ))}
          </ul>
        ) : (
          <p className="muted">
            Няма налична история на версиите.
          </p>
        )}
      </div>

      {error && (
        <p className="material-form-error" role="alert">
          {error}
        </p>
      )}

      {success && (
        <p className="material-form-success" role="status">
          {success}
        </p>
      )}
    </section>
  );
}

export default MaterialPage;
