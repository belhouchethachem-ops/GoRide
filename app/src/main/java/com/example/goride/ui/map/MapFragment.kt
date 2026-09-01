package com.example.goride.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.goride.R
import com.example.goride.data.model.Vehicle
import com.example.goride.data.model.VehicleType
import com.example.goride.data.remote.MockVehicleDataSource
import com.example.goride.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.fragment.findNavController
/**
 * Schermata mappa (RF1, RF2).
 *
 * Il Fragment e' un UI Controller (Lez. 2.8): non contiene logica di
 * business, osserva il ViewModel e disegna. Il GoogleMap non esce mai
 * da questa classe.
 */
class MapFragment : Fragment() {

    // Lez. 2.8 - by viewModels(): l'istanza sopravvive alla rotazione
    private val viewModel: MapViewModel by viewModels()

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null

    // ---------------------------------------------------------------
    //  PERMESSI - Lez. 2.10, approccio con Activity Result API.
    //  Il launcher va registrato alla creazione del Fragment, mai
    //  dentro un click listener: altrimenti crash.
    // ---------------------------------------------------------------
    private val permessoPosizione = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concesso ->
        if (concesso) {
            abilitaPosizioneUtente()
        } else {
            // Best practice della Lez. 2.10: se l'utente nega, l'app
            // non si blocca. La mappa resta su Ancona con i mezzi visibili.
            Snackbar.make(
                binding.root,
                R.string.mappa_permesso_messaggio,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        // Senza questa riga le LiveData nel layout non si aggiornano mai
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inizializzaMappa()
        osservaViewModel()

        binding.fabPosizione.setOnClickListener { richiediPermessoPosizione() }
    }

    /** getMapAsync e' asincrono: la mappa non e' pronta subito */
    private fun inizializzaMappa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_container) as? SupportMapFragment

        mapFragment?.getMapAsync { map ->
            googleMap = map

            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isMapToolbarEnabled = false

            // Vista iniziale su Ancona
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(MockVehicleDataSource.ANCONA_LAT, MockVehicleDataSource.ANCONA_LON),
                    MockVehicleDataSource.ZOOM_DEFAULT
                )
            )

            // Il tag del marker porta il Vehicle: al tap lo recuperiamo
            map.setOnMarkerClickListener { marker ->
                (marker.tag as? Vehicle)?.let { viewModel.selezionaMezzo(it) }
                false   // false = mantiene il comportamento standard (info window)
            }

            // La mappa e' pronta solo ora: se i mezzi erano gia' arrivati
            // vanno disegnati adesso
            viewModel.mezzi.value?.let { disegnaMarker(it) }

            richiediPermessoPosizione()
        }
    }

    private fun osservaViewModel() {
        // Lez. 2.8 - viewLifecycleOwner, non this: l'observer si stacca
        // quando la View viene distrutta, evitando leak
        viewModel.mezzi.observe(viewLifecycleOwner) { mezzi ->
            disegnaMarker(mezzi)
        }

        viewModel.errore.observe(viewLifecycleOwner) { messaggio ->
            messaggio?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Riprova") { viewModel.caricaMezzi() }
                    .show()
                viewModel.erroreMostrato()
            }
        }

        viewModel.mezzoSelezionato.observe(viewLifecycleOwner) { mezzo ->
            mezzo?.let {
                val azione = MapFragmentDirections.actionMapToVehicleDetail(it)
                findNavController().navigate(azione)
                viewModel.deselezionaMezzo()
            }
        }
    }

    /**
     * Disegna i marker distinguendo per tipo (RF2).
     *
     * Il colore differenzia bici classiche (verde) ed e-bike (arancio).
     * Il titolo ripete l'informazione a parole: chi non distingue i
     * colori la legge comunque (Lez. 2.12, accessibilita').
     */
    private fun disegnaMarker(mezzi: List<Vehicle>) {
        val map = googleMap ?: return

        map.clear()

        mezzi.forEach { mezzo ->
            val colore = if (mezzo.tipo == VehicleType.EBIKE) {
                BitmapDescriptorFactory.HUE_ORANGE
            } else {
                BitmapDescriptorFactory.HUE_GREEN
            }

            // RF2.1 e RF3: batteria e tariffa visibili gia' dalla mappa
            val sottotitolo = if (mezzo.isEbike) {
                "${mezzo.batteriaPercentuale}% · ${mezzo.autonomiaKm?.toInt()} km · ${mezzo.tariffaAlMinutoEuro} €/min"
            } else {
                "Bici classica · ${mezzo.tariffaAlMinutoEuro} €/min"
            }

            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(mezzo.lat, mezzo.lon))
                    .title(mezzo.nome)
                    .snippet(sottotitolo)
                    .icon(BitmapDescriptorFactory.defaultMarker(colore))
            )

            marker?.tag = mezzo
        }
    }

    /** Lez. 2.10 - il flusso a tre rami */
    private fun richiediPermessoPosizione() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                abilitaPosizioneUtente()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // L'utente ha gia' negato una volta: spieghiamo perche' serve
                Snackbar.make(
                    binding.root,
                    R.string.mappa_permesso_messaggio,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.mappa_permesso_ok) {
                    permessoPosizione.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }

            else -> {
                permessoPosizione.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    /** Il try/catch e' obbligatorio: il compilatore esige il controllo del permesso */
    private fun abilitaPosizioneUtente() {
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            // Permesso revocato tra il controllo e la chiamata: caso raro ma possibile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Lez. 2.7 - il binding vive quanto la View, non quanto il Fragment
        _binding = null
        googleMap = null
    }
}