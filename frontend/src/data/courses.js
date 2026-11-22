export const courses = [
  {
    id: "web-programming",
    code: "SE101",
    title: "Уеб програмиране",
    teacher: "Иван Петров",
    description:
      "Основи на разработката на уеб приложения с HTML, CSS и JavaScript.",
    modules: [
      {
        id: "web-module-1",
        title: "Модул 1: Въведение в уеб технологиите",
        week: "Седмица 1",
        materials: [
          {
            id: "web-material-1",
            title: "Въведение в HTML",
            type: "Лекция",
          },
          {
            id: "web-material-2",
            title: "Основи на уеб страниците",
            type: "Презентация",
          },
        ],
      },
      {
        id: "web-module-2",
        title: "Модул 2: CSS и оформление",
        week: "Седмица 2",
        materials: [
          {
            id: "web-material-3",
            title: "Въведение в CSS",
            type: "Лекция",
          },
        ],
      },
    ],
  },
  {
    id: "databases",
    code: "SE102",
    title: "Бази от данни",
    teacher: "Мария Иванова",
    description:
      "Релационни бази от данни, SQL заявки и моделиране на данни.",
    modules: [
      {
        id: "db-module-1",
        title: "Модул 1: Основи на базите от данни",
        week: "Седмица 1",
        materials: [
          {
            id: "db-material-1",
            title: "Въведение в базите от данни",
            type: "Лекция",
          },
        ],
      },
      {
        id: "db-module-2",
        title: "Модул 2: SQL",
        week: "Седмица 2",
        materials: [
          {
            id: "db-material-2",
            title: "Основни SQL заявки",
            type: "Упражнение",
          },
        ],
      },
    ],
  },
  {
    id: "software-engineering",
    code: "SE103",
    title: "Софтуерно инженерство",
    teacher: "Георги Димитров",
    description:
      "Процеси, методологии и добри практики при разработката на софтуер.",
    modules: [
      {
        id: "se-module-1",
        title: "Модул 1: Жизнен цикъл на софтуера",
        week: "Седмица 1",
        materials: [
          {
            id: "se-material-1",
            title: "Въведение в софтуерното инженерство",
            type: "Лекция",
          },
        ],
      },
    ],
  },
];