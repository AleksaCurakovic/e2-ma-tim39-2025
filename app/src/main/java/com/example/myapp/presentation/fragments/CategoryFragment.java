package com.example.myapp.presentation.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.domain.models.Category;
import com.example.myapp.presentation.adapters.CategoryAdapter;
import com.example.myapp.presentation.viewmodels.CategoryViewModel;
import com.example.myapp.presentation.viewmodels.CategoryViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class CategoryFragment extends Fragment implements CategoryAdapter.CategoryClickListener {

    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;
    private String userUid;

    private EditText etCategoryName;
    private Button btnPickColor, btnCreateCategory;
    private TextView tvSelectedColor;
    private View viewColorPreview;
    private RecyclerView rvCategories;

    private String selectedColor = "#2196F3"; // default plava

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        CategoryViewModelFactory factory = new CategoryViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(CategoryViewModel.class);

        initViews(view);
        setupRecycler();
        setupColorPicker();
        setupObservers();

        viewModel.loadCategories(userUid);

        btnCreateCategory.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            viewModel.createCategory(userUid, name, selectedColor);
        });
    }

    private void initViews(View v) {
        etCategoryName    = v.findViewById(R.id.etCategoryName);
        btnPickColor      = v.findViewById(R.id.btnPickColor);
        btnCreateCategory = v.findViewById(R.id.btnCreateCategory);
        tvSelectedColor   = v.findViewById(R.id.tvSelectedColor);
        viewColorPreview  = v.findViewById(R.id.viewColorPreview);
        rvCategories      = v.findViewById(R.id.rvCategories);
    }

    private void setupRecycler() {
        adapter = new CategoryAdapter(new ArrayList<>(), this);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(adapter);
    }

    private void setupColorPicker() {
        updateColorPreview(selectedColor);
        btnPickColor.setOnClickListener(v -> showColorPickerDialog(selectedColor, color -> {
            selectedColor = color;
            updateColorPreview(color);
        }));
    }

    private void updateColorPreview(String hex) {
        try {
            viewColorPreview.setBackgroundColor(android.graphics.Color.parseColor(hex));
            tvSelectedColor.setText(hex);
        } catch (Exception ignored) {}
    }

    private void setupObservers() {
        viewModel.categories.observe(getViewLifecycleOwner(), cats -> {
            if (cats != null) adapter.updateCategories(cats);
        });

        viewModel.successMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                etCategoryName.setText("");
                selectedColor = "#2196F3";
                updateColorPreview(selectedColor);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) return;
            switch (error) {
                case "NAME_EMPTY":
                    etCategoryName.setError("Naziv je obavezan");
                    etCategoryName.requestFocus();
                    break;
                case "COLOR_TAKEN":
                    Toast.makeText(requireContext(),
                            "Ova boja je već zauzeta", Toast.LENGTH_SHORT).show();
                    break;
                case "HAS_ACTIVE_TASKS":
                    Toast.makeText(requireContext(),
                            "Ne možete obrisati kategoriju sa aktivnim zadacima",
                            Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(requireContext(),
                            "Greška", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // ─── CategoryAdapter.CategoryClickListener ───

    @Override
    public void onChangeColor(Category category) {
        showColorPickerDialog(category.getColor(), newColor ->
                viewModel.updateColor(userUid, category, newColor));
    }

    @Override
    public void onDelete(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Brisanje kategorije")
                .setMessage("Da li ste sigurni da želite da obrišete \"" + category.getName() + "\"?")
                .setPositiveButton("Obriši", (d, w) ->
                        viewModel.deleteCategory(userUid, category))
                .setNegativeButton("Otkaži", null)
                .show();
    }

    // ─── Color picker dijalog ───
    // Jednostavan dijalog sa predefinisanim bojama.
    // Možeš zameniti sa pravom color picker bibliotekom ako hoćeš.
    private void showColorPickerDialog(String currentColor, ColorPickedCallback callback) {
        String[] colorNames = {"Crvena", "Plava", "Zelena", "Narandžasta",
                "Ljubičasta", "Roze", "Teal", "Žuta"};
        String[] colorHexes = {"#F44336", "#2196F3", "#4CAF50", "#FF9800",
                "#9C27B0", "#E91E63", "#009688", "#FFC107"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Odaberi boju")
                .setItems(colorNames, (dialog, which) ->
                        callback.onColorPicked(colorHexes[which]))
                .show();
    }

    interface ColorPickedCallback {
        void onColorPicked(String color);
    }
}