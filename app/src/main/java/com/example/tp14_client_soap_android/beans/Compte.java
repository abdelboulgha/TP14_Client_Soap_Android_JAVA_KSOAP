package com.example.tp14_client_soap_android.beans;

import java.util.Date;

public class Compte {
    private Long id;
    private Double solde;
    private Date dateCreation;
    private TypeCompte type;

    public Compte(Long id, Double solde, Date dateCreation, TypeCompte type) {
        this.id = id;
        this.solde = solde;
        this.dateCreation = dateCreation;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getSolde() {
        return solde;
    }

    public void setSolde(Double solde) {
        this.solde = solde;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public TypeCompte getType() {
        return type;
    }

    public void setType(TypeCompte type) {
        this.type = type;
    }
}
