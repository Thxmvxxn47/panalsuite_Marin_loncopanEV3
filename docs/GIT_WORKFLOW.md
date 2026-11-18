# Guía de Git para el equipo

## Ramas

| Rama | Uso |
|---|---|
| `main` | Versión estable y entregable. Solo recibe merges por Pull Request. |
| `develop` | Integración del trabajo del equipo. |
| `feature/<integrante>-<tema>` | Trabajo de cada persona (ej. `feature/ana-login`, `feature/luis-tickets-api`). |

## Pasos

```bash
git clone <url-del-repo>
git checkout develop
git checkout -b feature/<tu-nombre>-<tema>

# trabajar...
git add .
git commit -m "feat(tickets): agrega pantalla de detalle"
git push -u origin feature/<tu-nombre>-<tema>
```

Abrir un Pull Request hacia `develop`, pedir revisión a otro integrante y hacer merge. Al cierre de cada hito, `develop` → `main`.

## Convención de commits

`feat:` funcionalidad · `fix:` corrección · `test:` pruebas · `docs:` documentación · `refactor:` mejora interna · `chore:` configuración.

## Buenas prácticas

- Commits pequeños y con mensaje claro; cada integrante hace commits con **su propio usuario** de Git.
- No subir `build/`, `.gradle/`, `local.properties` ni datos locales (`data/`, ya en `.gitignore`).
- Antes de integrar: `mvn test` en los servicios y `./gradlew :app:testDebugUnitTest` en la app.
