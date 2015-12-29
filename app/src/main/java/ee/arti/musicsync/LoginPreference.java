package ee.arti.musicsync;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class LoginPreference extends DialogPreference {

    public final static String KEY_SETTINGS_USERNAME = "username";
    public final static String KEY_SETTINGS_PASSWORD = "password";

    private EditText wUsername;
    private EditText wPassword;

    public LoginPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false); // we will do our own preferences saving
        setDialogLayoutResource(R.layout.login_layout);
    }

    @Override
    protected void onBindDialogView(View view) {
        wUsername = (EditText)view.findViewById(R.id.username);
        wPassword = (EditText)view.findViewById(R.id.password);
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Editor editor = getEditor();
            editor.putString(KEY_SETTINGS_USERNAME, wUsername.getText().toString());
            editor.putString(KEY_SETTINGS_PASSWORD, wPassword.getText().toString());
            editor.commit();
        }
    }
}
