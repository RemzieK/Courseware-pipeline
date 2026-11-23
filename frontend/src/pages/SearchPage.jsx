
import { useEffect, useState } from "react";
import useDebounce from "../hooks/useDebounce";
import api from "../services/api";
import SearchResults from "../components/search/SearchResults";

function SearchPage() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [discipline, setDiscipline] = useState("");
  const [teacher, setTeacher] = useState("");
  const [topic, setTopic] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const debouncedQuery = useDebounce(query, 300);

  useEffect(() => {
    const normalizedQuery = debouncedQuery.trim();

    if (
  !normalizedQuery &&
  !discipline.trim() &&
  !teacher.trim() &&
  !topic.trim()
) {
  setResults([]);
  setLoading(false);
  setError("");
  return;
}

    const controller = new AbortController();

    const search = async () => {
      setLoading(true);
      setError("");

      try {
        const response = await api.get("/search", {
          params: {
            q: normalizedQuery || undefined,
            discipline: discipline || undefined,
            teacher: teacher || undefined,
            topic: topic || undefined,
            page: 0,
            size: 20,
          },
          signal: controller.signal,
        });

        setResults(response.data.content ?? []);
      } catch (err) {
        if (err.name !== "CanceledError") {
          setError("Възникна грешка при търсенето. Опитайте отново.");
          setResults([]);
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    search();

    return () => controller.abort();
  }, [debouncedQuery, discipline, teacher, topic]);

  return (
    <section className="page">
      <h1>Търсене на учебни материали</h1>
      <p>
        Намерете курсове и учебни материали в UniCloud.
      </p>

      
<div className="search-panel">
  <label htmlFor="search-input">Търсене</label>
  <input
    id="search-input"
    type="search"
    placeholder="Въведете име на материал, курс или тема..."
    value={query}
    onChange={(event) => setQuery(event.target.value)}
  />

  <label htmlFor="discipline-filter">Дисциплина</label>
  <input
    id="discipline-filter"
    type="text"
    placeholder="Например: Информатика"
    value={discipline}
    onChange={(event) => setDiscipline(event.target.value)}
  />

  <label htmlFor="teacher-filter">Преподавател</label>
  <input
    id="teacher-filter"
    type="text"
    placeholder="Име на преподавател"
    value={teacher}
    onChange={(event) => setTeacher(event.target.value)}
  />

  <label htmlFor="topic-filter">Тема</label>
  <input
    id="topic-filter"
    type="text"
    placeholder="Въведете тема"
    value={topic}
    onChange={(event) => setTopic(event.target.value)}
  />
</div>


      {loading && <p role="status">Търсене...</p>}

      {error && <p role="alert">{error}</p>}

      {!loading &&
  !error &&
  (debouncedQuery.trim() ||
    discipline.trim() ||
    teacher.trim() ||
    topic.trim()) && (
    <>
      <p className="search-count">
        Намерени резултати: {results.length}
      </p>
      <SearchResults results={results} query={debouncedQuery} />
    </>
  )}
    </section>
  );
}

export default SearchPage;
