package com.vijay.jsonwizard.widgets;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.DatePickerDialog;
import com.vijay.jsonwizard.customviews.GenericTextWatcher;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.NativeFormsProperties;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 25/01/2017
 */
public class DatePickerFactory implements FormWidgetFactory {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    public static final String DATE_FORMAT_REGEX = "(^(((0[1-9]|1[0-9]|2[0-8])[-](0[1-9]|1[012]))|((29|30|31)[-](0[13578]|1[02]))|((29|30)[-](0[4,6,9]|11)))[-](19|[2-9][0-9])\\d\\d$)|(^29[-]02[-](19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)$)|\\s*";
    public static final SimpleDateFormat DATE_FORMAT_LOCALE_INDEPENDENT = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    private static final String TAG = "DatePickerFactory";

    private static void updateDateText(MaterialEditText editText, TextView duration, String date) {
        editText.setText(date);
        String durationLabel = (String) duration.getTag(R.id.label);
        if (!TextUtils.isEmpty(durationLabel)) {
            String durationText = Utils.getDuration(date);
            if (!TextUtils.isEmpty(durationText)) {
                durationText = String.format("(%s: %s)", durationLabel, durationText);
            }
            duration.setText(durationText);
        }

    }


    private static void showDatePickerDialog(Activity context,
                                             DatePickerDialog datePickerDialog,
                                             MaterialEditText editText) {
        FragmentTransaction ft = context.getFragmentManager().beginTransaction();
        Fragment prev = context.getFragmentManager().findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        datePickerDialog.show(ft, TAG);
        String text = editText.getText().toString();
        Calendar date = FormUtils.getDate(text);
        if (text.isEmpty()) {
            Object defaultValue = datePickerDialog.getArguments().get(JsonFormConstants.DEFAULT);
            if (defaultValue != null)
                datePickerDialog.setDate(FormUtils.getDate(defaultValue.toString()).getTime());
            else
                datePickerDialog.setDate(date.getTime());
        } else {
            datePickerDialog.setDate(date.getTime());
        }
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject,
                                       CommonListener listener, boolean popup) {
        return attachJson(stepName, context, formFragment, jsonObject, popup, listener);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, false, listener);
    }

    protected List<View> attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject,
                                    boolean popup, CommonListener listener) {
        List<View> views = new ArrayList<>(1);
        try {

            RelativeLayout dateViewRelativeLayout = (RelativeLayout) LayoutInflater
                    .from(context).inflate(getLayout(), null);

            MaterialEditText editText = dateViewRelativeLayout.findViewById(R.id.edit_text);

            TextView duration = dateViewRelativeLayout.findViewById(R.id.duration);

            attachLayout(stepName, context, formFragment, jsonObject, editText, duration);

            JSONArray canvasIds = new JSONArray();
            dateViewRelativeLayout.setId(ViewUtil.generateViewId());
            canvasIds.put(dateViewRelativeLayout.getId());
            editText.setTag(R.id.canvas_ids, canvasIds.toString());
            editText.setTag(R.id.extraPopup, popup);

            ((JsonApi) context).addFormDataView(editText);
            views.add(dateViewRelativeLayout);
            attachInfoIcon(stepName, jsonObject, dateViewRelativeLayout, canvasIds, listener);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return views;
    }

    protected void attachLayout(String stepName, final Context context, JsonFormFragment formFragment, JSONObject jsonObject,
                                final MaterialEditText editText, final TextView duration) {

        try {
            String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
            String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
            String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
            String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
            String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
            String calculations = jsonObject.optString(JsonFormConstants.CALCULATION);

            duration.setTag(R.id.key, jsonObject.getString(KEY.KEY));
            duration.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
            duration.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
            duration.setTag(R.id.openmrs_entity, openMrsEntity);
            duration.setTag(R.id.openmrs_entity_id, openMrsEntityId);
            if (jsonObject.has(KEY.DURATION)) {
                duration.setTag(R.id.label, jsonObject.getJSONObject(KEY.DURATION).getString(JsonFormConstants.LABEL));
            }

            updateEditText(editText, jsonObject, stepName, context, duration);
            editText.setTag(R.id.json_object, jsonObject);

            final DatePickerDialog datePickerDialog = createDateDialog(context, duration, editText, jsonObject);
            if (formFragment !=null) {
                NativeFormsProperties nativeFormsProperties = formFragment.getNativeFormProperties();https://docs.google.com/document/d/1qGuQ-yw2epegvKZjPd5OE3lTjWrNTxzKk79hTPKi054/edit?pli=1
                if (nativeFormsProperties != null) {
                    datePickerDialog.setNumericDatePicker(nativeFormsProperties.isTrue(NativeFormsProperties.KEY.WIDGET_DATEPICKER_IS_NUMERIC));
                }
            }

            if (jsonObject.has(JsonFormConstants.EXPANDED) && jsonObject.getBoolean(JsonFormConstants.EXPANDED)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                datePickerDialog.setCalendarViewShown(true);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                datePickerDialog.setCalendarViewShown(false);
            }

            GenericTextWatcher genericTextWatcher = getGenericTextWatcher(stepName, (Activity) context, formFragment,
                    editText, datePickerDialog);
            editText.addTextChangedListener(genericTextWatcher);
            addRefreshLogicView(context, editText, relevance, constraints, calculations);
            Bundle datePickerArgs = new Bundle();
            datePickerArgs.putString(JsonFormConstants.DEFAULT, jsonObject.optString(JsonFormConstants.DEFAULT));
            datePickerDialog.setArguments(datePickerArgs);
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog((Activity) context, datePickerDialog, editText);
                }
            });

            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    updateDateText(editText, duration, "");
                    return true;
                }
            });
            editText.setFocusable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void attachInfoIcon(String stepName, JSONObject jsonObject, RelativeLayout rootLayout, JSONArray canvasIds,
                                CommonListener listener) throws JSONException {
        if (jsonObject.has(JsonFormConstants.LABEL_INFO_TEXT)) {
            ImageView infoIcon = rootLayout.findViewById(R.id.date_picker_info_icon);
            FormUtils.showInfoIcon(stepName, jsonObject, listener, FormUtils.getInfoDialogAttributes(jsonObject), infoIcon, canvasIds);
        }

    }

    @NonNull
    private GenericTextWatcher getGenericTextWatcher(String stepName, final Activity context, JsonFormFragment formFragment,
                                                     final MaterialEditText editText,
                                                     final DatePickerDialog datePickerDialog) {
        GenericTextWatcher genericTextWatcher = new GenericTextWatcher(stepName, formFragment, editText);
        genericTextWatcher.addOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    datePickerDialog.setArguments(new Bundle());
                    showDatePickerDialog(context, datePickerDialog, editText);
                }
            }
        });
        return genericTextWatcher;
    }

    private void addRefreshLogicView(Context context, MaterialEditText editText, String relevance, String constraints,
                                     String calculations) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            editText.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(editText);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            editText.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(editText);
        }

        if (!TextUtils.isEmpty(calculations) && context instanceof JsonApi) {
            editText.setTag(R.id.calculation, calculations);
            ((JsonApi) context).addCalculationLogicView(editText);
        }
    }

    private void updateEditText(MaterialEditText editText, JSONObject jsonObject, String stepName, Context context,
                                TextView duration) throws JSONException {
        SimpleDateFormat DATE_FORMAT_LOCALE = new SimpleDateFormat("dd-MM-yyyy", context.getResources().getConfiguration().locale);

        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        editText.setHint(jsonObject.getString(KEY.HINT));
        editText.setFloatingLabelText(jsonObject.getString(KEY.HINT));
        editText.setId(ViewUtil.generateViewId());
        editText.setTag(R.id.key, jsonObject.getString(KEY.KEY));
        editText.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        editText.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        editText.setTag(R.id.openmrs_entity, openMrsEntity);
        editText.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        editText.setTag(R.id.address, stepName + ":" + jsonObject.getString(KEY.KEY));
        editText.setTag(R.id.locale_independent_value, jsonObject.optString(KEY.VALUE));
        if (jsonObject.has(JsonFormConstants.V_REQUIRED)) {
            JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
            boolean requiredValue = requiredObject.getBoolean(KEY.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                editText.addValidator(new RequiredValidator(requiredObject.getString(JsonFormConstants.ERR)));
                FormUtils.setRequiredOnHint(editText);
            }
        }

        if (!TextUtils.isEmpty(jsonObject.optString(KEY.VALUE))) {
            updateDateText(editText, duration, DATE_FORMAT_LOCALE.format(FormUtils.getDate(jsonObject.optString(KEY.VALUE)).getTime()));
        }

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            boolean readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            editText.setEnabled(!readOnly);
            editText.setFocusable(!readOnly);
        }
    }

    protected DatePickerDialog createDateDialog(Context context, final TextView duration, final MaterialEditText editText,
                                                JSONObject jsonObject) throws JSONException {
        final DatePickerDialog datePickerDialog = new DatePickerDialog();
        datePickerDialog.setContext(context);

        datePickerDialog.setOnDateSetListener(new android.app.DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendarDate = Calendar.getInstance();
                calendarDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendarDate.set(Calendar.MONTH, monthOfYear);
                calendarDate.set(Calendar.YEAR, year);

                editText.setTag(R.id.locale_independent_value, DATE_FORMAT_LOCALE_INDEPENDENT.format(calendarDate.getTime()));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        && calendarDate.getTimeInMillis() >= view.getMinDate()
                        && calendarDate.getTimeInMillis() <= view.getMaxDate()) {
                    updateDateText(editText, duration,
                            DATE_FORMAT.format(calendarDate.getTime()));
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    updateDateText(editText, duration, "");
                }
            }
        });

        if (jsonObject.has(JsonFormConstants.MIN_DATE) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Calendar minDate = FormUtils.getDate(jsonObject.getString(JsonFormConstants.MIN_DATE));
            minDate.set(Calendar.HOUR_OF_DAY, 0);
            minDate.set(Calendar.MINUTE, 0);
            minDate.set(Calendar.SECOND, 0);
            minDate.set(Calendar.MILLISECOND, 0);
            datePickerDialog.setMinDate(minDate.getTimeInMillis());
        }

        if (jsonObject.has(JsonFormConstants.MAX_DATE) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Calendar maxDate = FormUtils.getDate(jsonObject.getString(JsonFormConstants.MAX_DATE));
            maxDate.set(Calendar.HOUR_OF_DAY, 23);
            maxDate.set(Calendar.MINUTE, 59);
            maxDate.set(Calendar.SECOND, 59);
            maxDate.set(Calendar.MILLISECOND, 999);
            datePickerDialog.setMaxDate(maxDate.getTimeInMillis());
        }

        return datePickerDialog;
    }

    protected int getLayout() {
        return R.layout.native_form_item_date_picker;
    }

    public static class KEY {
        public static final String DURATION = "duration";

        public static final String HINT = "hint";

        public static final String KEY = "key";

        public static final String VALUE = (JsonFormConstants.VALUE);

        public static final String DEFAULT = "default";
    }

}
