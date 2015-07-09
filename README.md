## Steuerung eines Arduino per Firmata (mit Java) über USB-Verbindung von einem PC aus mit Webfrontend (vertx)

Diesen Text findet man auch als PDF-Datei [hier](https://github.com/menzelths/firmata/blob/master/src/main/resources/FirmataMitLCD/Anleitung.pdf).


### Vorbemerkung:
Der Einsatz des Firmata-Protokolls erlaubt es, von einem PC aus mittels Java den Arduino direkt zu steuern. Beim Testen hat sich herausgestellt, dass einige Arduino-Nachbauten von Drittarbeitern das Firmata-Protokoll nicht unterstützen. Der Arduino UNO R3 als Original unterstützt das Protokoll jedoch problemlos.

Die Steuerung per Java ist erfreulicherweise sogar von einem Raspberry PI aus möglich (Modell B 2), indem die kompilierte jar-Datei dort mittels der aktuellen Java-JRE von Oracle gestartet werden kann. Näheres dazu weiter unten.

### Kurzbeschreibung:
Der Aufbau erlaubt es, ein Arduino-Board mittels Java von einem PC (oder Raspberry PI) aus über die USB-Schnittstelle direkt anzusprechen. Dabei ist es möglich, Pins ein- und auszulesen, sowie Textnachrichten an das Board zu senden und zu empfangen.
Darüber hinaus wurde mit Hilfe der vertx-Bibliothek ein Webfrontend zur Steuerung des Boards und zur Ausgabe von Informationen erstellt. Dieses Frontend ist über localhost:<port> im Browser aufrufbar, wobei <port> durch den gewählten Wert zu ersetzen ist (siehe weiter unten).
Dadurch ist die Steuerung des Arduino auch über eine Webseite möglich. Die Spannung am Fotowiderstand wird per Websocket in Echtzeit an die Webseite übertragen. Dies gelingt hier besonders einfach unter Einsatz der hervorragenden vertx-Bibliothek.

Für die Steuerung des Displays wurde die Datei FirmataStandard aus den Arduino-Beispielen entsprechend für Textempfang und Display-Steuerung angepasst. Diese veränderte Datei befindet sich im „Ressources“-Verzeichnis im Unterordner „FirmataMitLCD“. Sie muss zur Steuerung des Arduino auf diesen vor Start des Java-Programms hochgeladen werden.

Firmata wird von Java aus mit Hilfe der Bibliothek Firmata4J umgesetzt. I2C ist in dieser Bibliothek laut deren Autor noch nicht implementiert, könnte aber mit Hilfe geeigneter Textbefehle an das Arduino-Board umgesetzt werden, so wie bei der Ansteuerung des LC-Displays.

## Aufruf:
Nach dem Kompilieren steht die Datei firmata-1.0-SNAPSHOT-fat.jar im Verzeichnis target zur Verfügung. Diese kann mit einer aktuellen Java-JRE (mindestens Java 8) über die Kommandozeile gestartet werden:

> java -jar firmata-1.0-SNAPSHOT-fat.jar

startet im Modus 0, im Programm für den Mac eingestellt. Als Port für den Server wird 8080 verwendet, als Adresse für den Arduino „/dev/tty.usbmodem1411“.

> sudo java -jar firmata-1.0-SNAPSHOT-fat.jar 1

startet im Modus 1, also für den Raspberry Pi eingestellt. Port ist hier Port 80, so dass das Programm als Superuser gestartet werden sollte (mit dem vorangestelltem sudo). Für andere Ports muss die Firewall des Raspberry PI angepasst werden.

> java -jar firmata-1.0-SNAPSHOT-fat.jar COM3 8080

startet den Server auf Port 8080 und versucht, den angeschlossenen Arduino über die Schnittstelle COM3 anzusprechen.

## Verkabelung:

![Arduino-Verkabelung](https://github.com/menzelths/firmata/blob/master/src/main/resources/FirmataMitLCD/firmataDisplay.png)

## Links:
- [Firmata4J](https://github.com/kurbatov/firmata4j)
- [vertx-Toolkit](http://vertx.io/)
- [Arduino-Homepage](https://www.arduino.cc/)
- [Java für ARM (Raspberry PI)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-arm-downloads-2187472.html)
- [Fritzing](http://fritzing.org/home/) zur Erstellung von Schaltbildern

