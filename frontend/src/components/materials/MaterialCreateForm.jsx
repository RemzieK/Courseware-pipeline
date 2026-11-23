
import { useState } from "react";
import api from "../../services/api";

const MATERIAL_TYPES = [
  { value: "NOTES", label: "Конспект / записки" },
  { value: "PRESENTATION", label: "Презентация" },
  { value: "VIDEO_LINK", label: "Видео линк" },
  { value: "ASSIGNMENT", label: "Задание" },
  { value: "LITERATURE", label: "Литература" },
];

const MAX_FILE_SIZE = 50 * 1024 * 1024;
const ALLOWED_EXTENSIONS = [
  "pdf", "ppt", "pptx", "doc", "docx", "png", "jpg", "jpeg",
];

function MaterialCreateForm({ lessonId, onCreated, onCancel }) {
  const [type, setType] = useState("NOTES");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [tags, setTags] = useState("");
  const [videoUrl, setVideoUrl] = useState("");
  const [file, setFile] = useState(null);
  const [hidden, setHidden] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!title.trim()) {
      setError("Въведете заглавие на материала.");
      return;
    }

    if (type === "VIDEO_LINK" && !videoUrl.trim()) {
      setError("Въведете URL адрес на видеото.");
      return;
    }

    if (type !== "VIDEO_LINK" && !file) {
      setError("Изберете файл за качване.");
      return;
    }

    if (file) {
      const extension = file.name.split(".").pop().toLowerCase();

      if (!ALLOWED_EXTENSIONS.includes(extension)) {
        setError("Неподдържан файлов формат.");
        return;
      }

      if (file.size > MAX_FILE_SIZE) {
        setError("Файлът не трябва да надвишава 50 MB.");
        return;
      }
    }

    const formData = new FormData();

    const request = {
      type,
      title: title.trim(),
      description: description.trim(),
      videoUrl: type === "VIDEO_LINK" ? videoUrl.trim() : null,
      tags: tags
        .split(",")
        .map((tag) => tag.trim())
        .filter(Boolean),
      hidden,
    };

    formData.append(
      "request",
      new Blob([JSON.stringify(request)], {
        type: "application/json",
      })
    );

    if (file) {
      formData.append("file", file);
    }

    try {
      setLoading(true);

      const response = await api.post(
        `/lessons/${lessonId}/materials`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      onCreated?.(response.data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Материалът не можа да бъде качен."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="material-form" onSubmit={handleSubmit}>
      <h4>Добавяне на учебен материал</h4>

      <label>
        Тип материал
        <select
          value={type}
          onChange={(event) => {
            setType(event.target.value);
            setFile(null);
            setVideoUrl("");
          }}
        >
          {MATERIAL_TYPES.map((item) => (
            <option key={item.value} value={item.value}>
              {item.label}
            </option>
          ))}
        </select>
      </label>

      <label>
        Заглавие *
        <input
          type="text"
          value={title}
          maxLength={255}
          onChange={(event) => setTitle(event.target.value)}
          required
        />
      </label>

      <label>
        Описание
        <textarea
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          rows={3}
        />
      </label>

      <label>
        Тагове (разделени със запетая)
        <input
          type="text"
          value={tags}
          onChange={(event) => setTags(event.target.value)}
          placeholder="например: Java, програмиране"
        />
      </label>

      {type === "VIDEO_LINK" ? (
        <label>
          URL адрес на видеото *
          <input
            type="url"
            value={videoUrl}
            onChange={(event) => setVideoUrl(event.target.value)}
            placeholder="https://..."
            required
          />
        </label>
      ) : (
        <label>
          Файл *
          <input
            type="file"
            accept=".pdf,.ppt,.pptx,.doc,.docx,.png,.jpg,.jpeg"
            onChange={(event) =>
              setFile(event.target.files?.[0] ?? null)
            }
            required
          />
          <small>PDF, DOC, DOCX, PPT, PPTX, PNG, JPG или JPEG. До 50 MB.</small>
        </label>
      )}

      <label className="material-checkbox">
        <input
          type="checkbox"
          checked={hidden}
          onChange={(event) => setHidden(event.target.checked)}
        />
        Скрит материал
      </label>

      {error && <p className="material-form-error" role="alert">{error}</p>}

      <div className="material-form-actions">
        <button type="submit" disabled={loading}>
          {loading ? "Качване..." : "Качи материал"}
        </button>

        <button type="button" onClick={onCancel} disabled={loading}>
          Отказ
        </button>
      </div>
    </form>
  );
}

export default MaterialCreateForm;
