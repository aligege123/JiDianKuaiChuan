package com.jidiankuaichuan.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jidiankuaichuan.android.R;

public class MyDialog extends Dialog {

    private MyDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {

        private View view;

        private TextView mTitle;
        private LinearLayout checkLayout;
        private CheckBox mCheckBox;
        private TextView no;
        private TextView yes;

        private CheckCallBack checkCallBack;

        private View.OnClickListener yesClickListener;

        private CompoundButton.OnCheckedChangeListener checkedChangeListener;

        private MyDialog mDialog;

        public Builder(Context context) {
            mDialog = new MyDialog(context, R.style.Theme_AppCompat_Dialog);
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //加载布局文件
            view = inflater.inflate(R.layout.my_dialog, null, false);
            //添加布局文件到 Dialog
            mDialog.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            mTitle = (TextView) view.findViewById(R.id.dialog_title);
            checkLayout = (LinearLayout) view.findViewById(R.id.dialog_check_layout);
            mCheckBox = (CheckBox) view.findViewById(R.id.dialog_check);
            no = (TextView) view.findViewById(R.id.dialog_negative_text);
            yes = (TextView) view.findViewById(R.id.dialog_positive_text);
        }

        /**
         * 设置 Dialog 标题
         */
        @NonNull
        public Builder setTitle(@NonNull String title) {
            mTitle.setText(title);
            return this;
        }

        /**
         * 设置复选布局
         */
        public Builder setCheckLayout() {
            checkLayout.setVisibility(View.VISIBLE);
            return this;
        }

        public Builder setCheckCallBack(CheckCallBack checkCallBack) {
            this.checkCallBack = checkCallBack;
            return this;
        }

        /**
         * 设置取消按钮
         */
        public Builder setNoText() {
            no.setText("取消");
            return this;
        }

        public Builder setNoTextGone() {
            no.setVisibility(View.GONE);
            return this;
        }

        /**
         * 确认按钮
         */
        public Builder setYesText(View.OnClickListener onClickListener) {
            yes.setText("确认");
            yesClickListener = onClickListener;
            return this;
        }

        public Builder setEmptyYesText() {
            yes.setText("确认");
            return this;
        }

        public MyDialog create() {
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            if (yesClickListener == null && checkLayout.getVisibility() == View.GONE) {
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
            } else if (yesClickListener == null && checkLayout.getVisibility() == View.VISIBLE) {
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isCheck = mCheckBox.isChecked();
                        if (checkCallBack != null) {
                            checkCallBack.onCheck(isCheck);
                        }
                        mDialog.dismiss();
                    }
                });
            } else if (yesClickListener != null && checkLayout.getVisibility() == View.GONE){
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        yesClickListener.onClick(view);
                        mDialog.dismiss();
                    }
                });
            }
            mDialog.setContentView(view);
            mDialog.setCancelable(false);                //用户可以点击后退键关闭 Dialog
            mDialog.setCanceledOnTouchOutside(false);   //用户不可以点击外部来关闭 Dialog
            return mDialog;
        }

        public interface CheckCallBack {
            void onCheck(boolean isChecked);
        }
    }
}
