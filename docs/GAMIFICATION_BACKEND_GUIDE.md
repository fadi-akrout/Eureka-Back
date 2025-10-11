# 🎮 Système de Gamification Backend - Guide Frontend

## Table des Matières
1. [Vue d'ensemble](#vue-densemble)
2. [Système de Niveaux](#système-de-niveaux)
3. [Système de Badges](#système-de-badges)
4. [Système de Récompenses](#système-de-récompenses)
5. [API Endpoints](#api-endpoints)
6. [Types TypeScript](#types-typescript)
7. [Modèles de Données](#modèles-de-données)
8. [Intégration Frontend](#intégration-frontend)
9. [Exemples d'Utilisation](#exemples-dutilisation)

---

## Vue d'ensemble

Le système de gamification est **100% basé sur la participation**. Tous les éléments (niveaux, badges, récompenses) dépendent uniquement du nombre de campagnes auxquelles un utilisateur a participé.

### Principes Clés
- **Simple & Transparent** : Seul le nombre de participations compte
- **Pas de Calculs Complexes** : Aucun point, bonus ou multiplicateur
- **Progression Claire** : Les utilisateurs savent exactement ce qu'ils doivent faire
- **Mises à Jour Automatiques** : Le système se met à jour lors de l'enregistrement des participations

---

## Système de Niveaux

### Niveaux Disponibles

| Niveau | Participations Requises | Nom d'Affichage | Couleur | Icône |
|--------|------------------------|-----------------|---------|-------|
| `BRONZE` | 0 | Bronze | #CD7F32 | fas fa-award |
| `ARGENT` | 3 | Silver | #C0C0C0 | fas fa-medal |
| `OR` | 10 | Gold | #FFD700 | fas fa-trophy |
| `DIAMANT` | 25 | Diamond | #B9F2FF | fas fa-gem |

### Calcul de Progression

```typescript
// Exemples de progression par niveau
BRONZE (0-2 participations): 0% à 100% (2/3 * 100 = 66.67%)
ARGENT (3-9 participations): 0% à 100% ((5-3)/7 * 100 = 28.57%)
OR (10-24 participations): 0% à 100% ((15-10)/15 * 100 = 33.33%)
DIAMANT (25+ participations): 100% (niveau maximum)
```

---

## Système de Badges

### Badges Disponibles

| ID | Nom | Description | Participations | Icône |
|---|-----|-------------|----------------|-------|
| `first_participation` | First Steps | Complete your first campaign | 1 | fas fa-baby |
| `regular_participant` | Regular Participant | Complete 5 campaigns | 5 | fas fa-user-check |
| `dedicated_tester` | Dedicated Tester | Complete 10 campaigns | 10 | fas fa-star |
| `expert_reviewer` | Expert Reviewer | Complete 15 campaigns | 15 | fas fa-crown |
| `campaign_master` | Campaign Master | Complete 25 campaigns | 25 | fas fa-trophy |
| `legend` | Legend | Complete 50 campaigns | 50 | fas fa-dragon |

### Structure Badge DTO

```typescript
interface BadgeDTO {
  id: string;
  name: string;
  description: string;
  requiredParticipations: number;
  iconClass: string;
  isUnlocked: boolean;
  unlockedAt?: string; // ISO date string
}
```

---

## Système de Récompenses

### Récompenses Disponibles

| ID | Nom | Description | Participations | Valeur | Devise |
|---|-----|-------------|----------------|--------|--------|
| `welcome_bonus` | Welcome Bonus | 5€ credit for first participation | 1 | 5.0 | EUR |
| `loyalty_reward` | Loyalty Reward | 10€ credit for being regular | 5 | 10.0 | EUR |
| `dedication_prize` | Dedication Prize | 20€ credit for dedication | 10 | 20.0 | EUR |
| `expert_bonus` | Expert Bonus | 30€ credit for expertise | 15 | 30.0 | EUR |
| `master_reward` | Master Reward | 50€ credit for mastery | 25 | 50.0 | EUR |
| `legendary_prize` | Legendary Prize | 100€ credit for legend status | 50 | 100.0 | EUR |

### Structure Reward DTO

```typescript
interface RewardDTO {
  id: string;
  name: string;
  description: string;
  requiredParticipations: number;
  value: number;
  currency: string;
  isUnlocked: boolean;
  isClaimed: boolean;
  unlockedAt?: string; // ISO date string
  claimedAt?: string; // ISO date string
}
```

---

## API Endpoints

### 1. Récupérer le Profil de Gamification

```http
GET /api/gamification/profile
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "userId": 123,
  "participationsCount": 7,
  "level": "ARGENT",
  "currentLevelProgress": 57.14,
  "nextLevelThreshold": 10,
  "participationsToNextLevel": 3,
  "badges": [
    {
      "id": "first_participation",
      "name": "First Steps",
      "description": "Complete your first campaign",
      "requiredParticipations": 1,
      "iconClass": "fas fa-baby",
      "isUnlocked": true,
      "unlockedAt": "2025-01-15T10:30:00Z"
    }
  ],
  "rewards": [
    {
      "id": "welcome_bonus",
      "name": "Welcome Bonus",
      "description": "5€ credit for first participation",
      "requiredParticipations": 1,
      "value": 5.0,
      "currency": "EUR",
      "isUnlocked": true,
      "isClaimed": false,
      "unlockedAt": "2025-01-15T10:30:00Z"
    }
  ]
}
```

### 2. Récupérer le Leaderboard

```http
GET /api/gamification/leaderboard?limit=50&offset=0
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "leaderboard": [
    {
      "userId": 456,
      "username": "john_doe",
      "participationsCount": 42,
      "level": "DIAMANT",
      "avatarUrl": null,
      "rank": 1
    }
  ],
  "currentUser": {
    "userId": 123,
    "rank": 15,
    "participationsCount": 7
  },
  "totalUsers": 250
}
```

### 3. Enregistrer une Participation

```http
POST /api/gamification/participation
Authorization: Bearer {token}
Content-Type: application/json

{
  "campaignId": 456
}
```

**Réponse (avec level up) :**
```json
{
  "success": true,
  "newParticipationCount": 10,
  "levelChanged": true,
  "oldLevel": "ARGENT",
  "newLevel": "OR",
  "newlyUnlockedBadges": [
    {
      "id": "dedicated_tester",
      "name": "Dedicated Tester",
      "description": "Complete 10 campaigns",
      "requiredParticipations": 10,
      "iconClass": "fas fa-star",
      "isUnlocked": true,
      "unlockedAt": "2025-03-01T14:30:00Z"
    }
  ],
  "newlyUnlockedRewards": [
    {
      "id": "dedication_prize",
      "name": "Dedication Prize",
      "description": "20€ credit for dedication",
      "requiredParticipations": 10,
      "value": 20.0,
      "currency": "EUR",
      "isUnlocked": true,
      "isClaimed": false,
      "unlockedAt": "2025-03-01T14:30:00Z"
    }
  ],
  "message": "Congratulations! Level up to Gold!"
}
```

### 4. Réclamer une Récompense

```http
POST /api/gamification/rewards/{rewardId}/claim
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "success": true,
  "reward": {
    "id": "welcome_bonus",
    "name": "Welcome Bonus",
    "value": 5.0,
    "currency": "EUR",
    "isUnlocked": true,
    "isClaimed": true,
    "claimedAt": "2025-03-01T09:15:00Z"
  },
  "message": "Reward claimed successfully"
}
```

---

## Types TypeScript

### Interface Principale du Profil

```typescript
export interface GamificationProfileDTO {
  userId: number;
  participationsCount: number;
  level: GamificationLevel;
  currentLevelProgress: number;
  nextLevelThreshold: number | null;
  participationsToNextLevel: number;
  badges: BadgeDTO[];
  rewards: RewardDTO[];
}

export enum GamificationLevel {
  BRONZE = 'BRONZE',
  ARGENT = 'ARGENT',
  OR = 'OR',
  DIAMANT = 'DIAMANT'
}

export interface BadgeDTO {
  id: string;
  name: string;
  description: string;
  requiredParticipations: number;
  iconClass: string;
  isUnlocked: boolean;
  unlockedAt?: string;
}

export interface RewardDTO {
  id: string;
  name: string;
  description: string;
  requiredParticipations: number;
  value: number;
  currency: string;
  isUnlocked: boolean;
  isClaimed: boolean;
  unlockedAt?: string;
  claimedAt?: string;
}

export interface LeaderboardDTO {
  leaderboard: LeaderboardEntry[];
  currentUser: CurrentUserPosition | null;
  totalUsers: number;
}

export interface LeaderboardEntry {
  userId: number;
  username: string;
  participationsCount: number;
  level: GamificationLevel;
  avatarUrl?: string;
  rank: number;
}

export interface CurrentUserPosition {
  userId: number;
  rank: number;
  participationsCount: number;
}

export interface ParticipationResult {
  success: boolean;
  newParticipationCount: number;
  levelChanged: boolean;
  oldLevel?: GamificationLevel;
  newLevel?: GamificationLevel;
  newlyUnlockedBadges: BadgeDTO[];
  newlyUnlockedRewards: RewardDTO[];
  message: string;
}

export interface RewardClaimResult {
  success: boolean;
  reward?: RewardDTO;
  message: string;
}

export interface ParticipationRequest {
  campaignId: number;
}
```

---

## Modèles de Données

### Base de Données

Le système utilise 4 tables principales :

1. **user_gamification_profiles** : Profils utilisateur avec niveau et compteur
2. **user_badges** : Badges débloqués par utilisateur
3. **user_rewards** : Récompenses débloquées et réclamées par utilisateur
4. **gamification_events** : Journal des événements (pour analytics)

### Événements Automatiques

Le système déclenche automatiquement des événements lors de :
- Complétion d'une campagne
- Changement de niveau
- Déblocage de badge
- Déblocage de récompense

---

## Intégration Frontend

### Service Angular

```typescript
@Injectable({
  providedIn: 'root'
})
export class GamificationService {
  private apiUrl = '/api/gamification';

  constructor(private http: HttpClient) {}

  getProfile(): Observable<GamificationProfileDTO> {
    return this.http.get<GamificationProfileDTO>(`${this.apiUrl}/profile`);
  }

  getLeaderboard(limit = 50, offset = 0): Observable<LeaderboardDTO> {
    return this.http.get<LeaderboardDTO>(`${this.apiUrl}/leaderboard`, {
      params: { limit: limit.toString(), offset: offset.toString() }
    });
  }

  recordParticipation(campaignId: number): Observable<ParticipationResult> {
    return this.http.post<ParticipationResult>(`${this.apiUrl}/participation`, {
      campaignId
    });
  }

  claimReward(rewardId: string): Observable<RewardClaimResult> {
    return this.http.post<RewardClaimResult>(`${this.apiUrl}/rewards/${rewardId}/claim`, {});
  }
}
```

### Composant Profile

```typescript
@Component({
  selector: 'app-gamification-profile',
  template: `
    <div class="gamification-profile" *ngIf="profile">
      <!-- Niveau actuel -->
      <div class="level-display">
        <i [class]="getLevelIcon()" [style.color]="getLevelColor()"></i>
        <h3>{{ getLevelDisplayName() }}</h3>
        <div class="progress-bar">
          <div class="progress" [style.width.%]="profile.currentLevelProgress"></div>
        </div>
        <p>{{ profile.participationsToNextLevel }} participations jusqu'au prochain niveau</p>
      </div>

      <!-- Badges -->
      <div class="badges-section">
        <h4>Badges ({{ getUnlockedBadgesCount() }}/{{ profile.badges.length }})</h4>
        <div class="badges-grid">
          <div *ngFor="let badge of profile.badges" 
               class="badge-item" 
               [class.unlocked]="badge.isUnlocked">
            <i [class]="badge.iconClass"></i>
            <span>{{ badge.name }}</span>
          </div>
        </div>
      </div>

      <!-- Récompenses -->
      <div class="rewards-section">
        <h4>Récompenses</h4>
        <div *ngFor="let reward of getAvailableRewards()" class="reward-item">
          <span>{{ reward.name }} - {{ reward.value }}{{ reward.currency }}</span>
          <button *ngIf="!reward.isClaimed" 
                  (click)="claimReward(reward.id)"
                  class="claim-btn">
            Réclamer
          </button>
          <span *ngIf="reward.isClaimed" class="claimed">✓ Réclamé</span>
        </div>
      </div>
    </div>
  `
})
export class GamificationProfileComponent implements OnInit {
  profile?: GamificationProfileDTO;

  constructor(private gamificationService: GamificationService) {}

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.gamificationService.getProfile().subscribe(profile => {
      this.profile = profile;
    });
  }

  getLevelIcon(): string {
    const icons = {
      BRONZE: 'fas fa-award',
      ARGENT: 'fas fa-medal',
      OR: 'fas fa-trophy',
      DIAMANT: 'fas fa-gem'
    };
    return icons[this.profile!.level];
  }

  getLevelColor(): string {
    const colors = {
      BRONZE: '#CD7F32',
      ARGENT: '#C0C0C0',
      OR: '#FFD700',
      DIAMANT: '#B9F2FF'
    };
    return colors[this.profile!.level];
  }

  getLevelDisplayName(): string {
    const names = {
      BRONZE: 'Bronze',
      ARGENT: 'Silver',
      OR: 'Gold',
      DIAMANT: 'Diamond'
    };
    return names[this.profile!.level];
  }

  getUnlockedBadgesCount(): number {
    return this.profile?.badges.filter(b => b.isUnlocked).length || 0;
  }

  getAvailableRewards(): RewardDTO[] {
    return this.profile?.rewards.filter(r => r.isUnlocked) || [];
  }

  claimReward(rewardId: string) {
    this.gamificationService.claimReward(rewardId).subscribe(result => {
      if (result.success) {
        this.loadProfile(); // Recharger le profil
        // Afficher notification de succès
      }
    });
  }
}
```

---

## Exemples d'Utilisation

### 1. Affichage de Notifications de Level Up

```typescript
// Après complétion d'une campagne
completeCampaign(campaignId: number) {
  this.campaignService.complete(campaignId).pipe(
    switchMap(() => this.gamificationService.recordParticipation(campaignId))
  ).subscribe(result => {
    if (result.levelChanged) {
      this.showLevelUpNotification(result.newLevel!, result.message);
    }
    
    if (result.newlyUnlockedBadges.length > 0) {
      this.showBadgeNotifications(result.newlyUnlockedBadges);
    }
    
    if (result.newlyUnlockedRewards.length > 0) {
      this.showRewardNotifications(result.newlyUnlockedRewards);
    }
  });
}
```

### 2. Widget de Progression

```typescript
@Component({
  selector: 'app-progress-widget',
  template: `
    <div class="progress-widget" *ngIf="profile">
      <div class="current-level">
        <i [class]="getLevelIcon()" [style.color]="getLevelColor()"></i>
        <span>{{ getLevelDisplayName() }}</span>
      </div>
      <div class="progress-info">
        <div class="progress-bar">
          <div class="fill" [style.width.%]="profile.currentLevelProgress"></div>
        </div>
        <span>{{ profile.participationsCount }} participations</span>
        <span *ngIf="profile.participationsToNextLevel > 0">
          {{ profile.participationsToNextLevel }} restantes
        </span>
      </div>
    </div>
  `
})
export class ProgressWidgetComponent {
  @Input() profile?: GamificationProfileDTO;
  
  // Mêmes méthodes que le composant profile
}
```

### 3. Leaderboard

```typescript
@Component({
  selector: 'app-leaderboard',
  template: `
    <div class="leaderboard" *ngIf="leaderboard">
      <div class="current-user" *ngIf="leaderboard.currentUser">
        <strong>Votre position: #{{ leaderboard.currentUser.rank }}</strong>
      </div>
      
      <div class="rankings">
        <div *ngFor="let entry of leaderboard.leaderboard" 
             class="rank-entry"
             [class.current-user]="entry.userId === currentUserId">
          <span class="rank">#{{ entry.rank }}</span>
          <span class="username">{{ entry.username }}</span>
          <span class="level">{{ entry.level }}</span>
          <span class="participations">{{ entry.participationsCount }}</span>
        </div>
      </div>
    </div>
  `
})
export class LeaderboardComponent implements OnInit {
  leaderboard?: LeaderboardDTO;
  currentUserId?: number;

  constructor(private gamificationService: GamificationService) {}

  ngOnInit() {
    this.gamificationService.getLeaderboard().subscribe(leaderboard => {
      this.leaderboard = leaderboard;
      this.currentUserId = leaderboard.currentUser?.userId;
    });
  }
}
```

---

## Notes Importantes

### 1. Déclenchement Automatique
- Le système se met à jour automatiquement lors de la complétion des campagnes
- Aucune intervention manuelle n'est requise pour les mises à jour de gamification

### 2. Cohérence des Données
- Les compteurs de participation sont synchronisés avec la table `PanelisteCampagne`
- Le système vérifie et corrige automatiquement les incohérences

### 3. Performance
- Les API sont optimisées avec des index appropriés
- Le traitement des événements est asynchrone
- Possibilité d'ajouter du cache si nécessaire

### 4. Extensibilité
- Facile d'ajouter de nouveaux badges ou récompenses
- Structure modulaire permettant d'étendre les fonctionnalités
- Events system pour intégrer d'autres fonctionnalités

---

Cette documentation fournit toutes les informations nécessaires pour intégrer le système de gamification dans votre frontend Angular. Le système est maintenant entièrement fonctionnel et prêt à être utilisé !
