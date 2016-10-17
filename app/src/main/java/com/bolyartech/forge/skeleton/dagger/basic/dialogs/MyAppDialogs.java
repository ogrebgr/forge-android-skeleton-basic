package com.bolyartech.forge.skeleton.dagger.basic.dialogs;


import android.app.DialogFragment;
import android.app.FragmentManager;


public class MyAppDialogs {

    // Non-instantiable utility class
    private MyAppDialogs() {
        throw new AssertionError("No instances allowed");
    }


    public static void showNoInetDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfNoInet.DIALOG_TAG) == null) {
            DfNoInet fra = new DfNoInet();
            fra.show(fm, DfNoInet.DIALOG_TAG);
        }
    }


    public static void showInvalidLoginDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfInvalidLogin.DIALOG_TAG) == null) {
            DfInvalidLogin fra = new DfInvalidLogin();
            fra.show(fm, DfInvalidLogin.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showInvalidAutologinDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfInvalidAutologin.DIALOG_TAG) == null) {
            DfInvalidAutologin fra = new DfInvalidAutologin();
            fra.show(fm, DfInvalidAutologin.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showUpgradeNeededDialog(FragmentManager fm) {
        //TODO - show button "Go to market" 
        if (fm.findFragmentByTag(DfUpgradeNeeded.DIALOG_TAG) == null) {
            DfUpgradeNeeded fra = new DfUpgradeNeeded();
            fra.show(fm, DfUpgradeNeeded.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showCommProblemDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfCommProblem.DIALOG_TAG) == null) {
            DfCommProblem fra = new DfCommProblem();
            fra.show(fm, DfCommProblem.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showCommWaitDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfCommWait.DIALOG_TAG) == null) {
            DfCommWait fra = new DfCommWait();
            fra.show(fm, DfCommWait.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    /**
     *
     * @param fm FragmentManager instance
     * @return true if dialog is found and dismissed, false otherwise
     */
    public static boolean hideCommWaitDialog(FragmentManager fm) {
        DialogFragment df = (DialogFragment) fm.findFragmentByTag(DfCommWait.DIALOG_TAG);
        if (df != null) {
            df.dismiss();
            return true;
        } else {
            return false;
        }
    }


    public static void showNeedReloginDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfNeedRelogin.DIALOG_TAG) == null) {
            DfNeedRelogin fra = new DfNeedRelogin();
            fra.show(fm, DfNeedRelogin.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showGenericWaitDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfGenericWait.DIALOG_TAG) == null) {
            DfGenericWait fra = new DfGenericWait();
            fra.show(fm, DfGenericWait.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void hideGenericWaitDialog(FragmentManager fm) {
        DialogFragment df = (DialogFragment) fm.findFragmentByTag(DfGenericWait.DIALOG_TAG);
        if (df != null) {
            df.dismiss();
        }
    }


    public static void showCannotStartSessionDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfCannotStartSession.DIALOG_TAG) == null) {
            DfCannotStartSession fra = new DfCannotStartSession();
            fra.show(fm, DfCannotStartSession.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showFbLoginErrorDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfFbLoginError.DIALOG_TAG) == null) {
            DfFbLoginError fra = new DfFbLoginError();
            fra.show(fm, DfFbLoginError.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showGoogleLoginErrorDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfGoogleLoginError.DIALOG_TAG) == null) {
            DfGoogleLoginError fra = new DfGoogleLoginError();
            fra.show(fm, DfGoogleLoginError.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void showLoggingInDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfLoggingIn.DIALOG_TAG) == null) {
            DfLoggingIn fra = new DfLoggingIn();
            fra.show(fm, DfLoggingIn.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void hideLoggingInDialog(FragmentManager fm) {
        DialogFragment df = (DialogFragment) fm.findFragmentByTag(DfLoggingIn.DIALOG_TAG);
        if (df != null) {
            df.dismiss();
        }
    }


    public static void showLoggingOutDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfLoggingOut.DIALOG_TAG) == null) {
            DfLoggingOut fra = new DfLoggingOut();
            fra.show(fm, DfLoggingOut.DIALOG_TAG);
            fm.executePendingTransactions();
        }
    }


    public static void hideLoggingOutDialog(FragmentManager fm) {
        DialogFragment df = (DialogFragment) fm.findFragmentByTag(DfLoggingOut.DIALOG_TAG);
        if (df != null) {
            df.dismiss();
        }
    }
}
