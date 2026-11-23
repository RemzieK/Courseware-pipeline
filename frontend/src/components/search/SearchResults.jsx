
import { Link } from "react-router-dom";
import HighlightText from "./HighlightText";

function SearchResults({ results, query }) {
  if (results.length === 0) {
    return (
      <div className="state-message">
        <h2>Няма намерени резултати</h2>
        <p>Опитайте с друга дума за търсене.</p>
      </div>
    );
  }

  return (
    <div className="search-results">
      {results.map((result) => {
        const isCourse = result.itemType === "COURSE";

        const destination = isCourse
          ? `/catalog/${result.id}`
          : `/materials/${result.id}`;

        return (
          <article className="search-result" key={`${result.itemType}-${result.id}`}>
            <span className="badge">
              {isCourse ? "Курс" : "Учебен материал"}
            </span>

            <h2>
              <HighlightText
                text={result.title ?? ""}
                query={query}
              />
            </h2>

            {result.description && (
              <p>
                <HighlightText
                  text={result.description}
                  query={query}
                />
              </p>
            )}

            {result.courseCode && (
              <p>Код на курса: {result.courseCode}</p>
            )}

            {result.discipline && (
              <p>Дисциплина: {result.discipline}</p>
            )}

            {result.teacherName && (
              <p>Преподавател: {result.teacherName}</p>
            )}

            {result.materialType && (
              <p>Тип: {result.materialType}</p>
            )}

            <Link to={destination}>
              {isCourse ? "Отвори курса" : "Отвори материала"}
            </Link>
          </article>
        );
      })}
    </div>
  );
}

export default SearchResults;
