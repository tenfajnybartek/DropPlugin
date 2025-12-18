# Changelog - DropPlugin

## [SQLite Support] - 2025-12-18

### ✨ Nowe Funkcje

**Wsparcie dla SQLite jako alternatywy dla MySQL**
- Plugin teraz obsługuje zarówno SQLite jak i MySQL
- SQLite jest domyślną bazą danych (brak konfiguracji)
- Wybór bazy w `config.yml` przez `database.type: sqlite` lub `mysql`
- Automatyczne tworzenie pliku `database.db` w folderze pluginu dla SQLite
- Uniwersalne zapytania SQL kompatybilne z obiema bazami
- SQLite używa WAL mode dla lepszej wydajności

### 🔧 Zmiany Techniczne

- Dodano zależność `org.xerial:sqlite-jdbc:3.45.0.0`
- Dodano pole `dbType` w ConfigManager
- Zaktualizowano Database class z obsługą obu typów baz
- Automatyczna detekcja typu bazy i odpowiednie konfigurowanie HikariCP
- SQLite używa `INSERT OR REPLACE` zamiast `REPLACE INTO`
- Obsługa INTEGER jako BOOLEAN w SQLite

### 📝 Aktualizacje Dokumentacji

- Zaktualizowano README.md z sekcją wyboru bazy danych
- Dodano porównanie SQLite vs MySQL
- Zaktualizowano instrukcję instalacji
- Dodano przykłady konfiguracji dla obu baz

### 💡 Zalety SQLite

- Brak wymagań zewnętrznych (nie trzeba instalować MySQL)
- Automatyczna konfiguracja
- Idealne dla małych/średnich serwerów
- Łatwe backupy (jeden plik)

## [PlaceholderAPI & Negative Y Support] - 2025-12-18

### ✨ Nowe Funkcje

1. **Wsparcie dla ujemnych wartości Y (-64 do 320)**
   - Obsługa nowych limitów wysokości z Minecraft 1.18+
   - Zaktualizowana metoda `Count.parse()` do obsługi wartości ujemnych
   - Przykłady w `drops.yml` dla głębokich warstw (np. `-64-16` dla diamentów)
   - Format: `-64-90` lub `-64--32` dla zakresów z ujemnymi wartościami

2. **Integracja z PlaceholderAPI**
   - Dodano ekspansję PlaceholderAPI
   - Dostępne placeholdery:
     - `%dropplugin_level%` - poziom kopania gracza
     - `%dropplugin_points%` - aktualne punkty gracza
     - `%dropplugin_points_required%` - punkty wymagane do następnego poziomu
     - `%dropplugin_points_to_next%` - punkty brakujące do awansu
   - Automatyczna detekcja PlaceholderAPI przy starcie
   - Soft dependency w `plugin.yml`

### 📝 Aktualizacje Dokumentacji

- Zaktualizowano `README.md` z sekcją PlaceholderAPI
- Dodano przykłady użycia placeholderów
- Zaktualizowano opis wysokości w `drops.yml`
- Dodano informacje o wspieranych zakresach Y

### 🔧 Zmiany Techniczne

- Dodano zależność `me.clip:placeholderapi:2.11.6` (compileOnly)
- Dodano pakiet `integrations` z klasą `DropPluginExpansion`
- Zmieniono logikę parsowania w `Count.parse()` dla ujemnych liczb
- Dodano metodę `registerPlaceholderAPI()` w `DropPlugin`

## [Code Review & Improvements] - 2025-12-18

### 🔴 Krytyczne Poprawki

- **Paper API**: Zmieniono z 1.21.8 na 1.21.4 (zgodnie z wymaganiami użytkownika)
- **Ładowanie z bazy danych**: Naprawiono bug gdzie użytkownicy nie byli wczytywani z bazy przy starcie
  - Dodano implementację pętli `while(resultSet.next())` która faktycznie wczytuje dane
  - Dodano licznik załadowanych użytkowników
  - Dodano obsługę błędów dla poszczególnych użytkowników

### 🐛 Poprawione Bugi

1. **Nazwa zmiennej**: `diableAllStatus` → `disableAllStatus` w ConfigManager i DropMenu
2. **Mechanika Unbreaking**: Poprawiono logikę szansy na uszkodzenie narzędzia
   - Wcześniej: losowanie < szansa (błędne)
   - Teraz: losowanie >= szansa (zgodne z Minecraft)
   - Szansa na uszkodzenie = 100/(level+1)%
3. **Duplikacja wiadomości**: Usunięto podwójne wysyłanie wiadomości o awansie w UserLevelChangeListener
4. **Null pointer**: Dodano sprawdzenie lokacji przed odtworzeniem dźwięku

### 🔒 Bezpieczeństwo i Walidacja

