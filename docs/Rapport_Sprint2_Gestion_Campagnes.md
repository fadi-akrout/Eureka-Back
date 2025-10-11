refait le chapitre en lattex en se basant sur le rapport que je vais donner et assurre l'integration du contenu deja trouvee dans le chapitre comme la cote frontend ...voici le rapport generer par copilote
# Rapport Technique Sprint 2 - Gestion des Campagnes avec Formulaires
## Projet Eureka - Backend Spring Boot

### Équipe de développement
**Période :** Sprint 2 (4 semaines)  
**Date :** Août 2025  
**Technologies :** Spring Boot 3.x, PostgreSQL, JWT, Spring Security, Angular

---

## 1. Introduction du Sprint

### 1.1 Objectif prioritaire
Le Sprint 2 se concentre sur l'implémentation complète du module de gestion des campagnes avec intégration des produits, formulaires et questions associés. L'objectif est de permettre aux annonceurs de créer et gérer des campagnes d'évaluation de produits avec des formulaires personnalisés, tout en offrant aux panelistes un accès consultatif aux campagnes publiées.

### 1.2 Enjeux principaux
- **Création unifiée** : Campagne avec création automatique du produit et formulaire associés
- **Gestion des questions** : Interface dédiée pour personnaliser les formulaires d'évaluation
- **Workflow simplifié** : Processus de création en 4 étapes claires et logiques
- **Sécurité par rôles** : Contrôle d'accès différencié selon les utilisateurs
- **Architecture évolutive** : Base solide pour les prochains sprints

### 1.3 Périmètre fonctionnel
Le Sprint 2 couvre :
- ✅ CRUD complet des campagnes
- ✅ Gestion automatique des produits associés
- ✅ Création et gestion des formulaires d'évaluation
- ✅ Ajout, modification et suppression de questions
- ✅ Gestion des statuts de campagne (Brouillon/Publiée/Clôturée)
- ✅ Prévisualisation des formulaires
- ✅ Contrôle d'accès par rôle utilisateur

---

## 2. Architecture et Conception

### 2.1 Diagramme de Cas d'Utilisation

Le diagramme suivant présente l'ensemble des fonctionnalités du module de gestion des campagnes :

```plantuml
@startuml
!theme plain
title Diagramme de Cas d'Utilisation - Sprint 2 : Gestion de Base des Campagnes\nPlateforme Eureka - Fonctionnalités Essentielles

' Définition des acteurs
actor "Annonceur" as Annonceur
actor "Paneliste" as Paneliste
actor "Admin" as Admin

' Définition du système simplifié
rectangle "Module Gestion Campagnes" {
    
    ' Cas d'utilisation principaux de base
    usecase "Créer campagne\n(avec produit et formulaire)" as UC1
    usecase "Modifier campagne" as UC2
    usecase "Supprimer campagne" as UC3
    usecase "Lister campagnes" as UC4
    usecase "Consulter détails campagne" as UC5
    usecase "Gérer statut campagne\n(Brouillon/Publiée/Clôturée)" as UC6
    
    ' Gestion formulaires et questions
    usecase "Ajouter questions\nau formulaire" as UC7
    usecase "Modifier questions" as UC8
    usecase "Supprimer questions" as UC9
    usecase "Prévisualiser formulaire" as UC10
    
    ' Cas d'utilisation techniques de base
    usecase "Authentification utilisateur" as AUTH
    usecase "Contrôle d'accès par rôle" as RBAC
    usecase "Validation des données" as VAL
    usecase "Pagination des résultats" as PAGE
    usecase "Filtrage par statut" as FILTER
}

' Relations principales
Annonceur --> UC1 : "Crée ses campagnes"
Paneliste --> UC4 : "Voit campagnes publiées"
Admin --> UC3 : "Supprime campagnes"

' Relations d'inclusion
UC1 ..> AUTH : "<<include>>"
UC4 ..> PAGE : "<<include>>"
UC7 ..> VAL : "<<include>>"

@enduml
```

### 2.2 Description des Acteurs

#### Annonceur
**Responsabilités principales :**
- Créer et gérer ses propres campagnes d'évaluation de produits
- Personnaliser les formulaires en ajoutant des questions adaptées
- Modifier les informations de ses campagnes et produits
- Gérer le cycle de vie de ses campagnes (statuts)
- Prévisualiser les formulaires avant publication

