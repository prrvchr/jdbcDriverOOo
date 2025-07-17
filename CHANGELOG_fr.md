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

Concernant l'installation, la configuration et l'utilisation, veuillez consulter la **[documentation][4]**.

### Ce qui a été fait pour la version 0.0.1:

- La rédaction de ce pilote a été facilitée par une [discussion avec Villeroy][5], sur le forum OpenOffice, que je tiens à remercier, car la connaissance ne vaut que si elle est partagée...
- Utilisation de la nouvelle version de HsqlDB 2.5.1.
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.2:

- Ajout d'une boîte de dialogue permettant de mettre à jour le pilote (hsqldb.jar) dans: Outils -> Options -> Pilotes Base -> Pilote HsqlDB
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.3:

- Je tiens particulièrement à remercier fredt à [hsqldb.org][6] pour:
    - Son accueil pour ce projet et sa permission d'utiliser le logo HsqlDB dans l'extension.
    - Son implication dans la phase de test qui a permis de produire cette version 0.0.3.
    - La qualité de sa base de données HsqlDB.
- Fonctionne désormais avec OpenOffice sous Windows.
- Un protocole non pris en charge affiche désormais une erreur précise.
- Une url non analysable affiche désormais une erreur précise.
- Gère désormais correctement les espaces dans les noms de fichiers et les chemins.
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 0.0.4:

- Réécriture de [Driver][7] en Java version 11 OpenJDK amd64 sous Eclipse IDE for Java Developers version 4.23.0 avec les plugins:
    - LOEclipse ou LibreOffice Eclipse plugin for extension development version 4.0.1.
    - PyDev ou Python IDE for Eclipse version 9.3.0.
- Ecriture des services `Statement`, `PreparedStatement`, `CallableStatement`, `ResultSet`, `...` de JDBC (merci à hanya pour [MRI][8] qui m'a été d'une aide précieuse...)
    - [com.sun.star.sdb.*][9]
    - [com.sun.star.sdbc.*][10]
    - [com.sun.star.sdbcx.*][11]
- Intégration dans jdbcDriverOOo des pilotes JDBC **H2** et **Derby** en plus de **HsqlDB**. Implémentation de Services Java:
    - [Driver-HsqlDB.jar][12]
    - [Driver-H2.jar][13]
    - [Driver-Derby.jar][14]
    Afin de corriger d'éventuels défauts, ou incompatibilité avec l'API UNO, des pilotes JDBC embarqués. 
- Renommage du dépot et de l'extension **HsqlDBDriverOOo** en **jdbcDriverOOo**.
- Prise en charge dans Base des **clés primaires auto incrémentées** pour HsqlDB, H2 et Derby.
- Ecriture de [com.sun.star.sdbcx.Driver][15]. Ce pilote de haut niveau doit permettre la **gestion des utilisateurs, des rôles et des privilèges dans Base**. Son utilisation peut être désactivée via le menu: **Outils -> Options -> LibreOffice Base -> Pilote JDBC pur Java**.
- Implémentation d'un fournisseur de services Java [UnoLogger.jar][16] pour l'API [SLF4J][17] afin de pouvoir rediriger la journalisation des pilotes des bases de données sous-jacentes vers l'API UNO [com.sun.star.logging.*][18].
- Réécriture, en suivant le modèle MVC, de la fenêtre des [Options][19], accessible par le menu: **Outils -> Options -> Pilotes Base -> Pilote JDBC**, pour permettre:
    - La mise à jour et/ou l'ajout d'archives Java de pilotes JDBC.
    - L'activation de la journalisation du pilote de la base de la données sous-jacente.