1. **Walidacja parametrów komendy `/adrop level`**:
   - Poziom: 1-1000 (stałe MIN_LEVEL, MAX_LEVEL)
   - Punkty: 0-1000000 (stałe MIN_POINTS, MAX_POINTS)
   
2. **Ochrona przed null**:
   - Sprawdzanie null w UserManager.getUser()
   - Sprawdzanie null w Database.saveUser()
   - Sprawdzanie null w DropManager.breakBlock()
   - Sprawdzanie null w wszystkich event listenerach

3. **Ochrona przed błędami matematycznymi**:
   - Dzielenie przez zero w User.getPointsRequired()
   - Ujemne wartości w Count.parse()
   - Ujemne exp w Drop (z ostrzeżeniem w konsoli)

### 📚 Dokumentacja

1. **JavaDoc** dodany do:
   - `User` - konstruktory, addPoints(), getPointsRequired()
   - `Drop` - konstruktor z pełnym opisem parametrów
   - `Chance` - klasa i wszystkie metody
   - `Count.parse()` - opis formatu i zwracanych wartości
   - `UserManager.loadUser()` - nowa metoda z pełną dokumentacją

2. **Komentarze w kodzie**:
   - Mechanika Unbreaking w ItemUtils
   - Logika awansu poziomów w UserLevelChangeListener
   - Parametry konfiguracyjne w config.yml
   - Struktura dropów w drops.yml

3. **Dokumentacja konfiguracji**:
   - Dodano opis parametrów HikariCP w config.yml
   - Dodano przykłady i wyjaśnienia w drops.yml

### 🔧 Refaktoryzacja

1. **Enkapsulacja**:
   - Dodano metodę `UserManager.loadUser()` zamiast bezpośredniego dostępu do mapy
   - Oznaczono `getUserMap()` jako metodę tylko do odczytu

2. **Stałe zamiast magicznych liczb**:
   - MIN_LEVEL, MAX_LEVEL w ADropCommand
   - MIN_POINTS, MAX_POINTS w ADropCommand

3. **Usunięto nieużywane importy**:
   - ChatColor z ItemUtils

### 🛡️ Skanowanie Bezpieczeństwa

- ✅ **CodeQL**: 0 alertów bezpieczeństwa
- ✅ **Code Review**: Wszystkie sugestie zaimplementowane

### 📝 Logi i Diagnostyka

1. **Ulepszone logowanie**:
   - Szczegółowe logi zapisu użytkowników do bazy
   - Logi błędów z kontekstem (UUID, nazwa gracza)
   - Ostrzeżenia o nieprawidłowych konfiguracjach
   - Fine-grained logging dla operacji bazy danych

2. **Obsługa błędów**:
   - Try-catch w BlockBreakListener
   - Try-catch w UserManager.getUser()
   - Bezpieczne tworzenie nowych użytkowników

### 📋 Konfiguracja

**config.yml**:
- Dodano parametry HikariCP z opisami
- Wszystkie parametry posiadają komentarze

**drops.yml**:
- Dodano sekcję z opisem formatu
- Dodano przykłady użycia
- Dodano brakujące pole `points` dla wszystkich dropów

### 🧪 Testy i Kompatybilność

- ✅ Zgodność z Paper 1.21.4
- ✅ Java 21+
- ✅ MySQL 5.7+ / MariaDB 10.2+
- ⚠️ Kompilacja wymaga dostępu do internetu (Paper API)

### 📦 Zależności

Aktualne wersje (bez zmian):
- HikariCP: 5.0.1
- MySQL Connector: 8.0.33
- Apache Commons Lang3: 3.12.0
- Adventure API: 4.14.0
- Adventure Platform Bukkit: 4.4.0

### 🔮 Zalecenia na Przyszłość

1. **Testy jednostkowe**: Dodać testy dla krytycznych metod (Count.parse, RandomUtils.getChance)
2. **Metryki**: Rozważyć dodanie Metrics (bStats) dla statystyk użycia
3. **Konfiguracja limitów**: Przenieść MIN_LEVEL, MAX_LEVEL do config.yml
4. **Backup bazy**: Dodać opcję automatycznego backupu danych
5. **API**: Rozważyć utworzenie API dla innych pluginów
6. **Optymalizacja**: Cache dla często używanych operacji (np. sprawdzanie permisji)

### 👥 Autorzy

- Code Review & Improvements: GitHub Copilot
- Original Author: tenfajnybartek

---

## [1.0.0-SNAPSHOT] - Initial Release

Pierwsza wersja pluginu z podstawowymi funkcjami:
- Custom drop ze stone
- System poziomów i punktów
- Turbo eventy
- GUI zarządzania
- Integracja z MySQL
- ActionBar
- Fortune support
