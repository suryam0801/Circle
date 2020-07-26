package circleapp.circlepackage.circle;

import org.junit.Test;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void process_share_url_is_correct() {
        String url = "https://worfo.app.link/8JMEs34W96/?9dff5f34-acfd-4080-b27d-3d293d367d42";
        String returnedID = HelperMethodsUI.getCircleIdFromShareURL(url);
        String EXPECTED_RESULT = "9dff5f34-acfd-4080-b27d-3d293d367d42";
        assertEquals(EXPECTED_RESULT, returnedID);
    }
}