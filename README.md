# DropPlugin

Plugin dla serwerów Minecraft (Paper 1.21.4+) wprowadzający zaawansowany system customowego dropu ze stone oraz system poziomów dla graczy.

## ⚠️ Issues

Jeśli znajdziesz jakieś błędy/bugi zgloś je w zakładce issues!
https://github.com/tenfajnybartek/DropPlugin/issues

## ✅ Obsługiwane wersje 

- ✅ Paper 1.21.4
- ✅ Paper 1.21.5
- ✅ Paper 1.21.6
- ✅ Paper 1.21.7
- ✅ Paper 1.21.8
- ✅ Paper 1.21.9
- ✅ Paper 1.21.10
- ✅ Paper 1.21.11

## 📖 Opis

DropPlugin to kompleksowy plugin do zarządzania dropem z kamienia (stone, granite, diorite, andesite, deepslate) na serwerach Minecraft. Plugin oferuje:

- **Custom drop** - konfigurowalne itemy wypada przy kopaniu stone
- **System poziomów** - gracze zdobywają poziomy i punkty za kopanie
- **Turbo eventy** - zwiększenie szansy na drop i exp (globalne lub per-player)
- **GUI** - intuicyjny interfejs do zarządzania dropami
- **Baza danych** - MySQL/MariaDB z wykorzystaniem HikariCP
- **ActionBar** - informacje o aktywnych eventach
- **Fortune support** - współpraca z enchantami
- **PlaceholderAPI** - integracja z innymi pluginami przez placeholdery

## ✨ Funkcje

### Główne funkcjonalności

- **Custom drop ze stone** - konfigurowalne szanse, ilości i wysokości spawnu
- **System doświadczenia** - punkty i poziomy za kopanie
- **Turbo eventy** - TurboDrop (2x szansa) i TurboExp (2x exp)
- **GUI zarządzania** - włączanie/wyłączanie poszczególnych dropów
- **Cobblestone toggle** - możliwość włączenia/wyłączenia cobble przy kopaniu
- **Wiadomości** - konfigurowalne komunikaty o dropie
- **Fortune enchant** - zwiększa szansę na drop
- **Szanse z permisji** - dodatkowe bonusy dla graczy z określonymi uprawnieniami
- **ActionBar** - wyświetlanie czasu trwania eventów
- **Automatyczny zapis** - okresowe zapisywanie danych graczy
- **PlaceholderAPI** - integracja do używania w innych pluginach

### System dropu

Każdy drop posiada:
- Nazwę i typ itemu
- Szansę na wypadnięcie (z bonusem od fortune i permisji)
- Zakres wysokości spawnu (Y-level od -64 do 320)
- Ilość punktów i doświadczenia
- Zakres ilości itemów
- Support dla fortune enchant
- Wymagany poziom do wydropienia przedmiotu

**UWAGA**: Od Minecraft 1.18+ wspierane są wartości ujemne dla wysokości (od -64 do 320)

## 🔧 Wymagania

- **Java**: 21+
- **Serwer**: Paper 1.21.4+ (lub kompatybilny fork)
- **Baza danych**: 
  - **SQLite** (domyślnie) - wbudowana, brak konfiguracji
  - **MySQL** 5.7+ lub MariaDB 10.2+ (opcjonalnie)
- **Opcjonalne**: PlaceholderAPI (dla integracji z innymi pluginami)

## 📥 Instalacja

1. Pobierz plik `.jar` z releases lub zbuduj samodzielnie
2. Umieść plik w folderze `plugins/` serwera
3. **(Opcjonalnie)** Skonfiguruj bazę danych w `config.yml`:
   - Domyślnie używa SQLite (brak konfiguracji)
   - Dla MySQL zmień `database.type: mysql` i skonfiguruj połączenie
4. Zrestartuj serwer (Nie używaj /reload!)
5. Plugin automatycznie utworzy wymagane tabele w bazie danych

## 🎮 Komendy i uprawnienia

### Komendy dla graczy

| Komenda | Aliasy | Opis | Uprawnienie |
|---------|--------|------|-------------|
| `/drop` | `/stone`, `/kamien` | Otwiera GUI dropu | `dropplugin.cmd.drop` |
| `/level [gracz]` | `/poziom`, `/lvl` | Pokazuje poziom | `dropplugin.cmd.level` / `dropplugin.cmd.alevel` (dla innych) |

### Komendy administratorskie

| Komenda | Opis | Uprawnienie |
|---------|------|-------------|
| `/adrop reload` | Przeładowuje konfigurację | `dropplugin.cmd.adrop` |
| `/adrop <drop/exp> all <czas>` | Włącza turbo dla wszystkich | `dropplugin.cmd.adrop` |
| `/adrop <drop/exp> <gracz> <czas>` | Włącza turbo dla gracza | `dropplugin.cmd.adrop` |
| `/adrop level <gracz> <lvl> [pkt]` | Ustawia poziom gracza | `dropplugin.cmd.adrop` |

### Format czasu

Można używać kombinacji jednostek:
- `1y` - rok
- `2mo` - miesiące
- `3w` - tygodnie
- `4d` - dni
- `5h` - godziny
- `6m` - minuty
- `7s` - sekundy

Przykłady: `1d12h`, `30m`, `2h30m15s`

### Permisje bonusowe

Zdefiniowane w `config.yml -> settings.chances`:
```yaml
chances:
    vip:
      permission: dropplugin.vip
      additionalchance: 0.5
```

## 📊 System poziomów

