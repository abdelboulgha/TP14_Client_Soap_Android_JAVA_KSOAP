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
    private static final String NAMESPACE = "http://ws.tp_12web_service_soap.example.com/";
    
    private static final String URL = "http://10.181.97.232:8080/services/ws";

    private static final String METHOD_GET_COMPTES = "getComptes";
    private static final String METHOD_CREATE_COMPTE = "createCompte";
    private static final String METHOD_DELETE_COMPTE = "deleteCompte";
    private static final String METHOD_UPDATE_COMPTE = "updateCompte";


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
            

            for (int i = 0; i < response.getPropertyCount(); i++) {
                Object prop = response.getProperty(i);
                
                if (prop instanceof SoapObject) {
                    SoapObject soapObj = (SoapObject) prop;
                    

                    try {
                        Object returnObj = soapObj.getProperty("return");
                        if (returnObj != null) {
                            parseReturnObject(returnObj, comptes);
                        }
                    } catch (Exception e) {

                    }
                    

                    if (soapObj.hasProperty("id") || soapObj.hasProperty("solde")) {
                        comptes.add(parseCompte(soapObj));
                    }
                    

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

    private void findAndParseAllReturns(SoapObject soapObj, List<Compte> comptes) {
        try {
            if (soapObj.hasProperty("id") || soapObj.hasProperty("solde")) {
                comptes.add(parseCompte(soapObj));
                return;
            }
            
            for (int i = 0; i < soapObj.getPropertyCount(); i++) {
                Object prop = soapObj.getProperty(i);
                
                if (prop instanceof SoapObject) {
                    SoapObject childSoapObj = (SoapObject) prop;
                    
                    String propName = soapObj.getPropertyInfo(i).getName();
                    if ("return".equals(propName)) {
                        comptes.add(parseCompte(childSoapObj));
                    } else {
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


    private void parseReturnObject(Object returnObj, List<Compte> comptes) {
        if (returnObj instanceof SoapObject) {
            SoapObject soapCompte = (SoapObject) returnObj;
            if (soapCompte.hasProperty("id") || soapCompte.hasProperty("solde")) {
                comptes.add(parseCompte(soapCompte));
            } else {
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
        }

        Double solde = 0.0;
        try {
            if (soldeStr != null && !soldeStr.isEmpty()) {
                solde = Double.parseDouble(soldeStr);
            }
        } catch (NumberFormatException e) {

        }

        Date dateCreation = new Date();
        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dateCreation = sdf.parse(dateStr);
            }
        } catch (Exception e) {
        }

        TypeCompte type = TypeCompte.COURANT;
        try {
            if (typeStr != null && !typeStr.isEmpty()) {
                type = TypeCompte.valueOf(typeStr);
            }
        } catch (IllegalArgumentException e) {
        }

        return new Compte(id, solde, dateCreation, type);
    }


    public boolean createCompte(double solde, TypeCompte type) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_CREATE_COMPTE);
        
        request.addProperty("solde", solde);
        request.addProperty("type", type.name());

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.setOutputSoapObject(request);
        
        MarshalFloat marshal = new MarshalFloat();
        marshal.register(envelope);

        HttpTransportSE transport = new HttpTransportSE(URL);
        String soapAction = "";

        try {
            android.util.Log.d("SOAP", "Tentative d'appel SOAP à: " + URL);
            transport.call(soapAction, envelope);
            
            Object response = envelope.bodyIn;
            if (response != null) {
                android.util.Log.d("SOAP", "Réponse reçue: " + response.getClass().getName());
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


    public boolean deleteCompte(long id) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_DELETE_COMPTE);
        
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
            
            if (response instanceof SoapObject) {
                SoapObject soapResponse = (SoapObject) response;
                Object returnObj = soapResponse.getProperty("return");
                if (returnObj instanceof Boolean) {
                    return (Boolean) returnObj;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("SOAP", "Erreur deleteCompte: " + e.getMessage(), e);
            return false;
        }
    }


    public boolean updateCompte(long id, double solde, TypeCompte type) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_UPDATE_COMPTE);
        
        PropertyInfo idProperty = new PropertyInfo();
        idProperty.setName("id");
        idProperty.setValue(id);
        idProperty.setType(Long.class);
        request.addProperty(idProperty);
        
        PropertyInfo soldeProperty = new PropertyInfo();
        soldeProperty.setName("solde");
        soldeProperty.setValue(solde);
        soldeProperty.setType(Double.class);
        request.addProperty(soldeProperty);
        
        request.addProperty("type", type.name());

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.setOutputSoapObject(request);
        
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


    private String getPropertySafelyAsString(SoapObject soapObject, String propertyName) {
        try {
            Object property = soapObject.getProperty(propertyName);
            return property != null ? property.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}