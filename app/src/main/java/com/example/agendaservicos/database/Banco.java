package com.example.agendaservicos.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.agendaservicos.converters.DateConverter;
import com.example.agendaservicos.dados.Agendamento;
import com.example.agendaservicos.dados.ItemAgendamento;
import com.example.agendaservicos.dados.Servico;
import com.example.agendaservicos.dao.AgendamentoDAO;
import com.example.agendaservicos.dao.ItemAgendamentoDAO;
import com.example.agendaservicos.dao.ServicoDAO;

@Database(entities = {Servico.class, Agendamento.class, ItemAgendamento.class}, version = 3)
@TypeConverters({DateConverter.class})
public abstract class Banco extends RoomDatabase {
    public abstract AgendamentoDAO getAgendamentoDAO();
    public abstract ServicoDAO getServicoDAO();
    public abstract ItemAgendamentoDAO getItemAgendamentoDAO();

}