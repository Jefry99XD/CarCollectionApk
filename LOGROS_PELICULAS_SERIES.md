# 🎬 Logros de Películas y Series - Car Collection App

Lista de logros basados en vehículos icónicos del cine, series de TV y anime.
Cada logro requiere registrar el/los carro(s) específico(s) en la colección.

---

## 🏁 FORMATO DE CADA LOGRO

| Campo | Descripción |
|-------|-------------|
| **Meta** | Cantidad de carros requeridos |
| **Concepto** | Texto a buscar en el campo indicado |
| **Campos** | NAME / BRAND / TAGS / SERIE |
| **Tipo** | EXACT / CONTAINS |
| **Lógica** | AND (todos) / OR (cualquiera) |
| **Dificultad** | Fácil / Media / Difícil / Épico |

---

## 🚗 FAST & FURIOUS — Colección Completa

### Por Entrega

1. **The Original Toretto** — *Fast & Furious (2001)*
   - Registra el Dodge Charger R/T 1970 de Dom
   - Meta: 1 carro
   - Concepto: `charger`, `dodge charger`, `1970 charger`
   - Campos: NAME, TAGS
   - Tipo: CONTAINS
   - Dificultad: **Fácil**

2. **Race Wars Champion** — *Fast & Furious (2001)*
   - Registra el Toyota Supra MK4 de Brian O'Conner
   - Meta: 1 carro
   - Concepto: `supra`, `mk4`, `toyota supra`
   - Campos: NAME
   - Tipo: CONTAINS
   - Dificultad: **Fácil**

3. **Mitsubishi Eclipse Runner** — *Fast & Furious (2001)*
   - Registra el Mitsubishi Eclipse GS de Brian
   - Meta: 1 carro
   - Concepto: `eclipse`, `mitsubishi eclipse`
   - Campos: NAME, BRAND
   - Tipo: CONTAINS
   - Dificultad: **Fácil**

4. **Supra del Millón** — *2 Fast 2 Furious (2003)*
   - Registra el Mitsubishi Evo VII de Brian y el Nissan Skyline de Roman
   - Meta: 2 carros (lógica OR)
   - Concepto: `evo`, `skyline`, `evolution`
   - Campos: NAME
   - Tipo: CONTAINS
   - Dificultad: **Media**

5. **Miami Drift** — *2 Fast 2 Furious (2003)*
   - Registra el Mitsubishi Eclipse Spyder de Brian
   - Meta: 1 carro
   - Concepto: `eclipse spyder`, `eclipse`
   - Campos: NAME
   - Tipo: CONTAINS
   - Dificultad: **Fácil**

6. **Tokyo Drifter** — *The Fast and the Furious: Tokyo Drift (2006)*
   - Registra el Mazda RX-7 Veilside de Han
   - Meta: 1 carro
   - Concepto: `rx-7`, `rx7`, `veilside`
   - Campos: NAME, TAGS
   - Tipo: CONTAINS
   - Dificultad: **Media**

7. **Drift King** — *Tokyo Drift (2006)*
   - Registra el Nissan Silvia S15 de Takeshi (Drift King)
   - Meta: 1 carro
   - Concepto: `silvia`, `s15`, `nissan silvia`
   - Campos: NAME
   - Tipo: CONTAINS
   - Dificultad: **Media**

8. **Monte Carlo Mustang** — *Tokyo Drift (2006)*
   - Registra el Ford Mustang Fastback 1967 de Sean
   - Meta: 1 carro
   - Concepto: `mustang fastback`, `1967 mustang`, `mustang`
   - Campos: NAME, TAGS
   - Tipo: CONTAINS
   - Dificultad: **Media**

9. **Han's Legacy** — *Fast & Furious 4 (2009)*
   - Registra 3 carros asociados a Han (RX-7, Eclipse, Toyota)
   - Meta: 3 carros (lógica OR)
   - Concepto: `rx-7`, `eclipse`, `han`
   - Campos: NAME, TAGS
   - Tipo: CONTAINS
   - Dificultad: **Difícil**

