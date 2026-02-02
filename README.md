# OblikPodorojList

JavaFX додаток для обліку подорожніх листів.

## 🚀 Швидкий старт

### Запуск через IntelliJ IDEA

1. Відкрийте проект в IntelliJ IDEA
2. Запустіть Docker контейнер з MySQL:
   ```bash
   docker-compose up -d
   ```
3. Запустіть `Main.java`

### База даних

Проект використовує MySQL 8.0 в Docker контейнері:

```bash
# Запустити БД
docker-compose up -d

# Перевірити статус
docker ps

# Зупинити БД
docker-compose down
```

**Налаштування за замовчуванням:**
- База даних: `39580810`
- Порт: `3306`
- Root користувач: `root` / `root`
- Guest користувач: `GUEST` / `GUEST`

## 📦 Створення .exe файлу для Windows

### ⭐ Варіант 1: GitHub Actions (АВТОМАТИЧНО, РЕКОМЕНДОВАНО)

GitHub автоматично створює .exe при кожному push в main гілку!

```bash
git add .
git commit -m "Мої зміни"
git push origin main
```

**Після завершення збірки:**
- GitHub → Actions → Download artifacts (artifacts зберігаються 30 днів)

**Створення релізу з .exe:**
```bash
git tag v1.0.0
git push origin v1.0.0
```
- GitHub → Releases → v1.0.0 → завантажити .exe

📖 **Детальна інструкція:** [GITHUB_ACTIONS.md](GITHUB_ACTIONS.md)

### Варіант 2: JAR + BAT файл (для локальної збірки на macOS)

1. **Зібрати JAR через IntelliJ IDEA:**
   - Maven панель → OblikPodorojList → Lifecycle
   - Запустіть `clean`, потім `package`
   - JAR файл: `target/OblikPodorojList-1.0-SNAPSHOT-shaded.jar`

2. **Скопіювати на Windows:**
   - `target/OblikPodorojList-1.0-SNAPSHOT-shaded.jar`
   - `run.bat`

3. **Запустити на Windows:**
   - Подвійний клік на `run.bat`

### Варіант 3: Через командний рядок на macOS

```bash
# Встановити Maven
brew install maven

# Зібрати проект
./build.sh
```

Скопіювати на Windows:
- `target/OblikPodorojList-1.0-SNAPSHOT-shaded.jar`
- `target/run.bat`

### Варіант 4: Повноцінний .exe інсталятор (тільки на Windows)

На Windows машині виконайте:

```bash
mvn clean package
mvn javafx:jlink
mvn jpackage:jpackage
```

Результат: `target/dist/OblikPodorojList-1.0.0.exe` (інсталятор ~150-200 MB з вбудованою Java)

📖 **Детальні інструкції:** [QUICK_START.md](QUICK_START.md)

## 🛠 Технології

- **Java 17**
- **JavaFX 22.0.1**
- **Maven**
- **MySQL 8.0** (Docker)
- **HikariCP** - пул з'єднань
- **Apache POI** - робота з Excel
- **Liquibase** - міграції БД

## 📁 Структура проекту

```
OblikLists/
├── .github/workflows/       # GitHub Actions (автоматична збірка)
├── src/main/java/          # Код програми
│   └── com/work/oblikpodorojlist/
│       ├── model/          # Моделі даних
│       ├── pages/          # Сторінки UI
│       ├── utils/          # Утиліти
│       └── Main.java       # Точка входу
├── src/main/resources/     # Ресурси (CSS, іконки)
├── init-db/                # SQL скрипти ініціалізації
├── docker-compose.yml      # Docker конфігурація
├── pom.xml                 # Maven конфігурація
└── build.sh                # Скрипт збірки
```

## 📝 Документація

- [QUICK_START.md](QUICK_START.md) - швидкий старт
- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - детальні інструкції збірки
- [GITHUB_ACTIONS.md](GITHUB_ACTIONS.md) - автоматична збірка через GitHub

## 🐛 Відладка

### Перевірка БД

```bash
# Подивитись логи MySQL
docker logs oblik_mysql

# Підключитись до БД
docker exec -it oblik_mysql mysql -uroot -proot 39580810

# Перевірити таблиці
docker exec oblik_mysql mysql -uroot -proot -e "USE 39580810; SHOW TABLES;"
```

### Логи програми

Логи зберігаються в файлі `app.log` в корені проекту.

## 📜 Ліцензія

Приватний проект.
