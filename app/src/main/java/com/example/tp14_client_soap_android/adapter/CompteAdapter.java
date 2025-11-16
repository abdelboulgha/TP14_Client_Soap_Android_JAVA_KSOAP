package com.example.tp14_client_soap_android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tp14_client_soap_android.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import com.example.tp14_client_soap_android.beans.Compte;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.CompteViewHolder> {
    private List<Compte> comptes = new ArrayList<>();
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Compte compte);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Compte compte);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    /**
     * Met à jour la liste des comptes affichés.
     */
    public void updateComptes(List<Compte> newComptes) {
        comptes.clear();
        comptes.addAll(newComptes);
        notifyDataSetChanged();
    }

    /**
     * Supprime un compte de la liste.
     */
    public void removeCompte(Compte compte) {
        int position = comptes.indexOf(compte);
        if (position >= 0) {
            comptes.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Met à jour un compte existant dans la liste.
     */
    public void updateCompte(Compte compte) {
        int position = -1;
        for (int i = 0; i < comptes.size(); i++) {
            if (comptes.get(i).getId() != null && comptes.get(i).getId().equals(compte.getId())) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            comptes.set(position, compte);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public CompteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new CompteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompteViewHolder holder, int position) {
        holder.bind(comptes.get(position));
    }

    @Override
    public int getItemCount() {
        return comptes.size();
    }

    class CompteViewHolder extends RecyclerView.ViewHolder {
        private TextView textId;
        private TextView textSolde;
        private Chip textType;
        private TextView textDate;
        private MaterialButton btnEdit;
        private MaterialButton btnDelete;

        CompteViewHolder(@NonNull View itemView) {
            super(itemView);
            textId = itemView.findViewById(R.id.textId);
            textSolde = itemView.findViewById(R.id.textSolde);
            textType = itemView.findViewById(R.id.textType);
            textDate = itemView.findViewById(R.id.textDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Compte compte) {
            if (compte == null) {
                return;
            }
            
            // Gérer les valeurs null
            String idText = compte.getId() != null ? String.valueOf(compte.getId()) : "N/A";
            textId.setText("Compte Numéro " + idText);
            
            String soldeText = compte.getSolde() != null ? String.valueOf(compte.getSolde()) : "0.0";
            textSolde.setText(soldeText + " DH");
            
            if (compte.getType() != null) {
                textType.setText(compte.getType().name());
            }
            
            if (compte.getDateCreation() != null) {
                textDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(compte.getDateCreation()));
            } else {
                textDate.setText("N/A");
            }

            btnEdit.setOnClickListener(v -> {
                if (onEditClickListener != null) {
                    onEditClickListener.onEditClick(compte);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(compte);
                }
            });
        }
    }
}
