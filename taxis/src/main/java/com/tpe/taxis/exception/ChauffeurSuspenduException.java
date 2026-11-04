package com.tpe.taxis.exception;

public class ChauffeurSuspenduException extends ChauffeurIndisponibleException {

    public ChauffeurSuspenduException(String message) {
        super(message);
    }

    public ChauffeurSuspenduException(Long chauffeurId) {
        super(String.format("Le chauffeur (id: %d) est actuellement suspendu. Aucune course ne peut lui être affectée.", chauffeurId));
    }
}
