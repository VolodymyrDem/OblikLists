# Збірка EXE через GitHub Actions

## Як працює:

1. **Push до main** → автоматично запускається збірка
2. **Або вручну**: GitHub → Actions → Build Windows EXE → Run workflow

## Де взяти EXE:

1. Відкрити GitHub → Actions
2. Вибрати останній білд
3. Скачати артефакт **OblikPodorojList-EXE**

## Вимоги на Windows:

- **Java 21+** (у вас Java 23 ✅)

## Запуск:

Просто подвійний клік по `OblikPodorojList.exe`

---

**Налаштування:**
- Білд на: `windows-latest` 
- JDK: 21 (Temurin)
- Maven plugin: Launch4j 2.5.1
- Причина Java 21: бібліотека `tilesfx` вимагає Java 21+
