package br.edu.ifspsaocarlos.agendafirebase.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifspsaocarlos.agendafirebase.model.Contato;
import br.edu.ifspsaocarlos.agendafirebase.R;


public class DetalheActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Contato contato;
    private DatabaseReference databaseReference;
    String FirebaseID;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        spinner = (Spinner) findViewById(R.id.spinner);
        final List<String> categorias = new ArrayList<String>();
        categorias.add("Amigo");
        categorias.add("Familia");
        categorias.add("Trabalho");
        categorias.add("Outro");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categorias);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        if (getIntent().hasExtra("FirebaseID")) {
            FirebaseID=getIntent().getStringExtra("FirebaseID");
              databaseReference.child(FirebaseID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    contato = snapshot.getValue(Contato.class);

                    if (contato != null) {
                        EditText nameText = (EditText) findViewById(R.id.editTextNome);
                        nameText.setText(contato.getNome());

                        EditText foneText = (EditText) findViewById(R.id.editTextFone);
                        foneText.setText(contato.getFone());

                        EditText emailText = (EditText) findViewById(R.id.editTextEmail);
                        emailText.setText(contato.getEmail());
                        spinner.setSelection(categorias.indexOf(contato.getTipoContato()));
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detalhe, menu);
        if (FirebaseID==null) {
            MenuItem item = menu.findItem(R.id.delContato);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.salvarContato:
                salvar();
                return true;
            case R.id.delContato:
                apagar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void apagar() {
        databaseReference.child(FirebaseID).removeValue();
        Intent resultIntent = new Intent();
        setResult(3,resultIntent);
        finish();
    }

    private void salvar() {
        String name = ((EditText) findViewById(R.id.editTextNome)).getText().toString();
        String fone = ((EditText) findViewById(R.id.editTextFone)).getText().toString();
        String email = ((EditText) findViewById(R.id.editTextEmail)).getText().toString();
        String tipoContato = spinner.getSelectedItem().toString();

        if (contato ==null) {
            contato = new Contato();
            contato.setNome(name);
            contato.setFone(fone);
            contato.setEmail(email);
            contato.setTipoContato(tipoContato);
            databaseReference.push().setValue(contato);
        } else {
            contato.setNome(name);
            contato.setFone(fone);
            contato.setEmail(email);
            contato.setTipoContato(tipoContato);
            databaseReference.child(FirebaseID).setValue(contato);
        }
        Intent resultIntent = new Intent();
        setResult(RESULT_OK,resultIntent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String tipoContato = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

