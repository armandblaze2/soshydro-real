<?php
// Connexion à la base de données unifiée
$host = 'localhost';
$dbname = 'trafic_libreville_db';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Vérification sécurisée des colonnes pour éviter les erreurs de syntaxe
    $cols = $pdo->query("SHOW COLUMNS FROM stations_carburant LIKE 'telephone'")->fetch();
    if (!$cols) {
        $pdo->exec("ALTER TABLE stations_carburant ADD COLUMN telephone VARCHAR(30) DEFAULT ''");
    }
    $colsLieu = $pdo->query("SHOW COLUMNS FROM stations_carburant LIKE 'lieu_dit'")->fetch();
    if (!$colsLieu) {
        $pdo->exec("ALTER TABLE stations_carburant ADD COLUMN lieu_dit VARCHAR(100) DEFAULT ''");
    }
    // Vérification des colonnes pour le gaz 35kg dans les stations si nécessaire
    $colsGaz35 = $pdo->query("SHOW COLUMNS FROM stations_carburant LIKE 'prix_gaz_35kg'")->fetch();
    if (!$colsGaz35) {
        $pdo->exec("ALTER TABLE stations_carburant ADD COLUMN prix_gaz_35kg DECIMAL(10,2) DEFAULT 14000");
        $pdo->exec("ALTER TABLE stations_carburant ADD COLUMN dispo_gaz_35kg TINYINT(1) DEFAULT 1");
    }
    // Vérification de la colonne mot_de_passe pour stations_carburant
    $colsPassSt = $pdo->query("SHOW COLUMNS FROM stations_carburant LIKE 'mot_de_passe'")->fetch();
    if (!$colsPassSt) {
        $pdo->exec("ALTER TABLE stations_carburant ADD COLUMN mot_de_passe VARCHAR(255) DEFAULT ''");
    }
    // Vérification de la colonne mot_de_passe pour detailleurs_gaz
    $colsPassDt = $pdo->query("SHOW COLUMNS FROM detailleurs_gaz LIKE 'mot_de_passe'")->fetch();
    if (!$colsPassDt) {
        $pdo->exec("ALTER TABLE detailleurs_gaz ADD COLUMN mot_de_passe VARCHAR(255) DEFAULT ''");
    }
} catch (PDOException $e) {
    die("Erreur de connexion : " . $e->getMessage());
}

$message = "";
$messageType = "";
$view = $_GET['view'] ?? 'accueil';
$stationId = intval($_GET['station_id'] ?? 0);
$detaillantId = intval($_GET['detaillant_id'] ?? 0);

$groupesPestroliers = ["TotalEnergies", "PetroGabon", "Engen", "OLA Energy", "Gab'Oil"];

