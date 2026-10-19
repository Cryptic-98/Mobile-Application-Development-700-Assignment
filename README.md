# Smart Pantry Manager

Smart Pantry Manager is an Android application scaffold for a pantry and recipe
management assignment. The pantry, recipe, persistence, and settings features
have not been implemented yet. The current app contains only a launcher screen
so the project has a valid starting point for development.

## Requirements

- Android Studio with a compatible Android Gradle Plugin
- Android SDK platform 37 and a matching build-tools version
- JDK 25 (configured through the Gradle daemon JVM criteria)

Open the project in Android Studio and allow Gradle sync to finish, or build
from the project root with:

```shell
./gradlew :app:assembleDebug
```

## Planned features

- Add, view, edit, and delete pantry ingredients.
- Store recipes and suggest only recipes for which every required ingredient
  is available.
- Normalize ingredient names and supported units.
- Validate ingredient input and persist application settings.

## Project packages

- `com.example.smartpantrymanager`: Android activities
- `com.example.adapters`: list adapters
- `com.example.database`: SQLite helpers and repositories
- `com.example.logic`: pantry and recipe business logic
- `com.example.models`: application data models

The unimplemented source files and placeholder layouts are reserved for
planned features; add implementations and tests as those features are
developed.
