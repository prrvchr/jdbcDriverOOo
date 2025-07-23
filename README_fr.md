<!--
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
║                                                                                    ║
║   Permission is hereby granted, free of charge, to any person obtaining            ║
║   a copy of this software and associated documentation files (the "Software"),     ║
║   to deal in the Software without restriction, including without limitation        ║
║   the rights to use, copy, modify, merge, publish, distribute, sublicense,         ║
║   and/or sell copies of the Software, and to permit persons to whom the Software   ║
║   is furnished to do so, subject to the following conditions:                      ║
║                                                                                    ║
║   The above copyright notice and this permission notice shall be included in       ║
║   all copies or substantial portions of the Software.                              ║
║                                                                                    ║
║   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,                  ║
║   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES                  ║
║   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.        ║
║   IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY             ║
║   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,             ║
║   TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE       ║
║   OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                    ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
-->
# [![jdbcDriverOOo logo][1]][2] Documentation

**This [document][3] in English.**

**L'utilisation de ce logiciel vous soumet à nos [Conditions d'utilisation][4]**

# version [1.5.3][5]

## Introduction:

**jdbcDriverOOo** fait partie d'une [Suite][6] d'extensions [LibreOffice][7] ~~et/ou [OpenOffice][8]~~ permettant de vous offrir des services inovants dans ces suites bureautique.  

Cette extension est la transcription en Java pur de l'API [java.sql.*][9] vers l'API SDBC de UNO (ie: [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] et [com.sun.star.sdb][12]).
**Elle vous permet d'utiliser le pilote JDBC de votre choix directement dans Base.**