**Restrictions :**
- Accès limité à ses propres campagnes uniquement
- Ne peut pas supprimer définitivement ses campagnes
- Ne peut pas accéder aux campagnes d'autres annonceurs

#### Paneliste
**Responsabilités principales :**
- Consulter la liste des campagnes publiées disponibles
- Visualiser les détails des campagnes et leurs formulaires
- Prévisualiser les formulaires d'évaluation

**Restrictions :**
- Accès en lecture seule strictement
- Voit uniquement les campagnes avec statut "Publiée"
- Ne peut pas créer, modifier ou supprimer de contenu

#### Admin
**Responsabilités principales :**
- Supervision complète de toutes les campagnes de la plateforme
- Création, modification et suppression de toutes campagnes
- Gestion forcée des statuts de campagne
- Modération du contenu et des questions

**Privilèges étendus :**
- Seul acteur autorisé à la suppression définitive
- Peut modifier les campagnes de tous les annonceurs
- Contrôle total sur les transitions de statut

### 2.3 Modèle de Données

#### Architecture des Entités

```plantuml
@startuml
!theme plain
title Modèle de Données - Campagnes avec Formulaires

class Campagne {
    - id: int
    - nom: String
    - description: String
    - dateDebut: Date
    - dateFin: Date
    - statut: String
    - imageUrl: String
    - annonceurId: int
    --
    + getId(): int
    + getStatut(): String
    + isPubliee(): boolean
}

class Produit {
    - id: int
    - nom: String
    - description: String
    - categorie: String
    - noteMoyenne: float
    - campagneId: int
    --
    + getCategorie(): String
    + calculerNoteMoyenne(): float
}

class Formulaire {
    - id: int
    - campagneId: int
    --
    + getNombreQuestions(): int
    + getDureeEstimee(): int
    + getQuestions(): List<Question>
}

class FormulaireQuestion {
    - id: int
    - formulaireId: int
    - questionId: int
    - ordre: int
    --
    + getOrdre(): int
}

class Question {
    - id: int
    - texte: String
    - type: String
    - obligatoire: boolean
    --
    + getType(): TypeQuestion
    + getOptions(): List<Option>
}

class Option {
    - id: int
    - questionId: int
    - texte: String
    - valeur: String
    - ordre: int
    --
    + getValeur(): String
}

class Annonceur {
    - id: int
    - entreprise: String
    --
    + getCampagnes(): List<Campagne>
}

' Relations
Campagne ||--|| Produit : "évalue"
Campagne ||--|| Formulaire : "contient"
Campagne }|--|| Annonceur : "créée par"
Formulaire ||--o{ FormulaireQuestion : "structure"
FormulaireQuestion }|--|| Question : "référence"
Question ||--o{ Option : "propose"

@enduml
```

#### Entités JPA Principales

**Campagne**
```java
@Entity
@Table(name = "campagne")
public class Campagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String nom;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Date dateDebut;
    private Date dateFin;
    private String statut; // "Brouillon", "Publiée", "Clôturée"
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonceur_id")
    private Annonceur annonceur;
    
    @OneToOne(mappedBy = "campagne", cascade = CascadeType.ALL)
    private Produit produit;
    
    @OneToOne(mappedBy = "campagne", cascade = CascadeType.ALL)
    private Formulaire formulaire;
    
    // Méthodes métier
    public boolean isPubliee() {
        return "Publiée".equals(statut);
    }
    
    public boolean peutEtreModifiee() {
        return "Brouillon".equals(statut);
    }
}
```

**Produit**
```java
@Entity
@Table(name = "produit")
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String nom;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String categorie;
    private float noteMoyenne;
    
    @OneToOne
    @JoinColumn(name = "campagne_id", unique = true)
    private Campagne campagne;
}
```

**Formulaire et Questions**
```java
@Entity
@Table(name = "formulaire")
public class Formulaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @OneToOne
    @JoinColumn(name = "campagne_id")
    private Campagne campagne;
    
    @OneToMany(mappedBy = "formulaire", cascade = CascadeType.ALL)
    @OrderBy("ordre ASC")
    private Set<FormulaireQuestion> formulaireQuestions = new HashSet<>();
    
    public int getNombreQuestions() {
        return formulaireQuestions.size();
    }
    
    public int getDureeEstimee() {
        // Estimation : 45 secondes par question
        return formulaireQuestions.size() * 45;
    }
}

@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String texte;
    
    @Enumerated(EnumType.STRING)
    private TypeQuestion type;
    
    private boolean obligatoire;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private Set<Option> options = new HashSet<>();
    
    public enum TypeQuestion {
        CHOIX_UNIQUE("Choix unique"),
        CHOIX_MULTIPLE("Choix multiple"),
        ECHELLE_NOTATION("Échelle de notation"),
        TEXTE_LIBRE("Texte libre"),
        ECHELLE_LIKERT("Échelle de Likert");
        
        private final String libelle;
        TypeQuestion(String libelle) { this.libelle = libelle; }
    }
}
```

