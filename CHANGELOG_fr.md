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
# [![jdbcDriverOOo logo][1]][2] Historique

**This [document][3] in English.**

**L'utilisation de ce logiciel vous soumet à nos [Conditions d'utilisation][4]**

## Ce qui a été fait pour la version 0.0.1:

- La rédaction de ce pilote a été facilitée par une [discussion avec Villeroy][53], sur le forum OpenOffice, que je tiens à remercier, car la connaissance ne vaut que si elle est partagée...

- Utilisation de la nouvelle version de HsqlDB 2.5.1.

- Beaucoup d'autres correctifs...

## Ce qui a été fait pour la version 0.0.2:

- Ajout d'une boîte de dialogue permettant de mettre à jour le pilote (hsqldb.jar) dans: Outils -> Options -> Pilotes Base -> Pilote HsqlDB

- Beaucoup d'autres correctifs...

## Ce qui a été fait pour la version 0.0.3:

- Je tiens particulièrement à remercier fredt à [hsqldb.org][54] pour:

    - Son accueil pour ce projet et sa permission d'utiliser le logo HsqlDB dans l'extension.

    - Son implication dans la phase de test qui a permis de produire cette version 0.0.3.

    - La qualité de sa base de données HsqlDB.

- Fonctionne désormais avec OpenOffice sous Windows.

- Un protocole non pris en charge affiche désormais une erreur précise.

- Une url non analysable affiche désormais une erreur précise.

- Gère désormais correctement les espaces dans les noms de fichiers et les chemins.

- Beaucoup d'autres correctifs...

## Ce qui a été fait pour la version 0.0.4:

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

- Ecriture de [com.sun.star.sdbcx.Driver][63]. Ce pilote de haut niveau doit permettre la **gestion des utilisateurs, des rôles et des privilèges dans Base**. Son utilisation peut être désactivée via le menu: **Outils -> Options -> LibreOffice Base -> Pilote JDBC pur Java**.

- Implémentation d'un fournisseur de services Java [UnoLogger.jar][64] pour l'API [SLF4J][65] afin de pouvoir rediriger la journalisation des pilotes des bases de données sous-jacentes vers l'API UNO [com.sun.star.logging.*][66].

- Réécriture, en suivant le modèle MVC, de la fenêtre des [Options][67], accessible par le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**, pour permettre:

    - La mise à jour et/ou l'ajout d'archives Java de pilotes JDBC.
    - L'activation de la journalisation du pilote de la base de la données sous-jacente.

