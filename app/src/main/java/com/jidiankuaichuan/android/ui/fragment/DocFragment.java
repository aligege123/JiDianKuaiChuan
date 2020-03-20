package com.jidiankuaichuan.android.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.jidiankuaichuan.android.R;
import com.jidiankuaichuan.android.data.FileBean;
import com.jidiankuaichuan.android.ui.Adapter.DocAdapter;
import com.jidiankuaichuan.android.utils.FileManager;
import com.jidiankuaichuan.android.utils.FileUtils;
import com.jidiankuaichuan.android.utils.MyLog;

import java.util.List;

public class DocFragment extends Fragment {

    private static final String TAG = "DocFragment";

    private List<FileBean> docList;

    private DocAdapter docAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.doc_fragment, container, false);
        docList = FileManager.getInstance(getContext()).getFilesByType(FileUtils.TYPE_DOC);
        final DocAdapter adapter = new DocAdapter(getContext(), R.layout.doc_item, docList);
        docAdapter = adapter;
        ListView docListView = (ListView) view.findViewById(R.id.doc_list);
        docListView.setAdapter(adapter);
        //MyLog.d(TAG, "this is docfragment");
        docListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setChecked(position);
            }
        });
        return view;
    }

    public int getSelectedCount() {
        return docAdapter == null ? 0 : docAdapter.getSelectedCount();
    }
}