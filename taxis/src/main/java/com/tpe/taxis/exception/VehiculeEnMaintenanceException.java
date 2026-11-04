package com.tpe.taxis.exception;

import com.tpe.taxis.entity.VehiculeStatut;

public class VehiculeEnMaintenanceException extends BusinessException {

    public VehiculeEnMaintenanceException(String message) {
        super(message);
    }

    public VehiculeEnMaintenanceException(Long vehiculeId, String immatriculation, VehiculeStatut statut) {
        super(String.format("Le véhicule %s (id: %d) ne peut pas être affecté car son statut est : %s.",
                immatriculation, vehiculeId, statut));
    }
}
