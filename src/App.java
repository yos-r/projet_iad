/**
 * Application principale démontrant trois architectures RLRA pour la reconfiguration d'usine.
 *
 * Cette application simule une usine FESTO CP Factory avec un système multi-agents qui gère
 * la reconfiguration dynamique en réponse aux pannes de machines, pics de production et autres anomalies.
 *
 * Fonctionnalités démontrées :
 * 1. Trois architectures RLRA (Centralisée, Modulaire, Distribuée)
 * 2. Agents personnalisés (ConveyorAgent, AssemblyAgent)
 * 3. Scénarios d'interaction inter-agents (6 scénarios)
 * 4. Scénarios de reconfiguration avancés (4 scénarios)
 * 5. Protocole de communication et résolution de conflits
 */
public class App {
    /**
     * Point d'entrée principal de l'application
     * @param args Arguments en ligne de commande (arch, interactions, scenarios, all, help)
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String mode = args[0].toLowerCase();
            switch (mode) {
                case "arch":
                    // Démontrer uniquement les trois architectures RLRA
                    demonstrateArchitectures();
                    break;
                case "interactions":
                    // Exécuter les 6 scénarios d'interaction
                    InteractionScenarios.runAll();
                    break;
                case "scenarios":
                    // Exécuter les 4 scénarios de reconfiguration avancés
                    ReconfigurationScenarios.runAll();
                    break;
                case "all":
                    // Démonstration complète
                    runFullDemo();
                    break;
                case "help":
                    // Afficher l'aide
                    printHelp();
                    break;
                default:
                    System.out.println("Mode inconnu : " + mode);
                    printHelp();
            }
        } else {
            // Par défaut : exécuter la démo complète
            runFullDemo();
        }
    }

    /**
     * Exécute la démonstration complète de toutes les fonctionnalités
     */
    private static void runFullDemo() {
        System.out.println("╔" + "═".repeat(50) + "╗");
        System.out.println("║" + " ".repeat(8) + "FESTO CP Factory - Complete Demo" + " ".repeat(9) + "║");
        System.out.println("╚" + "═".repeat(50) + "╝\n");

        System.out.println("1. Exécution des démonstrations d'architectures RLRA\n");
        demonstrateArchitectures();

        System.out.println("\n\n2. Exécution des scénarios d'interaction inter-agents\n");
        InteractionScenarios.runAll();

        System.out.println("\n3. Exécution des scénarios de reconfiguration avancés\n");
        ReconfigurationScenarios.runAll();

        System.out.println("\n" + "═".repeat(70));
        System.out.println("DÉMONSTRATION COMPLÈTE TERMINÉE");
        System.out.println("═".repeat(70) + "\n");
    }

    /**
     * Démontre les trois architectures RLRA (Centralisée, Modulaire, Distribuée)
     */
    private static void demonstrateArchitectures() {
        demonstrateCentralizedArchitecture();
        MessageBroker.getInstance().restart();  // Redémarrer le broker entre les architectures
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateModularArchitecture();
        MessageBroker.getInstance().restart();  // Redémarrer le broker entre les architectures
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateDistributedArchitecture();
    }

    /**
     * Démontre l'architecture RLRA Centralisée
     * Un seul agent RLRA prend toutes les décisions de reconfiguration
     */
    private static void demonstrateCentralizedArchitecture() {
        System.out.println("1. CENTRALIZED RLRA ARCHITECTURE");
        System.out.println("-".repeat(40));

        FactorySimulation simulation = new FactorySimulation(RLRAFactory.ArchitectureType.CENTRALIZED);
        simulation.initializeFactory();

        // Exécuter la simulation initiale
        simulation.run(2);

        // Simuler une panne de machine
        System.out.println("\n[SCENARIO] Exécution du scénario MACHINE_FAILURE avec RLRA CENTRALISÉ");
        System.out.println("[SCENARIO] Simulation d'une panne moteur sur M2_Machining");
        simulation.simulateMachineFailure("M2_Machining", "Spindle motor failure");

        // Continuer la simulation
        simulation.run(3);

        // Afficher les résultats
        simulation.printReport();
        simulation.stop();
    }

    /**
     * Démontre l'architecture RLRA Modulaire (Composite)
     * L'agent RLRA est divisé en trois modules : Monitor, Learner, Executor
     */
    private static void demonstrateModularArchitecture() {
        System.out.println("2. MODULAR (COMPOSITE) RLRA ARCHITECTURE");
        System.out.println("-".repeat(40));

        FactorySimulation simulation = new FactorySimulation(RLRAFactory.ArchitectureType.MODULAR);
        simulation.initializeFactory();

        // Exécuter la simulation initiale
        simulation.run(2);

        // Simuler une panne de machine
        System.out.println("\n[SCENARIO] Exécution du scénario MACHINE_FAILURE avec RLRA MODULAIRE");
        System.out.println("[SCENARIO] Simulation d'un dysfonctionnement du préhenseur sur M3_Assembly");
        simulation.simulateMachineFailure("M3_Assembly", "Gripper malfunction");

        // Continuer la simulation
        simulation.run(3);

        // Afficher les résultats
        simulation.printReport();
        simulation.stop();
    }

    /**
     * Démontre l'architecture RLRA Distribuée
     * Décisions locales par coordinateurs de site + superviseur global pour résolution de conflits
     */
    private static void demonstrateDistributedArchitecture() {
        System.out.println("3. DISTRIBUTED RLRA ARCHITECTURE");
        System.out.println("-".repeat(40));

        FactorySimulation simulation = new FactorySimulation(RLRAFactory.ArchitectureType.DISTRIBUTED);
        simulation.initializeFactory();

        // Exécuter la simulation initiale
        simulation.run(2);

        // Simuler une panne de machine
        System.out.println("\n[SCENARIO] Exécution du scénario MACHINE_FAILURE avec RLRA DISTRIBUÉ");
        System.out.println("[SCENARIO] Simulation d'un blocage du convoyeur sur M1_Distribution");
        System.out.println("[SCENARIO] Test de coordination multi-sites et résolution de conflits");
        simulation.simulateMachineFailure("M1_Distribution", "Conveyor belt stuck");

        // Continuer la simulation
        simulation.run(3);

        // Afficher les résultats
        simulation.printReport();
        simulation.stop();
    }

    /**
     * Affiche les informations d'utilisation du programme
     */
    private static void printHelp() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("FESTO CP Factory - Système de Reconfiguration Multi-Agents");
        System.out.println("═".repeat(70));
        System.out.println("\nUtilisation : java App [mode]\n");
        System.out.println("Modes :");
        System.out.println("  (aucun/all)    Exécuter la démonstration complète (par défaut)");
        System.out.println("  arch           Exécuter les démonstrations d'architectures RLRA uniquement");
        System.out.println("  interactions   Exécuter les scénarios d'interaction inter-agents");
        System.out.println("  scenarios      Exécuter les scénarios de reconfiguration avancés");
        System.out.println("  help           Afficher ce message d'aide");
        System.out.println("\nExemples :");
        System.out.println("  java App                  # Démo complète");
        System.out.println("  java App arch             # Architectures uniquement");
        System.out.println("  java App interactions     # Scénarios d'interaction");
        System.out.println("  java App scenarios        # Scénarios de reconfiguration");
        System.out.println("\n" + "═".repeat(70) + "\n");
    }
}
