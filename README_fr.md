<!--
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

# version [1.4.3][5]

## Introduction:

**jdbcDriverOOo** fait partie d'une [Suite][6] d'extensions [LibreOffice][7] ~~et/ou [OpenOffice][8]~~ permettant de vous offrir des services inovants dans ces suites bureautique.  

Cette extension est la transcription en Java pur de l'API [java.sql.*][9] vers l'API [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] et [com.sun.star.sdb][12] de UNO.
**Elle vous permet d'utiliser le pilote JDBC de votre choix directement dans Base.**  
Elle embarque les pilotes pour les base de données suivantes:
- [HyperSQL ou HsqlDB][13] version 2.7.3
- [SQLite via xerial sqlite-jdbc][14] version 3.45.1.6-SNAPSHOT
- [MariaDB via Connector/J][15] version 3.4.0
- [PostgreSQL via pgJDBC][16] version 42.7.1
- [H2 Database Engine][17] version 2.2.224 (2023-09-17)
- [Apache Derby][18] version 10.15.2.0
- [Firebird via Jaybird][19] version 5.0.5
- [MySQL via Connector/J][20] version 8.4.0 (en cours d'intégration, à utiliser avec prudence)
- [Trino ou PrestoSQL][21] version 453 (en cours d'intégration, à utiliser avec prudence)

Grâce aux pilotes fournissant un moteur de base de données intégré tels que: HsqlDB, H2, SQLite ou Derby, il est possible dans Base de créer et gérer très facilement des bases de données, aussi facilement que de créer des documents Writer.  
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
Son utilisation nécessite [l'installation et la configuration][33] dans LibreOffice d'un **JRE version 11 ou ultérieure**.  
Je vous recommande [Adoptium][34] comme source d'installation de Java.

Si vous utilisez le pilote HsqlDB avec **LibreOffice sous Linux**, alors vous êtes sujet au [dysfonctionnement #139538][35]. Pour contourner le problème, veuillez **désinstaller les paquets** avec les commandes:
- `sudo apt remove libreoffice-sdbc-hsqldb` (pour désinstaller le paquet libreoffice-sdbc-hsqldb)
- `sudo apt remove libhsqldb1.8.0-java` (pour désinstaller le paquet libhsqldb1.8.0-java)

Si vous souhaitez quand même utiliser la fonctionnalité HsqlDB intégré fournie par LibreOffice, alors installez l'extension [HyperSQLOOo][36].  

**Sous Linux et macOS les paquets Python** utilisés par l'extension, peuvent s'il sont déja installé provenir du système et donc, **peuvent ne pas être à jour**.  
Afin de s'assurer que vos paquets Python sont à jour il est recommandé d'utiliser l'option **Info système** dans les Options de l'extension accessible par:  
**Outils -> Options -> Pilotes Base -> Pilote JDBC -> Voir journal -> Info système**  
Si des paquets obsolètes apparaissent, vous pouvez les mettre à jour avec la commande:  
`pip install --upgrade <package-name>`

Pour plus d'information voir: [Ce qui a été fait pour la version 1.1.0][37].

___

## Installation:

Il semble important que le fichier n'ait pas été renommé lors de son téléchargement.  
Si nécessaire, renommez-le avant de l'installer.

- ![jdbcDriverOOo logo][38] Installer l'extension **[jdbcDriverOOo.oxt][39]** [![Version][40]][39]

Redémarrez LibreOffice après l'installation.  
**Attention, redémarrer LibreOffice peut ne pas suffire.**
- **Sous Windows** pour vous assurer que LibreOffice redémarre correctement, utilisez le Gestionnaire de tâche de Windows pour vérifier qu'aucun service LibreOffice n'est visible après l'arrêt de LibreOffice (et tuez-le si ç'est le cas).
- **Sous Linux ou macOS** vous pouvez également vous assurer que LibreOffice redémarre correctement, en le lançant depuis un terminal avec la commande `soffice` et en utilisant la combinaison de touches `Ctrl + C` si après l'arrêt de LibreOffice, le terminal n'est pas actif (pas d'invité de commande).

___

## Utilisation:

Ceci explique comment utiliser une base de données HsqlDB.  
Les protocoles pris en charge par HsqlDB sont: hsql://, hsqls://, http://, https://, mem://, file:// et res://.  
Ce mode d'utilisation vous explique comment vous connecter avec les protocoles **file://** et **hsql://**.

### Comment créer une nouvelle base de données:

Dans LibreOffice / OpenOffice aller au menu: **Fichier -> Nouveau -> Base de données**

![jdbcDriverOOo screenshot 1][41]

A l'étape: **Sélectionner une base de données**
- selectionner: Connecter une base de données existante
- choisir: **Pilote HsqlDB**
- cliquer sur le bouton: Suivant

![jdbcDriverOOo screenshot 2][42]

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

![jdbcDriverOOo screenshot 3][43]

A l'étape: **Paramétrer l'authentification de l'utilisateur**
- cliquer sur le bouton: Tester la connexion

![jdbcDriverOOo screenshot 4][44]

Si la connexion a réussi, vous devriez voir cette fenêtre de dialogue:

![jdbcDriverOOo screenshot 5][45]

Maintenant à vous d'en profiter...

### Comment mettre à jour le pilote JDBC:

Si vous souhaitez mettre à jour une base de données HsqlDB intégrée (un seul fichier odb), veuillez vous référer à la section: [Comment migrer une base de données intégrée][46].

Il est possible de mettre à jour le pilote JDBC (hsqldb.jar, h2.jar, derbytools.jar) vers une version plus récente.  
Si vous utilisez HsqlDB comme base de données, procédez comme suit:
1. Faite une copie (sauvegarde) du dossier contenant votre base de données.
2. Lancer LibreOffice / OpenOffice et changez la version du pilote JDBC par le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**, par une version plus récente.
3. Redémarrer LibreOffice / OpenOffice aprés le changement du pilote (hsqldb.jar, h2.jar, derbytools.jar).
4. Dans Base, aprés avoir ouvert votre base de données, allez au menu: **Outils -> SQL** et tapez la commande SQL: `SHUTDOWN COMPACT` ou `SHUTDOWN SCRIPT`.

Maintenant votre base de données est à jour.

___

## Amélioration dans LibreOffice/OpenOffice Base:

Ce pilote permet dans LibreOffice / OpenOffice Base la gestion des **utilisateurs**, des **rôles** (groupes) et de leurs **privilèges** associés de la base de données sous jacente.

### La gestion des utilisateurs et des privilèges dans Base:

La gestion des utilisateurs de la base de données sous jacente est accessible dans Base par le menu: **Administration -> Gestion des utilisateurs**

![jdbcDriverOOo screenshot 6][47]

La gestion des privilèges des utilisateurs de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Modifier les privilèges**  
Si le privilège est hérité d'un rôle assigné, la case à cocher est de type à trois états.

![jdbcDriverOOo screenshot 7][48]

### La gestion des rôles (groupes) dans Base:

La gestion des rôles (groupes) de la base de données sous jacente est accessible dans Base par le menu: **Administration -> Gestion des groupes**

![jdbcDriverOOo screenshot 8][49]

La gestion des utilisateurs membres du groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Utilisateurs du groupe**

![jdbcDriverOOo screenshot 9][50]

La gestion des roles assignés au groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Roles du groupe**  
Cette fonctionnalité est une extension de l'API UNO et ne sera disponible que si le pilote LibreOffice / OpenOffice sous jacent le permet.

![jdbcDriverOOo screenshot 10][51]

___

## URL de connexion:

Certaines bases de données comme hsqlDB, H2, SQLite ou Derby permettent la création de la base de données lors de la connexion si cette base de données n'existe pas encore.
Cette fonctionnalité rend la création de bases de données aussi simple que celle de documents Writer. Généralement il suffit d'ajouter l'option attendue par le driver à l'URL de connexion.
Cette URL de connexion peut être différente selon le système d'exploitation de votre ordinateur (Windows, Linux ou MacOS).  
Pour créer une base de données, dans LibreOffice allez dans le menu: **Fichier -> Nouveau -> Base de données -> Connecter une base de données existante**, puis selon votre choix:
- **Pilote HsqlDB**:
  - Linux: `file:///home/prrvchr/testdb/hsqldb/db;hsqldb.default_table_type=cached;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\hsqldb\db;hsqldb.default_table_type=cached;create=true`
- **Pilote H2**:
  - Linux: `file:///home/prrvchr/testdb/h2/db`
  - Windows: `C:\Utilisateurs\prrvc\testdb\h2\db`
- **Pilote SQLite**:
  - Linux: `file:///home/prrvchr/testdb/sqlite/test.db`
  - Windows: `C:/Utilisateurs/prrvc/testdb/sqlite/test.db`
- **Pilote Derby**:
  - Linux: `/home/prrvchr/testdb/derby;create=true`
  - Windows: `C:\Utilisateurs\prrvc\testdb\derby;create=true`

___

## A été testé avec:

* LibreOffice 24.2.1.2 (x86_64)- Windows 10

* LibreOffice 7.3.7.2 - Lubuntu 22.04

* LibreOffice 24.2.1.2 - Lubuntu 22.04

Je vous encourage en cas de problème :confused:  
de créer un [dysfonctionnement][32]  
J'essaierai de le résoudre :smile:

___

## Historique:

### Introduction:

Ce pilote a été écrit pour contourner certains problèmes inhérents à l'implémentation UNO du pilote JDBC intégré dans LibreOffice / OpenOffice, à savoir:

- L'impossibilité de fournir le chemin de l'archive Java du driver (hsqldb.jar) lors du chargement du pilote JDBC.
- Ne pas pouvoir utiliser les instructions SQL préparées (PreparedStatement) voir [dysfonctionnement #132195][52].

Afin de profiter des dernières fonctionnalités offertes par les bases de données et entre autre HsqlDB, il était nécessaire d'écrire un nouveau pilote.

Jusqu'à la version 0.0.3, ce nouveau pilote n'est qu'une surcouche ou emballage (wrapper) en Python autour des services UNO fournis par le pilote LibreOffice / OpenOffice JDBC défectueux.  
Depuis la version 0.0.4, il a été complètement réécrit en Java sous Eclipse, car qui mieux que Java peut donner accès à JDBC dans l'API UNO...  
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

### Ce qui a été fait pour la version 0.0.1:

- La rédaction de ce pilote a été facilitée par une [discussion avec Villeroy][53], sur le forum OpenOffice, que je tiens à remercier, car la connaissance ne vaut que si elle est partagée...

- Utilisation de la nouvelle version de HsqlDB 2.5.1.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.2:

- Ajout d'une boîte de dialogue permettant de mettre à jour le pilote (hsqldb.jar) dans: Outils -> Options -> Pilotes Base -> Pilote HsqlDB

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.3:

- Je tiens particulièrement à remercier fredt à [hsqldb.org][54] pour:

    - Son accueil pour ce projet et sa permission d'utiliser le logo HsqlDB dans l'extension.

    - Son implication dans la phase de test qui a permis de produire cette version 0.0.3.

    - La qualité de sa base de données HsqlDB.

- Fonctionne désormais avec OpenOffice sous Windows.

- Un protocole non pris en charge affiche désormais une erreur précise.

- Une url non analysable affiche désormais une erreur précise.

- Gère désormais correctement les espaces dans les noms de fichiers et les chemins.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.4:

- Réécriture de [Driver][55] en Java version 11 OpenJDK amd64 sous Eclipse IDE for Java Developers version 4.23.0 avec les plugins:
    - LOEclipse ou LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev ou Python IDE for Eclipse version 9.3.0.

- Ecriture des services `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` de JDBC (merci à hanya pour [MRI][56] qui m'a été d'une aide précieuse...)

    - [com.sun.star.sdb.*][57]
    - [com.sun.star.sdbc.*][58]
    - [com.sun.star.sdbcx.*][59]

- Intégration dans jdbcDriverOOo des pilotes JDBC **H2** et **Derby** en plus de **HsqlDB**. Implémentation de Services Java:

    - [Driver-HsqlDB.jar][60]
    - [Driver-H2.jar][61]
    - [Driver-Derby.jar][62]

    Afin de corriger d'éventuels défauts, ou incompatibilité avec l'API UNO, des pilotes JDBC embarqués. 

- Renommage du dépot et de l'extension **HsqlDBDriverOOo** en **jdbcDriverOOo**.

- Prise en charge dans Base des **clés primaires auto incrémentées** pour HsqlDB, H2 et Derby.

- Ecriture de [com.sun.star.sdbcx.Driver][63]. Ce pilote de haut niveau doit permettre la **gestion des utilisateurs, des rôles et des privilèges dans Base**. Son utilisation peut être désactivée via le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**.

- Implémentation d'un fournisseur de services Java [UnoLogger.jar][64] pour l'API [SLF4J][65] afin de pouvoir rediriger la journalisation des pilotes des bases de données sous-jacentes vers l'API UNO [com.sun.star.logging.*][66].

- Réécriture, en suivant le modèle MVC, de la fenêtre des [Options][67], accessible par le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**, pour permettre:

    - La mise à jour et/ou l'ajout d'archives Java de pilotes JDBC.
    - L'activation de la journalisation du pilote de la base de la données sous-jacente.

- Ecriture, en suivant le modèle MVC, des [fenêtres d'administration][68] des utilisateurs et des rôles (groupes) et de leurs privilèges associés, accessible dans Base par le menu: **Administration -> Gestion des utilisateurs** et/ou **Administration -> Gestion des groupes**, permettant:

    - La [gestion des utilisateurs][69] et de leurs privilèges.
    - La [gestion des rôles][70] (groupes) et de leurs privilèges.

    Ces nouvelles fonctionnalités n'ont étés testées pour l'instant qu'avec le pilote HsqlDB.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.0:

- Intégration de HyperSQL version 2.7.2.

### Ce qui a été fait pour la version 1.0.1:

- Intégration de [SQLite JDBC][14] version 3.42.0.0. Je tiens tout particulièrement à remercier [gotson][71] pour les [nombreuses améliorations apportées au pilote SQLite JDBC][72] qui ont rendu possible l'utilisation de SQLite dans LibreOffice/OpenOffice.

- Ce pilote peut être enveloppé par un autre pilote ([HyperSQLOOo][26] ou [SQLiteOOo][73]) grâce à une url de connexion désormais modifiable.

- Il est possible d'afficher ou non les tables système dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Afficher les tables système**

- Il est possible d'interdire l'utilisation de jeux de résultats actualisables dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Utiliser les bookmarks**

- De nombreuses corrections ont été apportées afin de rendre l'extension [SQLiteOOo][73] fonctionnelle.

### Ce qui a été fait pour la version 1.0.2:

- Intégration de [MariaDB Connector/J][15] version 3.1.4.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.3:

- Intégration de [H2][17] version 2.2.220.

- Intégration de la journalisation dans les jeux de résultat ([ResultSetBase][74] and [ResultSetSuper][75]) afin d'en savoir plus sur le [dysfonctionnement 156512][76].

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.4:

- Support dans la création de tables du paramètre [TypeInfoSettings][77] permettant de récupérer la précision pour les types SQL:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    Ceci n'est [integré][78] que pour le pilote [HsqlDB][79] pour le moment.

### Ce qui a été fait pour la version 1.0.5:

- Le résultat de l'accès à la méthode [XDatabaseMetaData.getDriverVersion()][80] est désormais enregistré dans le fichier journal.

### Ce qui a été fait pour la version 1.0.6:

- Ajout du paquet Python `packaging` dans le `pythonpath` de l'extension. Merci à [artem78][81] d'avoir permis cette correction en signalant cet oubli dans le [dysfonctionnement #4][82].

### Ce qui a été fait pour la version 1.0.7:

- Désormais, le pilote lève une exception si la création d'une nouvelle table échoue. Ceci est pour répondre au [dysfonctionnement #1][83] sur l'extension [HyperSQLOOo][26].

### Ce qui a été fait pour la version 1.0.8:

- Utilisation de la dernière version de l'API de journalisation.

### Ce qui a été fait pour la version 1.1.0:

- Tous les paquets Python nécessaires à l'extension sont désormais enregistrés dans un fichier [requirements.txt][84] suivant la [PEP 508][85].
- Désormais si vous n'êtes pas sous Windows alors les paquets Python nécessaires à l'extension peuvent être facilement installés avec la commande:  
  `pip install requirements.txt`
- Modification de la section [Prérequis][86].

### Ce qui a été fait pour la version 1.1.1:

- Le pilote n'utilise plus de jeux de résultats (ResultSet) pouvant être mis en signet (Bookmarkable) pour des raisons de performances dans LibreOffice Base. Ceci peut être changé dans les options d'extension.

### Ce qui a été fait pour la version 1.1.2:

- Implementation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][87]. Cette interface permet, lors d'une insertion de plusieurs lignes (ie: `INSERT INTO matable (Colonne1, Colonne2) VALUES (valeur1, valeur2), (valeur1, valeur2), ...`) dans une table disposant d'une clé primaire auto-incrémentée, de récupérer un ensemble de résultats à partir des lignes insérées dans la table et vous donne donc accès aux clés générées automatiquement en un seul coup.
- Implémentation de l'interface UNO [com.sun.star.sdbcx.XAlterTable][88]. Cette interface permet la modification des colonnes d'une table. Avec HsqlDB, il est maintenant possible dans Base:
  - D'attribuez une description aux colonnes des tables.
  - De modifier le type d'une colonne si le transtypage (CAST) des données contenues dans cette colonne le permet, sinon il vous sera proposé de remplacer cette colonne ce qui entraîne la suppression des données...
- Toutes les commandes DDL (ie: `CREATE TABLE...`, `ALTER TABLE...`) générées par jdbcDriverOOo sont désormais enregistrées dans la journalisation.
- Pilote SQLite mis à jour vers la dernière version 3.45.1.0.
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.1.3:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][89]. Ce nouveau pilote a été implémenté pour supporter une partie des spécifications JDBC 4.1 et plus particulièrement l'interface `java.sql.Statement.getGeneratedKeys()` et permet l'utilisation de l'interface [com.sun.star.sdbc.XGeneratedResultSet][87].

### Ce qui a été fait pour la version 1.1.4:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][90].
- Intégration du driver [PostgreSQL pgJDBC][16] version 42.7.1 dans l'archive de jdbcDriverOOo. Cette intégration a été réalisée sans utiliser de service Java spécifique à PostgreSQL mais uniquement en configurant le fichier [Drivers.xcu][91] permettant de déclarer le pilote JDBC à LibreOffice.
- Ouverture d'un [dysfonctionnement][92] pour le pilote [MariaDB Connector/J][15] afin qu'il prenne en charge `java.sql.Statement.getGeneratedKeys()` comme demandé par JDBC 4.1.
- Normalement les prochaines versions de jdbcDriverOOo devraient pouvoir être mises à jour dans la liste des extensions installées sous LibreOffice: **Outils -> Gestionnaire des extensions... -> Vérifier les mises à jour**.
- Désormais, seul le pilote HsqlDB a accès dans Base à l'administration des droits des utilisateurs et des groupes. Ceci est déterminé par le paramètre `IgnoreDriverPrivileges` dans le fichier [Drivers.xcu][91].
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.1.5:

- Vous pouvez désormais éditer une vue en mode SQL avec le pilote SQLite. Pour les pilotes qui ne prennent pas en charge la modification des vues, les vues sont supprimées puis recréées.

### Ce qui a été fait pour la version 1.1.6:

- Vous pouvez désormais renommer les tables et les vues dans Base. Toute la configuration nécessaire au renommage pour chaque pilote JDBC intégré est stockée uniquement dans le fichier [Drivers.xcu][81].
- Tous les pilotes JDBC intégrés à jdbcDriverOOo sont capables de renommer des tables ou des vues et même certains (ie: MariaDB et PostgreSQL) permettent de modifier le catalogue ou le schéma.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.2.0:

- Tous les pilotes intégrés à l'extension sont **désormais entièrement fonctionnels dans Base** pour la gestion des tables et des vues.
- Des fonctions intelligentes sont appelées pour:
  - Le déplacement avec renommage des tables, pour les drivers le permettant et utilisant deux commandes SQL, l'ordre des commandes SQL sera optimisé (PostgreSQL).
  - Le Renommage d'une vue, si le pilote ne le supporte pas, elle sera supprimée puis recréée (SQLite).
- Utilisation de [classe générique Java][93] pour la gestion des conteneurs utilisés pour la gestion des [tables][94], des [vues][95], des [colonnes][96], des [clefs][97] et des [indexes][98]. L'utilisation de classes génériques dans les [conteneurs][99] permettra de se passer de l'interface UNO XPropertySet et de pouvoir retranscrire le code existant en Java pur.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.2.1:

- Résolution d'une régression interdisant la suppression de colonnes dans une table.
- Mise à jour du pilote mariadb-java-client-3.3.3.jar.
- Généralisation des classes Java génériques pour toutes les classes devant être partagées au niveau de l'API UNO (ie: sdb, sdbc et sdbcx).
- On peut désormais renommer les colonnes des tables sous SQLite et MariaDB.
- Il est également possible de renommer les colonnes déclarées comme clé primaire dans tous les pilotes embarqués.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.2.2:

- Mise en place de la gestion des index.
- Renommer une colonne déclarée comme clé primaire renommera également l'index associé à la clé primaire.
- Seuls les membres des classes Java répondant à l'API UNO ont un niveau de visibilité public, tous les autres membres ont une visibilité protégée ou privée.
- Résolution de nombreux problèmes et régressions.

### Ce qui a été fait pour la version 1.2.3:

- Renommer une colonne déclarée comme index renommera également la colonne de l'index associée.

### Ce qui a été fait pour la version 1.2.4:

- Suppression de SmallSQL.
- Intégration de Jaybird 5.0.4 le pilote JDBC pour Firebird.
- Vous pouvez désormais supprimer une clé primaire avec PostgreSQL.
- L'ajout ou la suppression d'une clé primaire génère une erreur si le pilote sous-jacent ne le supporte pas (SQLite).
- Lors de la [création d'une table][100] avec une clé primaire, si le pilote sous-jacent le prend en charge, la création de la clé primaire peut être effectuée par une commande DDL distincte. Cela permet à Jaybird de contourner le [dysfonctionnement #791][101] en créant une clé primaire nommée et permet de gérer des cas particuliers comme MariaDB ou SQLite pour leur gestion des auto-increments.
- Si le pilote sous-jacent le permet, lors de [la modification des colonnes][102] d'une table vous pouvez désormais:
  - La déclarer en auto-incrément (Identity) sans qu'elle soit nécessairement la clé primaire.
  - Ajouter ou retirer la contrainte Identity (auto-incrément).
  - Ajouter des commentaires.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.3.0:

- Intégration de la gestion des clés étrangères dans Base (**Outils -> Relations...**).
  - Lorsque vous renommez une table, cela renommera également le référencement de cette table dans les éventuelles clés étrangères pointant vers cette table.
  - Lorsque vous renommez une colonne, cela renommera également le référencement de cette colonne dans les éventuelles clés étrangères pointant vers cette colonne.
  - Ces mises à jour de clés étrangères prennent en compte le chargement paresseux des conteneurs des tables et des clés et ne seront effectuées que si Base a déjà accédé aux données impliquées.
  - Un problème persiste lors de la création de clés étrangères entre des tables qui n'ont pas le même catalogue et/ou schéma, voir [dysfonctionnement #160375][103]. Ce problème semble être lié à Base, j'espère qu'il sera résolu rapidement.
- Meilleure gestion des exceptions avec la possibilité de connaître l'état, le code SQL et le message de l'exception qui a été générée par le pilote sous-jacent.
- De nombreuses corrections et améliorations.

Normalement, je suis arrivé à couvrir toute l'étendue de l'API UNO ([com.sun.star.sdbc][10], [sdbcx][11] et [sdb][12]), ce qui à pris pas mal de temps, mais je ne pensais pas au départ y arriver.

### Ce qui a été fait pour la version 1.3.1:

- Correction de l'implémentation de l'interface [XRowLocate][104] responsable de la gestion des signets (Bookmark) dans les jeux de résultats (ResultSet). Cette nouvelle implémentation fonctionne avec tous les pilotes à l'exception de SQLite qui ne prend pas en charge la mise à jour des jeux de résultats. La présence de cette interface dans les jeux de résultats permet à Base d'éditer des tables même en l'absence de clé primaire. Avec certains pilotes (HsqlDB, H2 et Derby) le rafraîchissement en cours de saisie ne sera pas automatique et devra être fait manuellement. L'utilisation des signets peut être désactivée dans les options de l'extension.
- Mise en place de [jeu de resultats simulé][105] (java.sql.ResultSet) permettant de produire des ResultSet à partir des données de connexion fourni par le pilote, plus exactement à partir du fichier [Drivers.xcu][91]. L'utilisation de ces resultset simulé permet de fournir à Base des resultset conforme à ce qu'il attend même si le pilote sous jancent n'est pas capable de les produire. Ils servent à patcher les résultats obtenus les méthodes `getTypeInfo()`, `getTableTypes` et `getTablePrivileges()` de l'interface java.sql.DatabaseMetaData en utilisant respectivement les propriétés `TypeInfoSettings`, `TableTypesSettings` et `TablePrivilegesSettings` du fichier [Drivers.xcu][91].
- Ecriture d'un [conteneur spécifique][106] pour gérer les utilisateurs d'un rôle ou les rôles d'un rôle. Ce conteneur n'est qu'un pointeur vers les elements des conteneurs utilisateur et/ou role de la base de données. Lors de la suppression d'un utilisateur ou d'un rôle, ce conteneur sera mis à jour si nécessaire.
- Réécriture des fenêtres **Administration des utilisateurs** et **Administration des groupes** accessibles dans le menu **Administration** de Base. Désormais, si la propriété `TablePrivilegesSettings` est fournie par le pilote sous-jacent, seuls les privilèges déclarés dans cette propriété seront affichés. Cela permet une utilisation plus facile. Une [demande d'amélioration #160516][107] a été faite afin d'intégrer cette fonctionalité dans le code de Base.
- Intégration de tous les pilotes embarqués dans l'extension (hors SQLite) dans la gestion des utilisateurs, des rôles et des privilèges sur les tables et les vues. Je suppose que de nombreux dysfonctionnements restent à corriger, merci de me le faire savoir, détecter les dysfonctionnements me prend plus de temps que les corriger....
- De nombreuses corrections et améliorations...

### Ce qui a été fait pour la version 1.3.2:

Désormais l'API UNO SDBCX peut être utilisée pour la création de bases de données, comme c'est le cas pour les dernières versions des extensions utilisant jdbcDriverOOo. Il est possible de créer des tables, grâce à l'API UNO, avec les caractéristiques suivantes:
- Déclaration de colonnes de types TIMESTAMP WITH TIME ZONE, TIMESTAMP, TIME WITH TIME ZONE, TIME avec gestion de la précision (ie: de 0 à 9).
- Déclaration de tables [temporelles versionnées par le système][108] (temporal system versioned tables). Ces types de tables sont utilisés dans les mêmes extensions pour faciliter la réplication des données.
- Déclaration de tables au [format texte][109]. Ces tables vous permettent d'utiliser les données de fichiers au format csv.
- Déclaration des clés primaires, clés étrangères, index, utilisateurs, rôles et privilèges associés.

L'utilisation de l'API UNO pour créer des bases de données vous permettra d'utiliser du code indépendant de la base de données sous-jacente.

Les clients utilisant le pilote jdbcDriverOOo peuvent accéder aux fonctionnalités du pilote JDBC sous-jacent à l'aide de la méthode [XDriver.getPropertyInfo()][110] afin d'accéder au paramètre nécessaire lors de la création de tables et d'afficher correctement les privilèges. Ces paramètres étant accessibles directement par le pilote, peuvent être obtenus avant toute connexion, et permettent donc la création de la base de données lors de la première connexion.

### Ce qui a été fait pour la version 1.3.3:

- [Modification de la gestion][111] du paramètre de connexion `JavaDriverClassPath`. Ce paramètre peut désormais désigner un répertoire et dans ce cas tous les fichiers jar contenus seront ajoutés au `Java ClassPath`. Cela permet le chargement dynamique des pilotes JDBC nécessitant plusieurs archives (ie: Derby et Jaybird embedded). Cette modification a été apportée pour permettre à la nouvelle extension [JaybirdOOo][112] de fonctionner.
- Reprise d'une partie de l'implémentation de `javax.sql.rowset.CachedRowSet` dans les jeux de résultats [ScrollableResultSet.java][113] et [SensitiveResultSet.java][114] afin de simuler le type `TYPE_SCROLL_SENSITIVE` à partir des jeux de résultats de type `TYPE_FORWARD_ONLY` et `TYPE_SCROLL_INSENSITIVE` respectivement. Cela permet à LibreOffice Base d'utiliser des signets (ie : l'interface UNO [XRowLocate][104]) qui permettent des insertions, mises à jour et suppressions positionnées et donc, pour les bases de données le supportant, la possibilité d'éditer des tables ne contenant aucune clé primaire. De plus, un [mode SQL][115] **permet de rendre éditable n'importe quel ResultSet.** Ce mode peut être validée dans les options de l'extension, elle est trés puissante et donc à utiliser avec prudence. Concernant les jeux de résultats de type `TYPE_FORWARD_ONLY`, leur implémentation chargeant progressivement l'intégralité des données du jeu de résultats en mémoire peut conduire à un débordement de mémoire. La mise en oeuvre d'une pagination éliminera ce risque.
- Ajout du pilote MySQL Connector/J version 8.4.0. Ce driver ne semble pas fonctionner correctement, des erreurs assez surprenantes apparaissent... Je le laisse en place au cas où des gens seraient prêts à participer à son intégration? A utiliser avec précaution.
- Suite à la demande de [PeterSchmidt23][116] ajout du pilote [Trino][117] version 448. Ne connaissant pas Trino, qui a l'air étonnant par ailleur, seulement un début d'intégration a été réalisée. L'edition du contenu des tables n'est pas encore possible, voir [dysfonctionnement #22306][118]. Le nom des tables doit être en minuscule afin d'autoriser leur création.
- L'implémentation de `CachedRowSet` semble avoir résolu le problème d'insertion de cellules depuis Calc, voir [dysfonctionnement #7][119].
- De nombreuses corrections et améliorations...

### Ce qui a été fait pour la version 1.4.0:

- Mise à jour du pilote Jaybird vers la version finale 5.0.5.
- Modification de l'implémentation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][87]. Cette nouvelle implémentation prend en charge les pilotes qui ne suivent pas l'API JDBC mais proposent une implémentation spécifique (ie: MariaDB et Derby). Pour être activé lors de l'utilisation de fichiers odb créés avec une version précédente, s'il est présent, il est nécessaire de modifier le paramètre : `Requête des valeurs générées` accessible par le menu : **Edition -> Base de données -> Paramètres avancés... -> Valeurs générées** par la valeur : `SELECT * FROM %s WHERE %s`.
- Ajout de nouveaux paramètres pris en charge par le fichier de configuration [Drivers.xcu][91]. Ces nouveaux paramètres permettent de modifier les valeurs renvoyées par les pilotes concernant la visibilité des modifications dans les jeux de résultats (ie: insertion, mise à jour et suppression). Ils permettent également de forcer le mode SQL pour les modifications souhaitées dans les jeux de résultats.
- Finalisation de l'implémentation de l'émulation rendant tout jeu de résultats modifiable, si l'enregistrement est unique dans ce jeu de résultats. Cette implémentation, utilisant les signets (ie: bookmark), permet l'édition de jeu de résultats provenant de **Requêtes Base**, cela rend tout simplement les **Requêtes LibreOffice Base éditables**. Les requêtes joignant plusieurs tables ne sont pas encore supportées et je suis ouvert à toute proposition technique concernant une éventuelle implémentation.
- Afin de rendre modifiables les jeux de résultats retournés par le driver **Trino** et de précéder la [demande d'amélioration #22408][120], une recherche de la clé primaire sera lancée afin de retrouver la première colonne, du jeu de résultats, ayant pas de doublons.
- Afin de contourner le [dysfonctionnement #368][121] le driver HsqlDB utilise des mises à jour en mode SQL dans les jeux de résultats.
- De nombreuses corrections et améliorations...

### Ce qui a été fait pour la version 1.4.1:

- Nouvelle implémentation, que j'espère définitive, des signets (bookmarks). Il est basé sur trois fichiers et est tiré de l'implémentation par Sun de `javax.sql.rowset.CachedRowSet` :
  - [ScollableResultSet.class][113]
  - [SensitiveResultSet.class][114]
  - [CachedResultSet.class][122]
- **Ces ResultSets sont capables d'éditer presque toutes les requêtes créées dans LibreOffice Base, même les vues...** Mais afin de garantir une bonne fonctionnalité, certaines règles doivent être respectées afin de rendre un jeu de résultats éditable. Si la requête concerne plusieurs tables alors il est impératif d'inclure les clés primaires de chaque table dans la liste des colonnes du jeu de résultats. Si la requête ne concerne qu'une seule table alors le jeu de résultats sera modifiable s'il existe une colonne qui ne contient pas de doublon (ie: une clé naturelle). Cela permet de rendre modifiables les jeux de résultats provenant du pilote Trino.
- Suppression de l'utilisation de classes génériques là où elles n'étaient pas nécessaires. Cela a rendu le pilote plus rapide...
- Ajout de paramètres spéciaux dans: **Edition -> Base de données -> Paramètres avancés... -> Paramètres spéciaux** afin de répondre à la demande d'intégration du pilote Trino (voir [demande d'amélioration n°8][123]). Il est nécessaire de recréer les fichiers odb afin d'avoir accès à ces nouveaux paramètres.

### Ce qui a été fait pour la version 1.4.2:

- Pilote JDBC Trino mis à jour vers la version 453.
- Mise à jour du paquet [Python packaging][124] vers la version 24.1.
- Mise à jour du paquet [Python setuptools][125] vers la version 72.1.0 afin de répondre à l'[alerte de sécurité Dependabot][126].

### Ce qui a été fait pour la version 1.4.3:

- Mise à jour du paquet [Python setuptools][125] vers la version 73.0.1.
- La journalisation accessible dans les options de l’extension s’affiche désormais correctement sous Windows.

### Que reste-t-il à faire pour la version 1.4.3:

- Ajouter de nouvelles langues pour l'internationalisation...

- Tout ce qui est bienvenu...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_fr>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-143>
[6]: <https://prrvchr.github.io/README_fr>
[7]: <https://fr.libreoffice.org/download/telecharger-libreoffice/>
[8]: <https://www.openoffice.org/fr/Telecharger/>
[9]: <https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/package-summary.html>
[10]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[11]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[12]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[13]: <http://hsqldb.org/>
[14]: <https://github.com/xerial/sqlite-jdbc>
[15]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[16]: <https://jdbc.postgresql.org/>
[17]: <https://www.h2database.com/html/main.html>
[18]: <https://db.apache.org/derby/>
[19]: <https://firebirdsql.org/en/jdbc-driver/>
[20]: <https://dev.mysql.com/downloads/connector/j/>
[21]: <https://trino.io/docs/current/client/jdbc.html#installing>
[30]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#url-de-connexion>
[31]: <https://github.com/prrvchr/jdbcDriverOOo/>
[32]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[33]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10/fr>
[34]: <https://adoptium.net/releases.html?variant=openjdk11>
[35]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[36]: <https://prrvchr.github.io/HyperSQLOOo/README_fr>
[37]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-110>
[38]: <img/jdbcDriverOOo.svg#middle>
[39]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[40]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.4.3#right>
[41]: <img/jdbcDriverOOo-1_fr.png>
[42]: <img/jdbcDriverOOo-2_fr.png>
[43]: <img/jdbcDriverOOo-3_fr.png>
[44]: <img/jdbcDriverOOo-4_fr.png>
[45]: <img/jdbcDriverOOo-5_fr.png>
[46]: <https://prrvchr.github.io/HyperSQLOOo/README_fr#comment-migrer-une-base-de-donn%C3%A9es-int%C3%A9gr%C3%A9e>
[47]: <img/jdbcDriverOOo-6_fr.png>
[48]: <img/jdbcDriverOOo-7_fr.png>
[49]: <img/jdbcDriverOOo-8_fr.png>
[50]: <img/jdbcDriverOOo-9_fr.png>
[51]: <img/jdbcDriverOOo-10_fr.png>
[52]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[53]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[54]: <http://hsqldb.org/>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[56]: <https://github.com/hanya/MRI>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[58]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[59]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[60]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[61]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[62]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[63]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[64]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[65]: <https://www.slf4j.org/>
[66]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[67]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[68]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[69]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[70]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[71]: <https://github.com/gotson>
[72]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[73]: <https://prrvchr.github.io/SQLiteOOo/README_fr>
[74]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[75]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[76]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[77]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[78]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/CustomTypeInfo.java>
[79]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[80]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[81]: <https://github.com/artem78>
[82]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[83]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[84]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[85]: <https://peps.python.org/pep-0508/>
[86]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#pr%C3%A9requis>
[87]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[88]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
[89]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.3-SNAPSHOT.jar>
[90]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.6-SNAPSHOT.jar>
[91]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu>
[92]: <https://jira.mariadb.org/browse/CONJ-1160>
[93]: <https://fr.wikibooks.org/wiki/Programmation_Java/Types_g%C3%A9n%C3%A9riques>
[94]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/TableContainerSuper.java>
[95]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ViewContainer.java>
[96]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ColumnContainerBase.java>
[97]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/KeyContainer.java>
[98]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/IndexContainer.java>
[99]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Container.java>
[100]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L178>
[101]: <https://github.com/FirebirdSQL/jaybird/issues/791>
[102]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L276>
[103]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160375>
[104]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html>
[105]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset>
[106]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleContainer.java>
[107]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160516>
[108]: <https://hsqldb.org/doc/guide/management-chapt.html#mtc_system_versioned_tables>
[109]: <https://hsqldb.org/doc/guide/texttables-chapt.html#ttc_table_definition>
[110]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DriverBase.java#L185>
[111]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DriverBase.java#L395>
[112]: <https://prrvchr.github.io/JaybirdOOo/README_fr>
[113]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset/ScrollableResultSet.java#L57>
[114]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset/SensitiveResultSet.java#L60>
[115]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/rowset/RowSetWriter.java#L41>
[116]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8>
[117]: <https://trino.io/>
[118]: <https://github.com/trinodb/trino/issues/22306>
[119]: <https://github.com/prrvchr/jdbcDriverOOo/issues/7>
[120]: <https://github.com/trinodb/trino/issues/22408>
[121]: <https://sourceforge.net/p/hsqldb/feature-requests/368/>
[122]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset/CachedResultSet.java#L55>
[123]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8#issuecomment-2182445391>
[124]: <https://pypi.org/project/packaging/>
[125]: <https://pypi.org/project/setuptools/>
[126]: <https://github.com/prrvchr/jdbcDriverOOo/pull/9>