// Traitement des requêtes POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    // 1. AUTHENTIFICATION STATION (Numéro de téléphone + Mot de passe)
    if ($action === 'auth_station') {
        $telephone = trim($_POST['telephone'] ?? '');
        $motDePasse = $_POST['mot_de_passe'] ?? '';

        $stmt = $pdo->prepare("SELECT id, mot_de_passe FROM stations_carburant WHERE telephone = ? LIMIT 1");
        $stmt->execute([$telephone]);
        $station = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($station) {
            if (!empty($station['mot_de_passe']) && password_verify($motDePasse, $station['mot_de_passe'])) {
                header("Location: ?view=form_station&station_id=" . $station['id']);
                exit;
            } elseif (empty($station['mot_de_passe'])) {
                $message = "Votre compte n'a pas encore de mot de passe défini. Contactez l'assistance au 066 23 31 81.";
                $messageType = "error";
                $view = 'auth_station';
            } else {
                $message = "Mot de passe incorrect. Réessayez ou contactez le support WhatsApp (066 23 31 81).";
                $messageType = "error";
                $view = 'auth_station';
            }
        } else {
            $message = "Ce numéro de téléphone n'est enregistré pour aucune station. Veuillez procéder à votre premier enregistrement ci-dessous.";
            $messageType = "error";
            $view = 'form_station';
            $stationId = 0;
        }
    }

    // 2. MISE À JOUR OU CRÉATION STATION
    if ($action === 'mettre_a_jour_station') {
        $sId = intval($_POST['station_id'] ?? 0);
        $nomStation = trim($_POST['nom_station'] ?? 'TotalEnergies');
        $lieuDit = trim($_POST['lieu_dit'] ?? '');
        $telephone = trim($_POST['telephone'] ?? '');
        $motDePasseRaw = trim($_POST['mot_de_passe'] ?? '');
        $latitude = floatval($_POST['latitude'] ?? 0);
        $longitude = floatval($_POST['longitude'] ?? 0);
        $estOuvert = isset($_POST['est_ouvert']) ? 1 : 0;
        
        $prixEssence = floatval($_POST['prix_essence'] ?? 850);
        $dispoEssence = isset($_POST['dispo_essence']) ? 1 : 0;
        $prixGazoil = floatval($_POST['prix_gazoil'] ?? 750);
        $dispoGazoil = isset($_POST['dispo_gazoil']) ? 1 : 0;
        $prixPetrole = floatval($_POST['prix_petrole'] ?? 650);
        $dispoPetrole = isset($_POST['dispo_petrole']) ? 1 : 0;
        
        $prixGaz12kg = floatval($_POST['prix_gaz_12kg'] ?? 6000);
        $dispoGaz12kg = isset($_POST['dispo_gaz_12kg']) ? 1 : 0;
        $prixGaz35kg = floatval($_POST['prix_gaz_35kg'] ?? 14000);
        $dispoGaz35kg = isset($_POST['dispo_gaz_35kg']) ? 1 : 0;

        if ($sId > 0) {
            // Mise à jour (Pas de modification de la latitude/longitude d'ancrage initial)
            if (!empty($motDePasseRaw)) {
                $hash = password_hash($motDePasseRaw, PASSWORD_DEFAULT);
                $sql = "UPDATE stations_carburant SET nom_station = ?, lieu_dit = ?, telephone = ?, mot_de_passe = ?, est_ouvert = ?, prix_essence = ?, dispo_essence = ?, prix_gazoil = ?, dispo_gazoil = ?, prix_petrole = ?, dispo_petrole = ?, prix_gaz = ?, dispo_gaz = ?, prix_gaz_35kg = ?, dispo_gaz_35kg = ?, derniere_maj = NOW() WHERE id = ?";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([$nomStation, $lieuDit, $telephone, $hash, $estOuvert, $prixEssence, $dispoEssence, $prixGazoil, $dispoGazoil, $prixPetrole, $dispoPetrole, $prixGaz12kg, $dispoGaz12kg, $prixGaz35kg, $dispoGaz35kg, $sId]);
            } else {
                $sql = "UPDATE stations_carburant SET nom_station = ?, lieu_dit = ?, telephone = ?, est_ouvert = ?, prix_essence = ?, dispo_essence = ?, prix_gazoil = ?, dispo_gazoil = ?, prix_petrole = ?, dispo_petrole = ?, prix_gaz = ?, dispo_gaz = ?, prix_gaz_35kg = ?, dispo_gaz_35kg = ?, derniere_maj = NOW() WHERE id = ?";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([$nomStation, $lieuDit, $telephone, $estOuvert, $prixEssence, $dispoEssence, $prixGazoil, $dispoGazoil, $prixPetrole, $dispoPetrole, $prixGaz12kg, $dispoGaz12kg, $prixGaz35kg, $dispoGaz35kg, $sId]);
            }
            $message = "Station $nomStation ($lieuDit) mise à jour avec succès !";
        } else {
            // Création avec ancrage GPS initial obligatoire
            $hash = password_hash($motDePasseRaw, PASSWORD_DEFAULT);
            $sql = "INSERT INTO stations_carburant (nom_station, lieu_dit, telephone, mot_de_passe, latitude, longitude, est_ouvert, prix_essence, dispo_essence, prix_gazoil, dispo_gazoil, prix_petrole, dispo_petrole, prix_gaz, dispo_gaz, prix_gaz_35kg, dispo_gaz_35kg, derniere_maj) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([$nomStation, $lieuDit, $telephone, $hash, $latitude, $longitude, $estOuvert, $prixEssence, $dispoEssence, $prixGazoil, $dispoGazoil, $prixPetrole, $dispoPetrole, $prixGaz12kg, $dispoGaz12kg, $prixGaz35kg, $dispoGaz35kg]);
            $sId = $pdo->lastInsertId();
            $message = "Première configuration : Station $nomStation ($lieuDit) ancrée par GPS avec succès !";
        }
        $messageType = "success";
        $view = 'form_station';
        $stationId = $sId;
    }

    // 3. AUTHENTIFICATION DÉTAILLANT DE GAZ (Numéro de téléphone + Mot de passe)
    if ($action === 'auth_detaillant') {
        $telephone = trim($_POST['telephone'] ?? '');
        $motDePasse = $_POST['mot_de_passe'] ?? '';

        $stmt = $pdo->prepare("SELECT id, mot_de_passe FROM detailleurs_gaz WHERE telephone = ? LIMIT 1");
        $stmt->execute([$telephone]);
        $detaillant = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($detaillant) {
            if (!empty($detaillant['mot_de_passe']) && password_verify($motDePasse, $detaillant['mot_de_passe'])) {
                header("Location: ?view=form_detaillant&detaillant_id=" . $detaillant['id']);
                exit;
            } elseif (empty($detaillant['mot_de_passe'])) {
                $message = "Votre boutique n'a pas encore de mot de passe défini. Contactez l'assistance au 066 23 31 81.";
                $messageType = "error";
                $view = 'auth_detaillant';
            } else {
                $message = "Mot de passe incorrect. Réessayez ou contactez le support WhatsApp (066 23 31 81).";
                $messageType = "error";
                $view = 'auth_detaillant';
            }
        } else {
            $message = "Ce numéro n'est pas encore enregistré. Veuillez remplir le formulaire ci-dessous pour créer votre boutique.";
            $messageType = "error";
            $view = 'form_detaillant';
            $detaillantId = 0;
        }
    }

    // 4. ENREGISTREMENT / MISE À JOUR DÉTAILLANT DE GAZ
    if ($action === 'maj_detaillant_gaz') {
        $dId = intval($_POST['detaillant_id'] ?? 0);
        $nomBoutique = trim($_POST['nom_boutique'] ?? '');
        $nomGerant = trim($_POST['nom_gerant'] ?? '');
        $quartier = trim($_POST['quartier'] ?? '');
        $telephone = trim($_POST['telephone'] ?? '');
        $motDePasseRaw = trim($_POST['mot_de_passe'] ?? '');
        $latitude = floatval($_POST['latitude'] ?? 0);
        $longitude = floatval($_POST['longitude'] ?? 0);
        $prix6kg = floatval($_POST['prix_6kg'] ?? 5000);
        $prix12kg = floatval($_POST['prix_12kg'] ?? 18000);
        $enStock = isset($_POST['en_stock']) ? 1 : 0;

        if ($dId > 0) {
            // Mise à jour (Pas de modification de la latitude/longitude d'ancrage initial)
            if (!empty($motDePasseRaw)) {
                $hash = password_hash($motDePasseRaw, PASSWORD_DEFAULT);
                $sql = "UPDATE detailleurs_gaz SET nom_boutique = ?, nom_gerant = ?, quartier = ?, telephone = ?, mot_de_passe = ?, prix_bouteille_6kg = ?, prix_bouteille_12kg = ?, en_stock = ?, derniere_maj = NOW() WHERE id = ?";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([$nomBoutique, $nomGerant, $quartier, $telephone, $hash, $prix6kg, $prix12kg, $enStock, $dId]);
            } else {
                $sql = "UPDATE detailleurs_gaz SET nom_boutique = ?, nom_gerant = ?, quartier = ?, telephone = ?, prix_bouteille_6kg = ?, prix_bouteille_12kg = ?, en_stock = ?, derniere_maj = NOW() WHERE id = ?";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([$nomBoutique, $nomGerant, $quartier, $telephone, $prix6kg, $prix12kg, $enStock, $dId]);
            }
            $message = "Boutique de quartier mise à jour avec succès !";
        } else {
            // Création avec ancrage GPS initial obligatoire
            $hash = password_hash($motDePasseRaw, PASSWORD_DEFAULT);
            $sql = "INSERT INTO detailleurs_gaz (nom_boutique, nom_gerant, quartier, telephone, mot_de_passe, latitude, longitude, prix_bouteille_6kg, prix_bouteille_12kg, en_stock, derniere_maj) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([$nomBoutique, $nomGerant, $quartier, $telephone, $hash, $latitude, $longitude, $prix6kg, $prix12kg, $enStock]);
            $dId = $pdo->lastInsertId();
            $message = "Première configuration : Votre boutique est officiellement ancrée à cette position GPS.";
        }
        $messageType = "success";
        $view = 'form_detaillant';
        $detaillantId = $dId;
    }
}
?>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portail Fournisseurs - Carburant & Gaz LBV</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-[#0F172A] text-slate-100 min-h-screen flex flex-col justify-between font-sans">

    <!-- Header -->
    <header class="bg-[#1E293B] border-b border-slate-800 p-4 shadow-md sticky top-0 z-50">
        <div class="max-w-md mx-auto flex items-center justify-between">
            <div class="flex items-center space-x-3">
                <div class="bg-blue-600/20 p-2 rounded-full text-blue-400">
                    <i class="fa-solid fa-store text-lg"></i>
                </div>
                <h1 class="font-bold text-base text-white">Espace Fournisseur SOS-HYDRO</h1>
            </div>
            <a href="?view=accueil" class="text-xs bg-slate-800 hover:bg-slate-700 px-3 py-1.5 rounded-lg text-slate-300 transition">Accueil</a>
        </div>
    </header>

    <!-- Contenu Principal -->
    <main class="max-w-md w-full mx-auto p-4 flex-grow flex flex-col justify-center">

        <!-- LOGO IMPOSANT -->
        <div class="text-center mb-6">
            <img src="logotraffic.png" alt="Logo Trafic Libreville" class="w-36 h-36 mx-auto object-contain drop-shadow-2xl rounded-2xl border border-slate-800 bg-[#1E293B] p-2">
        </div>

        <?php if (!empty($message)): ?>
            <div class="mb-4 p-4 rounded-xl text-sm font-medium <?= $messageType === 'success' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-amber-500/10 text-amber-300 border border-amber-500/20' ?>">
                <i class="fa-solid <?= $messageType === 'success' ? 'fa-circle-check' : 'fa-triangle-exclamation' ?> mr-2"></i>
                <?= htmlspecialchars($message) ?>
            </div>
        <?php endif; ?>

        <!-- VUE 1 : ACCUEIL -->
        <?php if ($view === 'accueil'): ?>
            <div class="text-center space-y-6">
                <div>
                    <h2 class="text-2xl font-bold text-white">Gestion Réseau Carburant & Gaz</h2>
                    <p class="text-slate-400 text-xs mt-2 px-4">Sélectionnez votre profil pour mettre à jour vos données en temps réel sur SOS-HYDRO.</p>
                </div>
                <div class="space-y-3 pt-2">
                    <a href="?view=auth_station" class="w-full flex items-center justify-center bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3.5 px-6 rounded-xl shadow-lg shadow-blue-600/20 transition">
                        <i class="fa-solid fa-gas-pump mr-2"></i> Je suis une Station-Service
                    </a>
                    <a href="?view=auth_detaillant" class="w-full flex items-center justify-center bg-[#334155] hover:bg-slate-700 text-white font-semibold py-3.5 px-6 rounded-xl transition">
                        <i class="fa-solid fa-fire-flame-curved mr-2 text-orange-400"></i> Détaillant de Gaz (Quartier)
                    </a>
                </div>
            </div>

        <!-- VUE 2A : AUTHENTIFICATION STATION -->
        <?php elseif ($view === 'auth_station'): ?>
            <div class="bg-[#1E293B] p-6 rounded-2xl border border-slate-800 shadow-xl space-y-5">
                <div class="text-center">
                    <div class="inline-flex bg-blue-600/10 p-4 rounded-full text-blue-500 mb-2">
                        <i class="fa-solid fa-gas-pump text-2xl"></i>
                    </div>
                    <h2 class="text-lg font-bold text-white">Accès Agent Station-Service</h2>
                    <p class="text-slate-400 text-xs mt-1">Entrez vos identifiants pour accéder directement à vos stocks.</p>
                </div>
                <form action="?view=auth_station" method="POST" class="space-y-4">
                    <input type="hidden" name="action" value="auth_station">

                    <div>
                        <label class="block text-xs font-medium text-slate-400 mb-1">Numéro de téléphone enregistré</label>
                        <input type="tel" name="telephone" required placeholder="06623XX67" pattern="[0-9+\s\-]{9,}" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-3 text-white text-xs focus:outline-none focus:border-blue-500">
                    </div>

                    <div>
                        <label class="block text-xs font-medium text-slate-400 mb-1">Mot de passe</label>
                        <input type="password" name="mot_de_passe" required placeholder="••••••••" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-3 text-white text-xs focus:outline-none focus:border-blue-500">
                    </div>

                    <button type="submit" class="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 rounded-xl transition shadow-lg text-xs">Accéder au Tableau de Bord</button>
                </form>

                <div class="text-center">
                    <a href="https://wa.me/24166233181?text=Bonjour%2C%20j'ai%20oubli%C3%A9%20le%20mot%20de%20passe%20de%20ma%20station%20sur%20SOS-HYDRO." target="_blank" class="text-[11px] text-slate-400 hover:text-emerald-400 transition inline-flex items-center">
                        <i class="fa-brands fa-whatsapp text-emerald-400 mr-1.5 text-sm"></i> Mot de passe oublié ? Écrivez-nous au <strong class="ml-1 text-slate-200">066 23 31 81</strong>
                    </a>
                </div>

                <div class="text-center pt-3 border-t border-slate-800">
                    <p class="text-slate-400 text-xs">Première visite pour cette station ?</p>
                    <a href="?view=form_station&station_id=0" class="text-emerald-400 text-xs font-bold hover:underline mt-1 inline-block">
                        <i class="fa-solid fa-plus-circle mr-1"></i> Enregistrer ma station pour la première fois
                    </a>
                </div>
            </div>

        <!-- VUE 2B : AUTHENTIFICATION DÉTAILLANT DE GAZ -->
        <?php elseif ($view === 'auth_detaillant'): ?>
            <div class="bg-[#1E293B] p-6 rounded-2xl border border-slate-800 shadow-xl space-y-5">
                <div class="text-center">
                    <div class="inline-flex bg-orange-600/10 p-4 rounded-full text-orange-400 mb-2">
                        <i class="fa-solid fa-fire-flame-curved text-2xl"></i>
                    </div>
                    <h2 class="text-lg font-bold text-white">Accès Détaillant de Gaz</h2>
                    <p class="text-slate-400 text-xs mt-1">Entrez vos identifiants pour gérer votre stock.</p>
                </div>
                <form action="?view=auth_detaillant" method="POST" class="space-y-4">
                    <input type="hidden" name="action" value="auth_detaillant">
                    <div>
                        <label class="block text-xs font-medium text-slate-400 mb-1">Votre Numéro de Téléphone</label>
                        <input type="tel" name="telephone" required placeholder="06623XX67" pattern="[0-9+\s\-]{9,}" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-3 text-white text-xs focus:outline-none focus:border-blue-500">
                    </div>
                    <div>
                        <label class="block text-xs font-medium text-slate-400 mb-1">Mot de passe</label>
                        <input type="password" name="mot_de_passe" required placeholder="••••••••" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-3 text-white text-xs focus:outline-none focus:border-blue-500">
                    </div>
                    <button type="submit" class="w-full bg-orange-600 hover:bg-orange-500 text-white font-semibold py-3 rounded-xl transition shadow-lg text-xs">Se connecter</button>
                </form>

                <div class="text-center">
                    <a href="https://wa.me/24166233181?text=Bonjour%2C%20j'ai%20oubli%C3%A9%20le%20mot%20de%20passe%20de%20ma%20boutique%20de%20gaz%20sur%20SOS-HYDRO." target="_blank" class="text-[11px] text-slate-400 hover:text-emerald-400 transition inline-flex items-center">
                        <i class="fa-brands fa-whatsapp text-emerald-400 mr-1.5 text-sm"></i> Mot de passe oublié ? Écrivez-nous au <strong class="ml-1 text-slate-200">066 23 31 81</strong>
                    </a>
                </div>

                <div class="text-center pt-3 border-t border-slate-800">
                    <p class="text-slate-400 text-xs">Nouvelle boutique de quartier ?</p>
                    <a href="?view=form_detaillant&detaillant_id=0" class="text-orange-400 text-xs font-bold hover:underline mt-1 inline-block">
                        <i class="fa-solid fa-plus-circle mr-1"></i> Créer et ancrer mon commerce pour la 1ère fois
                    </a>
                </div>
            </div>

        <!-- VUE 3 : FORMULAIRE STATION -->
        <?php elseif ($view === 'form_station'): ?>
            <?php
            $stData = [
                'nom_station' => 'TotalEnergies', 'lieu_dit' => '', 'telephone' => '', 'est_ouvert' => 1, 
                'prix_essence' => 850, 'dispo_essence' => 1, 
                'prix_gazoil' => 750, 'dispo_gazoil' => 1, 
                'prix_petrole' => 650, 'dispo_petrole' => 1, 
                'prix_gaz' => 6000, 'dispo_gaz' => 1,
                'prix_gaz_35kg' => 14000, 'dispo_gaz_35kg' => 1
            ];
            if ($stationId > 0) {
                $stmtSt = $pdo->prepare("SELECT * FROM stations_carburant WHERE id = ?");
                $stmtSt->execute([$stationId]);
                $fetched = $stmtSt->fetch(PDO::FETCH_ASSOC);
                if ($fetched) $stData = $fetched;
            }
            $isNewStation = ($stationId == 0);
            ?>
            <div class="bg-[#1E293B] p-5 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                <div class="flex items-center justify-between border-b border-slate-800 pb-3">
                    <h2 class="font-bold text-white text-base"><i class="fa-solid fa-gas-pump text-blue-400 mr-2"></i><?= !$isNewStation ? htmlspecialchars($stData['nom_station'] . ' (' . $stData['lieu_dit'] . ')') : 'Nouveau - Enregistrement Station' ?></h2>
                    <span class="text-[10px] <?= !$isNewStation ? 'bg-blue-500/10 text-blue-400' : 'bg-emerald-500/10 text-emerald-400' ?> px-2.5 py-1 rounded-full font-semibold"><?= !$isNewStation ? 'Mise à jour Stock & Prix' : '1er Ancrage GPS' ?></span>
                </div>

                <?php if ($isNewStation): ?>
                <div class="bg-amber-500/10 border border-amber-500/20 p-3 rounded-xl text-amber-300 text-[11px] leading-relaxed">
                    <i class="fa-solid fa-triangle-exclamation mr-1.5"></i> <strong>Important :</strong> Vous devez être physiquement sur site pour que le GPS capture l'emplacement exact de la station.
                </div>
                <?php endif; ?>

                <form id="stationForm" action="?view=form_station&station_id=<?= $stationId ?>" method="POST" class="space-y-3" onsubmit="return validerFormulaire('stationForm')">
                    <input type="hidden" name="action" value="mettre_a_jour_station">
                    <input type="hidden" name="station_id" value="<?= $stationId ?>">
                    <input type="hidden" name="latitude" id="lat">
                    <input type="hidden" name="longitude" id="lng">

                    <div>
                        <label class="block text-[11px] font-medium text-slate-400 mb-1">Groupe / Enseigne</label>
                        <select name="nom_station" required class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2 text-white text-xs">
                            <?php foreach($groupesPestroliers as $g): ?>
                                <option value="<?= $g ?>" <?= ($stData['nom_station'] === $g) ? 'selected' : '' ?>><?= $g ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                    <div class="grid grid-cols-2 gap-2">
                        <div>
                            <label class="block text-[11px] font-medium text-slate-400 mb-1">Lieu-dit exact (ex: Lac bleu)</label>
                            <input type="text" name="lieu_dit" value="<?= htmlspecialchars($stData['lieu_dit']) ?>" required placeholder="Lac bleu" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2 text-white text-xs">
                        </div>
                        <div>
                            <label class="block text-[11px] font-medium text-slate-400 mb-1">Téléphone de contact</label>
                            <input type="tel" name="telephone" value="<?= htmlspecialchars($stData['telephone']) ?>" required placeholder="066234567" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2 text-white text-xs">
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-2 bg-[#0F172A] p-3 rounded-xl border border-slate-700">
                        <div>
                            <label class="block text-[10px] font-medium text-slate-400 mb-1">
                                <?= !$isNewStation ? 'Nouveau MDP (optionnel)' : 'Mot de passe' ?>
                            </label>
                            <input type="password" id="mot_de_passe" name="mot_de_passe" <?= $isNewStation ? 'required' : '' ?> placeholder="••••••••" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-2.5 py-2 text-white text-xs">
                        </div>
                        <div>
                            <label class="block text-[10px] font-medium text-slate-400 mb-1">Confirmer le MDP</label>
                            <input type="password" id="confirmer_mot_de_passe" <?= $isNewStation ? 'required' : '' ?> placeholder="••••••••" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-2.5 py-2 text-white text-xs">
                        </div>
                    </div>

                    <div class="flex items-center justify-between bg-[#0F172A] p-2.5 rounded-xl border border-slate-700">
                        <span class="text-xs font-semibold text-slate-300">Station Ouverte</span>
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input type="checkbox" name="est_ouvert" value="1" <?= $stData['est_ouvert'] ? 'checked' : '' ?> class="sr-only peer">
                            <div class="w-11 h-6 bg-slate-700 rounded-full peer peer-checked:after:translate-x-full after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-500"></div>
                        </label>
                    </div>

                    <?php 
                    $items = [
                        ['nom' => 'Essence', 'p_name' => 'prix_essence', 'd_name' => 'dispo_essence', 'p_val' => $stData['prix_essence'], 'd_val' => $stData['dispo_essence']],
                        ['nom' => 'Gazoil', 'p_name' => 'prix_gazoil', 'd_name' => 'dispo_gazoil', 'p_val' => $stData['prix_gazoil'], 'd_val' => $stData['dispo_gazoil']],
                        ['nom' => 'Pétrole', 'p_name' => 'prix_petrole', 'd_name' => 'dispo_petrole', 'p_val' => $stData['prix_petrole'], 'd_val' => $stData['dispo_petrole']],
                        ['nom' => 'Bouteille de Gaz (12kg)', 'p_name' => 'prix_gaz_12kg', 'd_name' => 'dispo_gaz_12kg', 'p_val' => $stData['prix_gaz'], 'd_val' => $stData['dispo_gaz']],
                        ['nom' => 'Bouteille de Gaz (35kg)', 'p_name' => 'prix_gaz_35kg', 'd_name' => 'dispo_gaz_35kg', 'p_val' => $stData['prix_gaz_35kg'], 'd_val' => $stData['dispo_gaz_35kg']],
                    ];
                    foreach($items as $it):
                    ?>
                    <div class="bg-[#0F172A] p-2.5 rounded-xl border border-slate-700 flex items-center justify-between">
                        <div class="space-y-1">
                            <span class="text-xs font-bold text-white"><?= $it['nom'] ?></span>
                            <div class="relative w-28">
                                <input type="number" step="any" name="<?= $it['p_name'] ?>" value="<?= $it['p_val'] ?>" required class="w-full bg-slate-800 border border-slate-700 rounded-lg px-2 py-1 text-white text-xs pr-8">
                                <span class="absolute inset-y-0 right-0 flex items-center pr-2 text-[9px] text-slate-400">F</span>
                            </div>
                        </div>
                        <label class="flex items-center space-x-2 cursor-pointer text-xs text-slate-400">
                            <span>En stock</span>
                            <input type="checkbox" name="<?= $it['d_name'] ?>" value="1" <?= $it['d_val'] ? 'checked' : '' ?> class="w-4 h-4 rounded text-emerald-500 bg-slate-800 border-slate-600">
                        </label>
                    </div>
                    <?php endforeach; ?>

                    <!-- Bouton dynamique : GPS si création, Soumission directe si modification -->
                    <?php if ($isNewStation): ?>
                        <button type="button" onclick="gererSoumissionStation(true)" class="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-semibold py-3 rounded-xl transition shadow-lg text-xs flex items-center justify-center">
                            <i class="fa-solid fa-location-crosshairs mr-2"></i> Enregistrer & Ancrer GPS
                        </button>
                    <?php else: ?>
                        <button type="submit" class="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 rounded-xl transition shadow-lg text-xs flex items-center justify-center">
                            <i class="fa-solid fa-floppy-disk mr-2"></i> Mettre à jour ma Station
                        </button>
                    <?php endif; ?>
                </form>
            </div>

        <!-- VUE 4 : FORMULAIRE DÉTAILLANT DE GAZ -->
        <?php elseif ($view === 'form_detaillant'): ?>
            <?php
            $dtData = ['nom_boutique' => '', 'nom_gerant' => '', 'quartier' => '', 'telephone' => '', 'prix_bouteille_6kg' => 5000, 'prix_bouteille_12kg' => 18000, 'en_stock' => 1];
            if ($detaillantId > 0) {
                $stmtDt = $pdo->prepare("SELECT * FROM detailleurs_gaz WHERE id = ?");
                $stmtDt->execute([$detaillantId]);
                $fetchedDt = $stmtDt->fetch(PDO::FETCH_ASSOC);
                if ($fetchedDt) $dtData = $fetchedDt;
            }
            $isNewDetaillant = ($detaillantId == 0);
            ?>
            <div class="bg-[#1E293B] p-5 rounded-2xl border border-slate-800 shadow-xl space-y-4">
                <div class="flex items-center justify-between border-b border-slate-800 pb-3">
                    <h2 class="font-bold text-white text-base"><i class="fa-solid fa-fire-flame-curved text-orange-400 mr-2"></i><?= !$isNewDetaillant ? htmlspecialchars($dtData['nom_boutique']) : 'Nouveau - Enregistrement Boutique' ?></h2>
                    <span class="text-[10px] <?= !$isNewDetaillant ? 'bg-orange-500/10 text-orange-400' : 'bg-blue-500/10 text-blue-400' ?> px-2.5 py-1 rounded-full font-semibold"><?= !$isNewDetaillant ? 'Mise à jour Stock & Prix' : '1er Ancrage GPS' ?></span>
                </div>

                <?php if ($isNewDetaillant): ?>
                <div class="bg-amber-500/10 border border-amber-500/20 p-3 rounded-xl text-amber-300 text-[11px] leading-relaxed">
                    <i class="fa-solid fa-triangle-exclamation mr-1.5"></i> <strong>Important :</strong> Vous devez être physiquement sur place pour ancrer la position GPS exacte de votre commerce.
                </div>
                <?php endif; ?>

                <form id="detaillantForm" action="?view=form_detaillant" method="POST" class="space-y-3" onsubmit="return validerFormulaire('detaillantForm')">
                    <input type="hidden" name="action" value="maj_detaillant_gaz">
                    <input type="hidden" name="detaillant_id" value="<?= $detaillantId ?>">
                    <input type="hidden" name="latitude" id="lat">
                    <input type="hidden" name="longitude" id="lng">

                    <div>
                        <label class="block text-[11px] font-medium text-slate-400 mb-1">Nom de la Boutique / Épicerie</label>
                        <input type="text" name="nom_boutique" value="<?= htmlspecialchars($dtData['nom_boutique']) ?>" required placeholder="Ex: Épicerie Saint-Joseph" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2.5 text-white text-xs">
                    </div>
                    <div class="grid grid-cols-2 gap-2">
                        <div>
                            <label class="block text-[11px] font-medium text-slate-400 mb-1">Nom du Gérant</label>
                            <input type="text" name="nom_gerant" value="<?= htmlspecialchars($dtData['nom_gerant']) ?>" required placeholder="Nom complet" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2.5 text-white text-xs">
                        </div>
                        <div>
                            <label class="block text-[11px] font-medium text-slate-400 mb-1">Quartier</label>
                            <input type="text" name="quartier" value="<?= htmlspecialchars($dtData['quartier']) ?>" required placeholder="Ex: Glass, PK8" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2.5 text-white text-xs">
                        </div>
                    </div>
                    <div>
                        <label class="block text-[11px] font-medium text-slate-400 mb-1">Numéro WhatsApp / Téléphone</label>
                        <input type="tel" name="telephone" value="<?= htmlspecialchars($dtData['telephone']) ?>" required placeholder="06623XX67" class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2.5 text-white text-xs">
                    </div>

                    <div class="grid grid-cols-2 gap-2 bg-[#0F172A] p-3 rounded-xl border border-slate-700">
                        <div>
                            <label class="block text-[10px] font-medium text-slate-400 mb-1">
                                <?= !$isNewDetaillant ? 'Nouveau MDP (optionnel)' : 'Mot de passe' ?>
                            </label>
                            <input type="password" id="mot_de_passe" name="mot_de_passe" <?= $isNewDetaillant ? 'required' : '' ?> placeholder="••••••••" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-2.5 py-2 text-white text-xs">
                        </div>
                        <div>
                            <label class="block text-[10px] font-medium text-slate-400 mb-1">Confirmer le MDP</label>
                            <input type="password" id="confirmer_mot_de_passe" <?= $isNewDetaillant ? 'required' : '' ?> placeholder="••••••••" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-2.5 py-2 text-white text-xs">
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-2">
                        <div>
                            <label class="block text-[11px] font-medium text-slate-400 mb-1">Prix Bouteille 6kg</label>
                            <input type="number" name="prix_6kg" value="<?= $dtData['prix_bouteille_6kg'] ?>" required class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2.5 text-white text-xs">
                        </div>
                        <div>
                            <label class="block text-[11px] font-medium text-slate-400 mb-1">Prix Bouteille 12kg</label>
                            <input type="number" name="prix_12kg" value="<?= $dtData['prix_bouteille_12kg'] ?>" required class="w-full bg-[#0F172A] border border-slate-700 rounded-xl px-3 py-2.5 text-white text-xs">
                        </div>
                    </div>

                    <div class="flex items-center justify-between bg-[#0F172A] p-3 rounded-xl border border-slate-700">
                        <span class="text-xs font-semibold text-slate-300">Gaz en Stock (Disponible)</span>
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input type="checkbox" name="en_stock" value="1" <?= $dtData['en_stock'] ? 'checked' : '' ?> class="sr-only peer">
                            <div class="w-11 h-6 bg-slate-700 rounded-full peer peer-checked:after:translate-x-full after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-orange-500"></div>
                        </label>
                    </div>

                    <!-- Bouton dynamique : GPS si création, Soumission directe si modification -->
                    <?php if ($isNewDetaillant): ?>
                        <button type="button" onclick="gererSoumissionDetaillant(true)" class="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3.5 rounded-xl transition shadow-lg text-xs flex items-center justify-center">
                            <i class="fa-solid fa-location-crosshairs mr-2"></i> Enregistrer & Ancrer GPS
                        </button>
                    <?php else: ?>
                        <button type="submit" class="w-full bg-orange-600 hover:bg-orange-500 text-white font-semibold py-3.5 rounded-xl transition shadow-lg text-xs flex items-center justify-center">
                            <i class="fa-solid fa-floppy-disk mr-2"></i> Mettre à jour la Boutique
                        </button>
                    <?php endif; ?>
                </form>
            </div>
        <?php endif; ?>

    </main>

    <!-- Footer -->
    <footer class="bg-[#1E293B] border-t border-slate-800 p-3 text-center text-slate-500 text-[10px]">
        &copy; 2026 Jumpsy Media Group &bull; 
    </footer>

    <!-- Script de Gestion Intelligent (GPS uniquement à la création) -->
    <script>
        function validerFormulaire(formId) {
            const mdp = document.getElementById('mot_de_passe').value;
            const cmdp = document.getElementById('confirmer_mot_de_passe').value;

            // Si un mot de passe est saisi, vérifier qu'il matche
            if (mdp !== "" || cmdp !== "") {
                if (mdp !== cmdp) {
                    alert("Erreur de sécurité : Les deux mots de passe ne correspondent pas !");
                    return false;
                }
            }
            return true;
        }

        function gererSoumissionStation(isCreation) {
            if (isCreation) {
                capturerGpsEtSoumettre('stationForm');
            } else {
                if (validerFormulaire('stationForm')) {
                    document.getElementById('stationForm').submit();
                }
            }
        }

        function gererSoumissionDetaillant(isCreation) {
            if (isCreation) {
                capturerGpsEtSoumettre('detaillantForm');
            } else {
                if (validerFormulaire('detaillantForm')) {
                    document.getElementById('detaillantForm').submit();
                }
            }
        }

        function capturerGpsEtSoumettre(formId) {
            if (navigator.geolocation) {
                const btn = document.querySelector(`#${formId} button`);
                const originalText = btn.innerHTML;
                
                // Indicateur GPS hautement visible
                btn.className = "w-full bg-blue-600 text-white font-bold py-4 rounded-xl shadow-2xl text-xs flex items-center justify-center border-2 border-amber-400 animate-pulse";
                btn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin text-amber-300 text-base mr-3"></i> ACQUISITION GPS HAUTE PRÉCISION EN COURS...';
                btn.disabled = true;

                navigator.geolocation.getCurrentPosition(
                    function(position) {
                        const lat = position.coords.latitude;
                        const lng = position.coords.longitude;
                        const precision = position.coords.accuracy;

                        document.getElementById('lat').value = lat;
                        document.getElementById('lng').value = lng;

                        if (confirm(`Position GPS capturée avec succès !\n\nLatitude: ${lat.toFixed(5)}\nLongitude: ${lng.toFixed(5)}\nPrécision: à ${precision.toFixed(0)}m près.\n\nVoulez-vous valider et enregistrer ?`)) {
                            if (validerFormulaire(formId)) {
                                document.getElementById(formId).submit();
                            } else {
                                btn.className = "w-full bg-emerald-600 hover:bg-emerald-500 text-white font-semibold py-3 rounded-xl transition shadow-lg text-xs flex items-center justify-center";
                                btn.innerHTML = originalText;
                                btn.disabled = false;
                            }
                        } else {
                            btn.className = "w-full bg-emerald-600 hover:bg-emerald-500 text-white font-semibold py-3 rounded-xl transition shadow-lg text-xs flex items-center justify-center";
                            btn.innerHTML = originalText;
                            btn.disabled = false;
                        }
                    },
                    function(error) {
                        alert("Erreur GPS : Impossible d'obtenir votre position. Veuillez activer la géolocalisation haute précision sur votre appareil.");
                        btn.className = "w-full bg-emerald-600 hover:bg-emerald-500 text-white font-semibold py-3 rounded-xl transition shadow-lg text-xs flex items-center justify-center";
                        btn.innerHTML = originalText;
                        btn.disabled = false;
                    },
                    { enableHighAccuracy: true, timeout: 25000, maximumAge: 0 }
                );
            } else {
                alert("La géolocalisation n'est pas supportée par votre appareil.");
            }
        }
    </script>
</body>
</html>