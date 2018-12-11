package br.edu.ifspsaocarlos.agendafirebase.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import br.edu.ifspsaocarlos.agendafirebase.R;
import br.edu.ifspsaocarlos.agendafirebase.adapter.ContatoAdapter;
import br.edu.ifspsaocarlos.agendafirebase.model.Contato;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private TextView empty;
    private ProgressBar progressBar;

    private ContatoAdapter adapter;
    private SearchView searchView;

    private FloatingActionButton fab;

    private DatabaseReference databaseReference;
    private Query query;
    private FirebaseRecyclerOptions<Contato> options;


    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.onActionViewCollapsed();
            updateUI(null, null);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            updateUI(query, null);
            searchView.clearFocus();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        handleIntent(intent);

        empty= (TextView) findViewById(R.id.empty_view);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);


        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layout);


        databaseReference = FirebaseDatabase.getInstance().getReference();


        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.getChildrenCount()==0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), DetalheActivity.class);
                startActivityForResult(i, 1);
            }
        });

        updateUI(null, null);
        setupRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.pesqContato).getActionView();
        ImageView closeButton = (ImageView)searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText)findViewById(R.id.search_src_text);
                if (et.getText().toString().isEmpty())
                    searchView.onActionViewCollapsed();
                searchView.setQuery("", false);
                updateUI(null, null);
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.todos:
                updateUI(null, null);
                return true;
            case R.id.amigos:
                updateUI(null, "Amigo");
                return true;
            case R.id.familia:
                updateUI(null, "Familia");
                return true;
            case R.id.trabalho:
                updateUI(null, "Trabalho");
                return true;
            case R.id.outro:
                updateUI(null, "Outro");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1)
            if (resultCode == RESULT_OK) {
                showSnackBar(getResources().getString(R.string.contato_adicionado));

            }

        if (requestCode == 2) {
            if (resultCode == RESULT_OK)
                showSnackBar(getResources().getString(R.string.contato_alterado));
            if (resultCode == 3)
                showSnackBar(getResources().getString(R.string.contato_apagado));


        }
    }

    private void showSnackBar(String msg) {
        CoordinatorLayout coordinatorlayout= (CoordinatorLayout)findViewById(R.id.coordlayout);
        Snackbar.make(coordinatorlayout, msg,
                Snackbar.LENGTH_LONG)
                .show();
    }

    private void updateUI(String nomeContato, String tipoContato)
    {
        if (nomeContato == null && tipoContato == null) {
            query = databaseReference.orderByChild("nome");
        } else {
            if(tipoContato == null) {
                query = databaseReference.orderByChild("nome").startAt(nomeContato).endAt(nomeContato.concat("\uf8ff"));
            } else {
                query = databaseReference.orderByChild("tipoContato").equalTo(tipoContato);
            }
        }

        options = new FirebaseRecyclerOptions.Builder<Contato>().setQuery(query, Contato.class).build();
        adapter = new ContatoAdapter(options);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        empty.setText(getResources().getString(R.string.lista_vazia));
        fab.show();
    }

    private void setupRecyclerView() {
        adapter.setClickListener(new ContatoAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(getApplicationContext(), DetalheActivity.class);
                i.putExtra("FirebaseID", adapter.getRef(position).getKey());
                startActivityForResult(i, 2);
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (swipeDir == ItemTouchHelper.RIGHT) {
                    adapter.getRef(viewHolder.getAdapterPosition()).removeValue();
                    showSnackBar(getResources().getString(R.string.contato_apagado));

                }
            }


            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                Paint p = new Paint();
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(ContextCompat.getColor(getBaseContext(), R.color.colorDelete));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_account_remove);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }



        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


    }



    protected void onStart() {
        super.onStart();
        adapter.startListening();


    }

    protected void onResume() {
        super.onResume();
        adapter.startListening();


    }

    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }


}