package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.request.CreateClientRequest;
import com.tpe.taxis.dto.request.UpdateClientRequest;
import com.tpe.taxis.dto.response.ClientResponse;
import com.tpe.taxis.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestion des clients : recherche paginée, création, modification et suppression (ADMIN).
 * Réutilise directement la même couche métier que l'API REST.
 */
@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientWebController {

    private static final int TAILLE_PAGE = 10;

    private final ClientService clientService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), TAILLE_PAGE, Sort.by("nom", "prenom"));
        Page<ClientResponse> resultats;

        if (recherche != null && !recherche.isBlank()) {
            resultats = clientService.searchClients(recherche.trim(), pageable);
        } else {
            resultats = clientService.getAllClients(pageable);
        }

        model.addAttribute("page", resultats);
        model.addAttribute("recherche", recherche);
        return "clients";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String creer(@Valid @ModelAttribute CreateClientRequest request,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/clients";
        }
        try {
            ClientResponse client = clientService.createClient(request);
            redirectAttributes.addFlashAttribute("succes",
                    "Client " + client.nomComplet() + " (" + client.telephone() + ") enregistré avec succès.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/clients";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute UpdateClientRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/clients";
        }
        try {
            clientService.updateClient(id, request);
            redirectAttributes.addFlashAttribute("succes", "Client #" + id + " mis à jour avec succès.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/clients";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClient(id);
            redirectAttributes.addFlashAttribute("succes", "Client #" + id + " supprimé.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/clients";
    }

    private String messageValidation(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Les informations du client sont invalides.");
    }
}