10. **Charger Coronado** — *Fast Five (2011)*
    - Registra el Dodge Charger SRT8 de Toretto
    - Meta: 1 carro
    - Concepto: `charger srt8`, `charger`, `dodge`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

11. **Fast Five Bank Heist** — *Fast Five (2011)*
    - Registra el Koenigsegg CCX de Shaw y el Charger de Dom
    - Meta: 2 carros (lógica OR)
    - Concepto: `koenigsegg`, `charger`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

12. **GT-R Saga** — *Fast & Furious 6 (2013)*
    - Registra el Nissan GT-R de Letty
    - Meta: 1 carro
    - Concepto: `gt-r`, `gtr`, `nissan gt-r`
    - Campos: NAME
    - Tipo: CONTAINS
    - Dificultad: **Media**

13. **London Chase** — *Fast & Furious 6 (2013)*
    - Registra el Jaguar XKR y el Bentley Mulsanne del villano Owen Shaw
    - Meta: 2 carros (lógica OR)
    - Concepto: `jaguar xkr`, `bentley mulsanne`, `bentley`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

14. **Furious Seven — Brian's Last Ride** — *Furious 7 (2015)*
    - Registra el Nissan GT-R R35 de Brian como homenaje
    - Meta: 1 carro
    - Concepto: `gt-r r35`, `gtr r35`, `nissan`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Media**

15. **Lykan Hypersport Jump** — *Furious 7 (2015)*
    - Registra el W Motors Lykan Hypersport (el que saltó entre edificios)
    - Meta: 1 carro
    - Concepto: `lykan`, `lykan hypersport`, `w motors`
    - Campos: NAME, BRAND, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

16. **Ramsey's Hacking Machine** — *Furious 7 (2015)*
    - Registra el Mercedes-Benz SLS AMG de Ramsey
    - Meta: 1 carro
    - Concepto: `sls`, `mercedes sls`, `sls amg`
    - Campos: NAME
    - Tipo: CONTAINS
    - Dificultad: **Media**

17. **Cipher's Submarine Fleet** — *The Fate of the Furious (2017)*
    - Registra 5 carros controlados remotamente (cualquier carro con tag "remote" o similar)
    - Meta: 5 carros
    - Concepto: `fate`, `cipher`
    - Campos: SERIE, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

18. **Ice Road Charger** — *The Fate of the Furious (2017)*
    - Registra el Dodge Ice Charger de Dom
    - Meta: 1 carro
    - Concepto: `ice charger`, `dodge charger`, `charger`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

19. **Hobbs & Shaw Showdown** — *Hobbs & Shaw (2019)*
    - Registra el McLaren 720S de Deckard Shaw
    - Meta: 1 carro
    - Concepto: `720s`, `mclaren 720`, `mclaren`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

20. **F9 Magneto Car** — *F9 (2021)*
    - Registra el Pontiac Fiero modificado de Jakob Toretto
    - Meta: 1 carro
    - Concepto: `fiero`, `pontiac fiero`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Media**

21. **Familia Completa** — *Fast & Furious Saga*
    - Registra al menos 10 carros icónicos de la saga
    - Meta: 10 carros
    - Concepto: `fast`, `furious`, `f&f`
    - Campos: SERIE, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Épico**

---

## ⏱️ CLÁSICOS DE CARRERAS Y ACCIÓN

22. **Bullitt Mustang** — *Bullitt (1968)*
    - Registra el Ford Mustang GT390 Fastback 1968 de Steve McQueen
    - Meta: 1 carro
    - Concepto: `bullitt`, `mustang gt`, `1968 mustang`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

23. **Bandit's Trans Am** — *Smokey and the Bandit (1977)*
    - Registra el Pontiac Firebird Trans Am 1977
    - Meta: 1 carro
    - Concepto: `trans am`, `firebird`, `pontiac trans am`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

24. **The Bluesmobile** — *The Blues Brothers (1980)*
    - Registra el Dodge Monaco 1974 "Bluesmobile"
    - Meta: 1 carro
    - Concepto: `bluesmobile`, `dodge monaco`, `monaco`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

