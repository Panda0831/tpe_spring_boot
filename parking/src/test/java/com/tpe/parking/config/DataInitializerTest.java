package com.tpe.parking.config;

import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.repository.AbonnementRepository;
import com.tpe.parking.repository.EntreeSortieRepository;
import com.tpe.parking.repository.PlaceRepository;
import com.tpe.parking.repository.ReservationRepository;
import com.tpe.parking.repository.TarifRepository;
import com.tpe.parking.repository.UtilisateurRepository;
import com.tpe.parking.repository.VehiculeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTest {

    @Test
    @DisplayName("Les comptes de démonstration manquants sont créés même si la table contient déjà des utilisateurs")
    void creeLesComptesDemoManquants() {
        UtilisateurRepository utilisateurRepository = mock(UtilisateurRepository.class);
        VehiculeRepository vehiculeRepository = mock(VehiculeRepository.class);
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        AbonnementRepository abonnementRepository = mock(AbonnementRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        EntreeSortieRepository entreeSortieRepository = mock(EntreeSortieRepository.class);
        TarifRepository tarifRepository = mock(TarifRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(tarifRepository.count()).thenReturn(1L);
        when(placeRepository.count()).thenReturn(1L);
        when(vehiculeRepository.count()).thenReturn(1L);
        when(abonnementRepository.count()).thenReturn(1L);
        when(reservationRepository.count()).thenReturn(1L);
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        new DataInitializer(utilisateurRepository, vehiculeRepository, placeRepository,
                abonnementRepository, reservationRepository, entreeSortieRepository,
                tarifRepository, passwordEncoder).run();

        Set<String> comptesDemoAttendus = Set.of(
                "admin@parking.com",
                "manager@parking.com",
                "operator@parking.com",
                "user@parking.com",
                "vip@parking.com");
        verify(utilisateurRepository).saveAll(argThat((Iterable<Utilisateur> comptes) ->
                StreamSupport.stream(comptes.spliterator(), false)
                        .map(Utilisateur::getEmail)
                        .collect(Collectors.toSet())
                        .equals(comptesDemoAttendus)));
    }
}
