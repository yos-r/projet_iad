# 🏭 FESTO CP Factory - RLRA Multi-Agent System

**Auteurs:** Yosr Barghouti / Eya Belkadhi 3IDL2
**Date:** Décembre 2025  
**Projet:** Intelligence Artificielle Distribuée (IAD) - Mini-projet  

---

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture du système](#architecture-du-système)
3. [Installation et configuration](#installation-et-configuration)
4. [Exécution du projet](#exécution-du-projet)
5. [Description des architectures RLRA](#description-des-architectures-rlra)
6. [Modification et améliorations apportées](#modifications-et-améliorations-apportées)
7. [Scénarios et cas d'usage](#scénarios-et-cas-dusage)
8. [Résultats et sortie console](#résultats-et-sortie-console)

---

## 🎯 Vue d'ensemble

### Qu'est-ce que ce projet?

Ce projet simule une **usine intelligente (Smart Factory)** utilisant un système **multi-agents distribué** basé sur le framework **JADE**. Il démontre comment les agents peuvent **communiquer, prendre des décisions collectives et se reconfigurer dynamiquement** en réponse à des défaillances et des changements de demande.

### Objectif pédagogique

- Comprendre les **systèmes multi-agents distribués**
- Implémenter des **architectures de décision** (centralisée, modulaire, distribuée)
- Démontrer la **communication inter-agents** via messages FIPA-ACL
- Gérer les **conflits et résolutions** au niveau global
- Simuler des **scénarios complexes** (défaillances machines, pics de production, changements de produits)

### Points clés du système

✅ **3 Architectures RLRA** (Reconfiguration Learning Response Agent)  
✅ **Système de messagerie ACL** pour communication inter-agents  
✅ **6 scénarios d'interaction** entre agents  
✅ **4 scénarios avancés** (pics de production, changements produits, dégradation gracieuse, ruptures supply chain)  
✅ **Agent de transport** gérant les buffers et la logistique  
✅ **Superviseur global** pour résolution des conflits

---

## 🏗️ Architecture du système

### Structure générale

```
FESTO CP Factory
├── 4 Machines (M1, M2, M3, M4)
├── 2 Moniteurs (Monitor_SiteA, Monitor_SiteB)
├── 1 Agent Transport (T1_Transport)
├── 3 Architectures RLRA:
│   ├── CENTRALIZED: Contrôle centralisé unique
│   ├── MODULAR: Séparation Monitor/Learner/Executor
│   └── DISTRIBUTED: Coordinateurs locaux + Superviseur global
└── Message Broker: Routage centralisé des messages
```

### Machines et flux

```
SITE A                           SITE B
┌──────────────────┐            ┌──────────────────┐
│ M1_Distribution  │            │ M3_Assembly      │
│ (2s cycle)       │            │ (3s cycle)       │
└────────┬─────────┘            └────────┬─────────┘
         │                               │
         ▼                               ▼
┌──────────────────┐            ┌──────────────────┐
│ M2_Machining     │  TRANSPORT │ M4_QualityControl│
│ (5s cycle)       ├───────────►│ (2s cycle)       │
└──────────────────┘  Buffer=20 └──────────────────┘
```

### Communication FIPA-ACL

Le système utilise le protocole **FIPA-ACL** (Foundation for Intelligent Physical Agents) :

```
[ACL] FROM sender TO receiver TYPE performative CONTENT "message"

Performatives utilisés:
- RECONFIGURATION_REQUEST : Demande de reconfiguration
- RECONFIGURATION_PLAN : Plan d'action
- CONFLICT_REPORT : Rapport de conflit
- CONFLICT_RESOLUTION : Résolution globale
- INFORM : Information générale
```

---

## 💻 Installation et configuration

### Prérequis

- **Java 8+** avec UTF-8 encoding
- **JADE Framework** (lib/jade.jar fourni)
- **PowerShell ou Command Prompt**

### Structure des fichiers

```
projet_iad/
├── src/                          # Tous les fichiers Java source
│   ├── App.java                  # Point d'entrée principal
│   ├── FactorySimulation.java    # Simulation d'usine
│   ├── BaseAgent.java            # Classe de base pour tous les agents
│   ├── RLRACentralized.java      # Architecture centralisée
│   ├── RLRAModular.java          # Architecture modulaire
│   ├── RLRADistributed.java      # Architecture distribuée
│   ├── RLRAFactory.java          # Factory pattern pour créer RLRA
│   ├── MachineAgent.java         # Agent machine
│   ├── MonitorAgent.java         # Agent moniteur
│   ├── TransportAgent.java       # Agent de transport
│   ├── MessageBroker.java        # Routeur de messages
│   ├── Message.java              # Structure de message
│   ├── MachineState.java         # État des machines
│   ├── ACLMessageLogger.java     # Utilitaire de logging ACL
│   ├── InteractionScenarios.java # Scénarios d'interaction
│   ├── ReconfigurationScenarios.java # Scénarios avancés
│   ├── ConveyorAgent.java        # Agent convoyeur (optionnel)
│   └── AssemblyAgent.java        # Agent d'assemblage (optionnel)
├── lib/
│   └── jade.jar                  # JADE Framework
├── bin/                          # Fichiers compilés (.class)
├── README.md                     # Documentation générale
├── README_RLRA.md               # Documentation architecture RLRA
└── README_FINAL.md              # Ce fichier (guide complet)
```

### Compilation

```powershell
cd c:\chemin\vers\projet_iad
javac -encoding UTF-8 -cp "lib\jade.jar" -d bin src\*.java
```

**Résultat attendu:** Compilation sans erreurs (quelques warnings JADE)

---

## 🚀 Exécution du projet

### Exécution complète (mode par défaut)

```powershell
cd c:\chemin\vers\projet_iad
java -cp "bin;lib\jade.jar" App
```

**Sortie:** Démonstration complète (~767 lignes de logs)

### Modes d'exécution alternatifs

```powershell
# Affiche seulement les 3 architectures RLRA
java -cp "bin;lib\jade.jar" App arch

# Affiche seulement les 6 scénarios d'interaction
java -cp "bin;lib\jade.jar" App interactions

# Affiche seulement les 4 scénarios avancés
java -cp "bin;lib\jade.jar" App scenarios

# Aide
java -cp "bin;lib\jade.jar" App help
```

### Filtrer la sortie (exemples utiles)

```powershell
# Voir seulement les logs ACL
java -cp "bin;lib\jade.jar" App 2>&1 | Select-String "ACL"

# Voir seulement la partie DISTRIBUTED
java -cp "bin;lib\jade.jar" App 2>&1 | Select-String "DISTRIBUTED|SCENARIO|Supervisor|Coordinator"

# Voir seulement les décisions (LEARNER, EXECUTOR, GLOBAL DECISION)
java -cp "bin;lib\jade.jar" App 2>&1 | Select-String "Learner|Executor|GLOBAL DECISION"

# Sauvegarder dans un fichier
java -cp "bin;lib\jade.jar" App 2>&1 > sortie.txt
```

---

## 🏛️ Description des architectures RLRA

### 1. CENTRALIZED (Architecture Centralisée)

**Principe:**
- Un **seul agent RLRA** central prend TOUTES les décisions
- Les moniteurs remontent les alertes → RLRA décide → Exécute

**Avantages:**
- ✅ Simple à implémenter
- ✅ Décisions cohérentes et non conflictuelles
- ✅ Vue globale de l'usine

**Inconvénients:**
- ❌ Point unique de défaillance
- ❌ Pas scalable (surcharge avec plusieurs usines)
- ❌ Latence élevée

**Logs caractéristiques:**
```
[RLRA_Main] Processing reconfiguration request for M2_Machining
[RLRA_Main] Executing REASSIGNMENT strategy for M2_Machining
[RLRA_Main] Reassigning work to M3_Assembly
```

---

### 2. MODULAR (Architecture Modulaire - Composite)

**Principe:**
- Un agent RLRA contient **3 modules internes**:
  - **Monitor Module:** Reçoit les alertes des moniteurs
  - **Learner Module:** Analyse les scénarios, prend des décisions
  - **Executor Module:** Exécute les plans décidés

**Avantages:**
- ✅ Séparation des préoccupations (Monitor/Learner/Executor)
- ✅ Plus testable que monolithique
- ✅ Meilleure réutilisabilité

**Inconvénients:**
- ❌ Still centralized (un seul agent)
- ❌ Complexité interne accrue

**Logs caractéristiques:**
```
[ACL] FROM Monitor_SiteB TO RLRA_Main TYPE REQUEST CONTENT 'M3_Assembly'
[RLRA_Main_Monitor] Received request: M3_Assembly
[RLRA_Main_Learner] Detected scenario: MACHINE_FAILURE
[RLRA_Main_Learner] Final decision: STRATEGY_REASSIGNMENT
[RLRA_Main_Executor] Starting execution of plan: STRATEGY_REASSIGNMENT
[RLRA_Main_Executor] Reassigning work to M2_Machining
```

---

### 3. DISTRIBUTED (Architecture Distribuée - Hiérarchique)

**Principe:**
- **Coordinateurs locaux** (un par site) prennent les décisions LOCALES
- **Superviseur global** résout les CONFLITS inter-sites
- Communication hiérarchique: Local → Global → Local

**Flux décisionnel:**

```
1. LOCAL DECISION
   Monitor → SiteCoordinator
   SiteCoordinator analyse ressources locales
   Si solvable localement → Exécute

2. CONFLICT DETECTION
   Si conflit local → Escalade au Supervisor
   
3. GLOBAL DECISION PROCESS
   Supervisor analyse état global
   Supervisor cherche solutions inter-sites
   
4. RESOLUTION EXECUTION
   Coordinator applique décision globale
```

**Avantages:**
- ✅ Très scalable (ajouter des sites)
- ✅ Résilience (pas de point unique de défaillance)
- ✅ Meilleure performance (décisions locales rapides)
- ✅ Réaliste pour usines distribuées

**Inconvénients:**
- ❌ Plus complexe à implémenter
- ❌ Risques de conflits non résolus
- ❌ Latence de communication élevée

**Logs caractéristiques:**
```
[ACL] FROM Monitor_SiteA TO RLRA_Main TYPE REQUEST CONTENT 'M1_Distribution'

[SITE_A_Coordinator] ===== LOCAL DECISION PROCESS =====
[SITE_A_Coordinator] Proposed local strategy: STRATEGY_LOCAL_BYPASS
[SITE_A_Coordinator] ? LOCAL CONFLICT DETECTED
[SITE_A_Coordinator] Cannot solve locally → Escalating to Supervisor

[ACL] FROM SITE_A_Coordinator TO Supervisor TYPE CONFLICT_REPORT

[RLRA_Main_Supervisor] ===== GLOBAL DECISION PROCESS =====
[RLRA_Main_Supervisor] Checking alternative sites...
[RLRA_Main_Supervisor] Final global decision: GLOBAL_BYPASS: REASSIGN_TO_SITE_B

[ACL] FROM Supervisor TO SITE_A_Coordinator TYPE CONFLICT_RESOLUTION

[SITE_A_Coordinator] Received Supervisor resolution
[SITE_A_Coordinator] ? Applying global decision...
[SITE_A_Coordinator] ✓ Global decision applied
```

---

## 🔧 Modifications et améliorations apportées

### Modifications principales par Eya Ben El Kadhi

#### 1. **ACLMessageLogger.java** (Nouveau)

**But:** Standardiser les logs de communication inter-agents

**Méthodes:**
```java
logMessage(fromAgent, toAgent, performative, content)
logReconfigurationRequest(fromAgent, toAgent, machine)
logReconfigurationPlan(fromAgent, toAgent, strategy, machine)
logConflictReport(fromAgent, toAgent, machine, reason)
logConflictResolution(fromAgent, toAgent, resolution)
```

**Exemple:**
```
[ACL] FROM Monitor_SiteA TO RLRA_Main TYPE RECONFIGURATION_REQUEST CONTENT "FAILURE M1_Distribution"
```

---

#### 2. **RLRADistributed.java** (Améliorations)

**Supervisor.handleConflictReport()**
- Reçoit et traite les rapports de conflit
- Affiche "Received CONFLICT_REPORT from..."
- Stocke l'historique des résolutions

**Supervisor.resolveConflict()**
- Amélioration: Affichage détaillé du processus GLOBAL DECISION
- Cherche les sites alternatifs
- Retourne la meilleure résolution

**SiteCoordinator.handleConflictResolution()**
- Reçoit et APPLIQUE la décision du Supervisor
- Affiche "Applying global decision..."
- Enregistre dans l'historique de reconfiguration

**SiteCoordinator.reportConflict()**
- Appelle `ACLMessageLogger.logConflictReport()`
- Envoie message CONFLICT_REPORT au Supervisor

**Ajout de reconfigurationHistory**
- Enregistre toutes les décisions (local + global)
- Utile pour audit et debug

---

#### 3. **MessageBroker.java** (Correction critique)

**Problème identifié:**
Après la première simulation, `messageBroker.stop()` appelait `running = false`, empêchant les simulations suivantes

**Solution:**
```java
public void restart() {
    running = true;  // Réactive le broker
    mailboxes.clear();
    messageLog.clear();
}
```

**Utilisation dans App.java:**
```java
demonstrateCentralizedArchitecture();
MessageBroker.getInstance().restart();

demonstrateModularArchitecture();
MessageBroker.getInstance().restart();

demonstrateDistributedArchitecture();
```

---

### Résumé des changements

| Fichier | Change | Description |
|---------|--------|-------------|
| ACLMessageLogger.java | ✅ CRÉÉ | Logging ACL standardisé |
| RLRADistributed.java | ✅ MODIFIÉ | Supervisor + Coordinator améliorés |
| MessageBroker.java | ✅ MODIFIÉ | Ajout restart() |
| App.java | ✅ MODIFIÉ | Appels restart() entre architectures |
| Tous les autres | ✅ INCHANGÉS | Aucun changement cassant |

---

## 📚 Scénarios et cas d'usage

### Scénarios d'interaction (6 total)

1. **Simple Failure Detection and Notification**
   - Défaillance machine
   - Notification du moniteur
   - Réponse RLRA

2. **Multi-Site Coordination**
   - Défaillance à Site A
   - Décision locale suffisante
   - Pas escalade

3. **Conflict Resolution**
   - Défaillances multiples simultanées
   - Conflit local
   - Escalade au Supervisor
   - Résolution globale

4. **Sequential Dependencies Management**
   - Dégradation progressive d'une machine
   - Gestion du flux (load balancing)
   - Prévention de queue overflow

5. **Resource Sharing and Arbitration**
   - Conflit pour ressource partagée
   - Arbitrage par priorité/demand
   - Allocation juste

6. **Cascading Failures**
   - Défaillance initiale
   - Défaillances secondaires
   - Récupération intelligente

### Scénarios avancés (4 total)

**A. Production Peak - Handling High Demand**
- 325 units demandées en 15 minutes
- Mode ACCELERATION (augmenter vitesses, paralléliser)
- Gestion des ressources critiques

**B. Product Change - From Alpha to Beta**
- Changement de produit (Alpha → Beta)
- Reprogrammation des machines
- Gestion de la transition

**C. Graceful Degradation - Progressive Failure Management**
- Défaillance progressive (8h avertissement)
- Maintien production pendant réparation
- Minimisation de l'impact

**D. Component Supply Chain Failure**
- Rupture fournisseur (4h sans moteurs)
- Activation supplier alternatif
- Réduction production temporaire

---

## 📊 Résultats et sortie console

### Exemple de sortie complète

```
╔══════════════════════════════════════════════════════════╗
║        FESTO CP Factory - Complete Demo                 ║
╚══════════════════════════════════════════════════════════╝

1. Running RLRA Architecture Demonstrations

1. CENTRALIZED RLRA ARCHITECTURE
[RLRA_Main] Processing reconfiguration request for M2_Machining
[RLRA_Main] Executing REASSIGNMENT strategy

2. MODULAR (COMPOSITE) RLRA ARCHITECTURE
[ACL] FROM Monitor_SiteB TO RLRA_Main TYPE REQUEST
[RLRA_Main_Learner] Detected scenario: MACHINE_FAILURE
[RLRA_Main_Executor] Starting execution of plan: STRATEGY_REASSIGNMENT

3. DISTRIBUTED RLRA ARCHITECTURE
[ACL] FROM Monitor_SiteA TO RLRA_Main TYPE REQUEST
[SITE_A_Coordinator] ===== LOCAL DECISION PROCESS =====
[SITE_A_Coordinator] ? LOCAL CONFLICT DETECTED
[RLRA_Main_Supervisor] ===== GLOBAL DECISION PROCESS =====
[ACL] FROM Supervisor TO SITE_A_Coordinator TYPE CONFLICT_RESOLUTION

2. Running Inter-Agent Interaction Scenarios
SCENARIO 1: Simple Failure Detection and Notification ✓
SCENARIO 2: Multi-Site Coordination ✓
SCENARIO 3: Conflict Resolution ✓
...

3. Running Advanced Reconfiguration Scenarios
SCENARIO A: Production Peak ✓
SCENARIO B: Product Change ✓
SCENARIO C: Graceful Degradation ✓
SCENARIO D: Component Supply Chain Failure ✓

======================================================================
COMPLETE DEMONSTRATION FINISHED
======================================================================
```

### Interprétation des logs

**Logs CENTRALIZED:**
- Décisions instantanées
- Un seul agent ("RLRA_Main")
- Commandes directes aux machines

**Logs MODULAR:**
- 3 modules visibles: Monitor, Learner, Executor
- Communication intra-module implicitement
- ACL logs pour externe → RLRA

**Logs DISTRIBUTED:**
- Coordinateurs locaux ("SITE_A_Coordinator", "SITE_B_Coordinator")
- Supervisor global ("RLRA_Main_Supervisor")
- ACL logs explicites pour chaque escalade/résolution
- 2 phases: LOCAL DECISION → GLOBAL DECISION

---

## 🎓 Points clés à comprendre

### 1. Communication inter-agents
- Utilise protocole ACL standardisé
- Messages routés par MessageBroker
- Chaque agent a une boîte aux lettres

### 2. Hiérarchie des décisions
- **Centralized:** 1 niveau (RLRA central)
- **Modular:** 1 niveau + 3 modules internes
- **Distributed:** 2 niveaux (Local + Global)

### 3. Gestion des conflits
- **Local:** SiteCoordinator essaie de résoudre
- **Non solvable localement:** Escalade au Supervisor
- **Global:** Supervisor analyse tous les sites
- **Resolution:** Coordinator exécute la décision

### 4. Message Broker
- Singeton (unique instance)
- Maintient mailbox pour chaque agent
- Besoin de restart() entre simulations
- Gère MESSAGE_LOG pour statistiques

### 5. Scénarios et stratégies
- Détection de scénario (failure, peak, change, etc.)
- Stratégies pré-définies: REASSIGNMENT, ACCELERATION, BYPASS, etc.
- Exécution séquentielle ou parallèle

---

## 🔍 Debugging et troubleshooting

### Compilation échoue
```powershell
# Vérifier encodage UTF-8
javac -encoding UTF-8 -cp "lib\jade.jar" -d bin src\*.java

# Vérifier JADE jar existe
Test-Path lib\jade.jar
```

### Aucune sortie
```powershell
# Rediriger stderr
java -cp "bin;lib\jade.jar" App 2>&1 | more
```

### Logs incomplets
```powershell
# Sauvegarder dans fichier pour étudier
java -cp "bin;lib\jade.jar" App 2>&1 > output.txt
Get-Content output.txt | Measure-Object -Line
```

### MessageBroker warnings
- Normal d'avoir "unchecked operations" (JADE)
- N'affecte pas fonctionnalité

---

## 📝 Conclusion

Ce projet démontre comment:

✅ **Concevoir** un système multi-agents distribué  
✅ **Implémenter** 3 architectures différentes (centralisée, modulaire, distribuée)  
✅ **Communiquer** entre agents via ACL  
✅ **Gérer** les conflits et résolutions globales  
✅ **Simuler** des scénarios complexes et réalistes  
✅ **Maintenir** scalabilité et résilience  

Le système est **complet, fonctionnel et pédagogique**, prêt pour démonstration et extension.

---

**Bon apprentissage!** 🚀