25. **Eleanor — Gone in 60 Seconds** — *Gone in 60 Seconds (2000)*
    - Registra el Ford Mustang Shelby GT500 "Eleanor"
    - Meta: 1 carro
    - Concepto: `eleanor`, `gt500`, `shelby gt500`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

26. **Eleanor Collection** — *Gone in 60 Seconds*
    - Registra 5 Mustang Shelby GT500
    - Meta: 5 carros
    - Concepto: `eleanor`, `gt500`, `shelby`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

27. **The Italian Job — Mini Heist** — *The Italian Job (2003)*
    - Registra 3 Mini Cooper S de distintos colores (rojo, blanco, azul)
    - Meta: 3 carros (lógica AND por color)
    - Concepto: `mini cooper`, `mini`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Media**

28. **Baby Driver's WRX** — *Baby Driver (2017)*
    - Registra el Subaru WRX de Baby
    - Meta: 1 carro
    - Concepto: `wrx`, `subaru wrx`, `impreza wrx`
    - Campos: NAME
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

29. **Drive — Scorpion Jacket** — *Drive (2011)*
    - Registra el Chevrolet Impala 1973 del Driver
    - Meta: 1 carro
    - Concepto: `impala 1973`, `chevy impala`, `impala`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Media**

30. **Ronin — BMW E34 Chase** — *Ronin (1998)*
    - Registra el BMW 535i E34 de Vincent
    - Meta: 1 carro
    - Concepto: `535i`, `bmw 535`, `e34`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

31. **Le Mans — 917K** — *Le Mans (1971)*
    - Registra el Porsche 917K de Steve McQueen
    - Meta: 1 carro
    - Concepto: `917`, `porsche 917`, `917k`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

32. **Rush — Lauda vs Hunt** — *Rush (2013)*
    - Registra el Ferrari 312T de Niki Lauda y el McLaren M23 de James Hunt
    - Meta: 2 carros (lógica OR)
    - Concepto: `312t`, `m23`, `lauda`, `hunt`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

33. **Ford v Ferrari** — *Ford v Ferrari (2019)*
    - Registra el Ford GT40 Mk II y el Ferrari 330 P3
    - Meta: 2 carros (lógica OR)
    - Concepto: `gt40`, `330 p3`, `ford gt40`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

34. **Grease Lightning** — *Grease (1978)*
    - Registra el Packard Custom convertible "Greased Lightning"
    - Meta: 1 carro
    - Concepto: `greased lightning`, `packard`, `grease`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

---

## 🔮 CIENCIA FICCIÓN Y FANTASÍA

35. **DeLorean Time Machine** — *Back to the Future (1985)*
    - Registra el DeLorean DMC-12 con el condensador de fluzo
    - Meta: 1 carro
    - Concepto: `delorean`, `dmc-12`, `dmc12`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

36. **DeLorean Trilogy** — *Back to the Future (1985/89)*
    - Registra 3 DeLorean (el mismo modelo 3 veces = los 3 viajes)
    - Meta: 3 carros
    - Concepto: `delorean`, `dmc`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Media**

37. **Bumblebee Awakens** — *Transformers (2007)*
    - Registra el Chevrolet Camaro Concept 2006 (Bumblebee)
    - Meta: 1 carro
    - Concepto: `bumblebee`, `camaro`, `transformers`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

38. **Autobots Assemble** — *Transformers Saga*
    - Registra 5 carros de la saga Transformers
    - Meta: 5 carros
    - Concepto: `transformers`, `autobot`
    - Campos: SERIE, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

39. **Total Recall — Johnny Cab** — *Total Recall (1990)*
    - Registra 1 taxi/cab robótico
    - Meta: 1 carro
    - Concepto: `johnny cab`, `taxi`, `total recall`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

40. **Mad Max Interceptor** — *Mad Max (1979)*
    - Registra el Ford Falcon XB GT "V8 Interceptor"
    - Meta: 1 carro
    - Concepto: `interceptor`, `falcon xb`, `v8 interceptor`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

41. **Fury Road Armada** — *Mad Max: Fury Road (2015)*
    - Registra 5 vehículos post-apocalípticos
    - Meta: 5 carros
    - Concepto: `mad max`, `fury road`, `war boy`
    - Campos: SERIE, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

