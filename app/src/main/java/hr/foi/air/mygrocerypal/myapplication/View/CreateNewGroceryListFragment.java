package hr.foi.air.mygrocerypal.myapplication.View;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import hr.foi.air.mygrocerypal.myapplication.Controller.CreateNewGroceryListController;
import hr.foi.air.mygrocerypal.myapplication.Controller.GroceryListController;
import hr.foi.air.mygrocerypal.myapplication.Controller.Listeners.StoresListener;
import hr.foi.air.mygrocerypal.myapplication.Core.CurrentUser;
import hr.foi.air.mygrocerypal.myapplication.Model.StoresModel;
import hr.foi.air.mygrocerypal.myapplication.R;

public class CreateNewGroceryListFragment extends Fragment implements StoresListener {

    private CreateNewGroceryListController createNewGroceryListController;
    private Spinner spinnerStores;
    //private static final String[] stores = {"Konzum", "Lidl", "Kaufland"};
    private  ArrayList<StoresModel> storesArray;
    private String selectedStore;

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private EditText address, town;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_new_grocerylist, container, false);

        TextView labelForStore = view.findViewById(R.id.labelForStore);

        spinnerStores = view.findViewById(R.id.spinnerStores);
        address = view.findViewById(R.id.address);
        town = view.findViewById(R.id.town);
        address.setText(CurrentUser.currentUser.getAddress());
        town.setText(CurrentUser.currentUser.getTown());
        address.setEnabled(false);
        town.setEnabled(false);
        radioGroup = view.findViewById(R.id.rgroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton = view.findViewById(checkedId);

                if(radioButton.getText().equals("Moja adresa")){
                    address.setEnabled(false);
                    town.setEnabled(false);
                    address.setText(CurrentUser.currentUser.getAddress());
                    town.setText(CurrentUser.currentUser.getTown());
                }
                else{
                    address.setEnabled(true);
                    town.setEnabled(true);
                    address.getText().clear();
                    town.getText().clear();
                    address.setHint("Upišite drugu adresu");
                    town.setHint("Upišite grad");
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        createNewGroceryListController = new CreateNewGroceryListController(this);
        createNewGroceryListController.getAllStores();
    }


    @Override
    public void storesReceived(ArrayList<StoresModel> stores) {
        if(stores != null){
            storesArray = stores;
            ArrayList<String> storeNames = new ArrayList<>();
            for(int i = 0; i < stores.size(); i++){
                storeNames.add(stores.get(i).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, storeNames);
            spinnerStores.setAdapter(adapter);
            spinnerStores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedStore = parent.getItemAtPosition(position).toString();
                    Toast.makeText(parent.getContext(),"Odabrani dućan: " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }


}
