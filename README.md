# traffic-lights-simulator

Symulator świateł drogowych mający za zadanie przetestować działanie różnych typów skrzyżowań z sygnalizacją świetlną i porównywać ich wydajność, a także ułatwia dopasowanie parametrów algorytmu optymalizacyjnego przepływ samochodowy na wybranych skrzyżowaniach.


## Struktura symulacji


Struktura symulacji jest ściśle związana z wymaganiami dotyczącymi jej działania. Architektura silnika symulatora została zaprojektowana w oparciu o podejście **data-driven**. Wejściem do systemu jest plik JSON zawierający sekwencyjną listę komend.

### Start systemu

Kluczowym elementem mechanizmu wczytywania jest **polimorficzna deserializacja**. System automatycznie mapuje poszczególne komendy z pliku na instancje odpowiednich komend - `AddVehicleCommand` oraz `StepCommand` na podstawie deklaracji typu. Dzięki temu pozbywamy się rozbudowanych drabinek if-else oraz takie podejście ułatwia rozszerzanie funkcjonalności systemu o kolejne komendy, zgodnie z kluczową zasadą **Open/Closed Principle** z zestawu ***SOLID***. 


### Model Domenowy

Stan i fizyczna struktura skrzyżowania zostały zamodelowane z naciskiem na precyzyjne odzwierciedlenie rzeczywistości. Pakiet `model` dzieli się na dwie główne odpowiedzialności:

* **Infrastruktura:** skrzyżowanie opiera się na klasie `Lane` reprezentującą pas ruchu, która przechowuje strukturę z oczekującymi pojazdami. Kierunki wlotów oraz dozwolone manewry są ściśle kontrolowane przez typy enum (`Direction`, `Turn`, `TurnGroup`). Dzięki temu weryfikacja tego, czy dany pojazd może wykonać zaplanowany ruch z konkretnego pasa, jest niezawodna i zrozumiała
* **Sygnalizacja Świetlna:** reprezentowana przez klasę `TrafficLight`, której zachowanie determinują enumy `LightState` (RED, GREEN, ORANGE, NONFUNCIONAL), `LightType` (GENERAL, DIRECTIONAL, CONDITIONAL) oraz `TurnGroup`, który reprezentuje wszystkie możliwe kombinacje dozwolonych skrętów. Rozdzielenie typu światła od jego obecnego stanu pozwala na łatwe symulowanie m.in. sygnalizatorów ze strzałkami warunkowymi bez komplikowania głównej logiki


### Logika skrzyżowań 

Pakiet `intersection` stanowi architektoniczne serce całego symulatora. Odpowiada za zarządzanie ruchem, fizyczną topologią dróg oraz zaawansowaną decyzyjność dotyczącą zmiany świateł. Architektura tego modułu opiera się na obiektowym podejściu oraz wykorzystuje popularne wzorce projektowe.

**Główne komponenty:**

* **`Intersection` (Klasa abstrakcyjna)** – zawiera główne funkcjonalności charakterystyczne dla każdego typu skrzyżowań. Przechowuje aktualny stan, zarządza cyklem życia skrzyżowania i deleguje specjalistyczne zadania do innych klas. Implementuje interfejs dostarczający metryki - `PhaseMetricsProvider`, łącząc warstwę fizyczną z obliczeniową.
* **`SingleLaneIntersection` oraz `MultiLaneIntersection`** – konkretne implementacje skrzyżowań. Odpowiadają za fizyczną reprezentację infrastruktury (pasy ruchu, kolejki pojazdów). Znajduje się tu logika określająca zasady zarządzania światłami, reguły pierwszeństwa, takie jak zachowanie przy strzałkach warunkowych albo bezkolizyjnych.
* **`IntersectionFactory`** – wykorzystuje wzorzec fabryki. Izoluje proces tworzenia obiektu od jego użycia. Odpowiada za odczytanie żądanego typu skrzyżowania z pliku konfiguracyjnego i wstrzyknięcie do niego odpowiednich zależności oraz faz.
* **`IntersectionStats`** – dedykowany kontener na dane analityczne. Zbiera w czasie rzeczywistym metryki takie jak: czas trwania faz, liczba obsłużonych pojazdów czy sumaryczny czas oczekiwania.

**Pakiet `phase`:**
katalog ten zawiera obliczenia optymalizacyjne dla skrzyżowania. Zamiast zaszywać obliczenia optymalizacyjne w klasach infrastrukturalnych, wydzieliłem je do osobnych struktur:
* Znajdują się tu klasy takie jak `IntersectionPhase` (przechowująca dozwolone skręty i czasy dla danej fazy) oraz implementacje interfejsu `PhaseScheduler`, które w moim przypadku zawierają się w klasie `HybridPhaseScheduler` implementującej logikę zachowania sygnalizacji.
* Scheduler na bieżąco analizuje natężenie ruchu i dynamicznie oblicza priorytety oraz optymalny czas trwania zielonego światła.