- Ecriture, en suivant le modèle MVC, des [fenêtres d'administration][20] des utilisateurs et des rôles (groupes) et de leurs privilèges associés, accessible dans Base par le menu: **Administration -> Gestion des utilisateurs** et/ou **Administration -> Gestion des groupes**, permettant:
    - La [gestion des utilisateurs][21] et de leurs privilèges.
    - La [gestion des rôles][22] (groupes) et de leurs privilèges.
    Ces nouvelles fonctionnalités n'ont étés testées pour l'instant qu'avec le pilote HsqlDB.
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.0:

- Intégration de HyperSQL version 2.7.2.

### Ce qui a été fait pour la version 1.0.1:

- Intégration de [SQLite JDBC][23] version 3.42.0.0. Je tiens tout particulièrement à remercier [gotson][24] pour les [nombreuses améliorations apportées au pilote SQLite JDBC][25] qui ont rendu possible l'utilisation de SQLite dans LibreOffice/OpenOffice.
- Ce pilote peut être enveloppé par un autre pilote ([HyperSQLOOo][26] ou [SQLiteOOo][27]) grâce à une url de connexion désormais modifiable.
- Il est possible d'afficher ou non les tables système dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Afficher les tables système**
- Il est possible d'interdire l'utilisation de jeux de résultats actualisables dans: **Outils -> Options -> Pilotes Base -> Pilote JDBC -> Options du pilote UNO -> Utiliser les bookmarks**
- De nombreuses corrections ont été apportées afin de rendre l'extension [SQLiteOOo][27] fonctionnelle.

### Ce qui a été fait pour la version 1.0.2:

- Intégration de [MariaDB Connector/J][28] version 3.1.4.
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.3:

- Intégration de [H2][29] version 2.2.220.
- Intégration de la journalisation dans les jeux de résultat ([ResultSetBase][30] and [ResultSetSuper][31]) afin d'en savoir plus sur le [dysfonctionnement 156512][32].
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.0.4:

- Support dans la création de tables du paramètre [TypeInfoSettings][33] permettant de récupérer la précision pour les types SQL:
    - TIME
    - TIMESTAMP
    - TIME WITH TIME ZONE
    - TIMESTAMP WITH TIME ZONE
    Ceci n'est [integré][34] que pour le pilote [HsqlDB][35] pour le moment.

### Ce qui a été fait pour la version 1.0.5:

- Le résultat de l'accès à la méthode [XDatabaseMetaData.getDriverVersion()][36] est désormais enregistré dans le fichier journal.

### Ce qui a été fait pour la version 1.0.6:

- Ajout du paquet Python `packaging` dans le `pythonpath` de l'extension. Merci à [artem78][37] d'avoir permis cette correction en signalant cet oubli dans le [dysfonctionnement #4][38].

### Ce qui a été fait pour la version 1.0.7:

- Désormais, le pilote lève une exception si la création d'une nouvelle table échoue. Ceci est pour répondre au [dysfonctionnement #1][39] sur l'extension [HyperSQLOOo][26].

### Ce qui a été fait pour la version 1.0.8:

- Utilisation de la dernière version de l'API de journalisation.

### Ce qui a été fait pour la version 1.1.0:

- Tous les paquets Python nécessaires à l'extension sont désormais enregistrés dans un fichier [requirements.txt][40] suivant la [PEP 508][41].
- Désormais si vous n'êtes pas sous Windows alors les paquets Python nécessaires à l'extension peuvent être facilement installés avec la commande:  
  `pip install requirements.txt`
- Modification de la section [Prérequis][42].

### Ce qui a été fait pour la version 1.1.1:

- Le pilote n'utilise plus de jeux de résultats (ResultSet) pouvant être mis en signet (Bookmarkable) pour des raisons de performances dans LibreOffice Base. Ceci peut être changé dans les options d'extension.

### Ce qui a été fait pour la version 1.1.2:

- Implementation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][43]. Cette interface permet, lors d'une insertion de plusieurs lignes (ie: `INSERT INTO matable (Colonne1, Colonne2) VALUES (valeur1, valeur2), (valeur1, valeur2), ...`) dans une table disposant d'une clé primaire auto-incrémentée, de récupérer un ensemble de résultats à partir des lignes insérées dans la table et vous donne donc accès aux clés générées automatiquement en un seul coup.
- Implémentation de l'interface UNO [com.sun.star.sdbcx.XAlterTable][44]. Cette interface permet la modification des colonnes d'une table. Avec HsqlDB, il est maintenant possible dans Base:
  - D'attribuez une description aux colonnes des tables.
  - De modifier le type d'une colonne si le transtypage (CAST) des données contenues dans cette colonne le permet, sinon il vous sera proposé de remplacer cette colonne ce qui entraîne la suppression des données...
- Toutes les commandes DDL (ie: `CREATE TABLE...`, `ALTER TABLE...`) générées par jdbcDriverOOo sont désormais enregistrées dans la journalisation.
- Pilote SQLite mis à jour vers la dernière version 3.45.1.0.
- Beaucoup d'autres correctifs...

### Ce qui a été fait pour la version 1.1.3:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.3-SNAPSHOT.jar][45]. Ce nouveau pilote a été implémenté pour supporter une partie des spécifications JDBC 4.1 et plus particulièrement l'interface `java.sql.Statement.getGeneratedKeys()` et permet l'utilisation de l'interface [com.sun.star.sdbc.XGeneratedResultSet][43].

### Ce qui a été fait pour la version 1.1.4:

- Pilote SQLite mis à jour vers la dernière version [SQLite-jdbc-3.45.1.6-SNAPSHOT.jar][46].
- Intégration du driver [PostgreSQL pgJDBC][47] version 42.7.1 dans l'archive de jdbcDriverOOo. Cette intégration a été réalisée sans utiliser de service Java spécifique à PostgreSQL mais uniquement en configurant le fichier [Drivers.xcu][48] permettant de déclarer le pilote JDBC à LibreOffice.
- Ouverture d'un [dysfonctionnement][49] pour le pilote [MariaDB Connector/J][28] afin qu'il prenne en charge `java.sql.Statement.getGeneratedKeys()` comme demandé par JDBC 4.1.
- Normalement les prochaines versions de jdbcDriverOOo devraient pouvoir être mises à jour dans la liste des extensions installées sous LibreOffice: **Outils -> Gestionnaire des extensions... -> Vérifier les mises à jour**.
- Désormais, seul le pilote HsqlDB a accès dans Base à l'administration des droits des utilisateurs et des groupes. Ceci est déterminé par le paramètre `IgnoreDriverPrivileges` dans le fichier [Drivers.xcu][48].
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.1.5:

- Vous pouvez désormais éditer une vue en mode SQL avec le pilote SQLite. Pour les pilotes qui ne prennent pas en charge la modification des vues, les vues sont supprimées puis recréées.

### Ce qui a été fait pour la version 1.1.6:

- Vous pouvez désormais renommer les tables et les vues dans Base. Toute la configuration nécessaire au renommage pour chaque pilote JDBC intégré est stockée uniquement dans le fichier [Drivers.xcu][48].
- Tous les pilotes JDBC intégrés à jdbcDriverOOo sont capables de renommer des tables ou des vues et même certains (ie: MariaDB et PostgreSQL) permettent de modifier le catalogue ou le schéma.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.2.0:

- Tous les pilotes intégrés à l'extension sont **désormais entièrement fonctionnels dans Base** pour la gestion des tables et des vues.
- Des fonctions intelligentes sont appelées pour:
  - Le déplacement avec renommage des tables, pour les drivers le permettant et utilisant deux commandes SQL, l'ordre des commandes SQL sera optimisé (PostgreSQL).
  - Le Renommage d'une vue, si le pilote ne le supporte pas, elle sera supprimée puis recréée (SQLite).
- Utilisation de [classe générique Java][50] pour la gestion des conteneurs utilisés pour la gestion des [tables][51], des [vues][52], des [colonnes][53], des [clefs][54] et des [indexes][55]. L'utilisation de classes génériques dans les [conteneurs][56] permettra de se passer de l'interface UNO XPropertySet et de pouvoir retranscrire le code existant en Java pur.
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
- Lors de la [création d'une table][57] avec une clé primaire, si le pilote sous-jacent le prend en charge, la création de la clé primaire peut être effectuée par une commande DDL distincte. Cela permet à Jaybird de contourner le [dysfonctionnement #791][58] en créant une clé primaire nommée et permet de gérer des cas particuliers comme MariaDB ou SQLite pour leur gestion des auto-increments.
- Si le pilote sous-jacent le permet, lors de [la modification des colonnes][59] d'une table vous pouvez désormais:
  - La déclarer en auto-incrément (Identity) sans qu'elle soit nécessairement la clé primaire.
  - Ajouter ou retirer la contrainte Identity (auto-incrément).
  - Ajouter des commentaires.
- De nombreuses améliorations.

### Ce qui a été fait pour la version 1.3.0:

- Intégration de la gestion des clés étrangères dans Base (**Outils -> Relations...**).
  - Lorsque vous renommez une table, cela renommera également le référencement de cette table dans les éventuelles clés étrangères pointant vers cette table.
  - Lorsque vous renommez une colonne, cela renommera également le référencement de cette colonne dans les éventuelles clés étrangères pointant vers cette colonne.
  - Ces mises à jour de clés étrangères prennent en compte le chargement paresseux des conteneurs des tables et des clés et ne seront effectuées que si Base a déjà accédé aux données impliquées.
  - Un problème persiste lors de la création de clés étrangères entre des tables qui n'ont pas le même catalogue et/ou schéma, voir [dysfonctionnement #160375][60]. Ce problème semble être lié à Base, j'espère qu'il sera résolu rapidement.
- Meilleure gestion des exceptions avec la possibilité de connaître l'état, le code SQL et le message de l'exception qui a été générée par le pilote sous-jacent.
- De nombreuses corrections et améliorations.

Normalement, je suis arrivé à couvrir toute l'étendue de l'API UNO ([com.sun.star.sdbc][61], [sdbcx][62] et [sdb][63]), ce qui à pris pas mal de temps, mais je ne pensais pas au départ y arriver.

### Ce qui a été fait pour la version 1.3.1:

- Correction de l'implémentation de l'interface [XRowLocate][64] responsable de la gestion des signets (Bookmark) dans les jeux de résultats (ResultSet). Cette nouvelle implémentation fonctionne avec tous les pilotes à l'exception de SQLite qui ne prend pas en charge la mise à jour des jeux de résultats. La présence de cette interface dans les jeux de résultats permet à Base d'éditer des tables même en l'absence de clé primaire. Avec certains pilotes (HsqlDB, H2 et Derby) le rafraîchissement en cours de saisie ne sera pas automatique et devra être fait manuellement. L'utilisation des signets peut être désactivée dans les options de l'extension.
- Mise en place de [jeu de resultats simulé][65] (java.sql.ResultSet) permettant de produire des ResultSet à partir des données de connexion fourni par le pilote, plus exactement à partir du fichier [Drivers.xcu][48]. L'utilisation de ces resultset simulé permet de fournir à Base des resultset conforme à ce qu'il attend même si le pilote sous jancent n'est pas capable de les produire. Ils servent à patcher les résultats obtenus les méthodes `getTypeInfo()`, `getTableTypes` et `getTablePrivileges()` de l'interface java.sql.DatabaseMetaData en utilisant respectivement les propriétés `TypeInfoSettings`, `TableTypesSettings` et `TablePrivilegesSettings` du fichier [Drivers.xcu][48].
- Ecriture d'un [conteneur spécifique][66] pour gérer les utilisateurs d'un rôle ou les rôles d'un rôle. Ce conteneur n'est qu'un pointeur vers les elements des conteneurs utilisateur et/ou role de la base de données. Lors de la suppression d'un utilisateur ou d'un rôle, ce conteneur sera mis à jour si nécessaire.
- Réécriture des fenêtres **Administration des utilisateurs** et **Administration des groupes** accessibles dans le menu **Administration** de Base. Désormais, si la propriété `TablePrivilegesSettings` est fournie par le pilote sous-jacent, seuls les privilèges déclarés dans cette propriété seront affichés. Cela permet une utilisation plus facile. Une [demande d'amélioration #160516][67] a été faite afin d'intégrer cette fonctionalité dans le code de Base.
- Intégration de tous les pilotes embarqués dans l'extension (hors SQLite) dans la gestion des utilisateurs, des rôles et des privilèges sur les tables et les vues. Je suppose que de nombreux dysfonctionnements restent à corriger, merci de me le faire savoir, détecter les dysfonctionnements me prend plus de temps que les corriger....
- De nombreuses corrections et améliorations...

### Ce qui a été fait pour la version 1.3.2:

Désormais l'API UNO SDBCX peut être utilisée pour la création de bases de données, comme c'est le cas pour les dernières versions des extensions utilisant jdbcDriverOOo. Il est possible de créer des tables, grâce à l'API UNO, avec les caractéristiques suivantes:
- Déclaration de colonnes de types TIMESTAMP WITH TIME ZONE, TIMESTAMP, TIME WITH TIME ZONE, TIME avec gestion de la précision (ie: de 0 à 9).
- Déclaration de tables [temporelles versionnées par le système][68] (temporal system versioned tables). Ces types de tables sont utilisés dans les mêmes extensions pour faciliter la réplication des données.
- Déclaration de tables au [format texte][69]. Ces tables vous permettent d'utiliser les données de fichiers au format csv.
- Déclaration des clés primaires, clés étrangères, index, utilisateurs, rôles et privilèges associés.

L'utilisation de l'API UNO pour créer des bases de données vous permettra d'utiliser du code indépendant de la base de données sous-jacente.

Les clients utilisant le pilote jdbcDriverOOo peuvent accéder aux fonctionnalités du pilote JDBC sous-jacent à l'aide de la méthode [XDriver.getPropertyInfo()][70] afin d'accéder au paramètre nécessaire lors de la création de tables et d'afficher correctement les privilèges. Ces paramètres étant accessibles directement par le pilote, peuvent être obtenus avant toute connexion, et permettent donc la création de la base de données lors de la première connexion.

### Ce qui a été fait pour la version 1.3.3:

- [Modification de la gestion][71] du paramètre de connexion `JavaDriverClassPath`. Ce paramètre peut désormais désigner un répertoire et dans ce cas tous les fichiers jar contenus seront ajoutés au `Java ClassPath`. Cela permet le chargement dynamique des pilotes JDBC nécessitant plusieurs archives (ie: Derby et Jaybird embedded). Cette modification a été apportée pour permettre à la nouvelle extension [JaybirdOOo][72] de fonctionner.
- Reprise d'une partie de l'implémentation de `javax.sql.rowset.CachedRowSet` dans les jeux de résultats [ScrollableResultSet.java][73] et [SensitiveResultSet.java][74] afin de simuler le type `TYPE_SCROLL_SENSITIVE` à partir des jeux de résultats de type `TYPE_FORWARD_ONLY` et `TYPE_SCROLL_INSENSITIVE` respectivement. Cela permet à LibreOffice Base d'utiliser des signets (ie : l'interface UNO [XRowLocate][64]) qui permettent des insertions, mises à jour et suppressions positionnées et donc, pour les bases de données le supportant, la possibilité d'éditer des tables ne contenant aucune clé primaire. De plus, un [mode SQL][75] **permet de rendre éditable n'importe quel ResultSet.** Ce mode peut être validée dans les options de l'extension, elle est trés puissante et donc à utiliser avec prudence. Concernant les jeux de résultats de type `TYPE_FORWARD_ONLY`, leur implémentation chargeant progressivement l'intégralité des données du jeu de résultats en mémoire peut conduire à un débordement de mémoire. La mise en oeuvre d'une pagination éliminera ce risque.
- Ajout du pilote MySQL Connector/J version 8.4.0. Ce driver ne semble pas fonctionner correctement, des erreurs assez surprenantes apparaissent... Je le laisse en place au cas où des gens seraient prêts à participer à son intégration? A utiliser avec précaution.
- Suite à la demande de [PeterSchmidt23][76] ajout du pilote [Trino][77] version 448. Ne connaissant pas Trino, qui a l'air étonnant par ailleur, seulement un début d'intégration a été réalisée. L'edition du contenu des tables n'est pas encore possible, voir [dysfonctionnement #22306][78]. Le nom des tables doit être en minuscule afin d'autoriser leur création.
- L'implémentation de `CachedRowSet` semble avoir résolu le problème d'insertion de cellules depuis Calc, voir [dysfonctionnement #7][79].
- De nombreuses corrections et améliorations...

### Ce qui a été fait pour la version 1.4.0:

- Mise à jour du pilote Jaybird vers la version finale 5.0.5.
- Modification de l'implémentation de l'interface UNO [com.sun.star.sdbc.XGeneratedResultSet][43]. Cette nouvelle implémentation prend en charge les pilotes qui ne suivent pas l'API JDBC mais proposent une implémentation spécifique (ie: MariaDB et Derby). Pour être activé lors de l'utilisation de fichiers odb créés avec une version précédente, s'il est présent, il est nécessaire de modifier le paramètre : `Requête des valeurs générées` accessible par le menu : **Edition -> Base de données -> Paramètres avancés... -> Valeurs générées** par la valeur : `SELECT * FROM %s WHERE %s`.
- Ajout de nouveaux paramètres pris en charge par le fichier de configuration [Drivers.xcu][48]. Ces nouveaux paramètres permettent de modifier les valeurs renvoyées par les pilotes concernant la visibilité des modifications dans les jeux de résultats (ie: insertion, mise à jour et suppression). Ils permettent également de forcer le mode SQL pour les modifications souhaitées dans les jeux de résultats.
- Finalisation de l'implémentation de l'émulation rendant tout jeu de résultats modifiable, si l'enregistrement est unique dans ce jeu de résultats. Cette implémentation, utilisant les signets (ie: bookmark), permet l'édition de jeu de résultats provenant de **Requêtes Base**, cela rend tout simplement les **Requêtes LibreOffice Base éditables**. Les requêtes joignant plusieurs tables ne sont pas encore supportées et je suis ouvert à toute proposition technique concernant une éventuelle implémentation.
- Afin de rendre modifiables les jeux de résultats retournés par le driver **Trino** et de précéder la [demande d'amélioration #22408][80], une recherche de la clé primaire sera lancée afin de retrouver la première colonne, du jeu de résultats, ayant pas de doublons.
- Afin de contourner le [dysfonctionnement #368][81] le driver HsqlDB utilise des mises à jour en mode SQL dans les jeux de résultats.
- De nombreuses corrections et améliorations...

### Ce qui a été fait pour la version 1.4.1:

- Nouvelle implémentation, que j'espère définitive, des signets (bookmarks). Il est basé sur trois fichiers et est tiré de l'implémentation par Sun de `javax.sql.rowset.CachedRowSet` :
  - [ScollableResultSet.class][73]
  - [SensitiveResultSet.class][74]
  - [CachedResultSet.class][82]
- **Ces ResultSets sont capables d'éditer presque toutes les requêtes créées dans LibreOffice Base, même les vues...** Mais afin de garantir une bonne fonctionnalité, certaines règles doivent être respectées afin de rendre un jeu de résultats éditable. Si la requête concerne plusieurs tables alors il est impératif d'inclure les clés primaires de chaque table dans la liste des colonnes du jeu de résultats. Si la requête ne concerne qu'une seule table alors le jeu de résultats sera modifiable s'il existe une colonne qui ne contient pas de doublon (ie: une clé naturelle). Cela permet de rendre modifiables les jeux de résultats provenant du pilote Trino.
- Suppression de l'utilisation de classes génériques là où elles n'étaient pas nécessaires. Cela a rendu le pilote plus rapide...
- Ajout de paramètres spéciaux dans: **Edition -> Base de données -> Paramètres avancés... -> Paramètres spéciaux** afin de répondre à la demande d'intégration du pilote Trino (voir [demande d'amélioration n°8][83]). Il est nécessaire de recréer les fichiers odb afin d'avoir accès à ces nouveaux paramètres.

### Ce qui a été fait pour la version 1.4.2:

- Pilote JDBC Trino mis à jour vers la version 453.
- Mise à jour du paquet [Python packaging][84] vers la version 24.1.
- Mise à jour du paquet [Python setuptools][85] vers la version 72.1.0 afin de répondre à l'[alerte de sécurité Dependabot][86].

### Ce qui a été fait pour la version 1.4.3:

- Mise à jour du paquet [Python setuptools][85] vers la version 73.0.1.
- La journalisation accessible dans les options de l’extension s’affiche désormais correctement sous Windows.
- Les options de l'extension sont désormais accessibles via: **Outils -> Options -> LibreOffice Base -> Pilote JDBC**
- Les modifications apportées aux options d'extension, qui nécessitent un redémarrage de LibreOffice, entraîneront l'affichage d'un message.
- Support de LibreOffice version 24.8.x.

### Ce qui a été fait pour la version 1.4.4:

- Il est désormais possible d'insérer des données dans une table vide lors de l'utilisation d'un ResultSet `TYPE_FORWARD_ONLY` (ie: SQLite, Trino).
- Le bouton options est désormais accessible dans la liste des extensions installées obtenue par le menu : **Outils -> Gestionnaire d'extensions...**
- Les options de l'extension sont désormais accessibles via: **Outils -> Options -> LibreOffice Base -> Pilote JDBC pur Java**
- Les options de l'extension: **Voir les tables système**, **Utiliser les signets** et **Forcer le mode SQL** seront recherchées dans les informations fournies lors de la connexion et auront la priorité si elles sont présentes.
- Pilote Trino mis à jour vers la version 455.

### Ce qui a été fait pour la version 1.4.5:

- Correction pour permettre à l'extension eMailerOOo de fonctionner correctement dans la version 1.2.5.

### Ce qui a été fait pour la version 1.4.6:

- Modification de l'implémentation de l'interface UNO [XPropertySet][87]. Cette nouvelle implémentation assure l'unicité des [Handle][88] pour chaque propriété. Cette implémentation étant partagée avec l'extension vCardOOo, **elle rend toutes les versions existantes de vCardOOo obsolètes**. Elle est basée sur trois fichiers:
  - [PropertySet.java][89]
  - [PropertySetAdapter.java][90]
  - [PropertyWrapper.java][91]
- Correction de problèmes dans l'implémentation des signets (bookmark). Ces modifications ont été testées plus particulièrement avec les pilotes HsqlDB 2.7.4 et Jaybird 5.0.6.
- Nouvelle implémentation des options de l'extension et plus particulièrement de l'onglet **Options du pilote JDBC** ce qui devrait permettre à terme la configuration à partir de zéro d'un pilote JDBC pour pouvoir fonctionner avec LibreOffice Base. L’opération de mise à jour de l’archive du pilote JDBC a été simplifiée. Elle prend en charge la mise à jour des pilotes qui nécessitent plusieurs archives jar pour fonctionner (ie: Derby, Jaybird 6.x). Cette nouvelle fenêtre qui semble assez simple, nécessite en réalité une gestion assez compliquée, n'hésitez donc pas à me signaler d'éventuels dysfonctionnements.
- De nombreuses autres améliorations.

### Ce qui a été fait pour la version 1.5.0:

- Mise à jour du paquet [Python packaging][84] vers la version 25.0.
- Mise à jour du paquet [Python setuptools][85] vers la version 75.3.2.
- Mise à jour du paquet [Python six][92] vers la version 1.17.0.
- Déploiement de l'enregistrement passif permettant une installation beaucoup plus rapide des extensions et de différencier les services UNO enregistrés de ceux fournis par une implémentation Java ou Python. Cet enregistrement passif est assuré par l'extension [LOEclipse][93] via les [PR#152][94] et [PR#157][95].
- Modification de [LOEclipse][93] pour prendre en charge le nouveau format de fichier `rdb` produit par l'utilitaire de compilation `unoidl-write`. Les fichiers `idl` ont été mis à jour pour prendre en charge les deux outils de compilation disponibles: idlc et unoidl-write.
- Ajout de la prise en charge de l'[instrumentation Java][96] à LibreOffice avec [Enhancement Request #165774][97] puis [PR#183280][98]. Cela permettra, à partir de LibreOffice 25.8.x, d'accéder à la journalisation pour tous les pilotes JDBC utilisant `java.lang.System.Logger` comme interface de journalisation. Cette nouvelle fonctionnalité peut être activée dans les options d'extension si la version de LibreOffice le permet. Le rétroportage vers LibreOffice 25.2.x m'a été refusé alors soyez patient.
- Toutes les commandes SQL, DDL ou DCL proviennent désormais du fichier de configuration du pilote JDBC [Drivers.xcu][48]. L'implémentation du traitement de ces commandes et de leurs paramètres a été regroupée dans le package [io.github.prrvchr.driver.query][99].
- Compilation de toutes les archives Java contenues dans l'extension sous forme de modules et avec **Java JDK version 17**.
- Mise à jour de tous les pilotes JDBC intégrés, à l'exception de SQLite et Trino, vers leurs dernières versions respectives prenant en charge Java 17.
- Suppression de tous les fichiers idl definissant les structures suivantes: Date, DateTime, DateTimeWithTimezone, DateWithTimezone, Duration, Time et TimeWithTimezone. Ces fichiers étaient nécessaires à la compatibilité avec OpenOffice et sont désormais remplacés par les fichiers idl équivalents de l'API LibreOffice. **Ce changement rend toutes les versions des extensions utilisant la version précédente de jdbcDriverOOo incompatibles**.
- La gestion des utilisateurs, des rôles et des privilèges a été testée avec tous les pilotes intégrés à jdbcDriverOOo, à l'exception de SQLite et Trino.
- Il est désormais possible de créer le fichier oxt de l'extension jdbcDriverOOo uniquement avec Apache Ant et une copie du dépôt GitHub. La section [Comment créer l'extension][100] a été ajoutée à la documentation.
- Implémentation de [PEP 570][101] dans la [journalisation][102] pour prendre en charge les arguments multiples uniques.
- Toute erreur survenant lors du chargement du pilote sera consignée dans le journal de l'extension si la journalisation a été préalablement activé. Cela facilite l'identification des problèmes d'installation sous Windows.
- Lorsque les pilotes JDBC intégrés à l'extension jdbcDriverOOo sont enregistrés auprès de `java.sql.DriverManager`, c'est à dire lors de la première connexion nécessitant ce pilote, si ce pilote est déjà présent dans le classpath Java, alors cela sera détecté, le pilote non enregistré, la connexion refusée et l'erreur journalisée.

### Ce qui a été fait pour la version 1.5.1:

- **L'instrumentation Java est désormais requise pour le bon fonctionnement de jdbcDriverOOo.** Pour les versions de LibreOffice antérieures à la version 25.8.x, il est actuellement nécessaire d'installer l'instrumentation Java manuellement. Une section expliquant [Comment installer l'instrumentation Java][103] a été ajoutée à la documentation. Si l'instrumentation Java n'est pas présente, le chargement des pilotes JDBC échouera et un message d'erreur sera présent dans le journal.
- Réécriture du Service Provider Interface Java: `javax.sql.rowset.RowSetFactory`. Ce nouveau service SPI est implémenté à l'aide de l'archive [RowSetFactory.jar][104], chargée via l'instrumentation Java. Cette nouvelle implémentation a été modifiée pour prendre en charge:
    - Les identifiants avec casse mixte dans les requêtes SQL.
    - L'exclusion des colonnes à incrémentation automatique et/ou calculées dans les requêtes d'insertion (requis par PostgreSQL).
    - L'utilisation de l'interface `java.sql.Statement.getGeneratedKeys()` après toutes les insertions afin de récupérer la valeur des colonnes utilisant des valeurs générées par la base de données sous-jacente (ie: colonne auto-incrémentée et/ou calculée).
    - La mise en conformité du code source à l'aide de CheckStyle et du modèle [checkstyle.xml][105]. Il reste encore du travail à faire.
    - L'utilisation de `java.lang.System.Logger` comme facade de journalisation.
    - Beaucoup de petites corrections nécessaires pour que cela fonctionne correctement car l'implémentation de base du SDK Java 11 ne me semble pas très fonctionnelle, voire même fantaisiste.
    - Avec cette nouvelle implémentation de [CachedRowSetImpl][106], toutes les colonnes de la première table d'un `java.sql.ResultSet` **sont modifiables dans Base**. Actuellement, seule la table de la première colonne du ResultSet est prise en compte. Cependant, il est nécessaire que les colonnes de la table de ce jeu de résultats répondent aux critères suivants:
        - Si ces colonnes proviennent d'une table avec des clés primaires, ces clés doivent faire partie des colonnes du jeu de résultats.
        - Si ces colonnes proviennent d'une table sans clé primaire, les enregistrements d'un ResultSet seront modifiables s'ils sont clairement identifiables par les colonnes du ResultSet. Dans le cas contraire, une exception SQL sera levée lors de toute tentative de mise à jour ou de suppression.
    - Ce nouveau CachedRowSet, fonctionnant sans connexion, doit charger l'intégralité du contenu du jeu de résultats en mémoire et s'assurera, avant toute modification ou suppression, que l'enregistrement n'a pas été modifié dans la base de données sous-jacente. Si tel est le cas, une exception sera levée et l'opération sera annulée.
    - L'archive a été compilée avec Java 17 et sous forme de module.
- Nous avons maintenant la possibilité d'utiliser ou non CachedRowSet. Cette option peut être configurée dans les options de l'extension. Si cette option est forcée, **il est même possible de modifier les requêtes dans LibreOffice Base. Wahou...**
- Les ResultSets des méthodes `getTables()`, `getTableTypes()`, `getTypeInfo()` et `getTablePrivileges()` de l'interface XDatabaseMetaData sont désormais des CachedRowSet dont les données sont mises à jour selon le fichier de configuration [Drivers.xcu][48]. Cela permet d'obtenir les résultats attendus dans LibreOffice Base avec n'importe quel pilote sous-jacent.
- Grâce à ces ResultSets modifiables en fonction de la configuration, l'option d'affichage des tables système fonctionne avec n'importe quel pilote sous-jacent et quel que soit le niveau de l'API utilisée (sdbc, sdbcx et sdb).
- Il est désormais possible d'obtenir les numéros de ligne de code source dans les traces Java grâce au changement dans LOEclipse [PR#166][107].
- Correction de nombreuses régressions liées à la dernière mise à jour qui a apporté de nombreux changements.
- La nouvelle version du pilote SQLite est désormais compilée sous Java 11 et utilise `java.lang.System.Logger` comme façade de journalisation, ce qui permet d'y accéder dans LibreOffice. C'est le seul qui nécessite l'utilisation de l'option CachedRowSet, sinon Base n'affichera que les tables et vues en lecture seule.
- Il semble que ce soit la mise à jour la plus importante de JdbcDriverOOo, et je ne m'attendais pas à en arriver là. La prochaine étape consistera à intégrer Trino et à pouvoir exécuter des requêtes réparties sur différentes bases de données dans LibreOffice Base. CachecRowSet est exactement la brique dont j'avais besoin pour pouvoir terminer cela.

### Que reste-t-il à faire pour la version 1.5.1:

- Ajouter de nouvelles langues pour l'internationalisation...

- Tout ce qui est bienvenu...

[1]: </img/jdbcdriver.svg#collapse>
[2]: <https://prrvchr.github.io/jdbcDriverOOo/>
[3]: <https://prrvchr.github.io/jdbcDriverOOo/CHANGELOG>
[4]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr>
[5]: <https://forum.openoffice.org/en/forum/viewtopic.php?f=13&t=103912>
[6]: <http://hsqldb.org/>
[7]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/Driver.java>
[8]: <https://github.com/hanya/MRI>
[9]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdb>
[10]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc>
[11]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx>
[12]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-HsqlDB/source/io/github/prrvchr/jdbcdriver/hsqldb>
[13]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-H2/source/io/github/prrvchr/jdbcdriver/h2>
[14]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/Driver-Derby/source/io/github/prrvchr/jdbcdriver/derby>
[15]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Driver.java>
[16]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/UnoLogger/source/io/github/prrvchr/uno/logging>
[17]: <https://www.slf4j.org/>
[18]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/logging/module-ix.html>
[19]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/options>
[20]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/admin>
[21]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/user>
[22]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/service/pythonpath/jdbcdriver/group>
[23]: <https://github.com/xerial/sqlite-jdbc>
[24]: <https://github.com/gotson>
[25]: <https://github.com/xerial/sqlite-jdbc/issues/786>
[26]: <https://prrvchr.github.io/HyperSQLOOo/README_fr>
[27]: <https://prrvchr.github.io/SQLiteOOo/README_fr>
[28]: <https://mariadb.com/downloads/connectors/connectors-data-access/java8-connector/>
[29]: <https://www.h2database.com/html/main.html>
[30]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/ResultSetBase.java>
[31]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ResultSetSuper.java>
[32]: <https://bugs.documentfoundation.org/show_bug.cgi?id=156512>
[33]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/JDBCConnectionProperties.html#TypeInfoSettings>
[34]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/metadata/TypeInfoResultSet.java>
[35]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu#L332>
[36]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DatabaseMetaDataBase.java#L444>
[37]: <https://github.com/artem78>
[38]: <https://github.com/prrvchr/jdbcDriverOOo/issues/4>
[39]: <https://github.com/prrvchr/HyperSQLOOo/issues/1>
[40]: <https://github.com/prrvchr/jdbcDriverOOo/releases/latest/download/requirements.txt>
[41]: <https://peps.python.org/pep-0508/>
[42]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#pr%C3%A9requis>
[43]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/XGeneratedResultSet.html>
[44]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XAlterTable.html>
[45]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.3-SNAPSHOT.jar>
[46]: <https://github.com/prrvchr/sqlite-jdbc/releases/download/3.45.1.3-SNAPSHOT/sqlite-jdbc-3.45.1.6-SNAPSHOT.jar>
[47]: <https://jdbc.postgresql.org/>
[48]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/Drivers.xcu>
[49]: <https://jira.mariadb.org/browse/CONJ-1160>
[50]: <https://fr.wikibooks.org/wiki/Programmation_Java/Types_g%C3%A9n%C3%A9riques>
[51]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/TableContainerSuper.java>
[52]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ViewContainer.java>
[53]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/ColumnContainerBase.java>
[54]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/KeyContainer.java>
[55]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/IndexContainer.java>
[56]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/Container.java>
[57]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/helper/TableHelper.java#L199>
[58]: <https://github.com/FirebirdSQL/jaybird/issues/791>
[59]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/helper/TableHelper.java#L490>
[60]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160375>
[61]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbc/module-ix.html>
[62]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/module-ix.html>
[63]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdb/module-ix.html>
[64]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/sdbcx/XRowLocate.html>
[65]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset>
[66]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbcx/RoleContainer.java>
[67]: <https://bugs.documentfoundation.org/show_bug.cgi?id=160516>
[68]: <https://hsqldb.org/doc/guide/management-chapt.html#mtc_system_versioned_tables>
[69]: <https://hsqldb.org/doc/guide/texttables-chapt.html#ttc_table_definition>
[70]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/uno/sdbc/DriverBase.java#L160>
[71]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/provider/DriverManagerHelper.java>
[72]: <https://prrvchr.github.io/JaybirdOOo/README_fr>
[73]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset/ScrollableResultSet.java>
[74]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset/SensitiveResultSet.java>
[75]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/rowset/RowSetWriter.java>
[76]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8>
[77]: <https://trino.io/>
[78]: <https://github.com/trinodb/trino/issues/22306>
[79]: <https://github.com/prrvchr/jdbcDriverOOo/issues/7>
[80]: <https://github.com/trinodb/trino/issues/22408>
[81]: <https://sourceforge.net/p/hsqldb/feature-requests/368/>
[82]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/resultset/CachedResultSet.java>
[83]: <https://github.com/prrvchr/jdbcDriverOOo/issues/8#issuecomment-2182445391>
[84]: <https://pypi.org/project/packaging/>
[85]: <https://pypi.org/project/setuptools/>
[86]: <https://github.com/prrvchr/jdbcDriverOOo/pull/9>
[87]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/XPropertySet.html>
[88]: <https://www.openoffice.org/api/docs/common/ref/com/sun/star/beans/Property.html#Handle>
[89]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/UnoHelper/source/io/github/prrvchr/uno/helper/PropertySet.java>
[90]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/UnoHelper/source/io/github/prrvchr/uno/helper/PropertySetAdapter.java>
[91]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/UnoHelper/source/io/github/prrvchr/uno/helper/PropertyWrapper.java>
[92]: <https://pypi.org/project/six/>
[93]: <https://github.com/LibreOffice/loeclipse>
[94]: <https://github.com/LibreOffice/loeclipse/pull/152>
[95]: <https://github.com/LibreOffice/loeclipse/pull/157>
[96]: <https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/Instrumentation.html>
[97]: <https://bugs.documentfoundation.org/show_bug.cgi?id=165774>
[98]: <https://gerrit.libreoffice.org/c/core/+/183280>
[99]: <https://github.com/prrvchr/jdbcDriverOOo/tree/master/source/jdbcDriverOOo/source/io/github/prrvchr/driver/query>
[100]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#comment-cr%C3%A9er-lextension>
[101]: <https://peps.python.org/pep-0570/>
[102]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/uno/lib/uno/logger/logwrapper.py#L109>
[103]: <https://prrvchr.github.io/jdbcDriverOOo/README_fr#comment-installer-linstrumentation-java>
[104]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/RowSetFactory/dist/RowSetFactory.jar>
[105]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/checkstyle.xml>
[106]: <https://github.com/prrvchr/jdbcDriverOOo/blob/master/source/RowSetFactory/source/io/github/prrvchr/java/rowset/CachedRowSetImpl.java>
[107]: <https://github.com/LibreOffice/loeclipse/pull/166>