- Ecriture, en suivant le modèle MVC, des [fenêtres d'administration][68] des utilisateurs et des rôles (groupes) et de leurs privilèges associés, accessible dans Base par le menu: **Administration -> Gestion des utilisateurs** et/ou **Administration -> Gestion des groupes**, permettant:

    - La [gestion des utilisateurs][69] et de leurs privilèges.
    - La [gestion des rôles][70] (groupes) et de leurs privilèges.

    Ces nouvelles fonctionnalités n'ont étés testées pour l'instant qu'avec le pilote HsqlDB.

- Beaucoup d'autres correctifs...

## Ce qui a été fait pour la version 1.0.0:

- Intégration de HyperSQL version 2.7.2.

## Ce qui a été fait pour la version 1.0.1:

- Intégration de [SQLite JDBC][14] version 3.42.0.0. Je tiens tout particulièrement à remercier [gotson][71] pour les [nombreuses améliorations apportées au pilote SQLite JDBC][72] qui ont rendu possible l'utilisation de SQLite dans LibreOffice/OpenOffice.

- Ce pilote peut être enveloppé par un autre pilote ([HyperSQLOOo][26] ou [SQLiteOOo][73]) grâce à une url de connexion désormais modifiable.

- Il est possible d'afficher ou non les tables système dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Afficher les tables système**

- Il est possible d'interdire l'utilisation de jeux de résultats actualisables dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Utiliser les bookmarks**

- De nombreuses corrections ont été apportées afin de rendre l'extension [SQLiteOOo][73] fonctionnelle.

## Ce qui a été fait pour la version 1.0.2:

- Intégration de [MariaDB Connector/J][15] version 3.1.4.

- Beaucoup d'autres correctifs...

## Ce qui a été fait pour la version 1.0.3:

- Intégration de [H2][17] version 2.2.220.

- Intégration de la journalisation dans les jeux de résultat ([ResultSetBase][74] and [ResultSetSuper][75]) afin d'en savoir plus sur le [dysfonctionnement 156512][76].

- Beaucoup d'autres correctifs...

## Ce qui a été fait pour la version 1.0.4:

- Support dans la création de tables du paramètre [TypeInfoSettings][77] permettant de récupérer la précision pour les types SQL:

    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE

    Ceci n'est [integré][78] que pour le pilote [HsqlDB][79] pour le moment.

## Ce qui a été fait pour la version 1.0.5:

- Le résultat de l'accès à la méthode [XDatabaseMetaData.getDriverVersion()][80] est désormais enregistré dans le fichier journal.

## Ce qui a été fait pour la version 1.0.6:

- Ajout du paquet Python `packaging` dans le `pythonpath` de l'extension. Merci à [artem78][81] d'avoir permis cette correction en signalant cet oubli dans le [dysfonctionnement #4][82].

## Ce qui a été fait pour la version 1.0.7:

- Désormais, le pilote lève une exception si la création d'une nouvelle table échoue. Ceci est pour répondre au [dysfonctionnement #1][83] sur l'extension [HyperSQLOOo][26].

## Ce qui a été fait pour la version 1.0.8:

- Utilisation de la dernière version de l'API de journalisation.

## Ce qui a été fait pour la version 1.1.0:

- Tous les paquets Python nécessaires à l'extension sont désormais enregistrés dans un fichier [requirements.txt][84] suivant la [PEP 508][85].
- Désormais si vous n'êtes pas sous Windows alors les paquets Python nécessaires à l'extension peuvent être facilement installés avec la commande:  
  `pip install requirements.txt`
- Modification de la section [Prérequis][86].

## Ce qui a été fait pour la version 1.1.1:

- Le pilote n'utilise plus de jeux de résultats (ResultSet) pouvant être mis en signet (Bookmarkable) pour des raisons de performances dans LibreOffice Base. Ceci peut être changé dans les options d'extension.

## Ce qui a été fait pour la version 1.1.2:

- Implementation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][87]. Cette interface permet, lors d'une insertion de plusieurs lignes (ie: `INSERT INTO matable (Colonne1, Colonne2) VALUES (valeur1, valeur2), (valeur1, valeur2), ...`) dans une table disposant d'une clé primaire auto-incrémentée, de récupérer un ensemble de résultats à partir des lignes insérées dans la table et vous donne donc accès aux clés générées automatiquement en un seul coup.
- Implémentation de l'interface UNO [com.sun.star.sdbcx.XAlterTable][88]. Cette interface permet la modification des colonnes d'une table. Avec HsqlDB, il est maintenant possible dans Base:
  - D'attribuez une description aux colonnes des tables.
  - De modifier le type d'une colonne si le transtypage (CAST) des données contenues dans cette colonne le permet, sinon il vous sera proposé de remplacer cette colonne ce qui entraîne la suppression des données...
- Toutes les commandes DDL (ie: `CREATE TABLE...`, `ALTER TABLE...`) générées par jdbcDriverOOo sont désormais enregistrées dans la journalisation.
- Pilote SQLite mis à jour vers la dernière version 3.45.1.0.
- Beaucoup d'autres correctifs...

## Ce qui a été fait pour la version 1.1.3:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][89]. Ce nouveau pilote a été implémenté pour supporter une partie des spécifications JDBC 4.1 et plus particulièrement l'interface `java.sql.Statement.getGeneratedKeys()` et permet l'utilisation de l'interface [com.sun.star.sdbc.XGeneratedResultSet][87].

## Ce qui a été fait pour la version 1.1.4:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][90].
- Intégration du driver [PostgreSQL pgJDBC][16] version 42.7.1 dans l'archive de jdbcDriverOOo. Cette intégration a été réalisée sans utiliser de service Java spécifique à PostgreSQL mais uniquement en configurant le fichier [Drivers.xcu][91] permettant de déclarer le pilote JDBC à LibreOffice.
- Ouverture d'un [dysfonctionnement][92] pour le pilote [MariaDB Connector/J][15] afin qu'il prenne en charge `java.sql.Statement.getGeneratedKeys()` comme demandé par JDBC 4.1.
- Normalement les prochaines versions de jdbcDriverOOo devraient pouvoir être mises à jour dans la liste des extensions installées sous LibreOffice: **Outils -> Gestionnaire des extensions... -> Vérifier les mises à jour**.
- Désormais, seul le pilote HsqlDB a accès dans Base à l'administration des droits des utilisateurs et des groupes. Ceci est déterminé par le paramètre `IgnoreDriverPrivileges` dans le fichier [Drivers.xcu][91].
- De nombreuses améliorations.

## Ce qui a été fait pour la version 1.1.5:

- Vous pouvez désormais éditer une vue en mode SQL avec le pilote SQLite. Pour les pilotes qui ne prennent pas en charge la modification des vues, les vues sont supprimées puis recréées.

## Ce qui a été fait pour la version 1.1.6:

- Vous pouvez désormais renommer les tables et les vues dans Base. Toute la configuration nécessaire au renommage pour chaque pilote JDBC intégré est stockée uniquement dans le fichier [Drivers.xcu][81].
- Tous les pilotes JDBC intégrés à jdbcDriverOOo sont capables de renommer des tables ou des vues et même certains (ie: MariaDB et PostgreSQL) permettent de modifier le catalogue ou le schéma.
- De nombreuses améliorations.

## Ce qui a été fait pour la version 1.2.0:

- Tous les pilotes intégrés à l'extension sont **désormais entièrement fonctionnels dans Base** pour la gestion des tables et des vues.
- Des fonctions intelligentes sont appelées pour:
  - Le déplacement avec renommage des tables, pour les drivers le permettant et utilisant deux commandes SQL, l'ordre des commandes SQL sera optimisé (PostgreSQL).
  - Le Renommage d'une vue, si le pilote ne le supporte pas, elle sera supprimée puis recréée (SQLite).
- Utilisation de [classe générique Java][93] pour la gestion des conteneurs utilisés pour la gestion des [tables][94], des [vues][95], des [colonnes][96], des [clefs][97] et des [indexes][98]. L'utilisation de classes génériques dans les [conteneurs][99] permettra de se passer de l'interface UNO XPropertySet et de pouvoir retranscrire le code existant en Java pur.
- De nombreuses améliorations.

## Ce qui a été fait pour la version 1.2.1:

- Résolution d'une régression interdisant la suppression de colonnes dans une table.
- Mise à jour du pilote mariadb-java-client-3.3.3.jar.
- Généralisation des classes Java génériques pour toutes les classes devant être partagées au niveau de l'API UNO (ie: sdb, sdbc et sdbcx).
- On peut désormais renommer les colonnes des tables sous SQLite et MariaDB.
- Il est également possible de renommer les colonnes déclarées comme clé primaire dans tous les pilotes embarqués.
- De nombreuses améliorations.

## Ce qui a été fait pour la version 1.2.2:

- Mise en place de la gestion des index.
- Renommer une colonne déclarée comme clé primaire renommera également l'index associé à la clé primaire.
- Seuls les membres des classes Java répondant à l'API UNO ont un niveau de visibilité public, tous les autres membres ont une visibilité protégée ou privée.
- Résolution de nombreux problèmes et régressions.

## Ce qui a été fait pour la version 1.2.3:

- Renommer une colonne déclarée comme index renommera également la colonne de l'index associée.

## Ce qui a été fait pour la version 1.2.4:

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

## Ce qui a été fait pour la version 1.3.0:

- Intégration de la gestion des clés étrangères dans Base (**Outils -> Relations...**).
  - Lorsque vous renommez une table, cela renommera également le référencement de cette table dans les éventuelles clés étrangères pointant vers cette table.
  - Lorsque vous renommez une colonne, cela renommera également le référencement de cette colonne dans les éventuelles clés étrangères pointant vers cette colonne.
  - Ces mises à jour de clés étrangères prennent en compte le chargement paresseux des conteneurs des tables et des clés et ne seront effectuées que si Base a déjà accédé aux données impliquées.
  - Un problème persiste lors de la création de clés étrangères entre des tables qui n'ont pas le même catalogue et/ou schéma, voir [dysfonctionnement #160375][103]. Ce problème semble être lié à Base, j'espère qu'il sera résolu rapidement.
- Meilleure gestion des exceptions avec la possibilité de connaître l'état, le code SQL et le message de l'exception qui a été générée par le pilote sous-jacent.
- De nombreuses corrections et améliorations.

Normalement, je suis arrivé à couvrir toute l'étendue de l'API UNO ([com.sun.star.sdbc][10], [sdbcx][11] et [sdb][12]), ce qui à pris pas mal de temps, mais je ne pensais pas au départ y arriver.

## Ce qui a été fait pour la version 1.3.1:

- Correction de l'implémentation de l'interface [XRowLocate][104] responsable de la gestion des signets (Bookmark) dans les jeux de résultats (ResultSet). Cette nouvelle implémentation fonctionne avec tous les pilotes à l'exception de SQLite qui ne prend pas en charge la mise à jour des jeux de résultats. La présence de cette interface dans les jeux de résultats permet à Base d'éditer des tables même en l'absence de clé primaire. Avec certains pilotes (HsqlDB, H2 et Derby) le rafraîchissement en cours de saisie ne sera pas automatique et devra être fait manuellement. L'utilisation des signets peut être désactivée dans les options de l'extension.
- Mise en place de [jeu de resultats simulé][105] (java.sql.ResultSet) permettant de produire des ResultSet à partir des données de connexion fourni par le pilote, plus exactement à partir du fichier [Drivers.xcu][91]. L'utilisation de ces resultset simulé permet de fournir à Base des resultset conforme à ce qu'il attend même si le pilote sous jancent n'est pas capable de les produire. Ils servent à patcher les résultats obtenus les méthodes `getTypeInfo()`, `getTableTypes` et `getTablePrivileges()` de l'interface java.sql.DatabaseMetaData en utilisant respectivement les propriétés `TypeInfoSettings`, `TableTypesSettings` et `TablePrivilegesSettings` du fichier [Drivers.xcu][91].
- Ecriture d'un [conteneur spécifique][106] pour gérer les utilisateurs d'un rôle ou les rôles d'un rôle. Ce conteneur n'est qu'un pointeur vers les elements des conteneurs utilisateur et/ou role de la base de données. Lors de la suppression d'un utilisateur ou d'un rôle, ce conteneur sera mis à jour si nécessaire.
- Réécriture des fenêtres **Administration des utilisateurs** et **Administration des groupes** accessibles dans le menu **Administration** de Base. Désormais, si la propriété `TablePrivilegesSettings` est fournie par le pilote sous-jacent, seuls les privilèges déclarés dans cette propriété seront affichés. Cela permet une utilisation plus facile. Une [demande d'amélioration #160516][107] a été faite afin d'intégrer cette fonctionalité dans le code de Base.
- Intégration de tous les pilotes embarqués dans l'extension (hors SQLite) dans la gestion des utilisateurs, des rôles et des privilèges sur les tables et les vues. Je suppose que de nombreux dysfonctionnements restent à corriger, merci de me le faire savoir, détecter les dysfonctionnements me prend plus de temps que les corriger....
- De nombreuses corrections et améliorations...

## Ce qui a été fait pour la version 1.3.2:

Désormais l'API UNO SDBCX peut être utilisée pour la création de bases de données, comme c'est le cas pour les dernières versions des extensions utilisant jdbcDriverOOo. Il est possible de créer des tables, grâce à l'API UNO, avec les caractéristiques suivantes:
- Déclaration de colonnes de types TIMESTAMP WITH TIME ZONE, TIMESTAMP, TIME WITH TIME ZONE, TIME avec gestion de la précision (ie: de 0 à 9).
- Déclaration de tables [temporelles versionnées par le système][108] (temporal system versioned tables). Ces types de tables sont utilisés dans les mêmes extensions pour faciliter la réplication des données.
- Déclaration de tables au [format texte][109]. Ces tables vous permettent d'utiliser les données de fichiers au format csv.
- Déclaration des clés primaires, clés étrangères, index, utilisateurs, rôles et privilèges associés.

L'utilisation de l'API UNO pour créer des bases de données vous permettra d'utiliser du code indépendant de la base de données sous-jacente.

Les clients utilisant le pilote jdbcDriverOOo peuvent accéder aux fonctionnalités du pilote JDBC sous-jacent à l'aide de la méthode [XDriver.getPropertyInfo()][110] afin d'accéder au paramètre nécessaire lors de la création de tables et d'afficher correctement les privilèges. Ces paramètres étant accessibles directement par le pilote, peuvent être obtenus avant toute connexion, et permettent donc la création de la base de données lors de la première connexion.

## Ce qui a été fait pour la version 1.3.3:

- [Modification de la gestion][111] du paramètre de connexion `JavaDriverClassPath`. Ce paramètre peut désormais désigner un répertoire et dans ce cas tous les fichiers jar contenus seront ajoutés au `Java ClassPath`. Cela permet le chargement dynamique des pilotes JDBC nécessitant plusieurs archives (ie: Derby et Jaybird embedded). Cette modification a été apportée pour permettre à la nouvelle extension [JaybirdOOo][112] de fonctionner.
- Reprise d'une partie de l'implémentation de `javax.sql.rowset.CachedRowSet` dans les jeux de résultats [ScrollableResultSet.java][113] et [SensitiveResultSet.java][114] afin de simuler le type `TYPE_SCROLL_SENSITIVE` à partir des jeux de résultats de type `TYPE_FORWARD_ONLY` et `TYPE_SCROLL_INSENSITIVE` respectivement. Cela permet à LibreOffice Base d'utiliser des signets (ie : l'interface UNO [XRowLocate][104]) qui permettent des insertions, mises à jour et suppressions positionnées et donc, pour les bases de données le supportant, la possibilité d'éditer des tables ne contenant aucune clé primaire. De plus, un [mode SQL][115] **permet de rendre éditable n'importe quel ResultSet.** Ce mode peut être validée dans les options de l'extension, elle est trés puissante et donc à utiliser avec prudence. Concernant les jeux de résultats de type `TYPE_FORWARD_ONLY`, leur implémentation chargeant progressivement l'intégralité des données du jeu de résultats en mémoire peut conduire à un débordement de mémoire. La mise en oeuvre d'une pagination éliminera ce risque.
- Ajout du pilote MySQL Connector/J version 8.4.0. Ce driver ne semble pas fonctionner correctement, des erreurs assez surprenantes apparaissent... Je le laisse en place au cas où des gens seraient prêts à participer à son intégration? A utiliser avec précaution.
- Suite à la demande de [PeterSchmidt23][116] ajout du pilote [Trino][117] version 448. Ne connaissant pas Trino, qui a l'air étonnant par ailleur, seulement un début d'intégration a été réalisée. L'edition du contenu des tables n'est pas encore possible, voir [dysfonctionnement #22306][118]. Le nom des tables doit être en minuscule afin d'autoriser leur création.
- L'implémentation de `CachedRowSet` semble avoir résolu le problème d'insertion de cellules depuis Calc, voir [dysfonctionnement #7][119].
- De nombreuses corrections et améliorations...

## Ce qui a été fait pour la version 1.4.0:

- Mise à jour du pilote Jaybird vers la version finale 5.0.5.
- Modification de l'implémentation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][87]. Cette nouvelle implémentation prend en charge les pilotes qui ne suivent pas l'API JDBC mais proposent une implémentation spécifique (ie: MariaDB et Derby). Pour être activé lors de l'utilisation de fichiers odb créés avec une version précédente, s'il est présent, il est nécessaire de modifier le paramètre : `Requête des valeurs générées` accessible par le menu : **Edition -> Base de données -> Paramètres avancés... -> Valeurs générées** par la valeur : `SELECT * FROM %s WHERE %s`.
- Ajout de nouveaux paramètres pris en charge par le fichier de configuration [Drivers.xcu][91]. Ces nouveaux paramètres permettent de modifier les valeurs renvoyées par les pilotes concernant la visibilité des modifications dans les jeux de résultats (ie: insertion, mise à jour et suppression). Ils permettent également de forcer le mode SQL pour les modifications souhaitées dans les jeux de résultats.
- Finalisation de l'implémentation de l'émulation rendant tout jeu de résultats modifiable, si l'enregistrement est unique dans ce jeu de résultats. Cette implémentation, utilisant les signets (ie: bookmark), permet l'édition de jeu de résultats provenant de **Requêtes Base**, cela rend tout simplement les **Requêtes LibreOffice Base éditables**. Les requêtes joignant plusieurs tables ne sont pas encore supportées et je suis ouvert à toute proposition technique concernant une éventuelle implémentation.
- Afin de rendre modifiables les jeux de résultats retournés par le driver **Trino** et de précéder la [demande d'amélioration #22408][120], une recherche de la clé primaire sera lancée afin de retrouver la première colonne, du jeu de résultats, ayant pas de doublons.
- Afin de contourner le [dysfonctionnement #368][121] le driver HsqlDB utilise des mises à jour en mode SQL dans les jeux de résultats.
- De nombreuses corrections et améliorations...

## Ce qui a été fait pour la version 1.4.1:

- Nouvelle implémentation, que j'espère définitive, des signets (bookmarks). Il est basé sur trois fichiers et est tiré de l'implémentation par Sun de `javax.sql.rowset.CachedRowSet` :
  - [ScollableResultSet.class][113]
  - [SensitiveResultSet.class][114]
  - [CachedResultSet.class][122]
- **Ces ResultSets sont capables d'éditer presque toutes les requêtes créées dans LibreOffice Base, même les vues...** Mais afin de garantir une bonne fonctionnalité, certaines règles doivent être respectées afin de rendre un jeu de résultats éditable. Si la requête concerne plusieurs tables alors il est impératif d'inclure les clés primaires de chaque table dans la liste des colonnes du jeu de résultats. Si la requête ne concerne qu'une seule table alors le jeu de résultats sera modifiable s'il existe une colonne qui ne contient pas de doublon (ie: une clé naturelle). Cela permet de rendre modifiables les jeux de résultats provenant du pilote Trino.
- Suppression de l'utilisation de classes génériques là où elles n'étaient pas nécessaires. Cela a rendu le pilote plus rapide...
- Ajout de paramètres spéciaux dans: **Edition -> Base de données -> Paramètres avancés... -> Paramètres spéciaux** afin de répondre à la demande d'intégration du pilote Trino (voir [demande d'amélioration n°8][123]). Il est nécessaire de recréer les fichiers odb afin d'avoir accès à ces nouveaux paramètres.

## Ce qui a été fait pour la version 1.4.2:

- Pilote JDBC Trino mis à jour vers la version 453.
- Mise à jour du paquet [Python packaging][124] vers la version 24.1.
- Mise à jour du paquet [Python setuptools][125] vers la version 72.1.0 afin de répondre à l'[alerte de sécurité Dependabot][126].

## Ce qui a été fait pour la version 1.4.3:

- Mise à jour du paquet [Python setuptools][125] vers la version 73.0.1.
- La journalisation accessible dans les options de l’extension s’affiche désormais correctement sous Windows.
- Les options de l'extension sont désormais accessibles via: **Outils -> Options... -> LibreOffice Base -> Pilote JDBC**
- Les modifications apportées aux options d'extension, qui nécessitent un redémarrage de LibreOffice, entraîneront l'affichage d'un message.
- Support de LibreOffice version 24.8.x.

## Ce qui a été fait pour la version 1.4.4:

- Il est désormais possible d'insérer des données dans une table vide lors de l'utilisation d'un ResultSet `TYPE_FORWARD_ONLY` (ie: SQLite, Trino).
- Le bouton options est désormais accessible dans la liste des extensions installées obtenue par le menu : **Outils -> Gestionnaire d'extensions...**
- Les options de l'extension sont désormais accessibles via: **Outils -> Options... -> LibreOffice Base -> Pilote JDBC pur Java**
- Les options de l'extension: **Voir les tables système**, **Utiliser les signets** et **Forcer le mode SQL** seront recherchées dans les informations fournies lors de la connexion et auront la priorité si elles sont présentes.
- Pilote Trino mis à jour vers la version 455.

## Ce qui a été fait pour la version 1.4.5:

- Correction pour permettre à l'extension eMailerOOo de fonctionner correctement dans la version 1.2.5.

## Ce qui a été fait pour la version 1.4.6:

- Modification de l'implémentation de l'interface UNO [XPropertySet][127]. Cette nouvelle implémentation assure l'unicité des [Handle][128] pour chaque propriété. Cette implémentation étant partagée avec l'extension vCardOOo, **elle rend toutes les versions existantes de vCardOOo obsolètes**. Elle est basée sur trois fichiers:
  - [PropertySet.java][129]
  - [PropertySetAdapter.java][130]
  - [PropertyWrapper.java][131]
- Correction de problèmes dans l'implémentation des signets (bookmark). Ces modifications ont été testées plus particulièrement avec les pilotes HsqlDB 2.7.4 et Jaybird 5.0.6.
- Nouvelle implémentation des options de l'extension et plus particulièrement de l'onglet **Options du pilote JDBC** ce qui devrait permettre à terme la configuration à partir de zéro d'un pilote JDBC pour pouvoir fonctionner avec LibreOffice Base. L’opération de mise à jour de l’archive du pilote JDBC a été simplifiée. Elle prend en charge la mise à jour des pilotes qui nécessitent plusieurs archives jar pour fonctionner (ie: Derby, Jaybird 6.x). Cette nouvelle fenêtre qui semble assez simple, nécessite en réalité une gestion assez compliquée, n'hésitez donc pas à me signaler d'éventuels dysfonctionnements.
- De nombreuses autres améliorations.

## Ce qui a été fait pour la version 1.4.7:

- Déploiement de la registration passive permettant une installation de l'extension beaucoup plus rapide ainsi que la posibilité de differencier les services UNO enregistrés des services UNO fournis par une implementation Java ou Python. Cet enregistrement passif est fourni par l'extension LOEclipse via les [PR#152][132] et [PR#157][133].

### Que reste-t-il à faire pour la version 1.4.7:

- Ajouter de nouvelles langues pour l'internationalisation...

- Tout ce qui est bienvenu...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/source/jdbcDriverOOo/registration/TermsOfUse_fr>
[5]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#ce-qui-a-%C3%A9t%C3%A9-fait-pour-la-version-146>
[6]: <https://prrvchr.github.io/README_fr>
[7]: <https://fr.libreoffice.org/download/telecharger-libreoffice/>
[8]: <https://www.openoffice.org/fr/Telecharger/>
[9]: <https://devdocs.io/openjdk~17/java.sql/java/sql/package-summary>
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
[40]: <https://img.shields.io/github/downloads/prrvchr/jdbcDriverOOo/latest/total?label=v1.4.6#right>
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
[127]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/XPropertySet.html>
[128]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/Property.html#Handle>
[129]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/java/helper/PropertySet.java>
[130]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/java/helper/PropertySetAdapter.java>
[131]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/java/helper/PropertyWrapper.java>
[132]: <https://github.com/LibreOffice/loeclipse/pull/152>
[133]: <https://github.com/LibreOffice/loeclipse/pull/157>