---

## 3. Workflow de Création de Campagne

### 3.1 Diagramme de Séquence

Le processus de création suit un workflow en 4 étapes :

```plantuml
@startuml
!theme plain
title Diagramme de Séquence - Création Campagne Complète\nSprint 2 : Plateforme Eureka

actor "Annonceur" as User
participant "Angular Frontend" as Frontend
participant "CampaignController" as Controller
participant "CampaignService" as CampService
participant "Database" as DB

== Étape 1: Création Campagne ==
User -> Frontend : Saisit informations campagne
Frontend -> Controller : POST /api/campagnes
Controller -> CampService : createCampagne(data, userId)
CampService -> DB : INSERT campagne
DB -> CampService : Campagne créée (id=123)

== Étape 2: Création Automatique Produit et Formulaire ==
CampService -> DB : INSERT produit (campagne_id=123)
CampService -> DB : INSERT formulaire (campagne_id=123)
CampService -> Controller : Campagne complète créée
Controller -> Frontend : 201 Created

== Étape 3: Configuration Questions ==
Frontend -> Frontend : Navigation /campaigns/123/questions
User -> Frontend : Ajoute questions du formulaire
loop Pour chaque question
    Frontend -> Controller : POST /api/formulaires/789/questions
    Controller -> CampService : addQuestion(formId, questionData)
    CampService -> DB : INSERT question + options
end

== Étape 4: Finalisation ==
Frontend -> Controller : GET /api/campagnes/123
Controller -> Frontend : Campagne complète
Frontend -> Frontend : Redirection /campaigns/123/details

@enduml
```

### 3.2 Analyse Détaillée des Étapes

#### Étape 1 : Création de la Campagne de Base
**Durée estimée :** 2-3 secondes
**Données requises :**
- Nom de la campagne (obligatoire, 3-255 caractères)
- Description (optionnelle, format texte enrichi)
- Date de début (obligatoire, dans le futur)
- Date de fin (obligatoire, postérieure à la date de début)

**Validations appliquées :**
- Unicité du nom de campagne pour l'annonceur
- Cohérence des dates (fin > début)
- Statut automatiquement défini à "Brouillon"
- Association automatique avec l'annonceur connecté

**Code exemple - Backend :**
```java
@PostMapping("/api/campagnes")
public ResponseEntity<CampagneDTO> createCampagne(
    @Valid @RequestBody CampagneRequest request,
    Authentication auth) {
    
    // Récupération de l'annonceur connecté
    Annonceur annonceur = getCurrentAnnonceur(auth);
    
    // Création de la campagne avec validations
    Campagne campagne = campagneService.createCampagne(request, annonceur);
    
    return ResponseEntity.status(201).body(campagneMapper.toDTO(campagne));
}
```

#### Étape 2 : Création Automatique du Produit et Formulaire
**Durée estimée :** 1-2 secondes
**Processus automatisé :**

1. **Création du produit associé** : Un produit vide est automatiquement créé et lié à la campagne
2. **Création du formulaire** : Un formulaire vide est créé, prêt à recevoir les questions
3. **Établissement des relations** : Les liens entre campagne, produit et formulaire sont configurés

**Avantages de cette approche :**
- **Simplicité** : L'utilisateur n'a pas à gérer ces créations
- **Cohérence** : Garantit que chaque campagne a ses composants
- **Performance** : Création atomique en une seule transaction

#### Étape 3 : Configuration des Questions
**Durée estimée :** 5-15 minutes (selon complexité)
**Interface dédiée :** `/campaigns/{id}/questions/create`

**Types de questions supportés :**
- **Choix unique** : Radio buttons (ex: "Quelle est votre impression générale ?")
- **Choix multiple** : Checkboxes (ex: "Quels aspects vous plaisent ?")
- **Échelle de notation** : Scale 1-5 ou 1-10 (ex: "Notez la qualité de 1 à 10")
- **Texte libre** : Zone de texte (ex: "Commentaires supplémentaires")
- **Échelle de Likert** : Accord/désaccord (ex: "Je recommande ce produit")

