package com.example.tp14_client_soap_android.ws;

import com.example.tp14_client_soap_android.beans.Compte;
import com.example.tp14_client_soap_android.beans.TypeCompte;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Service {
    // CORRECTION : Utiliser le namespace du WSDL
    private static final String NAMESPACE = "http://ws.tp_12web_service_soap.example.com/";
    
    // CORRECTION : URL sans /BanqueWS (selon le WSDL)
    private static final String URL = "http://10.181.97.232:8080/services/ws"; // Pour appareil physique
    // OU
    // private static final String URL = "http://10.0.2.2:8080/services/ws"; // Pour émulateur
    
    private static final String METHOD_GET_COMPTES = "getComptes";
    private static final String METHOD_CREATE_COMPTE = "createCompte";
    private static final String METHOD_DELETE_COMPTE = "deleteCompte";
    private static final String METHOD_UPDATE_COMPTE = "updateCompte";

    /**
     * Récupère la liste des comptes via le service SOAP.
     */
    public List<Compte> getComptes() {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_GET_COMPTES);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.setOutputSoapObject(request);

        HttpTransportSE transport = new HttpTransportSE(URL);
        List<Compte> comptes = new ArrayList<>();

        try {
            transport.call("", envelope);
            
            Object bodyIn = envelope.bodyIn;
            if (bodyIn == null) {
                android.util.Log.w("SOAP", "bodyIn est null");
                return comptes;
            }
            
            SoapObject response = (SoapObject) bodyIn;
            android.util.Log.d("SOAP", "Nombre de propriétés dans response: " + response.getPropertyCount());
            
            // Parcourir TOUTES les propriétés sans break
            for (int i = 0; i < response.getPropertyCount(); i++) {
                Object prop = response.getProperty(i);
                
                if (prop instanceof SoapObject) {
                    SoapObject soapObj = (SoapObject) prop;
                    
                    // Chercher "return" dans cette propriété
                    try {
                        Object returnObj = soapObj.getProperty("return");
                        if (returnObj != null) {
                            parseReturnObject(returnObj, comptes);
                        }
                    } catch (Exception e) {
                        // Ignorer et continuer
                    }
                    
                    // Vérifier si c'est directement un compte
                    if (soapObj.hasProperty("id") || soapObj.hasProperty("solde")) {
                        comptes.add(parseCompte(soapObj));
                    }
                    
                    // CORRECTION : Parcourir aussi toutes les propriétés de cet objet
                    // car il peut y avoir plusieurs "return" à différents niveaux
                    for (int j = 0; j < soapObj.getPropertyCount(); j++) {
                        Object subProp = soapObj.getProperty(j);
                        if (subProp instanceof SoapObject) {
                            SoapObject subSoapObj = (SoapObject) subProp;
                            if (subSoapObj.hasProperty("id") || subSoapObj.hasProperty("solde")) {
                                comptes.add(parseCompte(subSoapObj));
                            }
                        }
                    }
                }
            }
            
            android.util.Log.d("SOAP", "Nombre total de comptes récupérés: " + comptes.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("SOAP", "Erreur getComptes: " + e.getMessage(), e);
        }

        return comptes;
    }

    /**
     * Recherche récursivement tous les éléments "return" dans la réponse SOAP.
     */
    private void findAndParseAllReturns(SoapObject soapObj, List<Compte> comptes) {
        try {
            // Vérifier si c'est directement un compte
            if (soapObj.hasProperty("id") || soapObj.hasProperty("solde")) {
                comptes.add(parseCompte(soapObj));
                return;
            }
            
            // Parcourir toutes les propriétés
            for (int i = 0; i < soapObj.getPropertyCount(); i++) {
                Object prop = soapObj.getProperty(i);
                
                if (prop instanceof SoapObject) {
                    SoapObject childSoapObj = (SoapObject) prop;
                    
                    // Si le nom de la propriété est "return", c'est un compte
                    String propName = soapObj.getPropertyInfo(i).getName();
                    if ("return".equals(propName)) {
                        comptes.add(parseCompte(childSoapObj));
                    } else {
                        // Sinon, continuer la recherche récursive
                        findAndParseAllReturns(childSoapObj, comptes);
                    }
                } else if (prop instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) prop;
                    for (Object obj : list) {
                        if (obj instanceof SoapObject) {
                            findAndParseAllReturns((SoapObject) obj, comptes);
                        }
                    }
                } else if (prop instanceof java.util.Vector) {
                    @SuppressWarnings("unchecked")
                    java.util.Vector<Object> vector = (java.util.Vector<Object>) prop;
                    for (Object obj : vector) {
                        if (obj instanceof SoapObject) {
                            findAndParseAllReturns((SoapObject) obj, comptes);
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SOAP", "Erreur dans findAndParseAllReturns: " + e.getMessage());
        }
    }

    /**
     * Parse un objet "return" qui peut être un compte unique, une liste, un tableau, etc.
     */
    private void parseReturnObject(Object returnObj, List<Compte> comptes) {
        if (returnObj instanceof SoapObject) {
            SoapObject soapCompte = (SoapObject) returnObj;
            // Vérifier si c'est un compte (a les propriétés id ou solde)
            if (soapCompte.hasProperty("id") || soapCompte.hasProperty("solde")) {
                comptes.add(parseCompte(soapCompte));
            } else {
                // Sinon, c'est peut-être un conteneur, chercher récursivement
                findAndParseAllReturns(soapCompte, comptes);
            }
        } else if (returnObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) returnObj;
            android.util.Log.d("SOAP", "Liste trouvée, taille: " + list.size());
            for (Object obj : list) {
                if (obj instanceof SoapObject) {
                    comptes.add(parseCompte((SoapObject) obj));
                }
            }
        } else if (returnObj instanceof java.util.Vector) {
            @SuppressWarnings("unchecked")
            java.util.Vector<Object> vector = (java.util.Vector<Object>) returnObj;
            android.util.Log.d("SOAP", "Vector trouvé, taille: " + vector.size());
            for (Object obj : vector) {
                if (obj instanceof SoapObject) {
                    comptes.add(parseCompte((SoapObject) obj));
                }
            }
        } else if (returnObj.getClass().isArray()) {
            Object[] array = (Object[]) returnObj;
            android.util.Log.d("SOAP", "Tableau trouvé, taille: " + array.length);
            for (Object obj : array) {
                if (obj instanceof SoapObject) {
                    comptes.add(parseCompte((SoapObject) obj));
                }
            }
        } else {
            android.util.Log.w("SOAP", "Type de returnObj non reconnu: " + returnObj.getClass().getName());
        }
    }

    /**
     * Parse un SoapObject en Compte.
     */
    private Compte parseCompte(SoapObject soapCompte) {
        String idStr = getPropertySafelyAsString(soapCompte, "id");
        String soldeStr = getPropertySafelyAsString(soapCompte, "solde");
        String dateStr = getPropertySafelyAsString(soapCompte, "dateCreation");
        String typeStr = getPropertySafelyAsString(soapCompte, "type");

        Long id = null;
        try {
            if (idStr != null && !idStr.isEmpty()) {
                id = Long.parseLong(idStr);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        Double solde = 0.0;
        try {
            if (soldeStr != null && !soldeStr.isEmpty()) {
                solde = Double.parseDouble(soldeStr);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        Date dateCreation = new Date();
        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                // Le WSDL utilise xs:dateTime, donc le format peut être différent
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dateCreation = sdf.parse(dateStr);
            }
        } catch (Exception e) {
            // Use current date if parsing fails
        }

        TypeCompte type = TypeCompte.COURANT;
        try {
            if (typeStr != null && !typeStr.isEmpty()) {
                type = TypeCompte.valueOf(typeStr);
            }
        } catch (IllegalArgumentException e) {
            // Use default type
        }

        return new Compte(id, solde, dateCreation, type);
    }

    /**
     * Crée un nouveau compte via le service SOAP.
     */
    public boolean createCompte(double solde, TypeCompte type) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_CREATE_COMPTE);
        
        // Ajouter directement - MarshalFloat gère la sérialisation
        request.addProperty("solde", solde);
        request.addProperty("type", type.name());

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.setOutputSoapObject(request);
        
        // Enregistrer le Marshal pour les doubles
        MarshalFloat marshal = new MarshalFloat();
        marshal.register(envelope);

        HttpTransportSE transport = new HttpTransportSE(URL);
        String soapAction = "";

        try {
            android.util.Log.d("SOAP", "Tentative d'appel SOAP à: " + URL);
            transport.call(soapAction, envelope);
            
            // CORRECTION : Le WSDL montre que la réponse est dans createCompteResponse/return
            Object response = envelope.bodyIn;
            if (response != null) {
                android.util.Log.d("SOAP", "Réponse reçue: " + response.getClass().getName());
                // Si on reçoit une réponse, c'est un succès
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("SOAP", "Erreur createCompte: " + e.getMessage(), e);
            if (e.getCause() != null) {
                android.util.Log.e("SOAP", "Cause: " + e.getCause().getMessage());
            }
            return false;
        }
    }

    /**
     * Supprime un compte en fonction de son ID via le service SOAP.
     */
    public boolean deleteCompte(long id) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_DELETE_COMPTE);
        
        // Utiliser PropertyInfo pour l'ID (long)
        PropertyInfo idProperty = new PropertyInfo();
        idProperty.setName("id");
        idProperty.setValue(id);
        idProperty.setType(Long.class);
        request.addProperty(idProperty);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.setOutputSoapObject(request);

        HttpTransportSE transport = new HttpTransportSE(URL);
        String soapAction = "";

        try {
            transport.call(soapAction, envelope);
            Object response = envelope.bodyIn;
            
            // CORRECTION : Le WSDL montre que la réponse est dans deleteCompteResponse/return
            if (response instanceof SoapObject) {
                SoapObject soapResponse = (SoapObject) response;
                Object returnObj = soapResponse.getProperty("return");
                if (returnObj instanceof Boolean) {
                    return (Boolean) returnObj;
                }
            }
            return true; // Assume success if response is not boolean
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("SOAP", "Erreur deleteCompte: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Met à jour un compte existant via le service SOAP.
     */
    public boolean updateCompte(long id, double solde, TypeCompte type) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_UPDATE_COMPTE);
        
        // Utiliser PropertyInfo pour l'ID (long)
        PropertyInfo idProperty = new PropertyInfo();
        idProperty.setName("id");
        idProperty.setValue(id);
        idProperty.setType(Long.class);
        request.addProperty(idProperty);
        
        // Utiliser PropertyInfo pour le solde (double)
        PropertyInfo soldeProperty = new PropertyInfo();
        soldeProperty.setName("solde");
        soldeProperty.setValue(solde);
        soldeProperty.setType(Double.class);
        request.addProperty(soldeProperty);
        
        // Le type (String)
        request.addProperty("type", type.name());

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.setOutputSoapObject(request);
        
        // Enregistrer le Marshal pour les doubles
        MarshalFloat marshal = new MarshalFloat();
        marshal.register(envelope);

        HttpTransportSE transport = new HttpTransportSE(URL);
        String soapAction = "";

        try {
            android.util.Log.d("SOAP", "Tentative de mise à jour du compte ID: " + id);
            transport.call(soapAction, envelope);
            
            Object response = envelope.bodyIn;
            if (response != null) {
                android.util.Log.d("SOAP", "Compte mis à jour avec succès");
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("SOAP", "Erreur updateCompte: " + e.getMessage(), e);
            if (e.getCause() != null) {
                android.util.Log.e("SOAP", "Cause: " + e.getCause().getMessage());
            }
            return false;
        }
    }

    /**
     * Méthode utilitaire pour récupérer une propriété de manière sécurisée.
     */
    private String getPropertySafelyAsString(SoapObject soapObject, String propertyName) {
        try {
            Object property = soapObject.getProperty(propertyName);
            return property != null ? property.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}