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

# version [1.3.1][5]

## Introduction:

**jdbcDriverOOo** fait partie d'une [Suite][6] d'extensions [LibreOffice][7] ~~et/ou [OpenOffice][8]~~ permettant de vous offrir des services inovants dans ces suites bureautique.  

Cette extension est la transcription en Java pur de l'API [java.sql.*][9] vers l'API [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] et [com.sun.star.sdb][12] de UNO.
Elle vous permet d'utiliser le pilote JDBC de votre choix directement dans Base.  
Elle embarque les pilotes pour les base de données suivantes:
- [HyperSQL ou HsqlDB][13] version 2.7.2
- [SQLite via xerial sqlite-jdbc][14] version 3.45.1.6-SNAPSHOT
- [MariaDB via Connector/J][15] version 3.3.3
- [PostgreSQL via pgJDBC][16] version 42.7.1
- [H2 Database Engine][17] version 2.2.224 (2023-09-17)
- [Apache Derby][18] version 10.15.2.0
- [Firebird via Jaybird][19] version 5.0.4

Etant un logiciel libre je vous encourage:
- A dupliquer son [code source][20].
- A apporter des modifications, des corrections, des améliorations.
- D'ouvrir un [dysfonctionnement][21] si nécessaire.

Bref, à participer au developpement de cette extension.  
Car c'est ensemble que nous pouvons rendre le Logiciel Libre plus intelligent.

___

## Prérequis:

jdbcDriverOOo est un pilote JDBC écrit en Java.  
Son utilisation nécessite [l'installation et la configuration][22] dans LibreOffice d'un **JRE version 11 ou ultérieure**.  
Je vous recommande [Adoptium][23] comme source d'installation de Java.

Si vous utilisez le pilote HsqlDB avec **LibreOffice sous Linux**, alors vous êtes sujet au [dysfonctionnement #139538][24]. Pour contourner le problème, veuillez **désinstaller les paquets** avec les commandes:
- `sudo apt remove libreoffice-sdbc-hsqldb` (pour désinstaller le paquet libreoffice-sdbc-hsqldb)
- `sudo apt remove libhsqldb1.8.0-java` (pour désinstaller le paquet libhsqldb1.8.0-java)

Si vous souhaitez quand même utiliser la fonctionnalité HsqlDB intégré fournie par LibreOffice, alors installez l'extension [HyperSQLOOo][25].  

**Sous Linux et macOS les paquets Python** utilisés par l'extension, peuvent s'il sont déja installé provenir du système et donc, **peuvent ne pas être à jour**.  
Afin de s'assurer que vos paquets Python sont à jour il est recommandé d'utiliser l'option **Info système** dans les Options de l'extension accessible par:  
**Outils -> Options -> Pilotes Base -> Pilote JDBC -> Voir journal -> Info système**  
Si des paquets obsolètes apparaissent, vous pouvez les mettre à jour avec la commande:  
`pip install --upgrade <package-name>`

Pour plus d'information voir: [Ce qui a été fait pour la version 1.1.0][72].

___

## Installation:

Il semble important que le fichier n'ait pas été renommé lors de son téléchargement.  
Si nécessaire, renommez-le avant de l'installer.

- ![jdbcDriverOOo logo][26] Installer l'extension **[jdbcDriverOOo.oxt][27]** [![Version][28]][27]

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

![jdbcDriverOOo screenshot 1][29]

A l'étape: **Sélectionner une base de données**
- selectionner: Connecter une base de données existante
- choisir: **Pilote HsqlDB**
- cliquer sur le bouton: Suivant

![jdbcDriverOOo screenshot 2][30]

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

![jdbcDriverOOo screenshot 3][31]

A l'étape: **Paramétrer l'authentification de l'utilisateur**
- cliquer sur le bouton: Tester la connexion

![jdbcDriverOOo screenshot 4][32]

Si la connexion a réussi, vous devriez voir cette fenêtre de dialogue:

![jdbcDriverOOo screenshot 5][33]

Maintenant à vous d'en profiter...

### Comment mettre à jour le pilote JDBC:

Si vous souhaitez mettre à jour une base de données HsqlDB intégrée (un seul fichier odb), veuillez vous référer à la section: [Comment migrer une base de données intégrée][34].

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

![jdbcDriverOOo screenshot 6][35]

La gestion des privilèges des utilisateurs de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Modifier les privilèges**  
Si le privilège est hérité d'un rôle assigné, la case à cocher est de type à trois états.

![jdbcDriverOOo screenshot 7][36]

### La gestion des rôles (groupes) dans Base:

La gestion des rôles (groupes) de la base de données sous jacente est accessible dans Base par le menu: **Administration -> Gestion des groupes**

![jdbcDriverOOo screenshot 8][37]

La gestion des utilisateurs membres du groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Utilisateurs du groupe**

![jdbcDriverOOo screenshot 9][38]

La gestion des roles assignés au groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Roles du groupe**  
Cette fonctionnalité est une extension de l'API UNO et ne sera disponible que si le pilote LibreOffice / OpenOffice sous jacent le permet.

![jdbcDriverOOo screenshot 10][39]

___

## A été testé avec:

* LibreOffice 7.0.4.2 - Ubuntu 20.04 - LxQt 0.14.1

* LibreOffice 6.4.4.2 - Windows 7 SP1

Je vous encourage en cas de problème :confused:  
de créer un [dysfonctionnement][21]  
J'essaierai de le résoudre :smile:

___

## Historique:

### Introduction:

Ce pilote a été écrit pour contourner certains problèmes inhérents à l'implémentation UNO du pilote JDBC intégré dans LibreOffice / OpenOffice, à savoir:

- L'impossibilité de fournir le chemin de l'archive Java du driver (hsqldb.jar) lors du chargement du pilote JDBC.
- Ne pas pouvoir utiliser les instructions SQL préparées (PreparedStatement) voir [dysfonctionnement #132195][40].

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

- La rédaction de ce pilote a été facilitée par une [discussion avec Villeroy][41], sur le forum OpenOffice, que je tiens à remercier, car la connaissance ne vaut que si elle est partagée...

- Utilisation de la nouvelle version de HsqlDB 2.5.1.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.2:

- Ajout d'une boîte de dialogue permettant de mettre à jour le pilote (hsqldb.jar) dans: Outils -> Options -> Pilotes Base -> Pilote HsqlDB

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.3:

- Je tiens particulièrement à remercier fredt à [hsqldb.org][42] pour:

    - Son accueil pour ce projet et sa permission d'utiliser le logo HsqlDB dans l'extension.

    - Son implication dans la phase de test qui a permis de produire cette version 0.0.3.

    - La qualité de sa base de données HsqlDB.

- Fonctionne désormais avec OpenOffice sous Windows.

- Un protocole non pris en charge affiche désormais une erreur précise.

- Une url non analysable affiche désormais une erreur précise.

- Gère désormais correctement les espaces dans les noms de fichiers et les chemins.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.4:

- Réécriture de [Driver][43] en Java version 11 OpenJDK amd64 sous Eclipse IDE for Java Developers version 4.23.0 avec les plugins:
    - LOEclipse ou LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev ou Python IDE for Eclipse version 9.3.0.

- Ecriture des services `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` de JDBC (merci à hanya pour [MRI][44] qui m'a été d'une aide précieuse...)

    - [com.sun.star.sdb.*][45]
    - [com.sun.star.sdbc.*][46]
    - [com.sun.star.sdbcx.*][47]

- Intégration dans jdbcDriverOOo des pilotes JDBC **H2** et **Derby** en plus de **HsqlDB**. Implémentation de Services Java:

    - [Driver-HsqlDB.jar][48]
    - [Driver-H2.jar][49]
    - [Driver-Derby.jar][50]

    Afin de corriger d'éventuels défauts, ou incompatibilité avec l'API UNO, des pilotes JDBC embarqués. 

- Renommage du dépot et de l'extension **HsqlDBDriverOOo** en **jdbcDriverOOo**.

- Prise en charge dans Base des **clés primaires auto incrémentées** pour HsqlDB, H2 et Derby.

- Ecriture de [com.sun.star.sdbcx.Driver][51]. Ce pilote de haut niveau doit permettre la **gestion des utilisateurs, des rôles et des privilèges dans Base**. Son utilisation peut être désactivée via le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**.

- Implémentation d'un fournisseur de services Java [UnoLogger.jar][52] pour l'API [SLF4J][53] afin de pouvoir rediriger la journalisation des pilotes des bases de données sous-jacentes vers l'API UNO [com.sun.star.logging.*][54].

- Réécriture, en suivant le modèle MVC, de la fenêtre des [Options][55], accessible par le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**, pour permettre:

    - La mise à jour et/ou l'ajout d'archives Java de pilotes JDBC.
    - L'activation de la journalisation du pilote de la base de la données sous-jacente.

- Ecriture, en suivant le modèle MVC, des [fenêtres d'administration][56] des utilisateurs et des rôles (groupes) et de leurs privilèges associés, accessible dans Base par le menu: **Administration -> Gestion des utilisateurs** et/ou **Administration -> Gestion des groupes**, permettant:

    - La [gestion des utilisateurs][57] et de leurs privilèges.
    - La [gestion des rôles][58] (groupes) et de leurs privilèges.

    Ces nouvelles fonctionnalités n'ont étés testées pour l'instant qu'avec le pilote HsqlDB.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.0:

- Intégration de HyperSQL version 2.7.2.

### Ce qui a été fait pour la version 1.0.1:

- Intégration de [SQLite JDBC][14] version 3.42.0.0. Je tiens tout particulièrement à remercier [gotson][59] pour les [nombreuses améliorations apportées au pilote SQLite JDBC][60] qui ont rendu possible l'utilisation de SQLite dans LibreOffice/OpenOffice.

- Ce pilote peut être enveloppé par un autre pilote ([HyperSQLOOo][25] ou [SQLiteOOo][61]) grâce à une url de connexion désormais modifiable.

- Il est possible d'afficher ou non les tables système dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Afficher les tables système**

- Il est possible d'interdire l'utilisation de jeux de résultats actualisables dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Utiliser les bookmarks**

- De nombreuses corrections ont été apportées afin de rendre l'extension [SQLiteOOo][61] fonctionnelle.

### Ce qui a été fait pour la version 1.0.2:

- Intégration de [MariaDB Connector/J][15] version 3.1.4.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.3:

- Intégration de [H2][17] version 2.2.220.

- Intégration de la journalisation dans les jeux de résultat ([ResultSetBase][62] and [ResultSetSuper][63]) afin d'en savoir plus sur le [dysfonctionnement 156512][64].

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.4:

- Support dans la création de tables du paramètre [TypeInfoSettings][65] permettant de récupérer la précision pour les types SQL:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    Ceci n'est [integré][66] que pour le pilote [HsqlDB][67] pour le moment.

### Ce qui a été fait pour la version 1.0.5:

- Le résultat de l'accès à la méthode [XDatabaseMetaData.getDriverVersion()][68] est désormais enregistré dans le fichier journal.

### Ce qui a été fait pour la version 1.0.6:

- Ajout du paquet Python `packaging` dans le `pythonpath` de l'extension. Merci à [artem78][69] d'avoir permis cette correction en signalant cet oubli dans le [dysfonctionnement #4][70].

### Ce qui a été fait pour la version 1.0.7:

- Désormais, le pilote lève une exception si la création d'une nouvelle table échoue. Ceci est pour répondre au [dysfonctionnement #1][71] sur l'extension [HyperSQLOOo][25].

### Ce qui a été fait pour la version 1.0.8:

- Utilisation de la dernière version de l'API de journalisation.

### Ce qui a été fait pour la version 1.1.0:

- Tous les paquets Python nécessaires à l'extension sont désormais enregistrés dans un fichier [requirements.txt][73] suivant la [PEP 508][74].
- Désormais si vous n'êtes pas sous Windows alors les paquets Python nécessaires à l'extension peuvent être facilement installés avec la commande:  
  `pip install requirements.txt`
- Modification de la section [Prérequis][75].

### Ce qui a été fait pour la version 1.1.1:

- Le pilote n'utilise plus de jeux de résultats (ResultSet) pouvant être mis en signet (Bookmarkable) pour des raisons de performances dans LibreOffice Base. Ceci peut être changé dans les options d'extension.

### Ce qui a été fait pour la version 1.1.2:

- Implementation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][76]. Cette interface permet, lors d'une insertion de plusieurs lignes (ie: `INSERT INTO matable (Colonne1, Colonne2) VALUES (valeur1, valeur2), (valeur1, valeur2), ...`) dans une table disposant d'une clé primaire auto-incrémentée, de récupérer un ensemble de résultats à partir des lignes insérées dans la table et vous donne donc accès aux clés générées automatiquement en un seul coup.
- Implémentation de l'interface UNO [com.sun.star.sdbcx.XAlterTable][77]. Cette interface permet la modification des colonnes d'une table. Avec HsqlDB, il est maintenant possible dans Base:
  - D'attribuez une description aux colonnes des tables.
  - De modifier le type d'une colonne si le transtypage (CAST) des données contenues dans cette colonne le permet, sinon il vous sera proposé de remplacer cette colonne ce qui entraîne la suppression des données...
- Toutes les commandes DDL (ie: `CREATE TABLE...`, `ALTER TABLE...`) générées par jdbcDriverOOo sont désormais enregistrées dans la journalisation.
- Pilote SQLite mis à jour vers la dernière version 3.45.1.0.
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.1.3:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][78]. Ce nouveau pilote a été implémenté pour supporter une partie des spécifications JDBC 4.1 et plus particulièrement l'interface `java.sql.Statement.getGeneratedKeys()` et permet l'utilisation de l'interface [com.sun.star.sdbc.XGeneratedResultSet][75].

### Ce qui a été fait pour la version 1.1.4:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][79].
- Intégration du driver [PostgreSQL pgJDBC][16] version 42.7.1 dans l'archive de jdbcDriverOOo. Cette intégration a été réalisée sans utiliser de service Java spécifique à PostgreSQL mais uniquement en configurant le fichier [Drivers.xcu][80] permettant de déclarer le pilote JDBC à LibreOffice.
- Ouverture d'un [dysfonctionnement][81] pour le pilote [MariaDB Connector/J][15] afin qu'il prenne en charge `java.sql.Statement.getGeneratedKeys()` comme demandé par JDBC 4.1.
- Normalement les prochaines versions de jdbcDriverOOo devraient pouvoir être mises à jour dans la liste des extensions installées sous LibreOffice: **Outils -> Gestionnaire des extensions... -> Vérifier les mises à jour**.
- Désormais, seul le pilote HsqlDB a accès dans Base à l'administration des droits des utilisateurs et des groupes. Ceci est déterminé par le paramètre `IgnoreDriverPrivileges` dans le fichier [Drivers.xcu][80].
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.1.5:

- Vous pouvez désormais éditer une vue en mode SQL avec le pilote SQLite. Pour les pilotes qui ne prennent pas en charge la modification des vues, les vues sont supprimées puis recréées.

### Ce qui a été fait pour la version 1.1.6:

- Vous pouvez désormais renommer les tables et les vues dans Base. Toute la configuration nécessaire au renommage pour chaque pilote JDBC intégré est stockée uniquement dans le fichier [Drivers.xcu][80].
- Tous les pilotes JDBC intégrés à jdbcDriverOOo sont capables de renommer des tables ou des vues et même certains (ie: MariaDB et PostgreSQL) permettent de modifier le catalogue ou le schéma.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.2.0:

- Tous les pilotes intégrés à l'extension sont **désormais entièrement fonctionnels dans Base** pour la gestion des tables et des vues.
- Des fonctions intelligentes sont appelées pour:
  - Le déplacement avec renommage des tables, pour les drivers le permettant et utilisant deux commandes SQL, l'ordre des commandes SQL sera optimisé (PostgreSQL).
  - Le Renommage d'une vue, si le pilote ne le supporte pas, elle sera supprimée puis recréée (SQLite).
- Utilisation de [classe générique Java][82] pour la gestion des conteneurs utilisés pour la gestion des [tables][83], des [vues][84], des [colonnes][85], des [clefs][86] et des [indexes][87]. L'utilisation de classes génériques dans les [conteneurs][88] permettra de se passer de l'interface UNO XPropertySet et de pouvoir retranscrire le code existant en Java pur.
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
- Lors de la [création d'une table][89] avec une clé primaire, si le pilote sous-jacent le prend en charge, la création de la clé primaire peut être effectuée par une commande DDL distincte. Cela permet à Jaybird de contourner le [dysfonctionnement #791][90] en créant une clé primaire nommée et permet de gérer des cas particuliers comme MariaDB ou SQLite pour leur gestion des auto-increments.
- Si le pilote sous-jacent le permet, lors de [la modification des colonnes][91] d'une table vous pouvez désormais:
  - La déclarer en auto-incrément (Identity) sans qu'elle soit nécessairement la clé primaire.
  - Ajouter ou retirer la contrainte Identity (auto-incrément).
  - Ajouter des commentaires.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.3.0:

- Intégration de la gestion des clés étrangères dans Base (**Outils -> Relations...**).
  - Lorsque vous renommez une table, cela renommera également le référencement de cette table dans les éventuelles clés étrangères pointant vers cette table.
  - Lorsque vous renommez une colonne, cela renommera également le référencement de cette colonne dans les éventuelles clés étrangères pointant vers cette colonne.
  - Ces mises à jour de clés étrangères prennent en compte le chargement paresseux des conteneurs des tables et des clés et ne seront effectuées que si Base a déjà accédé aux données impliquées.
  - Un problème persiste lors de la création de clés étrangères entre des tables qui n'ont pas le même catalogue et/ou schéma, voir [dysfonctionnement #160375][92]. Ce problème semble être lié à Base, j'espère qu'il sera résolu rapidement.
- Meilleure gestion des exceptions avec la possibilité de connaître l'état, le code SQL et le message de l'exception qui a été générée par le pilote sous-jacent.
- De nombreuses corrections et améliorations.

Normalement, je suis arrivé à couvrir toute l'étendue de l'API UNO ([com.sun.star.sdbc][10], [sdbcx][11] et [sdb][12]), ce qui à pris pas mal de temps, mais je ne pensais pas au départ y arriver.

### Ce qui a été fait pour la version 1.3.1:

- Mise en place de [jeu de resultats simulé][93] (ResultSet) permettant de produire des ResultSet à partir des données de connexion fourni par le pilote, plus exactement à partir du [fichier Drivers.xcu][80]. L'utilisation de ces resultset simulé permet de fournir à Base des resultset conforme à ce qu'il attend même si le pilote sous jancent n'est pas capable de les produire. Ils servent à patcher les résultats obtenus les méthodes `getTypeInfo()` et `getTablePrivileges()` de l'interface UNO XDatabaseMetaData en utilisant les propriétés `TypeInfoSettings` et `TablePrivilegesSettings` du fichier Drivers.xcu.
- Ecriture d'un [conteneur specifique][94] pour la gestion des utilisateurs d'un role ou des roles d'un role. Ce conteneur n'est qu'un pointeur vers les elements des conteneurs utilisateur et/ou role de la base de données. Lors de la suppression d'un utilisateur ou d'un rôle, ce conteneur sera mis à jour si nécessaire.
- Réécriture des fenêtres **Administration des utilisateurs** et **Administration des groupes** accessibles dans le menu **Administration** de Base. Désormais, seuls les privilèges pris en charge par le pilote sous-jacent seront affichés. Cela permet une utilisation plus facile. Une [demande d'amélioration #160516][95] a été faite afin d'intégrer cette possibilité dans le code de Base.
- Intégration de tous les pilotes embarqués dans l'extension (hors SQLite) dans la gestion des utilisateurs, rôles et privilèges. Je suppose que de nombreux dysfonctionnements restent à corriger, merci de me le faire savoir, détecter les dysfonctionnements me prend plus de temps que les corriger....
- De nombreuses corrections et améliorations...

### Que reste-t-il à faire pour la version 1.3.1:

- Ajouter de nouvelles langues pour l'internationalisation...

- Tout ce qui est bienvenu...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_fr>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-130>
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
[20]: <https://github.com/prrvchr/jdbcDriverOOo/>
[21]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[22]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10/fr>
[23]: <https://adoptium.net/releases.html?variant=openjdk11>
[24]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[25]: <https://prrvchr.github.io/HyperSQLOOo/README_fr>
[26]: <img/jdbcDriverOOo.svg#middle>
[27]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[28]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.3.0#right>
[29]: <img/jdbcDriverOOo-1_fr.png>
[30]: <img/jdbcDriverOOo-2_fr.png>
[31]: <img/jdbcDriverOOo-3_fr.png>
[32]: <img/jdbcDriverOOo-4_fr.png>
[33]: <img/jdbcDriverOOo-5_fr.png>
[34]: <https://prrvchr.github.io/HyperSQLOOo/README_fr#comment-migrer-une-base-de-donn%C3%A9es-int%C3%A9gr%C3%A9e>
[35]: <img/jdbcDriverOOo-6_fr.png>
[36]: <img/jdbcDriverOOo-7_fr.png>
[37]: <img/jdbcDriverOOo-8_fr.png>
[38]: <img/jdbcDriverOOo-9_fr.png>
[39]: <img/jdbcDriverOOo-10_fr.png>
[40]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[41]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[42]: <http://hsqldb.org/>
[43]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[44]: <https://github.com/hanya/MRI>
[45]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[46]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[47]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[49]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[50]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[52]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[53]: <https://www.slf4j.org/>
[54]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[56]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[58]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[59]: <https://github.com/gotson>
[60]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[61]: <https://prrvchr.github.io/SQLiteOOo/README_fr>
[62]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[63]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[64]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[65]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[66]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/CustomTypeInfo.java>
[67]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[68]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[69]: <https://github.com/artem78>
[70]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[71]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[72]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-110>
[73]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[74]: <https://peps.python.org/pep-0508/>
[75]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#pr%C3%A9requis>
[76]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[77]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
[78]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.3-SNAPSHOT.jar>
[79]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.6-SNAPSHOT.jar>
[80]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu>
[81]: <https://jira.mariadb.org/browse/CONJ-1160>
[82]: <https://fr.wikibooks.org/wiki/Programmation_Java/Types_g%C3%A9n%C3%A9riques>
[83]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/TableContainerSuper.java>
[84]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ViewContainer.java>
[85]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ColumnContainerBase.java>
[86]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/KeyContainer.java>
[87]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/IndexContainer.java>
[88]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Container.java>
[89]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L178>
[90]: <https://github.com/FirebirdSQL/jaybird/issues/791>
[91]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/DBTableHelper.java#L276>
[92]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160375>
[93]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/resultset>
[94]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleContainer.java>
[95]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160516>