**Fonctionnalités de l'interface :**
- Ajout dynamique de questions
- Réorganisation par glisser-déposer
- Prévisualisation en temps réel
- Validation des données avant sauvegarde

#### Étape 4 : Finalisation et Redirection
**Actions automatiques :**
- Validation que la campagne est complète
- Vérification du nombre de questions (recommandation 3-8 questions)
- Calcul de la durée estimée du formulaire
- Redirection vers la page de détail de la campagne

---

## 4. Fonctionnalités Développées

### 4.1 CRUD Complet des Campagnes

#### Création (Create)
**Endpoint :** `POST /api/campagnes`
**Permissions :** Annonceur, Admin
**Payload exemple :**
```json
{
  "nom": "Test Smartphone XYZ",
  "description": "Évaluation du nouveau smartphone XYZ",
  "dateDebut": "2025-09-01",
  "dateFin": "2025-09-30"
}
```

**Réponse exemple :**
```json
{
  "id": 123,
  "nom": "Test Smartphone XYZ",
  "statut": "Brouillon",
  "produit": {"id": 456},
  "formulaire": {"id": 789, "nombreQuestions": 0}
}
```

#### Lecture (Read)
**Endpoints :**
- `GET /api/campagnes` - Liste paginée avec filtres
- `GET /api/campagnes/{id}` - Détails d'une campagne

**Filtres disponibles :**
- Par statut : `?statut=Publiée`
- Par annonceur : `?annonceurId=123` (Admin uniquement)
- Par période : `?dateDebut=2025-01-01&dateFin=2025-12-31`

**Pagination :**
- `?page=0&size=10&sort=dateCreation,desc`

#### Modification (Update)
**Endpoint :** `PUT /api/campagnes/{id}`
**Permissions :** Propriétaire de la campagne, Admin
**Restrictions :** Modifications limitées selon le statut

**Règles de modification :**
- **Brouillon** : Modification complète autorisée
- **Publiée** : Seules description et date de fin modifiables
- **Clôturée** : Aucune modification autorisée

#### Suppression (Delete)
**Endpoint :** `DELETE /api/campagnes/{id}`
**Permissions :** Admin uniquement
**Effet :** Suppression en cascade (campagne + produit + formulaire + questions)

### 4.2 Gestion des Statuts de Campagne

#### États possibles
1. **Brouillon** : Campagne en cours de création
   - Modification complète autorisée
   - Formulaire modifiable
   - Non visible par les panelistes

2. **Publiée** : Campagne active
   - Visible par les panelistes
   - Modifications limitées
   - Formulaire figé

3. **Clôturée** : Campagne terminée
   - Plus de nouvelles participations
   - Données conservées pour analyse
   - Statut définitif

#### Transitions de statut
**Endpoint :** `PUT /api/campagnes/{id}/statut`
**Transitions autorisées :**
- Brouillon → Publiée (validation formulaire requis)
- Publiée → Clôturée (fin de période ou décision manuelle)
- Admin peut forcer toute transition

### 4.3 Gestion des Questions de Formulaire

#### Ajout de questions
**Endpoint :** `POST /api/formulaires/{id}/questions`
**Payload exemple :**
```json
{
  "texte": "Comment évaluez-vous la qualité du produit ?",
  "type": "ECHELLE_NOTATION",
  "obligatoire": true,
  "options": [
    {"texte": "1 - Très mauvais", "valeur": "1"},
    {"texte": "5 - Excellent", "valeur": "5"}
  ]
}
```

#### Types de questions détaillés

**CHOIX_UNIQUE :**
```json
{
  "texte": "Quelle est votre impression générale ?",
  "type": "CHOIX_UNIQUE",
  "options": [
    {"texte": "Très satisfait", "valeur": "tres_satisfait"},
    {"texte": "Satisfait", "valeur": "satisfait"},
    {"texte": "Neutre", "valeur": "neutre"},
    {"texte": "Insatisfait", "valeur": "insatisfait"}
  ]
}
```

**ECHELLE_NOTATION :**
```json
{
  "texte": "Notez la facilité d'utilisation (1-10)",
  "type": "ECHELLE_NOTATION",
  "options": [
    {"texte": "1", "valeur": "1"},
    {"texte": "10", "valeur": "10"}
  ]
}
```

**TEXTE_LIBRE :**
```json
{
  "texte": "Avez-vous des commentaires supplémentaires ?",
  "type": "TEXTE_LIBRE",
  "obligatoire": false
}
```

