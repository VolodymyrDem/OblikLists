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

### ⭐ GitHub Actions (АВТОМАТИЧНО, РЕКОМЕНДОВАНО)

GitHub автоматично створює .exe wrapper при кожному push!

```bash
git add .
git commit -m "Мої зміни"
git push origin main
```

**Результат (через 2-4 хвилини):**
- GitHub → Actions → Download `OblikPodorojList-Windows-EXE`

**Створення релізу:**
```bash
git tag v1.0.0
git push origin v1.0.0
```
- GitHub → Releases → v1.0.0 → завантажити .exe

### Локальна збірка

**Через IntelliJ IDEA:**
1. Maven панель → Lifecycle → `clean`
2. Maven панель → Lifecycle → `package`
3. Результат: `target/OblikPodorojList.exe`

**Через командний рядок:**
```bash
mvn clean package
```

## 🛠 Технології

- **Java 17**
- **JavaFX 22.0.1**
- **Maven**
- **MySQL 8.0** (Docker)
- **HikariCP** - пул з'єднань
- **Apache POI** - робота з Excel
- **Liquibase** - міграції БД
- **Launch4j** - .exe wrapper

## 📋 Системні вимоги

**Для розробки:**
- Java 17+
- Maven
- Docker (для БД)

**Для користувачів (.exe файл):**
- Windows 10/11
- Java 17+ (завантажити: https://adoptium.net/)

## 🐛 Відладка

### Перевірка БД

```bash
# Подивитись логи MySQL
docker logs oblik_mysql

# Підключитись до БД
docker exec -it oblik_mysql mysql -uroot -proot 39580810
```

### Логи програми

Логи зберігаються в `app.log` в корені проекту.

## 📜 Ліцензія

Приватний проект.
