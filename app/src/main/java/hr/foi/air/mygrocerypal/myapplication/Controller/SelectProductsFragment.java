package hr.foi.air.mygrocerypal.myapplication.Controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.filter.FilterObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hr.foi.air.mygrocerypal.myapplication.Core.Adapters.SelectProductsAdapter;
import hr.foi.air.mygrocerypal.myapplication.FirebaseHelper.Listeners.SelectProductsListener;
import hr.foi.air.mygrocerypal.myapplication.FirebaseHelper.SelectProductsHelper;
import hr.foi.air.mygrocerypal.myapplication.Model.CategoriesModel;
import hr.foi.air.mygrocerypal.myapplication.Model.GroceryListProductsModel;
import hr.foi.air.mygrocerypal.myapplication.Model.ProductsModel;
import hr.foi.air.mygrocerypal.myapplication.R;

public class SelectProductsFragment extends Fragment implements SelectProductsListener{
    //lista vec dodanih proizvoda
    public List<GroceryListProductsModel> mListOfAddedProducts = new ArrayList<>();

    private Button btnAddProductsToGroceryList;
    private SelectProductsHelper mSelectProductsHelper;
    private RecyclerView mRecyclerView;
    private SelectProductsAdapter mSelectProductsAdapter;
    private ArrayList<ProductsModel> mProductsList;
    private SearchView mSearchView;
    private Spinner mSpinner;
    private TextView mNoneProducts;

    //pohrana liste filtriranih proizvoda
    ArrayList<ProductsModel> filteredList = new ArrayList<>();

    //genericka klasa za filtriranje proizvoda
    FilterObjects<ProductsModel> filterObjects = new FilterObjects<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_select_products, container, false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.addProducts));
        return rootView;
    }

    @Override
    public void onResume() {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.addProducts));
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSelectProductsHelper = new SelectProductsHelper(this);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        btnAddProductsToGroceryList = view.findViewById(R.id.addProductsToGroceryList);
        mSearchView = view.findViewById(R.id.searchView);
        mNoneProducts = view.findViewById(R.id.noneProducts);

        mSpinner = view.findViewById(R.id.spinner);
        mSelectProductsHelper.loadProductCategories();

        mSelectProductsHelper.loadProductsByStore(getArguments().getString("store_name"));
        mListOfAddedProducts = (List<GroceryListProductsModel>)getArguments().getSerializable("list_of_products");

        btnAddProductsToGroceryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ukoliko ne postoji prethodni fragment ne čini ništa!
                if(getFragmentManager().getBackStackEntryCount() > 0){
                    Intent intent = new Intent(getContext(), SelectProductsFragment.class);
                    intent.putExtra("groceryListOfProducts", (Serializable) mListOfAddedProducts);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    getFragmentManager().popBackStack();
                }
            }
        });

        //pretraga proizvoda upisom teksta u searchView
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mProductsList != null){
                    if(mSpinner.getSelectedItem().toString() != getResources().getString(R.string.chooseCategory)){
                        //filter by category
                        filteredList = filterObjects.filterListByCategories(mProductsList, mSpinner.getSelectedItem().toString());
                        //filter by search
                        filteredList = filterObjects.filterListByNames(filteredList, newText);
                    }else
                        filteredList = filterObjects.filterListByNames(mProductsList, newText);
                    inflateAdapter(filteredList);
                    setTextVisibility(filteredList);
                }else
                    mNoneProducts.setVisibility(View.GONE);
                return false;
            }
        });

        // pretraga proizvoda po kategoriji
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mProductsList != null){
                    if(mSpinner.getSelectedItem().toString() != getResources().getString(R.string.chooseCategory)){
                        //filter by category
                        filteredList = filterObjects.filterListByCategories(mProductsList, mSpinner.getSelectedItem().toString());
                        //filter by search if needed
                        if(mSearchView.getQuery().length() > 0)
                            filteredList = filterObjects.filterListByNames(filteredList, mSearchView.getQuery().toString());
                        inflateAdapter(filteredList);
                        setTextVisibility(filteredList);
                    }else{
                        inflateAdapter(mProductsList);
                        setTextVisibility(mProductsList);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // dohvati listu proizvoda odabrane trgovine
    @Override
    public void productsListByStoreReceived(ArrayList<ProductsModel> mProductsList) {
        if (mProductsList != null) {
            this.mProductsList = mProductsList;
            inflateAdapter(mProductsList);
            setTextVisibility(mProductsList);
        }else
            mNoneProducts.setVisibility(View.VISIBLE);
    }

    // dohvati listu svih kategorija
    @Override
    public void categoriesListReceived(ArrayList<CategoriesModel> mCategoriesList) {
        if(mCategoriesList != null){
            ArrayList<String> storeNames = new ArrayList<>();
            for(int i = 0; i < mCategoriesList.size(); i++){
                storeNames.add(mCategoriesList.get(i).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, storeNames);
            mSpinner.setAdapter(adapter);
        }
    }

    // proslijedi već dodane proizvode u adapter ako postoje
    private void initializeAdapter(ArrayList<ProductsModel> mProductsList){
        if(mListOfAddedProducts == null) mListOfAddedProducts = new ArrayList<>();
        mSelectProductsAdapter = new SelectProductsAdapter(mProductsList, mListOfAddedProducts);
    }

    // ispisi sve proizvode u recycler view
    private void inflateAdapter(ArrayList<ProductsModel> mProductsList){
        mRecyclerView.setAdapter(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        initializeAdapter(mProductsList);
        mRecyclerView.setAdapter(mSelectProductsAdapter);
    }

    //Prikazuje tekst korisniku da ne postoje proizvodi po zadanom kriteriju
    private void setTextVisibility(ArrayList<ProductsModel> list){
        if(list.size() > 0)
            mNoneProducts.setVisibility(View.GONE);
        else
            mNoneProducts.setVisibility(View.VISIBLE);
    }
}