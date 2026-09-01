package com.example.goride.data.remote

import com.example.goride.data.model.Vehicle
import com.example.goride.data.model.VehicleType

/**
 * Sorgente dati fittizia per i mezzi (RF1, RF2, RF2.1, RF3).
 *
 * Corrisponde al ramo "mock backend" dello schema della Lez. 2.11:
 * un Repository puo' avere piu' sorgenti, e in assenza di un server
 * reale questa espone gli stessi dati che fornirebbe un'API.
 *
 * Se un giorno arrivasse un backend vero, cambierebbe solo questa
 * classe: il VehicleRepository e tutto cio' che sta sopra restano
 * identici.
 *
 * object = singleton (una sola istanza per tutta l'app).
 */
object MockVehicleDataSource {

    /** Centro di Ancona: fallback quando il permesso di posizione e' negato */
    const val ANCONA_LAT = 43.6158
    const val ANCONA_LON = 13.5189

    /** Zoom iniziale della mappa: livello "quartiere" */
    const val ZOOM_DEFAULT = 14f

    private val mezzi = listOf(

        // ---------- BICI CLASSICHE ----------
        Vehicle(
            id = "BK-001",
            nome = "Bici 001 - Piazza Cavour",
            tipo = VehicleType.CLASSIC,
            lat = 43.6155, lon = 13.5175,
            batteriaPercentuale = null,      // le classiche non hanno batteria
            autonomiaKm = null,
            tariffaSbloccoEuro = 0.50,
            tariffaAlMinutoEuro = 0.15,
            disponibile = true
        ),
        Vehicle(
            id = "BK-002",
            nome = "Bici 002 - Stazione Centrale",
            tipo = VehicleType.CLASSIC,
            lat = 43.6089, lon = 13.4972,
            batteriaPercentuale = null,
            autonomiaKm = null,
            tariffaSbloccoEuro = 0.50,
            tariffaAlMinutoEuro = 0.15,
            disponibile = true
        ),
        Vehicle(
            id = "BK-003",
            nome = "Bici 003 - Porto Antico",
            tipo = VehicleType.CLASSIC,
            lat = 43.6215, lon = 13.5042,
            batteriaPercentuale = null,
            autonomiaKm = null,
            tariffaSbloccoEuro = 0.50,
            tariffaAlMinutoEuro = 0.15,
            disponibile = true
        ),
        Vehicle(
            id = "BK-004",
            nome = "Bici 004 - Universita' Monte Dago",
            tipo = VehicleType.CLASSIC,
            lat = 43.5867, lon = 13.5142,
            batteriaPercentuale = null,
            autonomiaKm = null,
            tariffaSbloccoEuro = 0.50,
            tariffaAlMinutoEuro = 0.15,
            disponibile = true
        ),
        Vehicle(
            id = "BK-005",
            nome = "Bici 005 - Parco del Cardeto",
            tipo = VehicleType.CLASSIC,
            lat = 43.6248, lon = 13.5218,
            batteriaPercentuale = null,
            autonomiaKm = null,
            tariffaSbloccoEuro = 0.50,
            tariffaAlMinutoEuro = 0.15,
            disponibile = false          // gia' noleggiata: non compare in mappa
        ),

        // ---------- E-BIKE ----------
        Vehicle(
            id = "EB-001",
            nome = "E-Bike 001 - Corso Garibaldi",
            tipo = VehicleType.EBIKE,
            lat = 43.6142, lon = 13.5133,
            batteriaPercentuale = 87,
            autonomiaKm = 52.0,
            tariffaSbloccoEuro = 1.00,
            tariffaAlMinutoEuro = 0.28,
            disponibile = true
        ),
        Vehicle(
            id = "EB-002",
            nome = "E-Bike 002 - Passetto",
            tipo = VehicleType.EBIKE,
            lat = 43.6089, lon = 13.5305,
            batteriaPercentuale = 42,
            autonomiaKm = 25.0,
            tariffaSbloccoEuro = 1.00,
            tariffaAlMinutoEuro = 0.28,
            disponibile = true
        ),
        Vehicle(
            id = "EB-003",
            nome = "E-Bike 003 - Duomo San Ciriaco",
            tipo = VehicleType.EBIKE,
            lat = 43.6252, lon = 13.5108,
            batteriaPercentuale = 95,
            autonomiaKm = 57.0,
            tariffaSbloccoEuro = 1.00,
            tariffaAlMinutoEuro = 0.28,
            disponibile = true
        ),
        Vehicle(
            id = "EB-004",
            nome = "E-Bike 004 - Mole Vanvitelliana",
            tipo = VehicleType.EBIKE,
            lat = 43.6178, lon = 13.5051,
            batteriaPercentuale = 15,        // batteria scarica: caso limite per la UI
            autonomiaKm = 9.0,
            tariffaSbloccoEuro = 1.00,
            tariffaAlMinutoEuro = 0.28,
            disponibile = true
        ),
        Vehicle(
            id = "EB-005",
            nome = "E-Bike 005 - Ospedale Torrette",
            tipo = VehicleType.EBIKE,
            lat = 43.6003, lon = 13.4681,
            batteriaPercentuale = 68,
            autonomiaKm = 41.0,
            tariffaSbloccoEuro = 1.00,
            tariffaAlMinutoEuro = 0.28,
            disponibile = true
        )
    )

    /** Tutti i mezzi liberi da mostrare sulla mappa (RF1) */
    fun getMezziDisponibili(): List<Vehicle> = mezzi.filter { it.disponibile }

    /** Un singolo mezzo per id, null se non esiste */
    fun getMezzoById(id: String): Vehicle? = mezzi.find { it.id == id }
}