#### Modification et suppression
**Endpoints :**
- `PUT /api/questions/{id}` - Modification d'une question
- `DELETE /api/questions/{id}` - Suppression d'une question
- `PUT /api/formulaires/{id}/questions/order` - Réorganisation de l'ordre

### 4.4 Prévisualisation des Formulaires

**Endpoint :** `GET /api/formulaires/{id}/preview`
**Fonctionnalité :** Permet de visualiser le formulaire tel qu'il apparaîtra aux panelistes

**Réponse exemple :**
```json
{
  "formulaireId": 789,
  "nombreQuestions": 5,
  "dureeEstimee": 225,
  "questions": [
    {
      "ordre": 1,
      "texte": "Comment évaluez-vous la qualité ?",
      "type": "ECHELLE_NOTATION",
      "obligatoire": true,
      "options": [...]
    }
  ]
}
```

---

## 5. Sécurité et Contrôle d'Accès

### 5.1 Authentification JWT

**Implémentation :**
- Token JWT dans l'en-tête `Authorization: Bearer {token}`
- Durée de vie : 24 heures
- Refresh token automatique

**Validation :**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtUtils.validateToken(token)) {
            Authentication auth = jwtUtils.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

### 5.2 Contrôle d'Accès par Rôle (RBAC)

**Configuration Spring Security :**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/campagnes").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/campagnes").hasAnyRole("ANNONCEUR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/campagnes/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

**Annotations de sécurité :**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('ANNONCEUR') and @campaignService.isOwner(#id, authentication.name))")
@PutMapping("/{id}")
public ResponseEntity<CampagneDTO> updateCampagne(@PathVariable int id, @RequestBody CampagneRequest request) {
    // Implémentation
}
```

### 5.3 Règles d'Accès Détaillées

**Annonceur :**
- ✅ Créer ses propres campagnes
- ✅ Modifier ses campagnes en statut "Brouillon"
- ✅ Lire ses propres campagnes (tous statuts)
- ✅ Gérer les questions de ses formulaires
- ❌ Supprimer définitivement ses campagnes
- ❌ Accéder aux campagnes d'autres annonceurs

**Paneliste :**
- ✅ Lire les campagnes publiées uniquement
- ✅ Prévisualiser les formulaires des campagnes publiées
- ❌ Créer, modifier ou supprimer du contenu
- ❌ Accéder aux campagnes en brouillon

**Admin :**
- ✅ Accès complet à toutes les campagnes
- ✅ Créer, modifier, supprimer toute campagne
- ✅ Forcer les transitions de statut
- ✅ Modérer le contenu des questions

---

## 6. Tests et Validation

### 6.1 Tests Unitaires

**CampagneServiceTest :**
```java
@ExtendWith(MockitoExtension.class)
class CampagneServiceTest {
    
    @Mock
    private CampagneRepository campagneRepository;
    
    @InjectMocks
    private CampagneServiceImpl campagneService;
    
    @Test
    void createCampagne_Success() {
        // Given
        CampagneRequest request = new CampagneRequest();
        request.setNom("Test Campaign");
        request.setDateDebut(LocalDate.now().plusDays(1));
        request.setDateFin(LocalDate.now().plusDays(30));
        
        Annonceur annonceur = new Annonceur();
        annonceur.setId(1);
        
        // When
        Campagne result = campagneService.createCampagne(request, annonceur);
        
        // Then
        assertThat(result.getNom()).isEqualTo("Test Campaign");
        assertThat(result.getStatut()).isEqualTo("Brouillon");
        assertThat(result.getAnnonceur()).isEqualTo(annonceur);
    }
    
    @Test
    void createCampagne_InvalidDates_ThrowsException() {
        // Given
        CampagneRequest request = new CampagneRequest();
        request.setDateDebut(LocalDate.now().plusDays(30));
        request.setDateFin(LocalDate.now().plusDays(1)); // Date fin avant début
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            campagneService.createCampagne(request, new Annonceur());
        });
    }
}
```

**Couverture des tests unitaires :**
- CampagneService : 95% des lignes
- QuestionService : 90% des lignes
- FormulaireService : 88% des lignes

### 6.2 Tests d'Intégration

**Collection Postman - Campagnes :**
```json
{
  "info": {
    "name": "Eureka - Campagnes API",
    "description": "Tests d'intégration pour le module campagnes"
  },
  "auth": {
    "type": "bearer",
    "bearer": [{"key": "token", "value": "{{jwt_token}}"}]
  },
  "tests": [
    {
      "name": "Créer campagne - Succès",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/campagnes",
        "body": {
          "nom": "Test Campaign API",
          "description": "Test de création via API",
          "dateDebut": "2025-09-01",
          "dateFin": "2025-09-30"
        }
      },
      "tests": [
        "pm.test('Status code is 201', () => pm.response.to.have.status(201))",
        "pm.test('Response has campaign id', () => pm.expect(pm.response.json()).to.have.property('id'))"
      ]
    }
  ]
}
```

### 6.3 Tests de Sécurité

**Scénarios testés :**
1. **Authentification requise** : Accès sans token JWT → 401
2. **Autorisation par rôle** : Paneliste tentant de créer campagne → 403
3. **Propriété des ressources** : Annonceur modifiant campagne d'un autre → 403
4. **Validation des données** : Création avec dates invalides → 400

### 6.4 Tests de Performance

**Métriques visées :**
- **Création campagne complète** : < 3 secondes (99e percentile)
- **Liste campagnes paginée** : < 500ms (95e percentile)
- **Ajout question au formulaire** : < 200ms (moyenne)

**Outils utilisés :**
- JMeter pour les tests de charge
- Spring Boot Actuator pour les métriques
- Logs applicatifs pour le monitoring

---

## 7. Architecture Technique

### 7.1 Structure en Couches

```
┌─────────────────────────────┐
│     Couche Présentation     │
│   Controllers REST API      │
├─────────────────────────────┤
│     Couche Service         │
│   Logique Métier           │
├─────────────────────────────┤
│     Couche Repository      │
│   Accès aux Données        │
├─────────────────────────────┤
│     Base de Données        │
│      PostgreSQL            │
└─────────────────────────────┘
```

### 7.2 Services Principaux

**CampagneService :**
```java
@Service
@Transactional
public class CampagneServiceImpl implements CampagneService {
    