### Data Transfer Object

Pakiet `dto` pełni rolę ścisłej pośrednika między plikami JSON z gotową konfiguracją lub danymi wejściowymi a wewnętrzną logiką symulatora. Takie rozdzielenie warstw chroni działanie symulacji przed zmianami w danych wejściowych oraz wyjściowych.

* **Niemutowalność:** wszystkie obiekty DTO zostały zaimplementowane jako **Java Records**. Gwarantuje to niemutowalność odczytanych danych, więc są one odporne na przypadkowe modyfikacje podczas trwania symulacji
* **Czyste mapowanie Input/Output:** pakiet zawiera precyzyjne odzwierciedlenie struktur plików konfiguracyjnych. Klasy główne, takie jak `SimulationInput` oraz `SimulationOutput`, mapują się na zadany w poleceiu format JSON, co sprawia, że proces serializacji i deserializacji jest przejrzysty i bezbłędny
* **Hermetyzacja konfiguracji:** wyodrębniony subpakiet `intersection` logicznie grupuje parametry konfiguracyjne dotyczące skrzyżowań - jego typu, rozróżnienia faz oraz wartości parametrów optymalizacyjnych 

### Narzędzia pomocnicze

Aby zachować pełną zgodność z zasadą pojedynczej odpowiedzialności, z głównego silnika symulacji wydzielono wszelkie narzędzia techniczne takie jak:

* **Parsery - `utils/InputParser`, `utils/OutputParser`:** klasy odpowiedzialne za wczytanie danych wejściowych oraz zapis danych wyjściowych symulacji
* **Konfiguracja skrzyżowań - `config/IntersectionConfig`:** klasa odpowiedzialna za wczytanie danych konfiguracyjnych symulacje - typy skrzyżowań, parametry optymalizacji ruchu drogowego
* **Wyjątki - `exception/UndeclaredNextStateException`:** definicja własnego typu wyjątków w celu zwiększenia bezpieczeństwa oraz przejrzystości kodu



## Algorytm optymalizujący ruch drogowy

Algorytm optymalizacyjny został zaprojektowany z myślą o kompromisie pomiędzy jak największym zwiększeniem przepustowości danego skrzyżowania oraz wyeliminowaniu sytuacji, w której jeden kierowca na niezatłoczonej drodze będzie czekał w nieskończoność.


### Algorytm proporcjonalny

Zamiast sztywno przypisywać stały czas trwania do każdego cyklu świateł, symulator wykorzystuje algorytm proporcjonalny do dynamicznego obliczania optymalnego czasu trwania danej fazy - zielonego światła.

* **Zasada działania:** na etapie aktywacji nowej fazy, system analizuje całkowite zagęszczenie ruchu na skrzyżowaniu. Czas zielonego światła jest następnie przydzielany proporcjonalnie do ilośći pojazdów, które są uprawnione do jazdy, względem wszystkich pojazdów oczekujących na skrzyżowaniu.
* **Reprezentacja matematyczna:**
  $$T_{optimal} = \left( \frac{Q_{phase}}{Q_{total}} \right) \times T_{cycle\_basic}$$
  Gdzie $Q_{phase}$ to liczba pojazdów uprawnionych do opuszczenia skrzyżowania w danej fazie, $Q_{total}$ to całkowita liczba aut na skrzyżowaniu, a $T_{cycle\_basic}$ to maksymalny, bazowy czas trwania cyklu świateł.
* **Korzyści:** skrzyżowanie natychmiast adaptuje się do nierównomiernego obciążenia np. podczas porannych godzin szczytu, czy eventu w bliskiej odległości.


### Podejście hybrydowe 

Wyżej opisany algorytm maksymalizuje ilość pojazdów, które przejadą przez skrzyżowanie w czasie, jednakże jest podatny na wystąpienie zjawiska tzw. *starvation*. Aby zapobiec sytuacjom, w których pojedynczy pojazd czeka w nieskończoność na zielone światło, zaimplementowałem mechanizm przydzielania następnej fazy świateł z użyciem odpowiednich wag.

* **Zasada działania:** kolejność faz nie jest deterministyczna. W momencie wygaszania aktualnych świateł, system skanuje wszystkie pozostałe fazy i dla każdej z nich wylicza wagowy wskaźnik priorytetu.
* **Ochrona przed *starvation*:** wzór decyzyjny bierze pod uwagę nie tylko liczbę aut w kolejce, ale również czas oczekiwania najdłużej stojącego pojazdu:
  $$Priority = (Q_{waiting} \times W_{queue}) + (T_{max\_wait} \times W_{time})$$
  Dzięki zastosowaniu powyższego mnożnika w postaci wag jesteśmy w stanie manipulować działaniem algorytmu z zależności od tego, czy większy priorytet chcemy przypisać maksymalizacji przepustowości skrzyżowania, czy sprawiedliwemu przepuszczeniu kierowców.