42. **Batmobile Classic** — *Batman (1989)*
    - Registra el Batmobile de Tim Burton
    - Meta: 1 carro
    - Concepto: `batmobile`, `batman`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

43. **The Tumbler** — *The Dark Knight (2005)*
    - Registra el Batmobile Tumbler de Nolan
    - Meta: 1 carro
    - Concepto: `tumbler`, `dark knight`, `batmobile`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

44. **Bat Fleet** — *Batman Saga*
    - Registra 3 versiones del Batmobile (distintas películas)
    - Meta: 3 carros
    - Concepto: `batmobile`, `tumbler`, `batman`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

45. **Jurassic Tour Vehicle** — *Jurassic Park (1993)*
    - Registra el Ford Explorer Tour Vehicle 1992
    - Meta: 1 carro
    - Concepto: `explorer`, `ford explorer`, `jurassic`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

46. **James Bond — Aston Martin DB5** — *Goldfinger (1964)*
    - Registra el Aston Martin DB5 de James Bond
    - Meta: 1 carro
    - Concepto: `db5`, `aston martin db5`, `bond`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

47. **James Bond Fleet** — *James Bond Saga*
    - Registra 5 carros icónicos de James Bond (DB5, Lotus Esprit, etc.)
    - Meta: 5 carros (lógica OR)
    - Concepto: `bond`, `db5`, `lotus esprit`, `aston`
    - Campos: NAME, TAGS, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Épico**

48. **Lotus Esprit Submarine** — *The Spy Who Loved Me (1977)*
    - Registra el Lotus Esprit S1 de Bond
    - Meta: 1 carro
    - Concepto: `esprit`, `lotus esprit`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

---

## 📺 SERIES DE TV

49. **General Lee** — *The Dukes of Hazzard*
    - Registra el Dodge Charger 1969 naranja "General Lee"
    - Meta: 1 carro
    - Concepto: `general lee`, `charger 1969`, `dukes`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

50. **KITT — Knight Industries** — *Knight Rider*
    - Registra el Pontiac Trans Am 1982 "KITT"
    - Meta: 1 carro
    - Concepto: `kitt`, `knight rider`, `trans am`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

51. **A-Team Van** — *The A-Team*
    - Registra el GMC G-Series Van "A-Team"
    - Meta: 1 carro
    - Concepto: `a-team`, `gmc van`, `g-series`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

52. **Magnum P.I. Ferrari** — *Magnum P.I.*
    - Registra el Ferrari 308 GTS de Magnum
    - Meta: 1 carro
    - Concepto: `308 gts`, `ferrari 308`, `magnum`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

53. **Miami Vice Testarossa** — *Miami Vice*
    - Registra el Ferrari Testarossa blanco de Sonny Crockett
    - Meta: 1 carro
    - Concepto: `testarossa`, `ferrari testarossa`, `miami vice`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

54. **Starsky & Hutch Gran Torino** — *Starsky & Hutch*
    - Registra el Ford Gran Torino rojo 1976
    - Meta: 1 carro
    - Concepto: `gran torino`, `ford torino`, `starsky`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

55. **Banacek's Rolls** — *Banacek*
    - Registra cualquier Rolls-Royce
    - Meta: 1 carro
    - Concepto: `rolls`, `rolls-royce`, `rolls royce`
    - Campos: BRAND
    - Tipo: CONTAINS
    - Dificultad: **Media**

56. **Breaking Bad — Aztek** — *Breaking Bad*
    - Registra el Pontiac Aztek 2004 de Walter White
    - Meta: 1 carro
    - Concepto: `aztek`, `pontiac aztek`, `breaking bad`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

57. **Breaking Bad — RV** — *Breaking Bad*
    - Registra el Fleetwood Bounder RV "The Lab"
    - Meta: 1 carro
    - Concepto: `rv`, `fleetwood`, `breaking bad rv`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

58. **The Walking Dead — Hyundai** — *The Walking Dead*
    - Registra 5 Hyundai (Hyundai fue el carro "oficial" de la serie)
    - Meta: 5 carros
    - Concepto: `hyundai`, `sonata`, `tucson`
    - Campos: BRAND, NAME
    - Tipo: CONTAINS
    - Dificultad: **Media**