    public Campagne createCampagne(CampagneRequest request, Annonceur annonceur) {
        // 1. Validation des données
        validateCampagneData(request);
        
        // 2. Création de la campagne
        Campagne campagne = new Campagne();
        campagne.setNom(request.getNom());
        campagne.setDescription(request.getDescription());
        campagne.setDateDebut(request.getDateDebut());
        campagne.setDateFin(request.getDateFin());
        campagne.setStatut("Brouillon");
        campagne.setAnnonceur(annonceur);
        
        campagne = campagneRepository.save(campagne);
        
        // 3. Création automatique du produit
        Produit produit = new Produit();
        produit.setCampagne(campagne);
        produitRepository.save(produit);
        
        // 4. Création automatique du formulaire
        Formulaire formulaire = new Formulaire();
        formulaire.setCampagne(campagne);
        formulaireRepository.save(formulaire);
        
        return campagne;
    }
}
```

### 7.3 Configuration Base de Données

**Script de création des tables :**
```sql
-- Table campagne
CREATE TABLE campagne (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut VARCHAR(20) DEFAULT 'Brouillon',
    image_url VARCHAR(500),
    annonceur_id INTEGER REFERENCES utilisateur(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table produit
CREATE TABLE produit (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255),
    description TEXT,
    categorie VARCHAR(100),
    note_moyenne DECIMAL(3,2) DEFAULT 0.0,
    campagne_id INTEGER UNIQUE REFERENCES campagne(id)
);

-- Table formulaire
CREATE TABLE formulaire (
    id SERIAL PRIMARY KEY,
    campagne_id INTEGER UNIQUE REFERENCES campagne(id)
);

-- Table question
CREATE TABLE question (
    id SERIAL PRIMARY KEY,
    texte TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    obligatoire BOOLEAN DEFAULT FALSE
);

-- Table de liaison formulaire_question
CREATE TABLE formulaire_question (
    id SERIAL PRIMARY KEY,
    formulaire_id INTEGER REFERENCES formulaire(id),
    question_id INTEGER REFERENCES question(id),
    ordre INTEGER NOT NULL
);

-- Table option (pour questions à choix)
CREATE TABLE option (
    id SERIAL PRIMARY KEY,
    question_id INTEGER REFERENCES question(id),
    texte VARCHAR(255) NOT NULL,
    valeur VARCHAR(100) NOT NULL,
    ordre INTEGER NOT NULL
);
```

---

## 8. Interface Utilisateur (Angular)

### 8.1 Composants Principaux

**CampaignListComponent :**
- Affichage paginé des campagnes
- Filtres par statut et période
- Actions rapides (modifier, voir détails)

**CampaignFormComponent :**
- Formulaire de création/modification de campagne
- Validation reactive forms
- Upload d'image produit

**QuestionManagerComponent :**
- Interface de gestion des questions
- Ajout dynamique avec types variés
- Réorganisation par drag & drop
- Prévisualisation en temps réel

### 8.2 Services Angular

**CampaignService :**
```typescript
@Injectable({providedIn: 'root'})
export class CampaignService {
  
  constructor(private http: HttpClient) {}
  
  createCampaign(campaign: CampaignRequest): Observable<Campaign> {
    return this.http.post<Campaign>('/api/campagnes', campaign);
  }
  
  getCampaigns(filters?: CampaignFilters): Observable<Page<Campaign>> {
    const params = this.buildParams(filters);
    return this.http.get<Page<Campaign>>('/api/campagnes', { params });
  }
  
  addQuestionToForm(formId: number, question: QuestionRequest): Observable<Question> {
    return this.http.post<Question>(`/api/formulaires/${formId}/questions`, question);
  }
}
```

### 8.3 Workflow UX

**Création de campagne :**
1. **Page création** : Formulaire simple (nom, description, dates)
2. **Confirmation** : Message de succès avec ID de campagne
3. **Redirection automatique** : Vers interface de gestion des questions
4. **Configuration questions** : Interface intuitive avec preview
5. **Finalisation** : Redirection vers page de détail

---

## 9. Gestion des Erreurs

### 9.1 Types d'Erreurs Gérées

**Erreurs de Validation :**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus("VALIDATION_ERROR");
        error.setMessage(e.getMessage());
        error.setTimestamp(LocalDateTime.now());
        
        return ResponseEntity.badRequest().body(error);
    }
}
```

**Erreurs d'Accès :**
```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
    ErrorResponse error = new ErrorResponse();
    error.setStatus("ACCESS_DENIED");
    error.setMessage("Vous n'avez pas les droits pour cette action");
    
    return ResponseEntity.status(403).body(error);
}
```

### 9.2 Codes d'Erreur Standardisés

| Code | Description | Action utilisateur |
|------|-------------|-------------------|
| 400 | Données invalides | Corriger le formulaire |
| 401 | Non authentifié | Se reconnecter |
| 403 | Accès refusé | Contacter l'admin |
| 404 | Ressource non trouvée | Vérifier l'URL |
| 409 | Conflit de données | Actualiser la page |
| 500 | Erreur serveur | Réessayer plus tard |

---

## 10. Monitoring et Logs

### 10.1 Logs Applicatifs

**Configuration Logback :**
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="org.example.eurekaback.Service" level="INFO"/>
    <logger name="org.example.eurekaback.Controller" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

**Exemples de logs :**
```java
@Slf4j
@Service
public class CampagneServiceImpl {
    
    public Campagne createCampagne(CampagneRequest request, Annonceur annonceur) {
        log.info("Création campagne '{}' pour annonceur ID: {}", 
                request.getNom(), annonceur.getId());
        
        try {
            Campagne campagne = // logique de création
            log.info("Campagne ID: {} créée avec succès", campagne.getId());
            return campagne;
        } catch (Exception e) {
            log.error("Erreur création campagne pour annonceur {}: {}", 
                     annonceur.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

### 10.2 Métriques de Performance

**Spring Boot Actuator :**
```properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
management.metrics.enable.jvm=true
management.metrics.enable.http=true
```

**Métriques surveillées :**
- Temps de réponse des endpoints
- Nombre de campagnes créées par heure
- Taux d'erreur par type
- Utilisation mémoire et CPU

---

## 11. Déploiement et Configuration

### 11.1 Profils d'Environnement

**application-dev.properties :**
```properties
# Base de données de développement
spring.datasource.url=jdbc:postgresql://localhost:5432/eureka_dev
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Logs en mode debug
logging.level.org.example.eurekaback=DEBUG
```

**application-prod.properties :**
```properties
# Base de données de production
spring.datasource.url=${DATABASE_URL}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Sécurité renforcée
server.error.include-stacktrace=never
logging.level.org.example.eurekaback=INFO
```

### 11.2 Docker Configuration

**Dockerfile :**
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/eureka-back-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml :**
```yaml
version: '3.8'
services:
  eureka-back:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://postgres:5432/eureka
    depends_on:
      - postgres
      
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: eureka
      POSTGRES_USER: eureka_user
      POSTGRES_PASSWORD: eureka_pass
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## 12. Conclusion et Perspectives

### 12.1 Objectifs du Sprint 2 Atteints

**✅ Fonctionnalités livrées :**
- CRUD complet des campagnes avec sécurité par rôles
- Création automatique des produits et formulaires associés
- Interface complète de gestion des questions de formulaire
- Prévisualisation des formulaires pour les panelistes
- Gestion des statuts de campagne avec workflow contrôlé
- API REST documentée et testée

**✅ Qualité technique :**
- Architecture en couches respectée
- Tests unitaires et d'intégration complets (> 90% de couverture)
- Sécurité robuste avec JWT et RBAC
- Gestion d'erreurs exhaustive
- Monitoring et logs opérationnels

**✅ Performance :**
- Temps de réponse API < 500ms (95e percentile)
- Base de données optimisée avec index appropriés
- Pagination efficace pour les grandes listes
- Validation des données côté client et serveur

### 12.2 Métriques de Réussite

| Indicateur | Objectif | Réalisé | Statut |
|------------|----------|---------|--------|
| Endpoints API | 15 | 17 | ✅ Dépassé |
| Couverture tests | 85% | 92% | ✅ Dépassé |
| Temps réponse moyen | < 300ms | 180ms | ✅ Atteint |
| Zéro vulnérabilité | 0 | 0 | ✅ Atteint |

### 12.3 Retours d'Expérience

**Points forts :**
- Architecture modulaire facilitant les évolutions
- Workflow de création intuitive et fluide
- Sécurité robuste sans impact sur les performances
- Tests automatisés garantissant la qualité

**Améliorations identifiées :**
- Cache Redis pour optimiser les requêtes fréquentes
- API GraphQL pour les besoins complexes du frontend
- Monitoring temps réel avec tableaux de bord
- Documentation utilisateur interactive

### 12.4 Préparation Sprint 3 : Participation des Panelistes

**Dépendances résolues par Sprint 2 :**
- ✅ Structure campagne-formulaire-questions opérationnelle
- ✅ Système d'authentification et autorisation fonctionnel
- ✅ Gestion des statuts préparant l'ouverture aux panelistes
- ✅ API REST prête pour l'intégration mobile

**Fonctionnalités prévues Sprint 3 :**
- **Inscription panelistes aux campagnes** : Système de matching automatique
- **Réponse aux formulaires** : Interface mobile-friendly de participation
- **Collecte des feedbacks** : Stockage et première analyse des réponses
- **Notifications temps réel** : Alertes campagnes, rappels participation
- **Tableau de bord annonceur** : Suivi en temps réel des participations

**Défis techniques identifiés :**
- **Concurrence** : Gestion des participations simultanées
- **Performance mobile** : Optimisation pour réseaux lents
- **Analytics temps réel** : Traitement des données en flux
- **Scalabilité** : Support de milliers de panelistes simultanés

### 12.5 Recommandations pour la Suite

**Architecture :**
1. **Mise en place d'un cache Redis** pour les données fréquemment consultées
2. **Message Queue (RabbitMQ)** pour les notifications asynchrones
3. **CDN** pour l'optimisation des images et fichiers statiques
4. **Load balancer** pour la répartition de charge

**Qualité :**
1. **Tests de charge** avec JMeter sur les scénarios critiques
2. **Security audit** périodique avec OWASP ZAP
3. **Code review** systématique avec SonarQube
4. **Documentation API** interactive avec Swagger UI

**Monitoring :**
1. **APM (Application Performance Monitoring)** avec Micrometer
2. **Logs centralisés** avec ELK Stack
3. **Alertes proactives** sur métriques critiques
4. **Dashboards** temps réel pour l'équipe opérationnelle

---

**Rapport rédigé par l'équipe Backend Eureka**  
**Date de finalisation :** 30 Août 2025  
**Version :** 3.1 - Sprint 2 Complet  
**Statut :** ✅ VALIDÉ POUR PRODUCTION  
**Classification :** Document technique interne

---

**Signatures et Validations :**
- **Lead Developer Backend :** ✅ Validé
- **Architecte Technique :** ✅ Validé  
- **Responsable Sécurité :** ✅ Validé
- **Product Owner :** ✅ Validé
- **Scrum Master :** ✅ Sprint 2 clos - Sprint 3 initialisé