- Gracze zdobywają **punkty** za wykopywanie stone i otrzymywanie dropu
- Po zebraniu wymaganej liczby punktów otrzymują **poziom**
- Wymagane punkty = `poziom * pointsToLvlup` (domyślnie 100)
- Maksymalny poziom jest konfigurowalny
- Poziom gracza jest wyświetlany w prefiksie na chacie: `[5] NickGracza`
- Niektóre poziomy mogą być ogłaszane całemu serwerowi

### Punkty i exp

- **Punkty**: zdobywane za drop, potrzebne do awansu
- **Exp**: standardowe doświadczenie Minecraft
  - Stone/Granite/Diorite/Andesite/Deepslate: konfigurowalny exp
  - Obsidian: konfigurowalny exp (osobna wartość)

## 🚀 Turbo eventy

### TurboDrop
- Podwaja szansę na wszystkie dropy
- Można włączyć globalnie lub dla konkretnego gracza
- Wyświetlany w GUI i na ActionBar w przypadku całego serwera

### TurboExp
- Podwaja otrzymywane doświadczenie
- Można włączyć globalnie lub dla konkretnego gracza
- Wyświetlany w GUI i na ActionBar w przypadku całego serwera

Oba eventy mogą działać jednocześnie (globalny + osobisty).

## 🖥️ GUI

GUI dostępne przez `/drop` zawiera:

### Sekcja dropów
- **Itemy dropów** - kliknięcie włącza/wyłącza dany drop
- Pokazuje szansę (bazową, bonusową, końcową)
- Pokazuje wymagania (wysokość, fortune)
- Pokazuje ile razy gracz już wykopał dany drop

### Przyciski kontrolne
- **Cobblestone** - włącz/wyłącz otrzymywanie cobble
- **Wiadomości** - włącz/wyłącz powiadomienia o dropie
- **Eventy** - informacje o turbo eventach
- **Poziom** - statystyki poziomu gracza
- **Włącz wszystkie** - aktywuje wszystkie dropy
- **Wyłącz wszystkie** - dezaktywuje wszystkie dropy

## 🗄️ Baza danych

Plugin obsługuje **SQLite** (domyślnie) oraz **MySQL** z pulą połączeń HikariCP.

### Wybór bazy danych

**SQLite** (zalecane dla małych/średnich serwerów):
- ✅ Brak konfiguracji - działa od razu
- ✅ Brak wymagań zewnętrznych
- ✅ Plik bazy w folderze pluginu (`database.db`)
- ⚠️ Jedna aplikacja na raz

**MySQL** (zalecane dla dużych serwerów):
- ✅ Lepsza wydajność przy wielu graczach
- ✅ Możliwość współdzielenia między serwerami
- ✅ Zaawansowane narzędzia backupu
- ⚠️ Wymaga serwera MySQL/MariaDB

### Konfiguracja

```yaml
database:
  type: sqlite              # 'sqlite' lub 'mysql'
  
  # Dla MySQL (ignorowane gdy type: sqlite):
  host: localhost
  port: 3306
  user: root
  base: minecraft
  password: haslo
  
  # Ustawienia puli HikariCP:
  maxPool: 10               # Tylko dla MySQL (SQLite = 1)
  connectionTimeoutMs: 30000       # 30 sekund
  idleTimeoutMs: 600000            # 10 minut
  leakDetectionThresholdMs: 0      # Wyłączone (włącz >0 dla debugowania)
```

### Automatyczny zapis

- Dane są zapisywane co **5 minut** (6000 ticków)
- Dodatkowo przy wyjściu gracza z serwera
- Przy wyłączaniu pluginu

## 🔌 Integracja z PlaceholderAPI

Plugin oferuje integrację z PlaceholderAPI, umożliwiającą wykorzystanie danych gracza w innych pluginach.

### Dostępne placeholdery

| Placeholder | Opis | Przykład |
|-------------|------|----------|
| `%dropplugin_level%` | Poziom kopania gracza | `15` |
| `%dropplugin_points%` | Aktualne punkty gracza | `750` |
| `%dropplugin_points_required%` | Punkty wymagane do następnego poziomu | `1500` |
| `%dropplugin_points_to_next%` | Punkty brakujące do awansu | `750` |

### Przykłady użycia

**W innych pluginach** (np. DeluxeMenus, FeatherBoard):
```yaml
# Wyświetlanie poziomu w menu
display_name: '&aPoziom kopania: &e%dropplugin_level%'

# Wyświetlanie postępu
lore:
  - '&7Punkty: &e%dropplugin_points%&7/&e%dropplugin_points_required%'
  - '&7Do awansu: &e%dropplugin_points_to_next% pkt'
```

**Instalacja**:
1. Zainstaluj PlaceholderAPI na serwerze
2. Uruchom serwer - DropPlugin automatycznie wykryje PlaceholderAPI
3. Użyj placeholderów w innych pluginach

## 🐛 Znane problemy i rozwiązania

### Problem: "Plugin nie łączy się z bazą danych"
- Sprawdź dane w `config.yml`
- Upewnij się, że baza danych jest uruchomiona i dostępna
- Sprawdź czy użytkownik ma uprawnienia do tworzenia tabel

### Problem: "Gracze tracą poziomy po restarcie"
- Sprawdź logi czy zapis do bazy działa poprawnie
- Upewnij się że plugin się prawidłowo wyłącza (nie używaj `/stop` w trakcie zapisu)

### Problem: "ActionBar nie działa"
- Upewnij się że używasz Paper/Purpur, nie Spigot
- Sprawdź czy w `config.yml` jest `actionbar.status: true`


---

**Autor**: tenfajnybartek  
**Wersja**: 1.0.0-SNAPSHOT  
**Minecraft**: 1.21.4 (Paper)  
**Java**: 21+