59. **Supernatural — Baby** — *Supernatural*
    - Registra el Chevrolet Impala 1967 "Baby" de Dean Winchester
    - Meta: 1 carro
    - Concepto: `impala 1967`, `chevy impala`, `supernatural`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

60. **Stranger Things — Family Wagon** — *Stranger Things*
    - Registra el Chevrolet K5 Blazer de Hopper o el Ford Pinto de Joyce
    - Meta: 1 carro (lógica OR)
    - Concepto: `blazer`, `k5`, `ford pinto`, `stranger things`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

---

## 🎌 ANIME Y MANGA

61. **Tofu Delivery Man** — *Initial D*
    - Registra el Toyota AE86 Sprinter Trueno de Takumi Fujiwara
    - Meta: 1 carro
    - Concepto: `ae86`, `trueno`, `sprinter trueno`, `initial d`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

62. **AE86 Army** — *Initial D*
    - Registra 5 Toyota AE86
    - Meta: 5 carros
    - Concepto: `ae86`, `trueno`, `sprinter`
    - Campos: NAME
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

63. **Akina Speed Stars Fleet** — *Initial D*
    - Registra el AE86, Mazda RX-7 FC de Keisuke, Toyota 86, FC3S
    - Meta: 4 carros (lógica OR)
    - Concepto: `ae86`, `rx-7 fc`, `fc3s`, `86`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

64. **Devil Z** — *Wangan Midnight*
    - Registra el Nissan Fairlady Z S30 (Devil Z)
    - Meta: 1 carro
    - Concepto: `fairlady z`, `s30`, `240z`, `devil z`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

65. **Mach 5 Go Go Go!** — *Speed Racer*
    - Registra el Mach 5 de Speed Racer
    - Meta: 1 carro
    - Concepto: `mach 5`, `mach5`, `speed racer`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

66. **Redline Racer** — *Redline (2009)*
    - Registra el Trans AM de JP del anime Redline
    - Meta: 1 carro
    - Concepto: `trans am`, `redline`, `jp`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

67. **Gurren Lagann Gunman** — *Gurren Lagann*
    - Registra cualquier carro/vehículo mecha o futurista
    - Meta: 1 carro
    - Concepto: `mecha`, `gurren`, `futurista`
    - Campos: TAGS, TYPE
    - Tipo: CONTAINS
    - Dificultad: **Media**

68. **Lupin III — Fiat 500** — *Lupin III*
    - Registra el Fiat 500 amarillo de Lupin
    - Meta: 1 carro
    - Concepto: `fiat 500`, `lupin`, `fiat`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

69. **Kino's Journey** — *Kino no Tabi*
    - Registra cualquier moto o vehículo de viaje
    - Meta: 1 carro
    - Concepto: `kino`, `motocicleta`, `motorcycle`
    - Campos: NAME, TYPE, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

70. **No Game No Life — Shiro's Ride** — *Temática Anime*
    - Registra 10 carros de anime/cultura otaku
    - Meta: 10 carros
    - Concepto: `anime`
    - Campos: TAGS, SERIE
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

---

## 🕹️ VIDEOJUEGOS CON CARROS ICÓNICOS

71. **Warthog — Halo** — *Halo*
    - Registra el M12 Warthog de Halo
    - Meta: 1 carro
    - Concepto: `warthog`, `halo`, `m12`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

72. **Outrun Ferrari** — *OutRun (1986)*
    - Registra el Ferrari Testarossa Spider de OutRun
    - Meta: 1 carro
    - Concepto: `outrun`, `testarossa spider`, `testarossa`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

73. **Gran Turismo Dream Car** — *Gran Turismo*
    - Registra el Dodge Viper SRT-10 o el Nissan GT-R (icónicos de GT)
    - Meta: 1 carro (lógica OR)
    - Concepto: `viper`, `gt-r`, `gran turismo`
    - Campos: NAME, TAGS, SERIE
    - Tipo: CONTAINS
    - Dificultad: **Media**