### Uwaga

W skrzyżowaniu jednopasmowym ten algorytm lekko przekłamywał rzeczywistość z powodu blokowania się aut. Na skrzyżowaniu wielopasmowym ten problem jest znacznie mniejszy. Ze względu na fakt, że pojazdy we wcześniejszych krokach inteligentnie rozdzieliły się na odpowiednie pasy (zgodnie z manewrami, jakie chcą wykonać), algorytm staje się tutaj znacznie bardziej precyzyjny.

## Uruchomienie symulacji 

Aplikacja została skonfigurowana tak, aby dało się ją uruchomić jednym poleceniem. Należy uruchomić skompilowany kod za pomocą komendy:

```bash
java -jar simulator.jar input.json output.json [intersection_type]
```
#### Parametry polecenia:
* `input.json` – ścieżka do istniejącego pliku z listą komend do wykonania
* `output.json` – ścieżka, pod którą symulator zapisze plik JSON zawierający wynik symulacji
* `[intersection_type]` (opcjonalny) – flaga definiująca rodzaj skrzyżowania (np. MULTI_MAJOR_ARTERIAL). Jeśli ten argument zostanie pominięty, aplikacja automatycznie uruchomi się dla typu standard. Dostępny typy skrzyżowań dostępne są w pliku `simulator/data/config/intersection_config.json`.
## Testy

Niezawodność i stabilność symulatora została zagwarantowana dzięki pokryciu kodu testami jednostkowymi oraz integracyjnymi. Testy jednostkowe weryfikują poprawność kluczowych mechanizmów domenowych, takich jak wyliczanie priorytetów faz, poprawność matematyczna algorytmu proporcjonalnego czy poprawność wczytywania danych wejściowych. Z kolei testy integracyjne sprawdzają całościowe działanie systemu - wielokrokowe wykonanie sekwencji zdarzeń na skrzyżowaniu, walidację formatu i logiki wygenerowanych danych wyjściowych. Dodatkowo, zastosowałem pipeline CI/CD - GitHub Actions, co zapewnia natychmiastową weryfikację poprawności kodu przy każdej modyfikacji.

### Lokalne uruchomienie testów

```bash
cd ./simulator
mvn clean verify
```


## Stos technologiczny

Projekt został zbudowany z wykorzystaniem nowoczesnych narzędzi ekosystemu Javy:

* **Java 21** — język programowania. Wykorzystano funkcjonalności języka takie jak *Java Records* czy rozbudowane *Stream API*
* **Apache Maven** — narzędzie do zarządzania cyklem życia projektu, zależnościami oraz budowaniem aplikacji
* **Lombok** — biblioteka wykorzystana do znacznej redukcji boilerplate kodu
* **JUnit 5 & Mockito** — zestaw do testowania logiki symulacji. JUnit posłużył do budowy testów jednostkowych i integracyjnych, natomiast Mockito umożliwiło izolację komponentów bez konieczności używania całego systemu
* **GitHub Actions (CI/CD)** — zautomatyzowany pipeline, który przy każdej operacji *push* oraz *pull request*, kompiluje aplikację i weryfikuje poprawność testów


## Przyszłe możliwości rozwojowe symulacji

Obecna architektura symulatora stanowi solidne fundamenty dla bardziej zaawansowanych scenariuszy drogowych. Otwarte są następujące ścieżki rozwoju symulacji:

* **Tryb awaryjny świateł - NONFUNCTIONAL:** wprowadzenie stanu awarii, w którym system dynamicznie przechodzi na klasyczne przepisy określone znakami na skrzyżowaniu. Pozwoliłoby to badać wpływ awarii sygnalizacji na powstawanie zatorów drogowych
* **Graficzny interfejs:** utworzenie aplikacji webowej lub desktopowej wizualizującej krok po kroku działanie aplikacji. Infrastruktura "fizyczna" jest przygotowana oraz zsychronizowana z główną logiką symulacji, więc dodanie interfejsy graficznego nie byłoby dużym problemem
* **Obsługa rond:** dodanie nowej klasy skrzyżowania działającej w sposób wielopasmowego ronda z sygnalizacją świetlną
* **Optymalizacja hiperparametrów:** wdrożenie narzędzia do uruchamiania symulacji seryjnych w celu zautomatyzowanego testowania różnych wag dla `HybridPhaseScheduler`
* **Benchmarking natężenia ruchu:** stworzenie profili ruchu np. poranny szczyt, ruch nocny i przepuszczanie ich przez różne topologie skrzyżowań. Symulacja pomogłaby wybrać najbardziej optymalne skrzyżowanie dla danej charakterystyki ruchu


## Autor

**Konrad Ćwięka** - konrad4cwieka@gmail.com

