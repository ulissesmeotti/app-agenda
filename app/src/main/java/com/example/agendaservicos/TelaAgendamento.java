package com.example.agendaservicos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.agendaservicos.dao.ItemAgendamentoDAO;
import com.example.agendaservicos.database.Banco;
import com.example.agendaservicos.database.DatabaseClient;
import com.example.agendaservicos.dao.AgendamentoDAO;
import com.example.agendaservicos.dao.ServicoDAO;
import com.example.agendaservicos.dados.Agendamento;
import com.example.agendaservicos.dados.ItemAgendamento;
import com.example.agendaservicos.dados.Servico;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TelaAgendamento extends AppCompatActivity {
    public static SimpleDateFormat dtFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    Agendamento agendamento;

    ArrayAdapter<ItemAgendamento> adapter;
    ArrayList<ItemAgendamento> itemAgendamentos;
    boolean editando = false;

    Banco bd;
    AgendamentoDAO dao;
    ServicoDAO servicoDao;
    ItemAgendamentoDAO itemDao;

    ListView lista;
    EditText edCliente, edEndereco, edQuantidade;
    TextView txtDataHora, txtUnidade, txtValor, txtValorTotal;
    Spinner opcoes_servico;
    Servico servicoSelecionado;
    Button btnCancelar;


    Date dataAgendamento = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_agendamento);
        lista = (ListView) findViewById(R.id.lista_servico);
        edCliente = (EditText) findViewById(R.id.ed_nome_cliente);
        edEndereco = (EditText) findViewById(R.id.ed_endereco);
        edQuantidade = (EditText) findViewById(R.id.ed_quantidade);
        txtDataHora = (TextView) findViewById(R.id.data_hora);
        txtUnidade = (TextView) findViewById(R.id.txt_unidade);
        txtValor = (TextView) findViewById(R.id.txt_valor);
        opcoes_servico = (Spinner) findViewById(R.id.opcoes_servico);
        btnCancelar = (Button) findViewById(R.id.btn_cancelar);

        Intent intent = getIntent();
        agendamento = (Agendamento) intent.getSerializableExtra("agendamento");

        itemAgendamentos = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice,
                itemAgendamentos);
        lista.setAdapter(adapter);

        edQuantidade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                atualizarValorTotal();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        opcoes_servico.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                servicoSelecionado = (Servico) parent.getItemAtPosition(position);
                if (servicoSelecionado != null) {
                    txtUnidade.setText(servicoSelecionado.getUnidadeMedida());
                    txtValor.setText(String.valueOf(servicoSelecionado.getValor()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ItemAgendamento itemSelecionado = itemAgendamentos.get(position);
                mostrarDialogoRemover(itemSelecionado);
                return true;
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        bd = DatabaseClient.getInstance(this).getDatabase();
        dao = bd.getAgendamentoDAO();
        servicoDao = bd.getServicoDAO();
        itemDao = bd.getItemAgendamentoDAO();
        carregarServicos();

        if (agendamento != null) {
            editando = true;
            edCliente.setText(agendamento.getNomeCliente());
            edEndereco.setText(agendamento.getEndereco());
            txtDataHora.setText(dtFormater.format(agendamento.getDataHora()));
            carregarItensAgendamento(agendamento.getId());
            btnCancelar.setVisibility(View.VISIBLE);
        }

    }

    public void onStop() {
        bd.close();
        super.onStop();
    }

    public void lerData(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int ano, int mes, int dia) {
                dataAgendamento = new Date(ano - 1900, mes, dia);

                showTimePickerDialog();
            }
        });
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hora, int minuto) {
                if (dataAgendamento != null) {
                    dataAgendamento.setHours(hora);
                    dataAgendamento.setMinutes(minuto);
                    dataAgendamento.setSeconds(0);
                    txtDataHora.setText(dtFormater.format(dataAgendamento));
                }
            }
        }, 0, 0, true);
        timePickerDialog.show();
    }


    public void adicionar(View v) {
        if (servicoSelecionado != null && !edQuantidade.getText().toString().isEmpty()) {
            double quantidade = Double.parseDouble(edQuantidade.getText().toString());
            double valorUnitario = servicoSelecionado.getValor();
            double valorTotal = quantidade * valorUnitario;
            ItemAgendamento itemAgendamento = new ItemAgendamento();
            itemAgendamento.setId_servico(servicoSelecionado.getId());
            itemAgendamento.setQuantidade(quantidade);
            itemAgendamento.setValorItem(valorTotal);
            itemAgendamento.setServico(servicoSelecionado);
            itemAgendamentos.add(itemAgendamento);
            adapter.notifyDataSetChanged();
            edQuantidade.setText("1");
        }
    }


    public void confirmar(View v) {
        long agendamentoId;

        if (dataAgendamento == null) {
            return;
        }

        Agendamento ag = editando ? agendamento : new Agendamento();
        ag.setNomeCliente(edCliente.getText().toString());
        ag.setEndereco(edEndereco.getText().toString());
        ag.setDataHora(dataAgendamento);
        ag.setValorTotal(calcularValorTotal());
        agendamentoId = dao.inserir(ag);


        new Thread() {
            public void run() {
                if (editando) {
                    dao.alterar(ag);

                    for (ItemAgendamento itemAgendamento : itemAgendamentos) {
                        if (itemAgendamento.getId() != 0) {
                            itemDao.alterar(itemAgendamento);
                        } else {
                            itemAgendamento.setId_agendamento(ag.getId());
                            itemDao.inserir(itemAgendamento);
                        }
                    }
                } else {
                    for (ItemAgendamento itemAgendamento : itemAgendamentos) {
                        itemAgendamento.setId_agendamento(agendamentoId);
                        itemDao.inserir(itemAgendamento);
                    }
                }
            }
        }.start();

        limparCampos();
    }

    public void cancelar(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Excluir agendamento");

        builder.setMessage("Tem certeza que deseja excluir o agendamento");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread() {
                    public void run() {
                        dao.remover(agendamento);
                        finish();
                    }
                }.start();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    private void carregarServicos() {
        servicoDao.listar().observe(this, new Observer<List<Servico>>() {
            @Override
            public void onChanged(List<Servico> servicos) {
                ArrayAdapter<Servico> servicosAdapter = new ArrayAdapter<Servico>(TelaAgendamento.this,
                        android.R.layout.simple_spinner_item, servicos) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView label = (TextView) super.getView(position, convertView, parent);
                        label.setText(getItem(position).getDescricao());
                        return label;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
                        label.setText(getItem(position).getDescricao());
                        return label;
                    }
                };
                servicosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                opcoes_servico.setAdapter(servicosAdapter);


            }
        });
    }

    private void atualizarValorTotal() {
        if (servicoSelecionado != null) {
            String quantidadeStr = edQuantidade.getText().toString();
            if (!quantidadeStr.isEmpty()) {
                try {
                    double quantidade = Double.parseDouble(quantidadeStr);
                    double valorUnitario = servicoSelecionado.getValor();
                    double valorTotal = quantidade * valorUnitario;
                    txtValor.setText(String.format("%.2f", valorTotal));
                } catch (NumberFormatException e) {
                    txtValor.setText("0.00");
                }
            } else {
                txtValor.setText("0.00");
            }
        }
    }

    private void carregarItensAgendamento(long id) {
        itemDao.listarPorAgendamento(id).observe(this, new Observer<List<ItemAgendamento>>() {
            @Override
            public void onChanged(List<ItemAgendamento> itens) {
                itemAgendamentos.clear();

                new Thread() {
                    public void run() {
                        Looper.prepare();

                        for (ItemAgendamento item : itens) {
                            long idServico = item.getId_servico();
                            Servico servico = servicoDao.listarPorId(idServico);
                            item.setServico(servico);
                            itemAgendamentos.add(item);
                        }
                        Looper.loop();
                    }
                }.start();

                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    public void abrirDialog(MenuItem mi) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edicao, null);

        double valorCalculado = calcularValorTotal();

        txtValorTotal = (TextView) dialogView.findViewById(R.id.txt_valor_total);
        txtValorTotal.setText("Valor restante: " + String.format("%.2f", valorCalculado));

        EditText txtValorRecebido = (EditText) dialogView.findViewById(R.id.valor_recebido);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lançar recebimento");
        builder.setView(dialogView);

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String valorRecebido = txtValorRecebido.getText().toString();
                lancarRecebimento(valorRecebido);
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void lancarRecebimento(String valor) {
        if (valor.isEmpty()) {
            Toast.makeText(this, "O valor recebido não pode estar vazio", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double valorRecebido = Double.parseDouble(valor);
            double valorTotal = calcularValorTotal();

            if (valorTotal <= 0) {
                Toast.makeText(this, "O valor total dos serviços deve ser maior que zero", Toast.LENGTH_SHORT).show();
                return;
            }

            if (valorRecebido > valorTotal) {
                Toast.makeText(this, "O valor recebido não pode ser maior do que o valor total dos serviços", Toast.LENGTH_SHORT).show();
                return;
            }

            double valorAtual = agendamento.getRecebido();

            agendamento.setRecebido(valorAtual + valorRecebido);
            new Thread(){
                public void run(){
                    dao.alterar(agendamento);
                }
            }.start();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "O valor recebido é inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private double calcularValorTotal() {
        double valorTotal = 0;
        for (ItemAgendamento item : itemAgendamentos) {
            valorTotal += item.getValorItem();
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Double> futureValorRecebido = executorService.submit(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                return dao.buscarPorValorRecebido(agendamento.getId());
            }
        });

        double valorRecebido = 0;
        try {
            valorRecebido = futureValorRecebido.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        return (valorTotal - valorRecebido) ;
    }

    private void mostrarDialogoRemover(final ItemAgendamento item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remover serviço");
        builder.setMessage("Tem certeza que deseja remover este serviço?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removerItem(item);
            }
        });


        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removerItem(ItemAgendamento item) {
        itemAgendamentos.remove(item);
        adapter.notifyDataSetChanged();

        new Thread() {
            public void run() {
                itemDao.remover(item);
            }
        }.start();
    }


    private void limparCampos() {
        edCliente.setText("");
        edEndereco.setText("");
        edQuantidade.setText("");
        txtValor.setText("");
    }

}