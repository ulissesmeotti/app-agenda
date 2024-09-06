package com.example.agendaservicos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.example.agendaservicos.database.Banco;
import com.example.agendaservicos.database.DatabaseClient;
import com.example.agendaservicos.dao.ServicoDAO;
import com.example.agendaservicos.dados.Servico;
import java.util.ArrayList;
import java.util.List;

public class TelaServicos extends AppCompatActivity {

    ArrayAdapter<Servico> adapter;
    ArrayList<Servico> servicos;

    Banco bd;
    ServicoDAO dao;

    ListView lista;
    EditText edNome, edUnidade, edPreco;
    Servico servicoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_servicos);

        lista = findViewById(R.id.lista_tipos_servico);
        edNome = findViewById(R.id.ed_nome_servico);
        edUnidade = findViewById(R.id.ed_unidade_medida);
        edPreco = findViewById(R.id.ed_preco);

        servicos = new ArrayList<>();
        adapter = new ArrayAdapter<Servico>(this, R.layout.list_item_servico, R.id.servico_descricao, servicos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_servico, parent, false);
                }

                Servico servico = getItem(position);

                TextView descricao = convertView.findViewById(R.id.servico_descricao);
                Button btnEditar = convertView.findViewById(R.id.btn_editar);
                Button btnRemover = convertView.findViewById(R.id.btn_remover);

                descricao.setText(servico.getDescricao());

                btnEditar.setOnClickListener(v -> editarServico(servico));
                btnRemover.setOnClickListener(v -> removerServico(servico));

                return convertView;
            }
        };
        lista.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bd = DatabaseClient.getInstance(this).getDatabase();
        dao = bd.getServicoDAO();

        dao.listar().observe(this, new ObservadorServico());
    }

    @Override
    public void onStop() {
        bd.close();
        super.onStop();
    }

    public void confirmar(View v) {
        if (!validarCampos()) {
            return;
        }
        if (servicoAtual == null) {
            Servico s = new Servico();
            s.setDescricao(edNome.getText().toString());
            s.setUnidadeMedida(edUnidade.getText().toString());
            s.setValor(Integer.parseInt(edPreco.getText().toString()));

            new Thread() {
                public void run() {
                    Looper.prepare();
                    dao.inserir(s);
                    Looper.loop();
                }
            }.start();
        } else {
            servicoAtual.setDescricao(edNome.getText().toString());
            servicoAtual.setUnidadeMedida(edUnidade.getText().toString());
            servicoAtual.setValor(Double.parseDouble(edPreco.getText().toString()));

            new Thread() {
                public void run() {
                    Looper.prepare();
                    dao.alterar(servicoAtual);
                    Looper.loop();
                }
            }.start();
        }
        limparCampos();
    }


    private void editarServico(Servico servico) {
        servicoAtual = servico;
        edNome.setText(servico.getDescricao());
        edUnidade.setText(servico.getUnidadeMedida());
        edPreco.setText(String.valueOf(servico.getValor()));
    }

    private void removerServico(Servico servico) {
        new AlertDialog.Builder(this)
                .setTitle("Remover Serviço")
                .setMessage("Deseja remover este serviço?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    new Thread() {
                        public void run() {
                            Looper.prepare();
                            dao.remover(servico);
                            Looper.loop();
                        }
                    }.start();
                })
                .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void limparCampos(){
        edNome.setText("");
        edUnidade.setText("");
        edPreco.setText("");
    }
    private boolean validarCampos() {
        if (edNome.getText().toString().trim().isEmpty()) {
            mostrarMensagemErro("Nome do serviço é obrigatório.");
            return false;
        }
        if (edUnidade.getText().toString().trim().isEmpty()) {
            mostrarMensagemErro("Unidade de medida é obrigatória.");
            return false;
        }
        try {
            Double.parseDouble(edPreco.getText().toString().trim());
        } catch (NumberFormatException e) {
            mostrarMensagemErro("Preencha com um número válido.");
            return false;
        }
        return true;
    }

    private void mostrarMensagemErro(String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    class ObservadorServico implements Observer<List<Servico>> {
        @Override
        public void onChanged(List<Servico> servicos) {
            TelaServicos.this.servicos.clear();
            TelaServicos.this.servicos.addAll(servicos);
            adapter.notifyDataSetChanged();
        }
    }
}