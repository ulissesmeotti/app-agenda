package com.example.agendaservicos.dados;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(foreignKeys = {
        @ForeignKey(entity = Agendamento.class,
                parentColumns = "id",
                childColumns = "id_agendamento",
                onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Servico.class,
                parentColumns = "id",
                childColumns = "id_servico",
                onDelete = ForeignKey.CASCADE)
})
public class ItemAgendamento implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long id_agendamento;
    private long id_servico;
    private double quantidade;
    @ColumnInfo(name = "valor_item")
    private double valorItem;
    @Ignore
    private Servico servico;

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId_agendamento() {
        return id_agendamento;
    }

    public void setId_agendamento(long id_agendamento) {
        this.id_agendamento = id_agendamento;
    }

    public long getId_servico() {
        return id_servico;
    }

    public void setId_servico(long id_servico) {
        this.id_servico = id_servico;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public double getValorItem() {
        return valorItem;
    }

    public void setValorItem(double valorItem) {
        this.valorItem = valorItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemAgendamento)) return false;
        ItemAgendamento that = (ItemAgendamento) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return servico.getDescricao() + "\n" +
                servico.getUnidadeMedida() + " - R$ " +
                String.format("%.2f", valorItem);
    }

}