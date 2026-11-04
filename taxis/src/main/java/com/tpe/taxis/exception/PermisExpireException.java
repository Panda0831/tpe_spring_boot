package com.tpe.taxis.exception;

import java.time.LocalDate;

public class PermisExpireException extends BusinessException {

    public PermisExpireException(String message) {
        super(message);
    }

    public PermisExpireException(Long chauffeurId, String numeroPermis, LocalDate dateExpiration) {
        super(String.format("Le permis n°%s du chauffeur (id: %d) a expiré le %s. Impossible d'affecter une course.",
                numeroPermis, chauffeurId, dateExpiration));
    }
}
