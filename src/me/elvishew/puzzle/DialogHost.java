package me.elvishew.puzzle;

import android.app.Dialog;
import android.content.DialogInterface.OnDismissListener;

public interface DialogHost {
    public Dialog getDialogById(int dialogId);
    public void setDialogById(int dialogId, Dialog dialog);
    public void showDialogById(int dialogId);
    public void dismissDialogById(int dialogId);
    public void setOnDialogDissmissListener(int dialogId, OnDismissListener listener);
}
