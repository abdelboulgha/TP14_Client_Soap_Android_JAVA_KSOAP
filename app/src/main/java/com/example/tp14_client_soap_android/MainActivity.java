package com.example.tp14_client_soap_android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.tp14_client_soap_android.adapter.CompteAdapter;
import com.example.tp14_client_soap_android.beans.Compte;
import com.example.tp14_client_soap_android.beans.TypeCompte;
import com.example.tp14_client_soap_android.ws.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button btnAdd;
    private CompteAdapter adapter;
    private Service service;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();
        service = new Service();
        adapter = new CompteAdapter();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadComptes();
    }


    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.fabAdd);
    }


    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnDeleteClickListener(compte -> {
            if (compte == null || compte.getId() == null) {
                Toast.makeText(MainActivity.this, "Erreur: Compte invalide.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Supprimer le compte")
                    .setMessage("Voulez-vous vraiment supprimer ce compte ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        executorService.execute(() -> {
                            boolean success = service.deleteCompte(compte.getId());
                            runOnUiThread(() -> {
                                if (success) {
                                    adapter.removeCompte(compte);
                                    Toast.makeText(MainActivity.this, "Compte supprimé.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Erreur lors de la suppression.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
        
        adapter.setOnEditClickListener(compte -> {
            if (compte == null || compte.getId() == null) {
                Toast.makeText(MainActivity.this, "Erreur: Compte invalide.", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditCompteDialog(compte);
        });
    }


    private void setupListeners() {
        btnAdd.setOnClickListener(v -> showAddCompteDialog());
    }


    private void showAddCompteDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.popup, null);

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("Nouveau compte")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    TextInputEditText etSolde = dialogView.findViewById(R.id.etSolde);
                    RadioButton radioCourant = dialogView.findViewById(R.id.radioCourant);

                    if (etSolde == null || radioCourant == null) {
                        Toast.makeText(MainActivity.this, "Erreur: Éléments du formulaire non trouvés", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final double solde;
                    try {
                        String soldeText = etSolde.getText() != null ? etSolde.getText().toString() : "";
                        if (!soldeText.isEmpty()) {
                            solde = Double.parseDouble(soldeText);
                        } else {
                            Toast.makeText(MainActivity.this, "Veuillez entrer un solde", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Veuillez entrer un solde valide", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final TypeCompte type = radioCourant.isChecked() ? TypeCompte.COURANT : TypeCompte.EPARGNE;

                    executorService.execute(() -> {
                        try {
                            boolean success = service.createCompte(solde, type);
                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(MainActivity.this, "Compte ajouté.", Toast.LENGTH_SHORT).show();
                                    loadComptes();
                                } else {
                                    Toast.makeText(MainActivity.this, "Erreur lors de l'ajout.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            });
                        }
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }


    private void loadComptes() {
        executorService.execute(() -> {
            try {
                android.util.Log.d("MainActivity", "Début du chargement des comptes");
                java.util.List<Compte> comptes = service.getComptes();
                android.util.Log.d("MainActivity", "Comptes récupérés: " + (comptes != null ? comptes.size() : "NULL"));
                runOnUiThread(() -> {
                    if (comptes != null && !comptes.isEmpty()) {
                        android.util.Log.d("MainActivity", "Mise à jour de l'adapter avec " + comptes.size() + " comptes");
                        adapter.updateComptes(comptes);
                    } else {
                        android.util.Log.w("MainActivity", "Aucun compte trouvé");
                        Toast.makeText(MainActivity.this, "Aucun compte trouvé.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Erreur lors du chargement: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
            }
        });
    }


    private void showEditCompteDialog(Compte compte) {
        View dialogView = getLayoutInflater().inflate(R.layout.popup, null);
        
        TextInputEditText etSolde = dialogView.findViewById(R.id.etSolde);
        RadioButton radioCourant = dialogView.findViewById(R.id.radioCourant);
        RadioButton radioEpargne = dialogView.findViewById(R.id.radioEpargne);
        
        if (etSolde == null || radioCourant == null || radioEpargne == null) {
            Toast.makeText(MainActivity.this, "Erreur: Éléments du formulaire non trouvés", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (compte.getSolde() != null) {
            etSolde.setText(String.valueOf(compte.getSolde()));
        }
        
        if (compte.getType() == TypeCompte.COURANT) {
            radioCourant.setChecked(true);
        } else {
            radioEpargne.setChecked(true);
        }

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("Modifier le compte")
                .setPositiveButton("Modifier", (dialog, which) -> {
                    final double solde;
                    try {
                        String soldeText = etSolde.getText() != null ? etSolde.getText().toString() : "";
                        if (!soldeText.isEmpty()) {
                            solde = Double.parseDouble(soldeText);
                        } else {
                            Toast.makeText(MainActivity.this, "Veuillez entrer un solde", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Veuillez entrer un solde valide", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final TypeCompte type = radioCourant.isChecked() ? TypeCompte.COURANT : TypeCompte.EPARGNE;
                    final Long compteId = compte.getId();

                    executorService.execute(() -> {
                        try {
                            boolean success = service.updateCompte(compteId, solde, type);
                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(MainActivity.this, "Compte modifié.", Toast.LENGTH_SHORT).show();
                                    loadComptes();
                                } else {
                                    Toast.makeText(MainActivity.this, "Erreur lors de la modification.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            });
                        }
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}