74. **Need for Speed — Black Edition** — *NFS Most Wanted*
    - Registra el BMW M3 GTR de NFS Most Wanted
    - Meta: 1 carro
    - Concepto: `m3 gtr`, `bmw m3`, `need for speed`
    - Campos: NAME, TAGS, SERIE
    - Tipo: CONTAINS
    - Dificultad: **Media**

75. **Midnight Club Legend** — *Midnight Club*
    - Registra 5 carros estilo Midnight Club (tuneados, nocturnos)
    - Meta: 5 carros
    - Concepto: `midnight club`, `midnight`
    - Campos: SERIE, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

---

## 🎭 COMEDIAS Y CLÁSICOS FAMILIARES

76. **Herbie the Love Bug** — *Herbie (1968)*
    - Registra el Volkswagen Beetle "Herbie" número 53
    - Meta: 1 carro
    - Concepto: `herbie`, `beetle`, `vw beetle`
    - Campos: NAME, TAGS, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

77. **Herbie Collection** — *The Love Bug Saga*
    - Registra 5 Volkswagen Beetle
    - Meta: 5 carros
    - Concepto: `beetle`, `escarabajo`, `vocho`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

78. **Ecto-1 Ghostbusters** — *Ghostbusters (1984)*
    - Registra el Cadillac Miller-Meteor Ambulance "Ecto-1"
    - Meta: 1 carro
    - Concepto: `ecto-1`, `ecto1`, `ghostbusters`, `cadillac ambulance`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

79. **Ghostbusters Fleet** — *Ghostbusters*
    - Registra 3 Cadillac (Ecto-1, Ecto-1A, Ecto-1 2016)
    - Meta: 3 carros
    - Concepto: `ecto`, `cadillac`, `ghostbusters`
    - Campos: NAME, BRAND, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

80. **Mr. Bean's Mini** — *Mr. Bean*
    - Registra el Mini 1000 verde de Mr. Bean
    - Meta: 1 carro
    - Concepto: `mini 1000`, `mini`, `mr. bean`, `bean`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

81. **Doc Hudson — Fabulous Hudson Hornet** — *Cars (2006)*
    - Registra el Hudson Hornet de Pixar Cars
    - Meta: 1 carro
    - Concepto: `hudson hornet`, `hornet`, `doc hudson`, `cars`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

82. **Lightning McQueen** — *Cars (2006)*
    - Registra cualquier carro rojo de carreras Dinoco-estilo
    - Meta: 1 carro
    - Concepto: `lightning`, `mcqueen`, `radiator springs`, `cars`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

83. **Pixar Cars Collection** — *Cars Saga*
    - Registra 5 carros de la saga Cars
    - Meta: 5 carros
    - Concepto: `cars`, `radiator springs`, `mcqueen`
    - Campos: SERIE, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

84. **Ferris Bueller's Day Off** — *Ferris Bueller (1986)*
    - Registra el Ferrari 250 GT California Spyder
    - Meta: 1 carro
    - Concepto: `250 gt`, `ferrari 250`, `ferris bueller`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

85. **Wayne's World — Mirthmobile** — *Wayne's World (1992)*
    - Registra el AMC Pacer "Mirthmobile"
    - Meta: 1 carro
    - Concepto: `amc pacer`, `pacer`, `mirthmobile`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

---

## 🕵️ POLICÍAS, CRIMEN Y THRILLER

86. **Cop Car Crown Vic** — *Películas/Series Policiales*
    - Registra el Ford Crown Victoria Police Interceptor
    - Meta: 1 carro
    - Concepto: `crown victoria`, `crown vic`, `police interceptor`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Fácil**

87. **Drive (Ryan Gosling) — Chevy** — *Drive (2011)*
    - Registra el Ford Mustang Fastback o Chevy Impala del Driver
    - Meta: 1 carro (lógica OR)
    - Concepto: `mustang`, `impala`, `driver`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

88. **Heat — BMW 7 Series** — *Heat (1995)*
    - Registra el BMW 7 Series E38 de Neil McCauley
    - Meta: 1 carro
    - Concepto: `bmw 7`, `7 series`, `e38`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

