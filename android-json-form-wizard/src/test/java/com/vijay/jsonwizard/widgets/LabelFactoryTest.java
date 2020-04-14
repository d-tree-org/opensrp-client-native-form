package com.vijay.jsonwizard.widgets;

import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.LinearLayout;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class LabelFactoryTest extends BaseTest {
    private LabelFactory factory;
    @Mock
    private JsonFormActivity context;

    @Mock
    private JsonFormFragment formFragment;

    @Mock
    private Resources resources;

    @Mock
    private CommonListener listener;

    @Mock
    private LinearLayout rootLayout;

    @Mock
    private ConstraintLayout constraintLayout;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        factory = new LabelFactory();
    }

    @Test
    public void testLabelWidgetFactoryInstantiatesViewsCorrectly() throws Exception {
        Assert.assertNotNull(factory);
        LabelFactory factorySpy = Mockito.spy(factory);
        Assert.assertNotNull(factorySpy);

        FormUtils formUtils = new FormUtils();
        FormUtils formUtilsSpy = Mockito.spy(formUtils);
        Assert.assertNotNull(formUtilsSpy);

        Mockito.doReturn(constraintLayout).when(formUtilsSpy).getRootConstraintLayout(context);
        Assert.assertNotNull(constraintLayout);

        Mockito.doReturn(resources).when(context).getResources();
        Assert.assertNotNull(resources);

        String labelString = "{\"key\":\"enabled_label\",\"type\":\"label\",\"text\":\"This is enabled\",\"hint_on_text\":false,\"text_color\":\"#FFC100\",\"text_size\":\"17sp\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"label_info_text\":\"Checking out the display oof the info icon needed to display this infomation\",\"label_info_title\":\"Highest Level of School\"}";

        List<View> viewList = factorySpy.getViewsFromJson("RandomStepName", context, formFragment, new JSONObject(labelString), listener, false);
        Assert.assertNotNull(viewList);
        Assert.assertTrue(viewList.size() > 0);
    }
}
