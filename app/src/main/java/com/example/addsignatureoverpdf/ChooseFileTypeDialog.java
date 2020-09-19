package com.example.addsignatureoverpdf;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ChooseFileTypeDialog extends DialogFragment implements View.OnClickListener{

    private static final String TAG = ChooseFileTypeDialog.class.getSimpleName();

    private TextView pdfFileTypeView,imageFileTypeView;
    private FileTypeDialogListener typeDialogListener;
    private int imageFileType = 0;
    private int pdfFileType = 1;

    public static ChooseFileTypeDialog newInstance(String title) {
        
        Bundle args = new Bundle();
        args.putString("title",title);
        ChooseFileTypeDialog fragment = new ChooseFileTypeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        typeDialogListener = (FileTypeDialogListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_type_select_fragment,container);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageFileTypeView = view.findViewById(R.id.file_type_image);
        pdfFileTypeView = view.findViewById(R.id.file_type_pdf);
        String title = getArguments().getString("title",getString(R.string.select_file_type));
        getDialog().setTitle(title);
        imageFileTypeView.setOnClickListener(this);
        pdfFileTypeView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_type_image:
                Log.d(TAG,"image file type selected");
                typeDialogListener.onDialogClicked(imageFileType);
                dismiss();
                break;
            case R.id.file_type_pdf:
                Log.d(TAG,"pdf file type selected");
                typeDialogListener.onDialogClicked(pdfFileType);
                dismiss();
                break;
        }
    }

    public interface FileTypeDialogListener {
        void onDialogClicked(int id);
    }
}
