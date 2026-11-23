
function SearchFilters({
  selectedType,
  onTypeChange,
  selectedCourse,
  onCourseChange,
  courses,
}) {
  return (
    <div className="search-filters">
      <label>
        Тип материал
        <select
          value={selectedType}
          onChange={(event) => onTypeChange(event.target.value)}
        >
          <option value="all">Всички типове</option>
          <option value="Лекция">Лекция</option>
          <option value="Презентация">Презентация</option>
          <option value="Упражнение">Упражнение</option>
        </select>
      </label>

      <label>
        Курс
        <select
          value={selectedCourse}
          onChange={(event) => onCourseChange(event.target.value)}
        >
          <option value="all">Всички курсове</option>
          {courses.map((course) => (
            <option key={course.id} value={course.id}>
              {course.title}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}

export default SearchFilters;
