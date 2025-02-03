# Funktionsplotter

## Beschreibung

Der **Funktionsplotter** ist eine Anwendung zur visuellen Darstellung mathematischer Funktionen in einem Koordinatensystem. Er ermöglicht es, verschiedene Funktionen zu plotten, den Anzeigebereich anzupassen, eine Zoom-Funktion zu nutzen und gespeicherte Funktionen zu laden.

## Installation und Nutzung

### Voraussetzungen

* Java Development Kit (JDK) installiert
* JShell für interaktive Nutzung

### Schritte zur Nutzung

1. **JShell im Verzeichnis ********`LiveViewProgramming`******** starten**
2. **Datei ********`funktionsplotter.java`******** öffnen**

   ```shell
   /o funktionsplotter.java
   ```

   Dadurch wird die LVP gestartet und ein **Funktionsplotter-Objekt (********`fp`****\*\*\*\*)** sowie ein **Dokumentations-Objekt (********`doc`****\*\*\*\*)** erstellt.
3. **Generierung der LVP-Dokumentation**

   ```shell
   doc.generate();
   ```
4. **Funktionsplotter starten**

   ```shell
   fp.draw();
   ```

## Szenarien für die Darstellung

### 1. Anzeigebereich anpassen

Mit `szenario1(Turtle t)` können die Grenzen des Koordinatensystems angepasst werden.

```shell
fp.szenario1(doc.t1);
```

### 2. Zoom-Funktion

Die Zoom-Funktion passt das Koordinatensystem und die Graphen an.

```shell
fp.szenario2(doc.t2);
```

### 3. Mehrere Funktionen gleichzeitig darstellen

Bis zu drei Funktionen können gleichzeitig gezeichnet werden.

```shell
fp.szenario3(doc.t3);
```

### 4. Parameterabhängige Funktionen

Die Anwendung unterstützt die Darstellung von Funktionen mit Parametern.

```shell
fp.szenario4(doc.t4);
```

### 5. Funktionen speichern und laden

Der aktuelle Zustand kann gespeichert und später geladen werden.

```shell
fp.szenario5(doc.t5);
fp.save();
fp.load(doc.t5);
```

## Unterstützte Funktionen


| **Lineare Funktionen** | **Potenzfunktionen** | **Trigonometrische Funktionen** |
| ---------------------- | -------------------- | ------------------------------- |
| `x - 2`                | `3x^-2 + 4`          | `sin(x)`,`cos(x)`,`tan(x)`      |
| `-x + 2`               | `-3x^2 - 4`          | `-sin(x)`,`-cos(x)`,`-tan(x)`   |

Weitere mathematische Funktionen sind ebenfalls möglich.

## Reset-Funktion für konsistente Ergebnisse

Vor jedem neuen Test oder Wechsel zwischen Szenarien sollte `fp.resetPlotter(Turtle t)` ausgeführt werden, um eine korrekte Darstellung zu gewährleisten
