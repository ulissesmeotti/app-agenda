package com.example.agendaservicos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.example.agendaservicos.database.Banco;
import com.example.agendaservicos.database.DatabaseClient;
import com.example.agendaservicos.dao.AgendamentoDAO;
import com.example.agendaservicos.dados.Agendamento;
import com.example.agendaservicos.dados.Servico;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<Agendamento> adapter;
    ArrayList<Agendamento> agendamentos;

    Banco bd;
    AgendamentoDAO dao;

    ListView lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lista = (ListView) findViewById(R.id.lista_servicos);

        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Agendamento agendamento = (Agendamento) parent.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, TelaAgendamento.class);
                intent.putExtra("agendamento", agendamento);

                startActivity(intent);

                return true;
            }
        });

        agendamentos = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                agendamentos);
        lista.setAdapter( adapter );
    }

    @Override
    public void onStart(){
        super.onStart();
        bd = DatabaseClient.getInstance(this).getDatabase();
        dao = bd.getAgendamentoDAO();

        dao.listar().observe(this, new ObservadorAgendamento());
    }

    public void onStop() {
        bd.close();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    public void cadastrarServicos(MenuItem mi) {
        Intent it = new Intent(this, TelaServicos.class);
        startActivity(it);
    }

    public void sair(MenuItem mi) {
        finish();
    }

    public void agendamentos(View v) {
        Intent it = new Intent(this, TelaAgendamento.class);
        startActivity(it);
    }


    class ObservadorAgendamento implements Observer<List<Agendamento>> {
        @Override
        public void onChanged(List<Agendamento> agendamentos) {
            MainActivity.this.agendamentos.clear();
            MainActivity.this.agendamentos.addAll( agendamentos );
            adapter.notifyDataSetChanged();
        }
    }
}