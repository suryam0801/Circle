package circleapp.circlepackage.circle;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import circleapp.circlepackage.circle.DataLayer.FirebaseWriteHelper;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Before
    public void createCircles() {
        FirebaseApp.initializeApp(appContext);
        FirebaseWriteHelper.createDefaultCircle("Test Circle", "Circle for Testing", "Public",
                "Surya", "Namakkal", 0, 0, "Events");
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        assertEquals("circleapp.circlepackage.circle", appContext.getPackageName());
    }
}