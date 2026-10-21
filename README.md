# Lesson Scheduler

REST-сервис для недельного расписания занятий: CRUD по урокам с проверкой конфликтов
по группе, преподавателю и аудитории.

## Стек

- Java 17
- Spring Boot 3.3.5 (Web, Data JPA, Validation)
- PostgreSQL 16 (production), H2 (тесты)
- Maven

## Запуск

1. Поднять базу данных:

   ```bash
   docker compose up -d
   ```

2. Запустить приложение:

   ```bash
   mvn spring-boot:run
   ```

Сервис будет доступен на `http://localhost:8080`.

## Тесты

```bash
mvn test
```

Unit-тесты сервиса (Mockito) и интеграционные тесты контроллера (`@WebMvcTest`).
Тесты используют H2 in-memory и не требуют запущенного PostgreSQL.

## API

Базовый путь: `/api/lessons`.

| Метод  | Путь                 | Назначение                                      | Успешный код |
|--------|----------------------|-------------------------------------------------|--------------|
| POST   | `/api/lessons`       | Создать урок                                    | 201 Created  |
| GET    | `/api/lessons/{id}`  | Получить урок по id                             | 200 OK       |
| GET    | `/api/lessons`       | Список с фильтрами `groupName`, `teacher`, `dayOfWeek` | 200 OK |
| PUT    | `/api/lessons/{id}`  | Заменить урок целиком                           | 200 OK       |
| DELETE | `/api/lessons/{id}`  | Удалить урок                                    | 204 No Content |

Сортировка списка: по дню недели (`MONDAY` → `FRIDAY`), затем по `startTime`.

Правило конфликтов: в один день у одной группы, у одного преподавателя и в одной
аудитории интервалы не пересекаются. Касание границ допустимо. При пересечении
возвращается `409 Conflict`.

## Формат ошибок

Все ошибки возвращаются в едином JSON-виде:

```json
{
  "timestamp": "2026-10-21T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Conflict with existing lesson id=1 (G-101, Ivanov, room A-1, MONDAY 10:30-11:30)",
  "path": "/api/lessons"
}
```

Коды: `400` — невалидные данные, `404` — урок не найден, `409` — конфликт расписания,
`500` — непредвиденная ошибка сервера.

## Примеры запросов

### Создать урок

```bash
curl -i -X POST http://localhost:8080/api/lessons \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Math",
    "teacher": "Ivanov",
    "groupName": "G-101",
    "room": "A-1",
    "dayOfWeek": "MONDAY",
    "startTime": "10:00",
    "endTime": "11:00"
  }'
```

Ответ `201 Created`:

```json
{
  "id": 1,
  "subject": "Math",
  "teacher": "Ivanov",
  "groupName": "G-101",
  "room": "A-1",
  "dayOfWeek": "MONDAY",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}
```

### Получить урок по id

```bash
curl http://localhost:8080/api/lessons/1
```

### Список с фильтрами

```bash
curl "http://localhost:8080/api/lessons?groupName=G-101&teacher=Ivanov&dayOfWeek=MONDAY"
```

Все параметры необязательны. Сортировка — по дню недели, затем по времени начала.

### Обновить урок

```bash
curl -i -X PUT http://localhost:8080/api/lessons/1 \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Math",
    "teacher": "Ivanov",
    "groupName": "G-101",
    "room": "A-1",
    "dayOfWeek": "MONDAY",
    "startTime": "11:00",
    "endTime": "12:00"
  }'
```

### Удалить урок

```bash
curl -i -X DELETE http://localhost:8080/api/lessons/1
```

Ответ `204 No Content`.

### Пример конфликта (409)

Попытка создать урок в то же время, что и существующий, у той же группы:

```bash
curl -i -X POST http://localhost:8080/api/lessons \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Physics",
    "teacher": "Petrov",
    "groupName": "G-101",
    "room": "B-2",
    "dayOfWeek": "MONDAY",
    "startTime": "10:30",
    "endTime": "11:30"
  }'
```

```json
{
  "timestamp": "2026-10-21T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Conflict with existing lesson id=1 (G-101, Ivanov, room A-1, MONDAY 10:00-11:00)",
  "path": "/api/lessons"
}
```

### Пример валидации (400)

```bash
curl -i -X POST http://localhost:8080/api/lessons \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "",
    "teacher": "Ivanov",
    "groupName": "G-101",
    "room": "A-1",
    "dayOfWeek": "MONDAY",
    "startTime": "11:00",
    "endTime": "10:00"
  }'
```

```json
{
  "timestamp": "2026-10-21T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "subject: must not be blank; endTime must be strictly after startTime",
  "path": "/api/lessons"
}
```

### Пример «не найдено» (404)

```bash
curl -i http://localhost:8080/api/lessons/999
```

```json
{
  "timestamp": "2026-10-21T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Lesson with id 999 not found",
  "path": "/api/lessons/999"
}
```
