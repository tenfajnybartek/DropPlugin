# DropPlugin

Plugin dla serwerów Minecraft (Paper 1.21.4+) wprowadzający zaawansowany system customowego dropu ze stone oraz system poziomów dla graczy.


## 📖 Opis

DropPlugin to kompleksowy plugin do zarządzania dropem z kamienia (stone, granite, diorite, andesite, deepslate) na serwerach Minecraft. Plugin oferuje:

- **Custom drop** - konfigurowalne itemy wypada przy kopaniu stone
- **System poziomów** - gracze zdobywają poziomy i punkty za kopanie
- **Turbo eventy** - zwiększenie szansy na drop i exp (globalne lub per-player)
- **GUI** - intuicyjny interfejs do zarządzania dropami
- **Baza danych** - MySQL/MariaDB z wykorzystaniem HikariCP
- **ActionBar** - informacje o aktywnych eventach
- **Fortune support** - współpraca z enchantami

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

### System dropu

Każdy drop posiada:
- Nazwę i typ itemu
- Szansę na wypadnięcie (z bonusem od fortune i permisji)
- Zakres wysokości spawnu (Y-level)
- Ilość punktów i doświadczenia
- Zakres ilości itemów
- Support dla fortune enchant

## 🔧 Wymagania

- **Java**: 21+
- **Serwer**: Paper 1.21.4+ (lub kompatybilny fork)
- **Baza danych**: MySQL 5.7+ lub MariaDB 10.2+

## 📥 Instalacja

1. Pobierz plik `.jar` z releases lub zbuduj samodzielnie
2. Umieść plik w folderze `plugins/` serwera
3. Skonfiguruj połączenie z bazą danych w `config.yml`
4. Zrestartuj serwer
5. Plugin automatycznie utworzy wymagane tabele w bazie danych

## 🎮 Komendy i uprawnienia

### Komendy dla graczy

| Komenda | Aliasy | Opis | Uprawnienie |
|---------|--------|------|-------------|
| `/drop` | `/stone`, `/kamien` | Otwiera GUI dropu | `dropplugin.cmd.drop` |
| `/level [gracz]` | `/poziom`, `/lvl` | Pokazuje poziom | `tfbhc.cmd.level` / `tfbhc.cmd.alevel` (dla innych) |

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
  - "drop.vip@5"      # +5% do szansy na drop
  - "drop.svip@10"    # +10% do szansy na drop
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
- Wyświetlany w GUI i na ActionBar

### TurboExp
- Podwaja otrzymywane doświadczenie
- Można włączyć globalnie lub dla konkretnego gracza
- Wyświetlany w GUI

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

Plugin wykorzystuje MySQL z pulą połączeń HikariCP.

### Tabela: drop_users

```sql
CREATE TABLE drop_users (
  identifier VARCHAR(255) PRIMARY KEY,    -- UUID gracza
  cobble BOOLEAN NOT NULL,                -- Czy zbiera cobble
  messages BOOLEAN NOT NULL,              -- Czy pokazywać wiadomości
  turboDrop BIGINT(22) NOT NULL,         -- Timestamp końca turbo drop
  turboExp BIGINT(22) NOT NULL,          -- Timestamp końca turbo exp
  lvl INT(11) NOT NULL,                  -- Poziom gracza
  points INT(11) NOT NULL,               -- Punkty gracza
  minedDrops TEXT NOT NULL,              -- Mapa wykopanych dropów
  disabledDrops TEXT NOT NULL,           -- Lista wyłączonych dropów
  lastMessage VARCHAR(255),              -- Ostatnia wiadomość (dla przyszłych funkcji)
  lastSender VARCHAR(255)                -- Ostatni nadawca (dla przyszłych funkcji)
);
```

### Konfiguracja puli połączeń

```yaml
database:
  maxPool: 10                      # Rekomendowane: 5-10 dla małych serwerów
  connectionTimeoutMs: 30000       # 30 sekund
  idleTimeoutMs: 600000            # 10 minut
  leakDetectionThresholdMs: 0      # Wyłączone (włącz >0 dla debugowania)
```

### Automatyczny zapis

- Dane są zapisywane co **5 minut** (6000 ticków)
- Dodatkowo przy wyjściu gracza z serwera
- Przy wyłączaniu pluginu


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

## 📝 Changelog

### v1.0.0-SNAPSHOT
- Pierwsza wersja pluginu
- System custom dropu ze stone
- System poziomów i punktów
- Turbo eventy (drop i exp)
- GUI zarządzania
- Integracja z MySQL przez HikariCP
- ActionBar z informacjami o eventach
- Fortune enchant support
- Bonusy z permisji

## 📄 Licencja

Projekt jest własnością autora. Wszelkie prawa zastrzeżone.

---

**Autor**: tenfajnybartek  
**Wersja**: 1.0.0-SNAPSHOT  
**Minecraft**: 1.21.4 (Paper)  
**Java**: 21+