89. **John Wick's Mustang** — *John Wick (2014)*
    - Registra el Ford Mustang Boss 429 1969 de John Wick
    - Meta: 1 carro
    - Concepto: `boss 429`, `mustang boss`, `john wick`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Media**

90. **John Wick Armory** — *John Wick Saga*
    - Registra 3 carros de John Wick
    - Meta: 3 carros
    - Concepto: `john wick`, `mustang`, `charger`
    - Campos: NAME, TAGS, SERIE
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

---

## 🌟 COLECCIONES ÉPICAS CRUZADAS

91. **Steve McQueen Legacy** — *Bullitt + Le Mans*
    - Registra el Mustang GT390 (Bullitt) Y el Porsche 917 (Le Mans)
    - Meta: 2 carros (lógica AND)
    - Concepto: `bullitt`, `917`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Lógica: **AND**
    - Dificultad: **Difícil**

92. **The Holy Screen Trinity** — *Clásicos del Cine*
    - Registra: DeLorean (BTTF), Ecto-1 (Ghostbusters), KITT (Knight Rider)
    - Meta: 3 carros (lógica AND)
    - Concepto: `delorean`, `ecto`, `kitt`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Lógica: **AND**
    - Dificultad: **Difícil**

93. **Pop Culture Garage** — *Múltiples Sagas*
    - Registra: DeLorean, Batmobile, KITT, General Lee, Ecto-1, Herbie
    - Meta: 6 carros (lógica AND)
    - Concepto: `delorean`, `batmobile`, `kitt`, `general lee`, `ecto`, `herbie`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Lógica: **AND**
    - Dificultad: **Épico**

94. **Bond's Garage** — *James Bond Saga*
    - Registra: DB5, Lotus Esprit, BMW Z8, Aston Martin DBS, DB10
    - Meta: 5 carros (lógica OR)
    - Concepto: `db5`, `esprit`, `z8`, `dbs`, `db10`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Épico**

95. **Anime Garage** — *Colección Anime*
    - Registra: AE86 (Initial D), Mach 5 (Speed Racer), Fairlady Z (Wangan), Fiat 500 (Lupin), Trans AM (Redline)
    - Meta: 5 carros (lógica OR)
    - Concepto: `ae86`, `mach 5`, `fairlady`, `fiat 500`, `trans am`
    - Campos: NAME, TAGS
    - Tipo: CONTAINS
    - Dificultad: **Épico**

96. **Cinematic Universe** — *Colección Épica de Cine*
    - Registra al menos 20 carros con tag de película/serie
    - Meta: 20 carros
    - Concepto: `movie`, `pelicula`, `serie`, `film`
    - Campos: TAGS, SERIE
    - Tipo: CONTAINS
    - Dificultad: **Épico**

97. **Tokyo Nights** — *Colección Noche Japonesa*
    - Registra: AE86, RX-7, Silvia S15, Fairlady Z, Supra MK4 (Initial D/Wangan/FF Tokyo)
    - Meta: 5 carros (lógica OR)
    - Concepto: `ae86`, `rx-7`, `silvia`, `fairlady`, `supra`
    - Campos: NAME
    - Tipo: CONTAINS
    - Dificultad: **Épico**

98. **Muscle Movie Legends** — *Clásicos del Cine Americano*
    - Registra: Charger (FF), Mustang (Bullitt), Trans Am (Bandit), Impala (Supernatural/Drive)
    - Meta: 4 carros (lógica OR)
    - Concepto: `charger`, `mustang`, `trans am`, `impala`
    - Campos: NAME, BRAND
    - Tipo: CONTAINS
    - Dificultad: **Difícil**

99. **From Screen to Collection** — *Logro Maestro*
    - Registra 50 carros con tags relacionados a películas o series
    - Meta: 50 carros
    - Concepto: `movie`, `serie`, `film`, `tv`
    - Campos: TAGS, SERIE
    - Tipo: CONTAINS
    - Dificultad: **Épico**