Elle embarque les pilotes pour les base de données suivantes:
- [HyperSQL ou HsqlDB][13] version 2.7.4
- [SQLite via xerial sqlite-jdbc][14] version 3.50.2.1-SNAPSHOT
- [MariaDB via Connector/J][15] version 3.5.3
- [PostgreSQL via pgJDBC][16] version 42.7.5
- [H2 Database Engine][17] version 2.2.224
- [Apache Derby][18] version 11.16.1.1
- Firebird via [Jaybird][19] version 6.0.2 et [JaybirdEmbedded][20] version 1.0.0
- [MySQL via Connector/J][21] version 9.3.0
- [Trino ou PrestoSQL][22] version 458-SNAPSHOT (en cours d'intégration, à utiliser avec prudence)

Grâce aux pilotes fournissant un moteur de base de données intégré tels que: HsqlDB, H2, SQLite, Derby ou Jaybird, il est possible dans Base de créer et gérer très facilement des bases de données, aussi facilement que de créer des documents Writer.  
Vous trouverez les informations nécessaires à la création d'une base de données avec ces pilotes dans la section: [URL de connexion][30]

Etant un logiciel libre je vous encourage:
- A dupliquer son [code source][31].
- A apporter des modifications, des corrections, des améliorations.
- D'ouvrir un [dysfonctionnement][32] si nécessaire.

Bref, à participer au developpement de cette extension.  
Car c'est ensemble que nous pouvons rendre le Logiciel Libre plus intelligent.

___

## Prérequis:

jdbcDriverOOo est un pilote JDBC écrit en Java.  
Son utilisation nécessite [l'installation et la configuration][33] dans LibreOffice d'un **JRE ou JDK Java version 17 ou ultérieure**.  
Je vous recommande [Adoptium][34] comme source d'installation de Java.

**Si vous utilisez une version de LibreOffice antérieure à 25.8.x, vous devez installer manuellement l'instrumentation Java.** Pour installer l'instrumentation Java avec LibreOffice, veuillez consulter la section [Comment installer l'Instrumentation Java][35].

La version minimale de LibreOffice prise en charge par l'extension jdbcDriverOOo dépend de la façon dont vous avez installé LibreOffice sur votre ordinateur:

- **Quelle que soit la plateforme**, si vous avez installé LibreOffice depuis le [site de téléchargement de LibreOffice][36], **la version minimale de LibreOffice est 7.0**.

- **Sous Linux** si vous avez utilisé le gestionnaire de paquets pour installer LibreOffice, **la version minimale de LibreOffice est 6.0**. Cependant, vous devez vous assurer que la version de Python fournie par le système n'est pas inférieure à 3.8.  
De plus, vous pouvez rencontrer les problèmes suivants:
    - Vous êtes sujet au [dysfonctionnement #139538][37]. Pour contourner le problème, veuillez **désinstaller les paquets** avec les commandes:
        - `sudo apt remove libreoffice-sdbc-hsqldb` (pour désinstaller le paquet libreoffice-sdbc-hsqldb)
        - `sudo apt remove libhsqldb1.8.0-java` (pour désinstaller le paquet libhsqldb1.8.0-java)
    Si vous souhaitez quand même utiliser la fonctionnalité HsqlDB intégré fournie par LibreOffice, alors installez l'extension [HyperSQLOOo][38].
    - Vos packages Python fournis par le système sont obsolètes. La journalisation de l'extension vous permettera de vérifier si c'est le cas. Elle est accessible via le menu: **Outils -> Options -> LibreOffice Base -> Pilote JDBC pur Java -> Options du pilote UNO -> Voir journal -> Info système** et nécessite le redemarrage de LibreOffice aprés son activation.  
    Si des paquets obsolètes apparaissent, vous pouvez les mettre à jour avec la commande:  
    `pip install --upgrade <package-name>`  
    Pour plus d'information voir: [Ce qui a été fait pour la version 1.1.0][39].

___

## Installation:

Il semble important que le fichier n'ait pas été renommé lors de son téléchargement.  
Si nécessaire, renommez-le avant de l'installer.

- ![jdbcDriverOOo logo][40] Installer l'extension **[jdbcDriverOOo.oxt][41]** [![Version][42]][41]

Redémarrez LibreOffice après l'installation.  
**Attention, redémarrer LibreOffice peut ne pas suffire.**
- **Sous Windows** pour vous assurer que LibreOffice redémarre correctement, utilisez le Gestionnaire de tâche de Windows pour vérifier qu'aucun service LibreOffice n'est visible après l'arrêt de LibreOffice (et tuez-le si ç'est le cas).
- **Sous Linux ou macOS** vous pouvez également vous assurer que LibreOffice redémarre correctement, en le lançant depuis un terminal avec la commande `soffice` et en utilisant la combinaison de touches `Ctrl + C` si après l'arrêt de LibreOffice, le terminal n'est pas actif (pas d'invité de commande).

Après avoir redémarré LibreOffice, vous pouvez vous assurer que l'extension et son pilote sont correctement installés en vérifiant que le pilote `io.github.prrvchr.jdbcDriverOOo.Driver` est répertorié dans le **Pool de Connexions**, accessible via le menu: **Outils -> Options -> LibreOffice Base -> Connexions**. Il n'est pas nécessaire d'activer le pool de connexions.

Si le pilote n'est pas répertorié, la raison de l'échec du chargement du pilote peut être trouvée dans la journalisation de l'extension. Cette journalisation est accessible via le menu: **Outils -> Options -> LibreOffice Base -> Pilote JDBC Pure Java -> Options de journalisation**.  
La journalisation `Driver` doit d'abord être activée, puis LibreOffice redémarré pour obtenir le message d'erreur dans le journal.

**Attention ne pas oublier:**
- De mettre à jour la version du JRE ou JDK Java installée sur votre ordinateur si nécessaire, cette nouvelle version de jdbcDriverOOo nécessite **Java version 17 ou ultérieure** au lieu de Java 11 auparavant.
- D'installer l'instrumentation Java si LibreOffice est inférieur à 25.8, veuillez suivre la description dans la section [Comment installer l'instrumentation Java][35].

___

## Utilisation:

Ceci explique comment utiliser une base de données HsqlDB.  
Les protocoles pris en charge par HsqlDB sont: hsql://, hsqls://, http://, https://, mem://, file:// et res://.  
Ce mode d'utilisation vous explique comment vous connecter avec les protocoles **file://** et **hsql://**.

### Comment créer une nouvelle base de données:

Dans LibreOffice / OpenOffice aller au menu: **Fichier -> Nouveau -> Base de données**

![jdbcDriverOOo screenshot 1][43]

A l'étape: **Sélectionner une base de données**
- selectionner: Connecter une base de données existante
- choisir: **Pilote HsqlDB**
- cliquer sur le bouton: Suivant

![jdbcDriverOOo screenshot 2][44]

A l'étape: **Paramètres de connexion**

- pour le protocole: **file://**
    - dans URL de la source de données saisir:
        - pour **Linux**: `file:///tmp/testdb;shutdown=true;hsqldb.default_table_type=cached;get_column_name=false`
        - pour **Windows**: `file:///c:/tmp/testdb;shutdown=true;hsqldb.default_table_type=cached;get_column_name=false`

- pour le protocole: **hsql://**
    - Dans un terminal, se placer dans un dossier contenant l'archive hsqldb.jar et lancer:
        - pour **Linux**: `java -cp hsqldb.jar org.hsqldb.server.Server --database.0 file:///tmp/testdb --silent false`
        - pour **Windows**: `java -cp hsqldb.jar org.hsqldb.server.Server --database.0 file:///c:/tmp/testdb --silent false`
    - dans URL de la source de données saisir: `hsql://localhost/`

- cliquer sur le bouton: Suivant

![jdbcDriverOOo screenshot 3][45]

A l'étape: **Paramétrer l'authentification de l'utilisateur**
- cliquer sur le bouton: Tester la connexion

![jdbcDriverOOo screenshot 4][46]

Si la connexion a réussi, vous devriez voir cette fenêtre de dialogue:

![jdbcDriverOOo screenshot 5][47]

Maintenant à vous d'en profiter...

### Comment mettre à jour le pilote JDBC:

Si vous souhaitez mettre à jour une base de données HsqlDB intégrée (un seul fichier odb), veuillez vous référer à la section: [Comment migrer une base de données intégrée][48].

Il est possible de mettre à jour le pilote JDBC (hsqldb.jar, h2.jar, derbytools.jar) vers une version plus récente.  
Si vous utilisez HsqlDB comme base de données, procédez comme suit:
1. Faite une copie (sauvegarde) du dossier contenant votre base de données.
2. Lancer LibreOffice / OpenOffice et changez la version du pilote JDBC par le menu: **Outils -> Options -> LibreOffice Base -> Pilote JDBC pur Java -> Options des pilotes JDBC**, par une version plus récente.
3. Redémarrer LibreOffice / OpenOffice aprés le changement du pilote (hsqldb.jar, h2.jar, derbytools.jar).
4. Dans Base, aprés avoir ouvert votre base de données, allez au menu: **Outils -> SQL** et tapez la commande SQL: `SHUTDOWN COMPACT` ou `SHUTDOWN SCRIPT`.

Maintenant votre base de données est à jour.

___

## Amélioration dans LibreOffice/OpenOffice Base:

Ce pilote permet dans LibreOffice / OpenOffice Base la gestion des **utilisateurs**, des **rôles** (groupes) et de leurs **privilèges** associés de la base de données sous jacente.

### La gestion des utilisateurs et des privilèges dans Base:

La gestion des utilisateurs de la base de données sous jacente est accessible dans Base par le menu: **Administration -> Gestion des utilisateurs**

![jdbcDriverOOo screenshot 6][49]

La gestion des privilèges des utilisateurs de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Modifier les privilèges**  
Si le privilège est hérité d'un rôle assigné, la case à cocher est de type à trois états.

![jdbcDriverOOo screenshot 7][50]

### La gestion des rôles (groupes) dans Base:

La gestion des rôles (groupes) de la base de données sous jacente est accessible dans Base par le menu: **Administration -> Gestion des groupes**

![jdbcDriverOOo screenshot 8][51]

La gestion des utilisateurs membres du groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Utilisateurs du groupe**

![jdbcDriverOOo screenshot 9][52]

La gestion des roles assignés au groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Roles du groupe**  
Cette fonctionnalité est une extension de l'API UNO et ne sera disponible que si le pilote LibreOffice / OpenOffice sous jacent le permet.

![jdbcDriverOOo screenshot 10][53]

___

## URL de connexion:

Certaines bases de données comme HsqlDB, H2, SQLite, Derby ou Firebird via Jaybird permettent la création de la base de données lors de la connexion si cette base de données n'existe pas encore.
Cette fonctionnalité rend la création de bases de données aussi simple que celle de documents Writer. Généralement il suffit d'ajouter l'option attendue par le driver à l'URL de connexion.
Cette URL de connexion peut être différente selon le système d'exploitation de votre ordinateur (Windows, Linux ou MacOS).  
Pour créer une base de données, dans LibreOffice allez dans le menu: **Fichier -> Nouveau -> Base de données -> Connecter une base de données existante**, puis selon votre choix:
- **HsqlDB pure Java**:
  - Linux: `file:///home/prrvchr/testdb/hsqldb/db;hsqldb.default_table_type=cached;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\hsqldb\db;hsqldb.default_table_type=cached;create=true`
- **H2 pure Java**:
  - Linux: `file:///home/prrvchr/testdb/h2/db`
  - Windows: `C:\Utilisateurs\prrvc\testdb\h2\db`
- **SQLite pure Java**:
  - Linux: `file:///home/prrvchr/testdb/sqlite/test.db`
  - Windows: `C:/Utilisateurs/prrvc/testdb/sqlite/test.db`
- **Derby pure Java**:
  - Linux: `/home/prrvchr/testdb/derby;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\derby;create=true`
- **Firebird pure Java**:
  - Linux: `embedded:/home/prrvchr/testdb/firebird?createDatabaseIfNotExist=true`
  - Windows: `embedded:C:\Utilisateurs\prrvc\testdb\firebird?createDatabaseIfNotExist=true`

Firebird utilise [JaybirdEmbedded][20] pour fonctionner en mode embarqué. Vous trouverez les plateformes prises en charge dans la documentation de [JaybirdEmbedded][20].  
Pour les plateformes non prises en charge, vous pouvez toujours installer [Firebird Server][54] correspondant à votre plateforme.

___

## Comment installer l'Instrumentation Java:

Afin d'utiliser les services Java SPI offerts par l'implémentation `RowSetFactory.jar`, l'instrumentation Java doit être installée sous LibreOffice.  
Cela se fait automatiquement à partir de la version 25.8.x de LibreOffice, mais doit être fait manuellement pour les versions inférieures.  
Voici les différentes étapes:
- Télécharger l'archive [InstrumentationAgent.jar][55] et placez-la dans un dossier.
- Dans LibreOffice, par le menu: **Outils -> Options -> LibreOffice -> Avancé -> Options Java -> Paramètres -> Paramètre de démarrage Java** ajouter la commande:
    - Pour Windows: `-javaagent:c:\dossier\InstrumentationAgent.jar`.
    - Pour Linux: `-javaagent:/dossier/InstrumentationAgent.jar`.

    Bien entendu, le chemin vers l'archive reste à adapter à votre cas d'utilisation.
- Redémarrez LibreOffice pour prendre en compte ces modifications.

Si vous pensez qu'il serait bon d'éviter cette manipulation, demandez à LibreOffice de [rétroporter l'instrumentation Java][56].

___

## Comment créer l'extension:

Normalement, l'extension est créée avec Eclipse pour Java et [LOEclipse][57]. Pour contourner Eclipse, j'ai modifié LOEclipse afin de permettre la création de l'extension avec Apache Ant.  
Pour créer l'extension jdbcDriverOOo avec l'aide d'Apache Ant, vous devez:
- Installer le [SDK Java][58] version 17 ou supérieure.
- Installer [Apache Ant][59] version 1.10.0 ou supérieure.
- Installer [LibreOffice et son SDK][60] version 7.x ou supérieure.
- Cloner le dépôt [jdbcDriverOOo][61] sur GitHub dans un dossier.
- Depuis ce dossier, accédez au répertoire: `source/jdbcDriverOOo/`
- Dans ce répertoire, modifiez le fichier `build.properties` afin que les propriétés `office.install.dir` et `sdk.dir` pointent vers les dossiers d'installation de LibreOffice et de son SDK, respectivement.
- Lancez la création de l'archive avec la commande: `ant`
- Vous trouverez l'archive générée dans le sous-dossier: `dist/`

___

## A été testé avec:

* LibreOffice 24.2.1.2 (x86_64)- Windows 10

* LibreOffice 7.3.7.2 - Lubuntu 22.04

* LibreOffice 24.2.1.2 - Lubuntu 22.04

* LibreOffice 24.8.0.3 (X86_64) - Windows 10(x64) - Python version 3.9.19 (sous Lubuntu 22.04 / VirtualBox 6.1.38)

Je vous encourage en cas de problème :confused:  
de créer un [dysfonctionnement][32]  
J'essaierai de le résoudre :smile:

___

## Historique:

### Introduction:

Ce pilote a été écrit pour contourner certains problèmes inhérents à l'implémentation UNO du pilote JDBC intégré dans LibreOffice / OpenOffice, à savoir:

- L'impossibilité de fournir le chemin de l'archive Java du driver (hsqldb.jar) lors du chargement du pilote JDBC.
- Ne pas pouvoir utiliser les instructions SQL préparées (PreparedStatement) voir [dysfonctionnement #132195][62].

Afin de profiter des dernières fonctionnalités offertes par les bases de données et entre autre HsqlDB, il était nécessaire d'écrire un nouveau pilote.

Jusqu'à la version 0.0.3, ce nouveau pilote n'est qu'une surcouche ou emballage (wrapper) en Python autour des services UNO fournis par le pilote LibreOffice / OpenOffice JDBC défectueux.  
Depuis la version 0.0.4, il a été complètement réécrit en Java sous Eclipse, car qui mieux que Java peut donner accès à JDBC dans l'API UNO?  
Afin de ne pas empêcher le pilote JDBC natif de fonctionner, il se charge lors de l'appel des protocoles suivants:

- `xdbc:*`
- `xdbc:hsqldb:*`
- `xdbc:sqlite:*`
- `xdbc:mariadb:*`
- `xdbc:...`

mais utilise le protocole `jdbc:*` en interne pour se connecter.

Il permet également d'offrir des fonctionnalités que le pilote JDBC implémenté dans LibreOffice ne fournit pas, à savoir:

- La gestion des utilisateurs, des roles (groupes) et des privilèges dans Base.
- L'utilisation du type SQL Array dans les requêtes.
- Tout ce que nous sommes prêts à mettre en œuvre.

### [Toutes les changements sont consignées dans l'Historique des versions][63]

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_fr>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-153>
[6]: <https://prrvchr.github.io/README_fr>
[7]: <https://fr.libreoffice.org/download/telecharger-libreoffice/>
[8]: <https://www.openoffice.org/fr/Telecharger/>
[9]: <https://devdocs.io/openjdk~17/java.sql/java/sql/package-summary>
[10]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[11]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[12]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[13]: <http://hsqldb.org/>
[14]: <https://github.com/prrvchr/sqlite-jdbc>
[15]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[16]: <https://jdbc.postgresql.org/>
[17]: <https://www.h2database.com/html/main.html>
[18]: <https://db.apache.org/derby/>
[19]: <https://firebirdsql.org/en/jdbc-driver/>
[20]: <https://prrvchr.github.io/JaybirdEmbedded/README_fr>
[21]: <https://dev.mysql.com/downloads/connector/j/>
[22]: <https://trino.io/docs/current/client/jdbc.html#installing>
[30]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#url-de-connexion>
[31]: <https://github.com/prrvchr/jdbcDriverOOo/>
[32]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[33]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10/fr>
[34]: <https://adoptium.net/temurin/releases/?version=17&package=jre>
[35]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#comment-installer-linstrumentation-java>
[36]: <https://fr.libreoffice.org/download/telecharger-libreoffice/>
[37]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[38]: <https://prrvchr.github.io/HyperSQLOOo/README_fr>
[39]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-110>
[40]: <img/jdbcDriverOOo.svg#middle>
[41]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[42]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.5.3#right>
[43]: <img/jdbcDriverOOo-1_fr.png>
[44]: <img/jdbcDriverOOo-2_fr.png>
[45]: <img/jdbcDriverOOo-3_fr.png>
[46]: <img/jdbcDriverOOo-4_fr.png>
[47]: <img/jdbcDriverOOo-5_fr.png>
[48]: <https://prrvchr.github.io/HyperSQLOOo/README_fr#comment-migrer-une-base-de-donn%C3%A9es-int%C3%A9gr%C3%A9e>
[49]: <img/jdbcDriverOOo-6_fr.png>
[50]: <img/jdbcDriverOOo-7_fr.png>
[51]: <img/jdbcDriverOOo-8_fr.png>
[52]: <img/jdbcDriverOOo-9_fr.png>
[53]: <img/jdbcDriverOOo-10_fr.png>
[54]: <https://firebirdsql.org/en/firebird-5-0-3>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/InstrumentationAgent.jar>
[56]: <https://bugs.documentfoundation.org/show_bug.cgi?id=167071>
[57]: <https://github.com/LibreOffice/loeclipse>
[58]: <https://adoptium.net/temurin/releases/?version=17&package=jdk>
[59]: <https://ant.apache.org/manual/install.html>
[60]: <https://downloadarchive.documentfoundation.org/libreoffice/old/7.6.7.2/>
[61]: <https://github.com/prrvchr/jdbcDriverOOo.git>
[62]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[63]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG_fr>
