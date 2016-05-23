package com.mclr.mini.recordatorios;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Date;

public class RecordatoriosActivity extends AppCompatActivity {

    private ListView mListView;
    private RecordatoriosDbAdapter mDbAdapter;
    private RecordatoriosSimpleCursorAdapter mCursorAdapter;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordatorios);
        mListView = (ListView) findViewById(R.id.recordatorios_list_view);
        mListView.setDivider(null);
        mDbAdapter = new RecordatoriosDbAdapter(this);
        mDbAdapter.open();
        if (savedInstanceState == null) {
            //Clear all data
            mDbAdapter.borraTodosLosRecordatorios();
            //Add some data
            insertaAlgunosRecordatorios();
        }

        Cursor cursor = mDbAdapter.fetchTodosLosRecordatorios();
        //from columns defined in the db
        String[] from = new String[]{
                RecordatoriosDbAdapter.COL_CONTENT
        };
        //to the ids of views in the layout
        int[] to = new int[]{
                R.id.texto_fila
        };
        mCursorAdapter = new RecordatoriosSimpleCursorAdapter(
                //context
                RecordatoriosActivity.this,
                //the layout of the row
                R.layout.fila_recordatorio,
                //cursor
                cursor,
                //from columns defined in the db
                from,
                //to the ids of views in the layout
                to,
                //flag - not used
                0);
        //the cursorAdapter (controller) is now updating the listView (view)
        //with data from the db (model)
        mListView.setAdapter(mCursorAdapter);

        //when we click an individual item in the listview
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RecordatoriosActivity.this);
                ListView modeListView = new ListView(RecordatoriosActivity.this);
                String[] modes = new String[]{"Edita Recordatorio", "Borra Recordatorio", "Programa Recordatorio"};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(RecordatoriosActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        int nId = getIdFromPosition(masterListPosition);
                        final Recordatorio recordatorio = mDbAdapter.fetchRecordatorioPorId(nId);
                        //edita recordatorio
                        if (position == 0) {
                            fireCustomDialog(recordatorio);
                            //borra recordatorio
                        } else if (position == 1) {
                            mDbAdapter.borraRecordatorioPorId(getIdFromPosition(masterListPosition));
                            mCursorAdapter.changeCursor(mDbAdapter.fetchTodosLosRecordatorios());
                        } else {
                            final Date today = new Date();
                            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                    Date alarm = new Date(today.getYear(), today.getMonth(), today.getDate(), hour,
                                            minute);
                                    programaRecordatorio(alarm.getTime(), recordatorio.getContent());
                                }
                            };
                            new TimePickerDialog(RecordatoriosActivity.this, listener,today.getHours(), today.getMinutes(), false).show();
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean
                        checked) {
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cam_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_borra_recordatorio:
                            for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
                                if (mListView.isItemChecked(nC)) {
                                    mDbAdapter.borraRecordatorioPorId(getIdFromPosition(nC));
                                }
                            }
                            mode.finish();
                            mCursorAdapter.changeCursor(mDbAdapter.fetchTodosLosRecordatorios());
                            return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
            });
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void programaRecordatorio(long time, String content) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, RecordatorioAlarma.class);
        alarmIntent.putExtra(RecordatorioAlarma.TEXTO_RECORDATORIO, content);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, broadcast);
    }

    private int getIdFromPosition(int nC) {
        return (int) mCursorAdapter.getItemId(nC);
    }

    private void insertaAlgunosRecordatorios() {
        mDbAdapter.crearRecordatorio("Entrenamiento matinal con Luis", true);
        mDbAdapter.crearRecordatorio("Comprar regalo de mamá", false);
        mDbAdapter.crearRecordatorio("Preparar libros para curso de Java", false);
        mDbAdapter.crearRecordatorio("Junta en la Dirección", false);
        mDbAdapter.crearRecordatorio("Limpiar escritorio", false);
        mDbAdapter.crearRecordatorio("Recoger trajes de la tintorería", true);
        mDbAdapter.crearRecordatorio("Entregarle libro de Android a Carmen", false);
        mDbAdapter.crearRecordatorio("Leer sección de Finanzas", false);
        mDbAdapter.crearRecordatorio("Llevar a lavar el auto", false);
        mDbAdapter.crearRecordatorio("Llamarle a papá por la noche", true);
        mDbAdapter.crearRecordatorio("Comprar boletos para la ópera", false);
        mDbAdapter.crearRecordatorio("Invitar un café a mi secretaría", false);
        mDbAdapter.crearRecordatorio("Revisar aplicación de cambio de divisas", false);
        mDbAdapter.crearRecordatorio("Felicitar a Peter por su cumpleaños", false);
        mDbAdapter.crearRecordatorio("Pagar 200 pesos a Miguel", true);


    }

    private void fireCustomDialog(final Recordatorio recordatorio) {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogo_personalizado);
        TextView titleView = (TextView) dialog.findViewById(R.id.titulo_dialogo);
        final EditText editCustom = (EditText) dialog.findViewById(R.id.edita_recordatorio_dialogo);
        Button commitButton = (Button) dialog.findViewById(R.id.boton_confirmar_dialogo);
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.es_importante_dialogo);
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.layout_raiz_dialogo);
        final boolean isEditOperation = (recordatorio != null);
        //this is for an edit
        if (isEditOperation) {
            titleView.setText("Edita Recordatorio");
            checkBox.setChecked(recordatorio.getImportant() == 1);
            editCustom.setText(recordatorio.getContent());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoRecordatorio = editCustom.getText().toString();
                if (isEditOperation) {
                    Recordatorio recordatorioEditado = new Recordatorio(recordatorio.getId(),
                            textoRecordatorio, checkBox.isChecked() ? 1 : 0);
                    mDbAdapter.actualizaRecordatorio(recordatorioEditado);
                    //este es para un nuevo Recordatorio
                } else {
                    mDbAdapter.crearRecordatorio(textoRecordatorio, checkBox.isChecked());
                }
                mCursorAdapter.changeCursor(mDbAdapter.fetchTodosLosRecordatorios());
                dialog.dismiss();
            }
        });
        Button buttonCancel = (Button) dialog.findViewById(R.id.boton_cancelar_dialogo);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú; esto agraga elementos al action bar si esta presente.
        getMenuInflater().inflate(R.menu.menu_recordatorios, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accion_nuevo:
                //crear nuevo recordatorio
                fireCustomDialog(null);
                return true;
            case R.id.accion_about:
                //caja de dialogo About
                fireAbooutDialog();
                return true;
            case R.id.accion_salir:
                finish();
                return true;
            default:
                return false;
        }
    }

    private void fireAbooutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogo_about);
        dialog.show();
    }
}
