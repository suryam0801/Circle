package circleapp.circlepackage.circle.ui;


import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import circleapp.circlepackage.circle.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DumbDataPopulate {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void populate() {
        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.skip_get_started), withText("Skip"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.agreeandContinueEntryPage), withText("Agree and continue"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.phone_number_text),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        6),
                                2)));
        appCompatEditText.perform(scrollTo(), replaceText("1234567890"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.generate_btn), withText("Generate OTP"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        9),
                                0)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        SystemClock.sleep(10000);

        ViewInteraction pinEntryEditText = onView(
                allOf(withId(R.id.otp_text_view),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                2)));
        pinEntryEditText.perform(scrollTo(), replaceText("123456"), closeSoftKeyboard());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.verify_btn), withText("Verify OTP"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        4),
                                0)));
        appCompatButton4.perform(scrollTo(), click());

        SystemClock.sleep(10000);

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.add_circle_button),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.category_picker_recycler_view),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.create_circle_Name),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("C"), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.create_circle_Description),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                4)));
        appCompatEditText3.perform(scrollTo(), replaceText("D"), closeSoftKeyboard());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.create_circle_submit), withText("Create"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                4)));
        appCompatButton5.perform(scrollTo(), click());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.circlWallBackground6),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0)));
        appCompatImageView.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.bck_Circlewall),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.circle_wall_header),
                                        0),
                                0),
                        isDisplayed()));
        appCompatImageButton.perform(click());
        //Create n circles
        for(int i =0; i<50; i++){
            ViewInteraction floatingActionButton2 = onView(
                    allOf(withId(R.id.add_circle_button),
                            childAtPosition(
                                    childAtPosition(
                                            withId(android.R.id.content),
                                            0),
                                    2),
                            isDisplayed()));
            floatingActionButton2.perform(click());

            ViewInteraction recyclerView2 = onView(
                    allOf(withId(R.id.category_picker_recycler_view),
                            childAtPosition(
                                    withClassName(is("android.widget.LinearLayout")),
                                    2)));
            recyclerView2.perform(actionOnItemAtPosition(0, click()));

            ViewInteraction appCompatEditText4 = onView(
                    allOf(withId(R.id.create_circle_Name),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.LinearLayout")),
                                            3),
                                    1),
                            isDisplayed()));
            appCompatEditText4.perform(replaceText("C"), closeSoftKeyboard());

            ViewInteraction appCompatEditText5 = onView(
                    allOf(withId(R.id.create_circle_Description),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.LinearLayout")),
                                            2),
                                    4)));
            appCompatEditText5.perform(scrollTo(), replaceText("D"), closeSoftKeyboard());

            ViewInteraction appCompatButton6 = onView(
                    allOf(withId(R.id.create_circle_submit), withText("Create"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.LinearLayout")),
                                            0),
                                    4)));
            appCompatButton6.perform(scrollTo(), click());

            //make a couple of posts in each
            for (int j = 0; j< 2; j++){
                ViewInteraction floatingActionButton3 = onView(
                        allOf(childAtPosition(
                                allOf(withId(R.id.menu),
                                        childAtPosition(
                                                withId(R.id.circle_wall_parent_layout),
                                                2)),
                                3),
                                isDisplayed()));
                floatingActionButton3.perform(click());

                ViewInteraction floatingActionButton4 = onView(
                        allOf(withId(R.id.message_creation_FAB),
                                childAtPosition(
                                        allOf(withId(R.id.menu),
                                                childAtPosition(
                                                        withId(R.id.circle_wall_parent_layout),
                                                        2)),
                                        1),
                                isDisplayed()));
                floatingActionButton4.perform(click());

                ViewInteraction appCompatEditText6 = onView(
                        allOf(withId(R.id.broadcastTitleEditText),
                                childAtPosition(
                                        allOf(withId(R.id.create_broadcast_display),
                                                childAtPosition(
                                                        withClassName(is("android.widget.LinearLayout")),
                                                        0)),
                                        1)));
                appCompatEditText6.perform(scrollTo(), replaceText("Post"+j), closeSoftKeyboard());

                ViewInteraction appCompatButton7 = onView(
                        allOf(withId(R.id.upload_normal_broadcast_btn), withText("Post"),
                                childAtPosition(
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1),
                                        1)));
                appCompatButton7.perform(scrollTo(), click());

            }
            ViewInteraction appCompatImageButtonn = onView(
                    allOf(withId(R.id.bck_Circlewall),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.circle_wall_header),
                                            0),
                                    0),
                            isDisplayed()));
            appCompatImageButton.perform(click());
        }
        //Making circle with 1000comments and 200 posts

        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.add_circle_button),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        floatingActionButton2.perform(click());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.category_picker_recycler_view),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));
        recyclerView2.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.create_circle_Name),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText("Automated Testngs"), closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.create_circle_Description),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                4)));
        appCompatEditText5.perform(scrollTo(), replaceText("D"), closeSoftKeyboard());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.create_circle_submit), withText("Create"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                4)));
        appCompatButton6.perform(scrollTo(), click());
        for(int i = 0; i<200; i++){
            ViewInteraction floatingActionButton3 = onView(
                    allOf(childAtPosition(
                            allOf(withId(R.id.menu),
                                    childAtPosition(
                                            withId(R.id.circle_wall_parent_layout),
                                            2)),
                            3),
                            isDisplayed()));
            floatingActionButton3.perform(click());

            ViewInteraction floatingActionButton4 = onView(
                    allOf(withId(R.id.message_creation_FAB),
                            childAtPosition(
                                    allOf(withId(R.id.menu),
                                            childAtPosition(
                                                    withId(R.id.circle_wall_parent_layout),
                                                    2)),
                                    1),
                            isDisplayed()));
            floatingActionButton4.perform(click());

            ViewInteraction appCompatEditText6 = onView(
                    allOf(withId(R.id.broadcastTitleEditText),
                            childAtPosition(
                                    allOf(withId(R.id.create_broadcast_display),
                                            childAtPosition(
                                                    withClassName(is("android.widget.LinearLayout")),
                                                    0)),
                                    1)));
            appCompatEditText6.perform(scrollTo(), replaceText("Post"+i), closeSoftKeyboard());

            ViewInteraction appCompatButton7 = onView(
                    allOf(withId(R.id.upload_normal_broadcast_btn), withText("Post"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.LinearLayout")),
                                            1),
                                    1)));
            appCompatButton7.perform(scrollTo(), click());
        }


        ViewInteraction recyclerView3 = onView(
                allOf(withId(R.id.broadcastViewRecyclerView),
                        childAtPosition(
                                withId(R.id.circle_wall_header),
                                1)));
        recyclerView3.perform(actionOnItemAtPosition(0, click()));

        for (int k=0; k<1000; k++){
            ViewInteraction appCompatEditText9 = onView(
                    allOf(withId(R.id.full_page_broadcast_comment_type_editText),
                            childAtPosition(
                                    allOf(withId(R.id.full_page_write_comment_edit_text),
                                            childAtPosition(
                                                    withClassName(is("android.widget.RelativeLayout")),
                                                    1)),
                                    0),
                            isDisplayed()));
            appCompatEditText9.perform(replaceText("Comment lorem ipsum"+k), closeSoftKeyboard());

            ViewInteraction appCompatButton10 = onView(
                    allOf(withId(R.id.full_page_broadcast_comment_send_button),
                            childAtPosition(
                                    allOf(withId(R.id.full_page_write_comment_edit_text),
                                            childAtPosition(
                                                    withClassName(is("android.widget.RelativeLayout")),
                                                    1)),
                                    1),
                            isDisplayed()));
            appCompatButton10.perform(click());
        }

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.bck_fullpage_broadcast),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.full_page_broadcast_parent_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatImageButton2.perform(click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
