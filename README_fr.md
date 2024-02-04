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

# version [1.1.2][5]

## Introduction:

**jdbcDriverOOo** fait partie d'une [Suite][6] d'extensions [LibreOffice][7] ~~et/ou [OpenOffice][8]~~ permettant de vous offrir des services inovants dans ces suites bureautique.  

Cette extension est la transcription en Java pur de l'API [java.sql.*][9] vers l'API [com.sun.star.sdbc][10], [com.sun.star.sdbcx][11] et [com.sun.star.sdb][12] de UNO.
Elle vous permet d'utiliser le pilote JDBC de votre choix directement dans Base.  
Elle embarque les pilotes pour les base de données suivantes:
- [HyperSQL ou HsqlDB][13] version 2.7.2  
  Les protocoles gérés par HsqlDB pris en charge sont: hsql://, hsqls://, http://, https://, mem://, file:// et res://
- [SQLite JDBC Driver][14] version 3.42.0.0
- [MariaDB Connector/J][15] version 3.1.4
- [H2 Database Engine][16] version 2.2.220 (2023-07-04)
- [Apache Derby][17] version 10.15.2.0
- [SmallSQL][18] version 0.22

Etant un logiciel libre je vous encourage:
- A dupliquer son [code source][19].
- A apporter des modifications, des corrections, des améliorations.
- D'ouvrir un [dysfonctionnement][20] si nécessaire.

Bref, à participer au developpement de cette extension.  
Car c'est ensemble que nous pouvons rendre le Logiciel Libre plus intelligent.

___

## Prérequis:

jdbcDriverOOo est un pilote JDBC écrit en Java.  
Son utilisation nécessite [l'installation et la configuration][21] dans LibreOffice d'un **JRE version 11 ou ultérieure**.  
Je vous recommande [Adoptium][22] comme source d'installation de Java.

Si vous utilisez le pilote HsqlDB avec **LibreOffice sous Linux**, alors vous êtes sujet au [dysfonctionnement #139538][23]. Pour contourner le problème, veuillez **désinstaller les paquets** avec les commandes:
- `sudo apt remove libreoffice-sdbc-hsqldb` (pour désinstaller le paquet libreoffice-sdbc-hsqldb)
- `sudo apt remove libhsqldb1.8.0-java` (pour désinstaller le paquet libhsqldb1.8.0-java)

Si vous souhaitez quand même utiliser la fonctionnalité HsqlDB intégré fournie par LibreOffice, alors installez l'extension [HyperSQLOOo][24].  

**Sous Linux et macOS les paquets Python** utilisés par l'extension, peuvent s'il sont déja installé provenir du système et donc, **peuvent ne pas être à jour**.  
Afin de s'assurer que vos paquets Python sont à jour il est recommandé d'utiliser l'option **Info système** dans les Options de l'extension accessible par:  
**Outils -> Options -> Pilotes Base -> Pilote JDBC -> Voir journal -> Info système**  
Si des paquets obsolètes apparaissent, vous pouvez les mettre à jour avec la commande:  
`pip install --upgrade <package-name>`

Pour plus d'information voir: [Ce qui a été fait pour la version 1.1.0][71].

___

## Installation:

Il semble important que le fichier n'ait pas été renommé lors de son téléchargement.  
Si nécessaire, renommez-le avant de l'installer.

- ![jdbcDriverOOo logo][25] Installer l'extension **[jdbcDriverOOo.oxt][26]** [![Version][27]][26]

Redémarrez LibreOffice après l'installation.  
**Attention, redémarrer LibreOffice peut ne pas suffire.**
- **Sous Windows** pour vous assurer que LibreOffice redémarre correctement, utilisez le Gestionnaire de tâche de Windows pour vérifier qu'aucun service LibreOffice n'est visible après l'arrêt de LibreOffice (et tuez-le si ç'est le cas).
- **Sous Linux ou macOS** vous pouvez également vous assurer que LibreOffice redémarre correctement, en le lançant depuis un terminal avec la commande `soffice` et en utilisant la combinaison de touches `Ctrl + C` si après l'arrêt de LibreOffice, le terminal n'est pas actif (pas d'invité de commande).

___

## Utilisation:

Ce mode d'utilisation utilise une base de données HsqlDB.

### Comment créer une nouvelle base de données:

Dans LibreOffice / OpenOffice aller au menu: **Fichier -> Nouveau -> Base de données**

![jdbcDriverOOo screenshot 1][28]

A l'étape: **Sélectionner une base de données**
- selectionner: Connecter une base de données existante
- choisir: Pilote HsqlDB
- cliquer sur le bouton: Suivant

![jdbcDriverOOo screenshot 2][29]

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

![jdbcDriverOOo screenshot 3][30]

A l'étape: **Paramétrer l'authentification de l'utilisateur**
- cliquer sur le bouton: Tester la connexion

![jdbcDriverOOo screenshot 4][31]

Si la connexion a réussi, vous devriez voir cette fenêtre de dialogue:

![jdbcDriverOOo screenshot 5][32]

Maintenant à vous d'en profiter...

### Comment mettre à jour le pilote JDBC:

Si vous souhaitez mettre à jour une base de données HsqlDB intégrée (un seul fichier odb), veuillez vous référer à la section: [Comment migrer une base de données intégrée][33].

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

![jdbcDriverOOo screenshot 6][34]

La gestion des privilèges des utilisateurs de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Modifier les privilèges**  
Si le privilège est hérité d'un rôle assigné, la case à cocher est de type à trois états.

![jdbcDriverOOo screenshot 7][35]

### La gestion des rôles (groupes) dans Base:

La gestion des rôles (groupes) de la base de données sous jacente est accessible dans Base par le menu: **Administration -> Gestion des groupes**

![jdbcDriverOOo screenshot 8][36]

La gestion des utilisateurs membres du groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Utilisateurs du groupe**

![jdbcDriverOOo screenshot 9][37]

La gestion des roles assignés au groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Roles du groupe**  
Cette fonctionnalité est une extension de l'API UNO et ne sera disponible que si le pilote LibreOffice / OpenOffice sous jacent le permet.

![jdbcDriverOOo screenshot 10][38]

___

## A été testé avec:

* LibreOffice 7.0.4.2 - Ubuntu 20.04 - LxQt 0.14.1

* LibreOffice 6.4.4.2 - Windows 7 SP1

* Apache OpenOffice 4.1.13 - Lubuntu 22.04

Je vous encourage en cas de problème :confused:  
de créer un [dysfonctionnement][20]  
J'essaierai de le résoudre :smile:

___

## Historique:

### Introduction:

Ce pilote a été écrit pour contourner certains problèmes inhérents à l'implémentation UNO du pilote JDBC intégré dans LibreOffice / OpenOffice, à savoir:

- L'impossibilité de fournir le chemin de l'archive Java du driver (hsqldb.jar) lors du chargement du pilote JDBC.
- Ne pas pouvoir utiliser les instructions SQL préparées (PreparedStatement) voir [dysfonctionnement #132195][39].

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

- La rédaction de ce pilote a été facilitée par une [discussion avec Villeroy][40], sur le forum OpenOffice, que je tiens à remercier, car la connaissance ne vaut que si elle est partagée...

- Utilisation de la nouvelle version de HsqlDB 2.5.1.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.2:

- Ajout d'une boîte de dialogue permettant de mettre à jour le pilote (hsqldb.jar) dans: Outils -> Options -> Pilotes Base -> Pilote HsqlDB

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.3:

- Je tiens particulièrement à remercier fredt à [hsqldb.org][41] pour:

    - Son accueil pour ce projet et sa permission d'utiliser le logo HsqlDB dans l'extension.

    - Son implication dans la phase de test qui a permis de produire cette version 0.0.3.

    - La qualité de sa base de données HsqlDB.

- Fonctionne désormais avec OpenOffice sous Windows.

- Un protocole non pris en charge affiche désormais une erreur précise.

- Une url non analysable affiche désormais une erreur précise.

- Gère désormais correctement les espaces dans les noms de fichiers et les chemins.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.4:

- Réécriture de [Driver][42] en Java version 11 OpenJDK amd64 sous Eclipse IDE for Java Developers version 4.23.0 avec les plugins:
    - LOEclipse ou LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev ou Python IDE for Eclipse version 9.3.0.

- Ecriture des services `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` de JDBC (merci à hanya pour [MRI][43] qui m'a été d'une aide précieuse...)

    - [com.sun.star.sdb.*][44]
    - [com.sun.star.sdbc.*][45]
    - [com.sun.star.sdbcx.*][46]

- Intégration dans jdbcDriverOOo des pilotes JDBC **H2** et **Derby** en plus de **HsqlDB**. Implémentation de Services Java:

    - [Driver-HsqlDB.jar][47]
    - [Driver-H2.jar][48]
    - [Driver-Derby.jar][49]

    Afin de corriger d'éventuels défauts, ou incompatibilité avec l'API UNO, des pilotes JDBC embarqués. 

- Renommage du dépot et de l'extension **HsqlDBDriverOOo** en **jdbcDriverOOo**.

- Prise en charge dans Base des **clés primaires auto incrémentées** pour HsqlDB, H2 et Derby.

- Ecriture de [com.sun.star.sdbcx.Driver][50]. Ce pilote de haut niveau doit permettre la **gestion des utilisateurs, des rôles et des privilèges dans Base**. Son utilisation peut être désactivée via le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**.

- Implémentation d'un fournisseur de services Java [UnoLogger.jar][51] pour l'API [SLF4J][52] afin de pouvoir rediriger la journalisation des pilotes des bases de données sous-jacentes vers l'API UNO [com.sun.star.logging.*][53].

- Réécriture, en suivant le modèle MVC, de la fenêtre des [Options][54], accessible par le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**, pour permettre:

    - La mise à jour et/ou l'ajout d'archives Java de pilotes JDBC.
    - L'activation de la journalisation du pilote de la base de la données sous-jacente.

- Ecriture, en suivant le modèle MVC, des [fenêtres d'administration][55] des utilisateurs et des rôles (groupes) et de leurs privilèges associés, accessible dans Base par le menu: **Administration -> Gestion des utilisateurs** et/ou **Administration -> Gestion des groupes**, permettant:

    - La [gestion des utilisateurs][56] et de leurs privilèges.
    - La [gestion des rôles][57] (groupes) et de leurs privilèges.

    Ces nouvelles fonctionnalités n'ont étés testées pour l'instant qu'avec le pilote HsqlDB.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.0:

- Intégration de HyperSQL version 2.7.2.

### Ce qui a été fait pour la version 1.0.1:

- Intégration de [SQLite JDBC][14] version 3.42.0.0. Je tiens tout particulièrement à remercier [gotson][58] pour les [nombreuses améliorations apportées au pilote SQLite JDBC][59] qui ont rendu possible l'utilisation de SQLite dans LibreOffice/OpenOffice.

- Ce pilote peut être enveloppé par un autre pilote ([HyperSQLOOo][24] ou [SQLiteOOo][60]) grâce à une url de connexion désormais modifiable.

- Il est possible d'afficher ou non les tables système dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Afficher les tables système**

- Il est possible d'interdire l'utilisation de jeux de résultats actualisables dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Utiliser les bookmarks**

- De nombreuses corrections ont été apportées afin de rendre l'extension [SQLiteOOo][60] fonctionnelle.

### Ce qui a été fait pour la version 1.0.2:

- Intégration de [MariaDB Connector/J][15] version 3.1.4.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.3:

- Intégration de [H2][16] version 2.2.220.

- Intégration de la journalisation dans les jeux de résultat ([ResultSetBase][61] and [ResultSetSuper][62]) afin d'en savoir plus sur le [dysfonctionnement 156512][63].

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.4:

- Support dans la création de tables du paramètre [TypeInfoSettings][64] permettant de récupérer la précision pour les types SQL:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    Ceci n'est [integré][65] que pour le pilote [HsqlDB][66] pour le moment.

### Ce qui a été fait pour la version 1.0.5:

- Le résultat de l'accès à la méthode [XDatabaseMetaData.getDriverVersion()][67] est désormais enregistré dans le fichier journal.

### Ce qui a été fait pour la version 1.0.6:

- Ajout du paquet Python `packaging` dans le `pythonpath` de l'extension. Merci à [artem78][68] d'avoir permis cette correction en signalant cet oubli dans le [dysfonctionnement #4][69].

### Ce qui a été fait pour la version 1.0.7:

- Désormais, le pilote lève une exception si la création d'une nouvelle table échoue. Ceci est pour répondre au [dysfonctionnement #1][70] sur l'extension [HyperSQLOOo][24].

### Ce qui a été fait pour la version 1.0.8:

- Utilisation de la dernière version de l'API de journalisation.

### Ce qui a été fait pour la version 1.1.0:

- Tous les paquets Python nécessaires à l'extension sont désormais enregistrés dans un fichier [requirements.txt][72] suivant la [PEP 508][73].
- Désormais si vous n'êtes pas sous Windows alors les paquets Python nécessaires à l'extension peuvent être facilement installés avec la commande:  
  `pip install requirements.txt`
- Modification de la section [Prérequis][74].

### Ce qui a été fait pour la version 1.1.1:

- Le pilote n'utilise plus de jeux de résultats (ResultSet) pouvant être mis en signet (Bookmarkable) pour des raisons de performances dans LibreOffice Base. Ceci peut être changé dans les options d'extension.

### Ce qui a été fait pour la version 1.1.2:

- Implementation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][75]. Cette interface permet, lors d'une insertion de plusieurs lignes (ie: `INSERT INTO matable (Colonne1, Colonne2) VALUES (valeur1, valeur2), (valeur1, valeur2), ...`) dans une table disposant d'une clé primaire auto-incrémentée, de récupérer un ensemble de résultats à partir des lignes insérées dans la table et vous donne donc accès aux clés générées automatiquement en un seul coup.
- Implémentation de l'interface UNO [com.sun.star.sdbcx.XAlterTable][76]. Cette interface permet la modification des colonnes d'une table. Avec HsqlDB, il est maintenant possible dans Base:
  - D'attribuez une description aux colonnes des tables.
  - De modifier le type d'une colonne si le transtypage (CAST) des données contenues dans cette colonne le permet, sinon il vous sera proposé de remplacer cette colonne ce qui entraîne la suppression des données...
- Toutes les commandes DDL (ie: `CREATE TABLE...`, `ALTER TABLE...`) générées par Base sont désormais enregistrées dans la journalisation.
- Pilote SQLite mis à jour vers la dernière version 3.45.1.0.
- Beaucoup d'autres correctifs...

### Que reste-t-il à faire pour la version 1.1.2:

- Ajouter de nouvelles langues pour l'internationalisation...

- Tout ce qui est bienvenu...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_fr>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-110>
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
[16]: <https://www.h2database.com/html/main.html>
[17]: <https://db.apache.org/derby/>
[18]: <https://github.com/CptTZ/SmallSQL>
[19]: <https://github.com/prrvchr/jdbcDriverOOo/>
[20]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[21]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10/fr>
[22]: <https://adoptium.net/releases.html?variant=openjdk11>
[23]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[24]: <https://prrvchr.github.io/HyperSQLOOo/README_fr>
[25]: <img/jdbcDriverOOo.svg#middle>
[26]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/jdbcDriverOOo.oxt>
[27]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.1.2#right>
[28]: <img/jdbcDriverOOo-1_fr.png>
[29]: <img/jdbcDriverOOo-2_fr.png>
[30]: <img/jdbcDriverOOo-3_fr.png>
[31]: <img/jdbcDriverOOo-4_fr.png>
[32]: <img/jdbcDriverOOo-5_fr.png>
[33]: <https://prrvchr.github.io/HyperSQLOOo/README_fr#comment-migrer-une-base-de-donn%C3%A9es-int%C3%A9gr%C3%A9e>
[34]: <img/jdbcDriverOOo-6_fr.png>
[35]: <img/jdbcDriverOOo-7_fr.png>
[36]: <img/jdbcDriverOOo-8_fr.png>
[37]: <img/jdbcDriverOOo-9_fr.png>
[38]: <img/jdbcDriverOOo-10_fr.png>
[39]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[40]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[41]: <http://hsqldb.org/>
[42]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[43]: <https://github.com/hanya/MRI>
[44]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[45]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[46]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[47]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[49]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[50]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[52]: <https://www.slf4j.org/>
[53]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[54]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[56]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[58]: <https://github.com/gotson>
[59]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[60]: <https://prrvchr.github.io/SQLiteOOo/README_fr>
[61]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[62]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetSuper.java>
[63]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[64]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[65]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/jdbcdriver/CustomTypeInfo.java>
[66]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[67]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[68]: <https://github.com/artem78>
[69]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[70]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[71]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-110>
[72]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[73]: <https://peps.python.org/pep-0508/>
[74]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#pr%C3%A9requis>
[75]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[76]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
