package marcocipriani01.iparcos.prop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDINumberProperty;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIValueException;

import java.util.ArrayList;
import java.util.List;

import marcocipriani01.iparcos.IPARCOSApp;
import marcocipriani01.iparcos.R;

@SuppressWarnings({"WeakerAccess"})
public class NumberPropPref extends PropPref<INDINumberElement> {

    public NumberPropPref(Context context, INDIProperty<INDINumberElement> prop) {
        super(context, prop);
    }

    /**
     * Create the summary rich-text string
     *
     * @return the summary
     */
    @Override
    protected Spannable createSummary() {
        List<INDINumberElement> elements = prop.getElementsAsList();
        if (elements.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            stringBuilder.append(elements.get(0).getLabel()).append(": ");
            for (i = 0; i < elements.size() - 1; i++) {
                stringBuilder.append(elements.get(i).getValueAsString()).append(", ")
                        .append(elements.get(i + 1).getLabel()).append(": ");
            }
            stringBuilder.append(elements.get(i).getValueAsString());
            return new SpannableString(stringBuilder.toString());

        } else {
            return new SpannableString(getContext().getString(R.string.no_indi_elements));
        }
    }

    @Override
    protected void onClick() {
        if (!getSummary().toString().equals(getContext().getString(R.string.no_indi_elements))) {
            NumberRequestFragment requestFragment = new NumberRequestFragment();
            requestFragment.setArguments((INDINumberProperty) prop, this);
            requestFragment.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "request");
        }
    }

    public static class NumberRequestFragment extends DialogFragment {

        private INDINumberProperty prop;
        private PropPref<INDINumberElement> propPref;

        public void setArguments(INDINumberProperty prop, PropPref<INDINumberElement> propPref) {
            this.prop = prop;
            this.propPref = propPref;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            final List<INDINumberElement> elements = prop.getElementsAsList();
            final ArrayList<EditText> editTextViews = new ArrayList<>(elements.size());

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);

            for (int i = 0; i < elements.size(); i++) {
                TextView textView = new TextView(context);
                textView.setText(elements.get(i).getLabel());
                textView.setPadding(padding, padding, padding, 0);
                layout.addView(textView);
                editTextViews.add(new EditText(context));
                EditText editText = editTextViews.get(i);
                editText.setText(elements.get(i).getValueAsString());
                editText.setPadding(padding, padding, padding, padding);
                editText.setEnabled(prop.getPermission() != Constants.PropertyPermissions.RO);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                layout.addView(editText);
            }
            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(layout);
            builder.setView(scrollView);
            builder.setTitle(prop.getLabel());

            if (prop.getPermission() != Constants.PropertyPermissions.RO) {
                builder.setPositiveButton(R.string.send_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            for (int i = 0; i < elements.size(); i++) {
                                INDIElement element = elements.get(i);
                                String s = editTextViews.get(i).getText().toString();
                                if (element.checkCorrectValue(s)) {
                                    element.setDesiredValue(s);
                                }
                            }

                        } catch (INDIValueException | IllegalArgumentException e) {
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            IPARCOSApp.log(context.getResources().getString(R.string.error) + e.getLocalizedMessage());
                        }
                        propPref.sendChanges();
                    }
                });
                builder.setNegativeButton(R.string.cancel_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

            } else {
                builder.setNegativeButton(R.string.back_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
            }

            return builder.create();
        }
    }
}