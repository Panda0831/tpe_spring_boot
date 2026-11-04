package com.tpe.taxis.exception;

import com.tpe.taxis.entity.ChauffeurStatut;

public class ChauffeurIndisponibleException extends BusinessException {

    public ChauffeurIndisponibleException(String message) {
        super(message);
    }

    public ChauffeurIndisponibleException(Long chauffeurId, ChauffeurStatut statut) {
        super(String.format("Le chauffeur (id: %d) n'est pas disponible (statut actuel : %s).", chauffeurId, statut));
    }
}
