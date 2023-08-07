# Documentation

**This [document][2] in English.**

**L'utilisation de ce logiciel vous soumet à nos [Conditions d'utilisation][3]**

# version [1.0.2][4]

## Introduction:

**jdbcDriverOOo** fait partie d'une [Suite][5] d'extensions [LibreOffice][6] et/ou [OpenOffice][7] permettant de vous offrir des services inovants dans ces suites bureautique.  

Cette extension est la pure transcription Java de l'API [java.sql.*][8] vers l'API [com.sun.star.sdbc][9], [com.sun.star.sdbcx][10] et [com.sun.star.sdb][11] de UNO.
Elle vous permet d'utiliser le pilote JDBC de votre choix directement dans Base.  
Elle embarque les pilotes pour les base de données suivantes:
- [HyperSQL ou HsqlDB][12] version 2.72
    Les protocoles gérés par HsqlDB pris en charge sont: hsql://, hsqls://, http://, https://, mem://, file:// et res://
- [SQLite JDBC Driver][13] version 3.42.0.0
- [MariaDB Connector/J][14] version 3.1.4
- [H2 Database Engine][15] version 2.219-SNAPSHOT (2022-06-13)
- [Apache Derby][16] version 10.15.2.0
- [SmallSQL][17] version 0.22

Etant un logiciel libre je vous encourage:
- A dupliquer son [code source][18].
- A apporter des modifications, des corrections, des améliorations.
- D'ouvrir un [dysfonctionnement][19] si nécessaire.

Bref, à participer au developpement de cette extension.  
Car c'est ensemble que nous pouvons rendre le Logiciel Libre plus intelligent.

___
## Prérequis:

jdbcDriverOOo est un pilote JDBC écrit en Java.  
Son utilisation nécessite [l'installation et la configuration][20] dans LibreOffice / OpenOffice d'un **JRE version 11 ou ultérieure**.  
Je vous recommande [Adoptium][21] comme source d'installation de Java.

Si vous utilisez le pilote HsqlDB avec **LibreOffice sous Linux**, alors vous êtes sujet au [dysfonctionnement 139538][22]. Pour contourner le problème, veuillez **désinstaller les paquets** avec les commandes:
- `sudo apt remove libreoffice-sdbc-hsqldb` (pour désinstaller le paquet libreoffice-sdbc-hsqldb)
- `sudo apt remove libhsqldb1.8.0-java` (pour désinstaller le paquet libhsqldb1.8.0-java)

Si vous souhaitez quand même utiliser la fonctionnalité HsqlDB intégré fournie par LibreOffice, alors installez l'extension [HsqlDriverOOo][23].  

___
## Installation:

Il semble important que le fichier n'ait pas été renommé lors de son téléchargement.  
Si nécessaire, renommez-le avant de l'installer.

- Installer l'extension ![jdbcDriverOOo logo][1] **[jdbcDriverOOo.oxt][24]** version 1.0.1.

Redémarrez LibreOffice / OpenOffice après l'installation.

___
## Utilisation:

Ce mode d'utilisation utilise une base de données HsqlDB.

### Comment créer une nouvelle base de données:

Dans LibreOffice / OpenOffice aller au menu: **Fichier -> Nouveau -> Base de données**

![jdbcDriverOOo screenshot 1][25]

A l'étape: **Sélectionner une base de données**
- selectionner: Connecter une base de données existante
- choisir: Pilote HsqlDB
- cliquer sur le bouton: Suivant

![jdbcDriverOOo screenshot 2][26]

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

![jdbcDriverOOo screenshot 3][27]

A l'étape: **Paramétrer l'authentification de l'utilisateur**
- cliquer sur le bouton: Tester la connexion

![jdbcDriverOOo screenshot 4][28]

Si la connexion a réussi, vous devriez voir cette fenêtre de dialogue:

![jdbcDriverOOo screenshot 5][29]

Maintenant à vous d'en profiter...

### Comment mettre à jour le pilote JDBC:

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

![jdbcDriverOOo screenshot 6][30]

La gestion des privilèges des utilisateurs de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Modifier les privilèges**  
Si le privilège est hérité d'un rôle assigné, la case à cocher est de type à trois états.

![jdbcDriverOOo screenshot 7][31]

### La gestion des rôles (groupes) dans Base:

La gestion des rôles (groupes) de la base de données sous jacente est accessible dans Base par le menu: **Administration -> Gestion des groupes**

![jdbcDriverOOo screenshot 8][32]

La gestion des utilisateurs membres du groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Utilisateurs du groupe**

![jdbcDriverOOo screenshot 9][33]

La gestion des roles assignés au groupe de la base de données sous jacente est accessible dans cette fenêtre par le bouton: **Roles du groupe**  
Cette fonctionnalité est une extension de l'API UNO et ne sera disponible que si le pilote LibreOffice / OpenOffice sous jacent le permet.

![jdbcDriverOOo screenshot 10][34]

___
## A été testé avec:

* LibreOffice 7.0.4.2 - Ubuntu 20.04 - LxQt 0.14.1

* LibreOffice 6.4.4.2 - Windows 7 SP1

* Apache OpenOffice 4.1.13 - Lubuntu 22.04

Je vous encourage en cas de problème :-(  
de créer un [dysfonctionnement][35]  
J'essaierai de le résoudre ;-)

___
## Historique:

### Introduction:

Ce pilote a été écrit pour contourner certains problèmes inhérents à l'implémentation UNO du pilote JDBC intégré dans LibreOffice / OpenOffice, à savoir:

- L'impossibilité de fournir le chemin de l'archive Java du driver (hsqldb.jar) lors du chargement du pilote JDBC.
- Ne pas pouvoir utiliser les instructions SQL préparées (PreparedStatement) voir [bug 132195][36].

Afin de profiter des dernières fonctionnalités offertes par les bases de données et entre autre HsqlDB, il était nécessaire d'écrire un nouveau pilote.

Jusqu'à la version 0.0.3, ce nouveau pilote n'est qu'une surcouche ou emballage (wrapper) en Python autour des services UNO fournis par le pilote LibreOffice / OpenOffice JDBC défectueux.  
Depuis la version 0.0.4, il a été complètement réécrit en Java sous Eclipse, car qui mieux que Java peut donner accès à JDBC dans l'API UNO...  
Afin de ne pas empêcher le pilote JDBC natif de fonctionner, il se charge lors de l'appel des protocoles suivants:

- `xdbc:*`
- `xdbc:h2:*`
- `xdbc:derby:*`
- `xdbc:hsqldb:*`

mais utilise le protocole `jdbc:*` en interne pour se connecter.

Il permet également d'offrir des fonctionnalités que le pilote JDBC implémenté dans LibreOffice / OpenOffice ne fournit pas, à savoir:

- La gestion des utilisateurs, des roles (groupes) et des privilèges dans Base.
- L'utilisation du type SQL Array dans les requêtes.
- Tout ce que nous sommes prêts à mettre en œuvre.

### Ce qui a été fait pour la version 0.0.1:

- La rédaction de ce pilote a été facilitée par une [discussion avec Villeroy][37], sur le forum OpenOffice, que je tiens à remercier, car la connaissance ne vaut que si elle est partagée...

- Utilisation de la nouvelle version de HsqlDB 2.5.1.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.2:

- Ajout d'une boîte de dialogue permettant de mettre à jour le pilote (hsqldb.jar) dans: Outils -> Options -> Pilotes Base -> Pilote HsqlDB

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.3:

- Je tiens particulièrement à remercier fredt à [hsqldb.org][38] pour:

    - Son accueil pour ce projet et sa permission d'utiliser le logo HsqlDB dans l'extension.

    - Son implication dans la phase de test qui a permis de produire cette version 0.0.3.

    - La qualité de sa base de données HsqlDB.

- Fonctionne désormais avec OpenOffice sous Windows.

- Un protocole non pris en charge affiche désormais une erreur précise.

- Une url non analysable affiche désormais une erreur précise.

- Gère désormais correctement les espaces dans les noms de fichiers et les chemins.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.4:

- Réécriture de [Driver][39] en Java version 11 OpenJDK amd64 sous Eclipse IDE for Java Developers version 4.23.0 avec les plugins:
    - LOEclipse ou LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev ou Python IDE for Eclipse version 9.3.0.

- Ecriture des services `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` de JDBC (merci à hanya pour [MRI][40] qui m'a été d'une aide précieuse...)

    - [com.sun.star.sdb.*][41]
    - [com.sun.star.sdbc.*][42]
    - [com.sun.star.sdbcx.*][43]

- Intégration dans jdbcDriverOOo des pilotes JDBC **H2** et **Derby** en plus de **HsqlDB**. Implémentation de Services Java:

    - [Driver-HsqlDB.jar][44]
    - [Driver-H2.jar][45]
    - [Driver-Derby.jar][46]

    Afin de corriger d'éventuels défauts, ou incompatibilité avec l'API UNO, des pilotes JDBC embarqués. 

- Renommage du dépot et de l'extension **HsqlDBDriverOOo** en **jdbcDriverOOo**.

- Prise en charge dans Base des **clés primaires auto incrémentées** pour HsqlDB, H2 et Derby.

- Ecriture de [com.sun.star.sdbcx.Driver][47]. Ce pilote de haut niveau doit permettre la **gestion des utilisateurs, des rôles et des privilèges dans Base**. Son utilisation peut être désactivée via le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**.

- Implémentation d'un fournisseur de services Java [UnoLogger.jar][48] pour l'API [SLF4J][49] afin de pouvoir rediriger la journalisation des pilotes des bases de données sous-jacentes vers l'API UNO [com.sun.star.logging.*][50].

- Réécriture, en suivant le modèle MVC, de la fenêtre des [Options][51], accessible par le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**, pour permettre:

    - La mise à jour et/ou l'ajout d'archives Java de pilotes JDBC.
    - L'activation de la journalisation du pilote de la base de la données sous-jacente.

- Ecriture, en suivant le modèle MVC, des [fenêtres d'administration][52] des utilisateurs et des rôles (groupes) et de leurs privilèges associés, accessible dans Base par le menu: **Administration -> Gestion des utilisateurs** et/ou **Administration -> Gestion des groupes**, permettant:

    - La [gestion des utilisateurs][53] et de leurs privilèges.
    - La [gestion des rôles][54] (groupes) et de leurs privilèges.

    Ces nouvelles fonctionnalités n'ont étés testées pour l'instant qu'avec le pilote HsqlDB.

- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.0:

- Integration de HyperSQL version 2.7.2.

### Ce qui a été fait pour la version 1.0.1:

- Integration de [SQLite JDBC][13] version 3.42.0.0. Je tiens tout particulièrement à remercier [gotson][55] pour les [nombreuses améliorations apportées au pilote SQLite JDBC][56] qui ont rendu possible l'utilisation de SQLite dans LibreOffice/OpenOffice.

- Ce pilote peut être enveloppé par un autre pilote ([HsqlDriverOOo][23] ou [SQLiteOOo][57]) grâce à une url de connexion désormais modifiable.

- Il est possible d'afficher ou non les tables système dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Afficher les tables système**

- Il est possible d'interdire l'utilisation de jeux de résultats actualisables dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Utiliser les bookmarks**

- De nombreuses corrections ont été apportées afin de rendre l'extension [SQLiteOOo][57] fonctionnelle.

### Ce qui a été fait pour la version 1.0.2:

- Integration de [MariaDB Connector/J][14] version 3.1.4.

- Beaucoup d'autres correctifs...

### Que reste-t-il à faire pour la version 1.0.2:

- Ajouter de nouvelles langues pour l'internationalisation...

- Tout ce qui est bienvenu...

[1]: <img/jdbcDriverOOo.svg>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_fr>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-101>
[5]: <https://prrvchr.github.io/README_fr>
[6]: <https://fr.libreoffice.org/download/telecharger-libreoffice/>
[7]: <https://www.openoffice.org/fr/Telecharger/>
[8]: <https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/package-summary.html>
[9]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[10]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[11]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[12]: <http://hsqldb.org/>
[13]: <https://github.com/xerial/sqlite-jdbc>
[14]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[15]: <https://www.h2database.com/html/main.html>
[16]: <https://db.apache.org/derby/>
[17]: <https://github.com/CptTZ/SmallSQL>
[18]: <https://github.com/prrvchr/jdbcDriverOOo/>
[19]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[20]: <https://wiki.documentfoundation.org/Documentation/HowTo/Install_the_correct_JRE_-_LibreOffice_on_Windows_10/fr>
[21]: <https://adoptium.net/releases.html?variant=openjdk11>
[22]: <https://bugs.documentfoundation.org/show_bug.cgi?id=139538>
[23]: <https://prrvchr.github.io/HsqlDriverOOo/README_fr>
[24]: <https://github.com/prrvchr/jdbcDriverOOo/raw/master/jdbcDriverOOo.oxt>
[25]: <img/jdbcDriverOOo-1_fr.png>
[26]: <img/jdbcDriverOOo-2_fr.png>
[27]: <img/jdbcDriverOOo-3_fr.png>
[28]: <img/jdbcDriverOOo-4_fr.png>
[29]: <img/jdbcDriverOOo-5_fr.png>
[30]: <img/jdbcDriverOOo-6_fr.png>
[31]: <img/jdbcDriverOOo-7_fr.png>
[32]: <img/jdbcDriverOOo-8_fr.png>
[33]: <img/jdbcDriverOOo-9_fr.png>
[34]: <img/jdbcDriverOOo-10_fr.png>
[35]: <https://github.com/prrvchr/jdbcDriverOOo/issues/new>
[36]: <https://bugs.documentfoundation.org/show_bug.cgi?id=132195>
[37]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[38]: <http://hsqldb.org/>
[39]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[40]: <https://github.com/hanya/MRI>
[41]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[42]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[43]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[44]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[45]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[46]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[47]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[49]: <https://www.slf4j.org/>
[50]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[52]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[53]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[54]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[55]: <https://github.com/gotson>
[56]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[57]: <https://prrvchr.github.io/SQLiteOOo/README_fr>
