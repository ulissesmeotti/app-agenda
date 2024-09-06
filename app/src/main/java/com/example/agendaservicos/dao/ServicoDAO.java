package com.example.agendaservicos.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.agendaservicos.dados.Servico;

import java.util.List;

@Dao
public interface ServicoDAO {

    @Insert
    public void inserir(Servico serv);

    @Update
    public void alterar(Servico serv);

    @Delete
    public void remover(Servico serv);

    @Query("SELECT * FROM Servico WHERE id = :id")
    Servico listarPorId(long id);

    @Query("select * from servico")
    public LiveData<List<Servico>> listar();
}