100. **The Ultimate Cinephile** — *Logro Legendario*
     - Registra al menos 1 carro de cada categoría de esta lista:
       Fast & Furious, Clásicos, Sci-Fi, TV, Anime, Bond
     - Meta: 6 carros (lógica AND — uno por categoría)
     - Concepto: `fast`, `bullitt/eleanor`, `delorean`, `kitt`, `ae86`, `db5`
     - Campos: NAME, TAGS, SERIE
     - Tipo: CONTAINS
     - Lógica: **AND (uno por categoría)**
     - Dificultad: **Legendario**

---

## 🎁 BONUS — Logros Únicos Temáticos

101. **Speed Racer Completionist** — *Speed Racer Saga*
     - Registra 5 carros con tag "speed racer"
     - Meta: 5 carros
     - Concepto: `speed racer`, `mach`
     - Campos: TAGS, NAME
     - Tipo: CONTAINS
     - Dificultad: **Difícil**

102. **Classic Hollywood** — *Películas pre-1990*
     - Registra 10 carros de películas anteriores a 1990
     - Meta: 10 carros
     - Concepto: `bullitt`, `blues brothers`, `bandit`, `grease`, `herbie`
     - Campos: NAME, TAGS
     - Tipo: CONTAINS
     - Dificultad: **Difícil**

103. **The Cannonball Run** — *The Cannonball Run (1981)*
     - Registra 3 carros deportivos clásicos de los 80s
     - Meta: 3 carros
     - Concepto: `cannonball`, `lamborghini`, `ferrari`
     - Campos: NAME, TAGS, BRAND
     - Tipo: CONTAINS
     - Dificultad: **Media**

104. **Taxi! Paris Edition** — *Taxi (1998)*
     - Registra el Peugeot 406 de Daniel (taxi modificado)
     - Meta: 1 carro
     - Concepto: `peugeot 406`, `406`, `taxi`
     - Campos: NAME, TAGS
     - Tipo: CONTAINS
     - Dificultad: **Media**

105. **Christine's Curse** — *Christine (1983)*
     - Registra el Plymouth Fury 1958 "Christine"
     - Meta: 1 carro
     - Concepto: `christine`, `plymouth fury`, `fury 1958`
     - Campos: NAME, TAGS
     - Tipo: CONTAINS
     - Dificultad: **Media**

---

## 📊 RESUMEN DE DIFICULTADES

| Dificultad | Cantidad | Descripción |
|------------|----------|-------------|
| 🟢 Fácil | ~35 | 1 carro icónico reconocible |
| 🟡 Media | ~40 | 1-3 carros, más específicos |
| 🔴 Difícil | ~20 | 3-5 carros, combos |
| 🟣 Épico | ~8 | 5+ carros, colecciones cruzadas |
| ⭐ Legendario | 1 | El máximo: 1 de cada saga |

---

## 📝 NOTAS DE IMPLEMENTACIÓN

### Estrategia de Matching
- **CONTAINS**: Busca el concepto como substring en el campo (no distingue mayúsculas)
- **EXACT**: El campo debe ser exactamente igual al concepto
- **Lógica OR**: Cualquier concepto de la lista cumple el logro
- **Lógica AND**: TODOS los conceptos deben cumplirse

### Campos Sugeridos para Tags
Cuando se registre un carro de película/serie, agregar al campo TAGS:
- Nombre de la película: `fast and furious`, `batman`, `initial d`
- Tipo de aparición: `protagonist car`, `villain car`, `hero car`
- Franquicia: `dc`, `marvel`, `pixar`, `transformers`

### Para Fast & Furious por Entrega
Considera crear una **SERIE** en la app llamada:
- `"Fast & Furious 1"`, `"Fast & Furious 2"`, ..., `"Fast X"`
Así los logros por entrega son detectables automáticamente.

### Aliases Recomendados
Muchos nombres tienen variaciones:
- `DeLorean` / `De Lorean` / `DMC-12` / `DMC12`
- `Batmobile` / `Bat Mobile` / `Batman Car`
- `KITT` / `K.I.T.T.` / `Knight Industries`
- `Ecto-1` / `Ecto 1` / `ECTO1`

---

**Total: 105 logros de películas y series**

*Este archivo complementa `LISTA_LOGROS_EXTENDIDOS.md` y `LISTA_LOGROS_SUGERIDOS.md`*
*Última actualización: Febrero 2026*

